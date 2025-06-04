package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.dto.gap.GapCertificationResponse;
import org.fr.farmranding.dto.gap.GapSearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GapCertificationServiceImpl implements GapCertificationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${farmranding.gap.api-url}")
    private String gapApiUrl;
    
    @Value("${farmranding.gap.api-key}")
    private String gapApiKey;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
        public GapCertificationResponse searchGapCertification(GapSearchRequest request) {
            try {
                log.info("GAP 인증 정보 조회 시작: certificationNumber={}", request.certificationNumber());

                String baseUrl = gapApiUrl;

                // API_KEY 앞에 / 문자를 붙여서 경로로 쓴다.
                String apiKey = "/" + gapApiKey;
                String servicePath = "/xml/Grid_20141225000000000154_1/1/1000";

                // UriComponentsBuilder로 쿼리 파라미터를 하나씩 설정
                String url = UriComponentsBuilder
                        .fromUriString(baseUrl + apiKey + servicePath)
                        .queryParam("CRTFC_NO", request.certificationNumber())
                        .queryParam("TYPE", "xml")
                        .toUriString();

                log.debug("GAP API 호출 URL: {}", url);

                String xmlResponse = restTemplate.getForObject(url, String.class);
                if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
                    log.warn("GAP API 응답이 비어있음");
                    throw new BusinessException(FarmrandingResponseCode.GAP_API_ERROR);
                }

                log.debug("GAP API 응답 수신: {}자", xmlResponse.length());

                // 응답 파싱 (필터링 로직은 굳이 필요 없지만, 혹시 여러 건이 넘어오면 내부에서 한번 더 확인)
                GapCertificationResponse result = parseGapXmlResponse(xmlResponse, request.certificationNumber());
                if (result == null) {
                    log.info("GAP 인증 정보를 찾을 수 없음: {}", request.certificationNumber());
                    throw new BusinessException(FarmrandingResponseCode.GAP_NOT_FOUND);
                }
                return result;

            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("GAP 인증 정보 조회 실패: certificationNumber={}, error={}",
                        request.certificationNumber(), e.getMessage(), e);
                throw new BusinessException(FarmrandingResponseCode.GAP_API_ERROR);
            }
        }

    
    @Override
    public boolean validateGapCertificationNumber(String certificationNumber) {
        if (certificationNumber == null || certificationNumber.trim().isEmpty()) {
            return false;
        }
        
        // 기본적인 형식 검증
        String cleanNumber = certificationNumber.trim();
        
        // 숫자만 포함하고 7~15자리인지 확인
        if (!cleanNumber.matches("^[0-9]{7,15}$")) {
            log.debug("GAP 인증번호 형식 오류: {}", cleanNumber);
            return false;
        }
        
        log.debug("GAP 인증번호 형식 검증 통과: {}", cleanNumber);
        return true;
    }
    
    /**
     * GAP API XML 응답 파싱
     */
    private GapCertificationResponse parseGapXmlResponse(String xmlResponse, String targetCertificationNumber) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));
            
            // row 요소들 조회
            NodeList rows = document.getElementsByTagName("row");
            
            log.debug("GAP XML 응답에서 {}개 row 발견", rows.getLength());
            
            // 임시 디버깅: 실제 API에서 제공하는 모든 인증번호 출력
            log.info("=== 현재 GAP API에서 제공하는 실제 인증번호 목록 ===");
            for (int i = 0; i < rows.getLength(); i++) {
                Element row = (Element) rows.item(i);
                String certificationNumber = getElementTextContent(row, "CRTFC_NO");
                String productName = getElementTextContent(row, "PRDLST");
                String institution = getElementTextContent(row, "CRTFC_INSTT");
                log.info("인증번호: {}, 품목: {}, 기관: {}", certificationNumber, productName, institution);
            }
            log.info("=== 인증번호 목록 끝 ===");
            
            for (int i = 0; i < rows.getLength(); i++) {
                Element row = (Element) rows.item(i);
                
                // 인증번호 확인
                String certificationNumber = getElementTextContent(row, "CRTFC_NO");
                
                if (targetCertificationNumber.equals(certificationNumber)) {
                    log.info("GAP 인증 정보 발견: certificationNumber={}", certificationNumber);
                    
                    return new GapCertificationResponse(
                            certificationNumber,
                            getElementTextContent(row, "CRTFC_INSTT"),
                            getElementTextContent(row, "CRTFC_INSTT_CD"),
                            parseIndividualGroupType(getElementTextContent(row, "INDVDL_GRP_SE_CD")),
                            getElementTextContent(row, "MKER_GRP_NM"),
                            parseDate(getElementTextContent(row, "VALID_PD_START_DE")),
                            parseDate(getElementTextContent(row, "VALID_PD_END_DE")),
                            getElementTextContent(row, "PRDLST"),
                            getElementTextContent(row, "PRDLST_CD"),
                            parseInteger(getElementTextContent(row, "REGIST_NHUS_CO")),
                            parseInteger(getElementTextContent(row, "REGIST_LOT_CO")),
                            parseDouble(getElementTextContent(row, "CTVT_AR")),
                            parseDouble(getElementTextContent(row, "PRDCTN_PLAN_QY")),
                            parseDate(getElementTextContent(row, "APPN_DE")),
                            isValidCertification(
                                    parseDate(getElementTextContent(row, "VALID_PD_START_DE")),
                                    parseDate(getElementTextContent(row, "VALID_PD_END_DE"))
                            )
                    );
                }
            }
            
            log.info("GAP 인증 정보를 찾을 수 없음: certificationNumber={}", targetCertificationNumber);
            return null;
            
        } catch (Exception e) {
            log.error("GAP XML 응답 파싱 실패: error={}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * XML 요소의 텍스트 내용 추출
     */
    private String getElementTextContent(Element parent, String tagName) {
        try {
            NodeList nodeList = parent.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                String content = nodeList.item(0).getTextContent();
                return content != null ? content.trim() : "";
            }
            return "";
        } catch (Exception e) {
            log.debug("XML 요소 추출 실패: tagName={}, error={}", tagName, e.getMessage());
            return "";
        }
    }
    
    /**
     * 날짜 문자열 파싱 (yyyyMMdd -> LocalDate)
     */
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.debug("날짜 파싱 실패: dateString={}, error={}", dateString, e.getMessage());
            return null;
        }
    }
    
    /**
     * 정수 문자열 파싱
     */
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.debug("정수 파싱 실패: value={}, error={}", value, e.getMessage());
            return null;
        }
    }
    
    /**
     * 실수 문자열 파싱
     */
    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            log.debug("실수 파싱 실패: value={}, error={}", value, e.getMessage());
            return null;
        }
    }
    
    /**
     * 개인/단체 구분 코드 파싱
     */
    private String parseIndividualGroupType(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "알 수 없음";
        }
        
        return switch (code.trim()) {
            case "1" -> "개인";
            case "2" -> "단체";
            default -> "알 수 없음";
        };
    }
    
    /**
     * 인증 유효성 검증
     */
    private boolean isValidCertification(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        
        LocalDate now = LocalDate.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }
    
    @Override
    public GapCertificationResponse validateAndSearchGapCertification(String certificationNumber) {
        // 1. 형식 검증
        if (!validateGapCertificationNumber(certificationNumber)) {
            log.debug("GAP 인증번호 형식 검증 실패: {}", certificationNumber);
            throw new BusinessException(FarmrandingResponseCode.GAP_INVALID_FORMAT);
        }
        
        // 2. 실제 GAP 정보 조회
        GapSearchRequest request = new GapSearchRequest(certificationNumber, null);
        GapCertificationResponse result = searchGapCertification(request);
        
        if (result == null) {
            log.info("GAP 인증 정보를 찾을 수 없음: {}", certificationNumber);
            throw new BusinessException(FarmrandingResponseCode.GAP_NOT_FOUND);
        }
        
        return result;
    }
} 