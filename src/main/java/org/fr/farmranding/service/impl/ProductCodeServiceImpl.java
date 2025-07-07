package org.fr.farmranding.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.config.GarakApiProperties;
import org.fr.farmranding.dto.request.ProductCodeSyncRequest;
import org.fr.farmranding.dto.response.ProductCodeResponse;
import org.fr.farmranding.dto.response.ProductCodeSyncResponse;
import org.fr.farmranding.entity.product.ProductCode;
import org.fr.farmranding.repository.ProductCodeRepository;
import org.fr.farmranding.service.ProductCodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductCodeServiceImpl implements ProductCodeService {
    
    private final ProductCodeRepository productCodeRepository;
    private final GarakApiProperties garakApiProperties;
    private final RestTemplate restTemplate;
    
    @Override
    public ProductCodeSyncResponse syncProductCodes(ProductCodeSyncRequest request) {
        long startTime = System.currentTimeMillis();
        LocalDateTime syncStartTime = LocalDateTime.now();
        
        try {
            log.info("품목 코드 동기화 시작 - pageSize: {}, pageIndex: {}, forceUpdate: {}", 
                    request.pageSize(), request.pageIndex(), request.forceUpdate());
            
            // 가락시장 API 호출
            String apiUrl = garakApiProperties.buildApiUrl(request.pageSize(), request.pageIndex());
            log.debug("가락시장 API 호출 URL: {}", apiUrl);
            
            String xmlResponse = restTemplate.getForObject(apiUrl, String.class);
            if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
                throw new BusinessException(FarmrandingResponseCode.GARAK_API_ERROR);
            }
            
            // XML 파싱
            List<GarakProductCodeDto> garakProductCodes = parseXmlResponse(xmlResponse);
            log.info("가락시장 API에서 {}개 품목 코드 조회 완료", garakProductCodes.size());
            
            // 데이터베이스 동기화
            SyncResult syncResult = synchronizeWithDatabase(garakProductCodes, request.forceUpdate());
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("품목 코드 동기화 완료 - 신규: {}, 업데이트: {}, 비활성화: {}, 소요시간: {}ms",
                    syncResult.newCount, syncResult.updatedCount, syncResult.deactivatedCount, processingTime);
            
            return ProductCodeSyncResponse.of(
                    garakProductCodes.size(),
                    syncResult.newCount,
                    syncResult.updatedCount,
                    syncResult.deactivatedCount,
                    syncStartTime,
                    processingTime
            );
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("품목 코드 동기화 중 오류 발생", e);
            throw new BusinessException(FarmrandingResponseCode.PRODUCT_CODE_SYNC_FAILED);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductCodeResponse> getAllActiveProductCodes() {
        List<ProductCode> productCodes = productCodeRepository.findByIsActiveTrue();
        return productCodes.stream()
                .map(ProductCodeResponse::from)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductCodeResponse> searchProductCodes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveProductCodes();
        }
        
        List<ProductCode> productCodes = productCodeRepository.findActiveProductsByKeyword(keyword.trim());
        return productCodes.stream()
                .map(ProductCodeResponse::from)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductCodeResponse getProductCodeByGarakCode(String garakCode) {
        ProductCode productCode = productCodeRepository.findByGarakCode(garakCode)
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.PRODUCT_CODE_NOT_FOUND));
        
        return ProductCodeResponse.from(productCode);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getActiveProductCodeCount() {
        return productCodeRepository.countActiveProducts();
    }
    
    private List<GarakProductCodeDto> parseXmlResponse(String xmlResponse) {
        List<GarakProductCodeDto> productCodes = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));
            
            NodeList listNodes = document.getElementsByTagName("list");
            
            for (int i = 0; i < listNodes.getLength(); i++) {
                Element listElement = (Element) listNodes.item(i);
                
                String garakCode = getTextContent(listElement, "S_GOSA");
                String productName = getTextContent(listElement, "S_NM");
                
                if (garakCode != null && productName != null && 
                    !garakCode.trim().isEmpty() && !productName.trim().isEmpty()) {
                    productCodes.add(new GarakProductCodeDto(garakCode.trim(), productName.trim()));
                }
            }
            
        } catch (Exception e) {
            log.error("XML 파싱 중 오류 발생", e);
            throw new BusinessException(FarmrandingResponseCode.GARAK_API_ERROR);
        }
        
        return productCodes;
    }
    
    private String getTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }
    
    private SyncResult synchronizeWithDatabase(List<GarakProductCodeDto> garakProductCodes, boolean forceUpdate) {
        Set<String> garakCodes = new HashSet<>();
        int newCount = 0;
        int updatedCount = 0;
        
        for (GarakProductCodeDto garakDto : garakProductCodes) {
            garakCodes.add(garakDto.garakCode());
            
            ProductCode existingProductCode = productCodeRepository.findByGarakCode(garakDto.garakCode())
                    .orElse(null);
            
            if (existingProductCode == null) {
                // 신규 생성
                ProductCode newProductCode = ProductCode.builder()
                        .garakCode(garakDto.garakCode())
                        .productName(garakDto.productName())
                        .isActive(true)
                        .build();
                productCodeRepository.save(newProductCode);
                newCount++;
                
            } else {
                // 기존 데이터 업데이트
                boolean needsUpdate = false;
                
                if (!existingProductCode.getProductName().equals(garakDto.productName())) {
                    existingProductCode.updateProductName(garakDto.productName());
                    needsUpdate = true;
                }
                
                if (!existingProductCode.getIsActive()) {
                    existingProductCode.activate();
                    needsUpdate = true;
                }
                
                if (needsUpdate || forceUpdate) {
                    productCodeRepository.save(existingProductCode);
                    updatedCount++;
                }
            }
        }
        
        // 가락시장 API에 없는 품목들을 비활성화
        int deactivatedCount = 0;
        List<ProductCode> activeProductCodes = productCodeRepository.findByIsActiveTrue();
        
        for (ProductCode productCode : activeProductCodes) {
            if (!garakCodes.contains(productCode.getGarakCode())) {
                productCode.deactivate();
                productCodeRepository.save(productCode);
                deactivatedCount++;
            }
        }
        
        return new SyncResult(newCount, updatedCount, deactivatedCount);
    }
    
    private record GarakProductCodeDto(String garakCode, String productName) {}
    
    private record SyncResult(int newCount, int updatedCount, int deactivatedCount) {}
} 