package org.fr.farmranding.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.dto.response.CropResponse;
import org.fr.farmranding.dto.response.VarietyResponse;
import org.fr.farmranding.entity.StandardCode;
import org.fr.farmranding.repository.StandardCodeRepository;
import org.fr.farmranding.service.StandardCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardCodeServiceImpl implements StandardCodeService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StandardCodeRepository standardCodeRepository;
    
    @Value("${farmranding.api.standard-code.base-url:https://api.odcloud.kr/api/15060250/v1/uddi:75592a9e-cd61-437c-900d-a56d0ce01618}")
    private String baseUrl;
    
    @Value("${farmranding.api.standard-code.service-key}")
    private String serviceKey;
    
    private static final int PER_PAGE = 1000;

    @PostConstruct
    public void initializeData() {
        log.info("애플리케이션 시작 시 표준코드 데이터 동기화 시작");
        // 기존 데이터가 없으면 초기화
        if (standardCodeRepository.countByIsActive(true) == 0) {
            syncStandardCodeData();
        } else {
            log.info("기존 표준코드 데이터가 존재합니다. 건수: {}", 
                standardCodeRepository.countByIsActive(true));
        }
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시에 실행
    public void syncStandardCodeData() {
        try {
            log.info("표준코드 데이터 동기화 시작");
            
            // 기존 데이터 비활성화
            List<StandardCode> existingCodes = standardCodeRepository.findAll();
            existingCodes.forEach(code -> {
                StandardCode updatedCode = StandardCode.builder()
                    .lclassCode(code.getLclassCode())
                    .lclassName(code.getLclassName())
                    .mclassCode(code.getMclassCode())
                    .mclassName(code.getMclassName())
                    .sclassCode(code.getSclassCode())
                    .sclassName(code.getSclassName())
                    .isActive(false)
                    .build();
                standardCodeRepository.save(updatedCode);
            });
            
            // 새 데이터 가져오기
            List<StandardCode> newCodes = fetchAllStandardCodeData();
            
            // DB에 저장
            standardCodeRepository.saveAll(newCodes);
            
            log.info("표준코드 데이터 동기화 완료. 총 {}개 저장됨", newCodes.size());
            
        } catch (Exception e) {
            log.error("표준코드 데이터 동기화 중 오류 발생", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CropResponse> searchCrops(String query, int limit) {
        try {
            String searchQuery = query == null ? "" : query.trim();
            List<Object[]> results = standardCodeRepository
                .findDistinctCropsByMclassNameContaining(searchQuery);
            
            return results.stream()
                .limit(limit)
                .map(row -> CropResponse.of((String) row[0], (String) row[1]))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("작물 검색 실패", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CropResponse> getPopularCrops(int limit) {
        try {
            List<Object[]> results = standardCodeRepository.findPopularCrops();
            
            return results.stream()
                .limit(limit)
                .map(row -> CropResponse.of((String) row[0], (String) row[1]))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("인기 작물 조회 실패", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VarietyResponse> searchVarieties(String cropCode, String query, int limit) {
        try {
            String searchQuery = query == null ? "" : query.trim();
            List<StandardCode> results = standardCodeRepository
                .findVarietiesByMclassCodeAndSclassNameContaining(cropCode, searchQuery);
            
            return results.stream()
                .limit(limit)
                .map(VarietyResponse::from)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("품종 검색 실패", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 공공데이터에서 전체 표준코드 데이터를 가져오는 메서드
     */
    private List<StandardCode> fetchAllStandardCodeData() throws Exception {
        List<StandardCode> allCodes = new ArrayList<>();
        int page = 1;
        int totalCount = 0;
        boolean firstCall = true;
        
        do {
            String url = String.format("%s?page=%d&perPage=%d&serviceKey=%s", 
                baseUrl, page, PER_PAGE, serviceKey);
            
            log.debug("API 호출: page={}, url={}", page, url);
            log.info("API 키: {}", serviceKey); // API 키 로그 추가
            
            String response = restTemplate.getForObject(URI.create(url), String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            
            if (firstCall) {
                totalCount = rootNode.path("totalCount").asInt();
                log.info("전체 데이터 건수: {}", totalCount);
                firstCall = false;
            }
            
            JsonNode dataArray = rootNode.path("data");
            if (dataArray.isArray()) {
                for (JsonNode item : dataArray) {
                    String lclassCode = item.path("LCLASSCODE").asText();
                    String lclassName = item.path("LCLASSNAME").asText();
                    String mclassCode = item.path("MCLASSCODE").asText();
                    String mclassName = item.path("MCLASSNAME").asText();
                    String sclassCode = item.path("SCLASSCODE").asText();
                    String sclassName = item.path("SCLASSNAME").asText();
                    
                    StandardCode standardCode = StandardCode.builder()
                        .lclassCode(lclassCode)
                        .lclassName(lclassName)
                        .mclassCode(mclassCode)
                        .mclassName(mclassName)
                        .sclassCode(sclassCode)
                        .sclassName(sclassName)
                        .isActive(true)
                        .build();
                    
                    allCodes.add(standardCode);
                }
            }
            
            int currentCount = rootNode.path("currentCount").asInt();
            if (currentCount < PER_PAGE) {
                break; // 마지막 페이지
            }
            page++;
            
        } while (page <= Math.ceil((double) totalCount / PER_PAGE));
        
        log.info("공공API에서 {}개 표준코드 데이터 수집 완료", allCodes.size());
        return allCodes;
    }
} 