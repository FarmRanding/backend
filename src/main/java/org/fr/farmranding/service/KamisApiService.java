package org.fr.farmranding.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * KAMIS API 호출 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KamisApiService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${farmranding.garak.id}")
    private String garakId;
    
    @Value("${farmranding.garak.password}")
    private String garakPassword;
    
    private static final String KAMIS_API_URL = "https://www.kamis.or.kr/service/price/xml.do";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * KAMIS API 호출 - 소매/도매 가격 데이터 조회
     * 
     * @param productClassCode "01":소매, "02":도매
     * @param itemCategoryCode 부류코드
     * @param itemCode 품목코드
     * @param kindCode 품종코드
     * @param productRankCode 등급코드
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return JSON 형태의 응답 데이터
     */
    public String fetchPriceData(String productClassCode, String itemCategoryCode, 
                                String itemCode, String kindCode, String productRankCode,
                                LocalDate startDate, LocalDate endDate) {
        
        try {
            String url = UriComponentsBuilder.fromHttpUrl(KAMIS_API_URL)
                    .queryParam("action", "periodProductList")
                    .queryParam("p_productclscode", productClassCode)
                    .queryParam("p_startday", startDate.format(DATE_FORMATTER))
                    .queryParam("p_endday", endDate.format(DATE_FORMATTER))
                    .queryParam("p_itemcategorycode", itemCategoryCode)
                    .queryParam("p_itemcode", itemCode)
                    .queryParam("p_kindcode", kindCode)
                    .queryParam("p_productrankcode", productRankCode)
                    .queryParam("p_convert_kg_yn", "Y")
                    .queryParam("p_cert_key", garakPassword)
                    .queryParam("p_cert_id", garakId)
                    .queryParam("p_returntype", "json")
                    .build()
                    .toUriString();
            
            log.info("KAMIS API 호출: {}", url);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8");
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String response = responseEntity.getBody();
            
            if (response == null || response.trim().isEmpty()) {
                log.warn("KAMIS API 응답이 비어있습니다.");
                return "{}";
            }
            
            log.debug("KAMIS API 응답 (처음 500자): {}", 
                response.length() > 500 ? response.substring(0, 500) + "..." : response);
            
            return response;
            
        } catch (Exception e) {
            log.error("KAMIS API 호출 실패: productClassCode={}, itemCode={}, error={}", 
                productClassCode, itemCode, e.getMessage(), e);
            return "{}";
        }
    }
    
    /**
     * 소매 가격 데이터 조회
     */
    public String fetchRetailPriceData(String itemCategoryCode, String itemCode, 
                                      String kindCode, String productRankCode,
                                      LocalDate startDate, LocalDate endDate) {
        return fetchPriceData("01", itemCategoryCode, itemCode, kindCode, 
                             productRankCode, startDate, endDate);
    }
    
    /**
     * 도매 가격 데이터 조회
     */
    public String fetchWholesalePriceData(String itemCategoryCode, String itemCode, 
                                         String kindCode, String productRankCode,
                                         LocalDate startDate, LocalDate endDate) {
        return fetchPriceData("02", itemCategoryCode, itemCode, kindCode, 
                             productRankCode, startDate, endDate);
    }
    
    /**
     * 소매/도매 데이터를 모두 조회하여 통합 반환
     */
    public KamisPriceResponse fetchBothPriceData(String itemCategoryCode, String itemCode, 
                                                String kindCode, String productRankCode,
                                                LocalDate startDate, LocalDate endDate) {
        
        log.info("소매/도매 가격 데이터 통합 조회 시작: itemCode={}, kindCode={}, 기간={} ~ {}", 
            itemCode, kindCode, startDate, endDate);
        
        // 소매 데이터 조회
        String retailData = fetchRetailPriceData(itemCategoryCode, itemCode, kindCode, 
                                                productRankCode, startDate, endDate);
        
        // 도매 데이터 조회
        String wholesaleData = fetchWholesalePriceData(itemCategoryCode, itemCode, kindCode, 
                                                      productRankCode, startDate, endDate);
        
        return KamisPriceResponse.builder()
                .retailData(retailData)
                .wholesaleData(wholesaleData)
                .itemCode(itemCode)
                .kindCode(kindCode)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
    
    /**
     * 5일간 가격 데이터 조회 (오늘부터 5일 전까지)
     */
    public KamisPriceResponse fetch5DayPriceData(String itemCategoryCode, String itemCode, 
                                                String kindCode, String productRankCode) {
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(4); // 5일간 (오늘 포함)
        
        return fetchBothPriceData(itemCategoryCode, itemCode, kindCode, productRankCode, 
                                 startDate, endDate);
    }
    
    /**
     * 5일간 가격 데이터 조회 (지정된 날짜 기준)
     */
    public KamisPriceResponse fetch5DayPriceData(String itemCategoryCode, String itemCode, 
                                                String kindCode, String productRankCode, String baseDate) {
        
        LocalDate endDate = LocalDate.parse(baseDate, DATE_FORMATTER);
        LocalDate startDate = endDate.minusDays(4); // 5일간 (기준일 포함)
        
        return fetchBothPriceData(itemCategoryCode, itemCode, kindCode, productRankCode, 
                                 startDate, endDate);
    }
    
    /**
     * 5일간 가격 데이터 조회 (LocalDate 기준)
     */
    public KamisPriceResponse fetch5DayPriceData(String itemCategoryCode, String itemCode, 
                                                String kindCode, String productRankCode, LocalDate baseDate) {
        
        LocalDate endDate = baseDate;
        LocalDate startDate = endDate.minusDays(4); // 5일간 (기준일 포함)
        
        return fetchBothPriceData(itemCategoryCode, itemCode, kindCode, productRankCode, 
                                 startDate, endDate);
    }
    
    /**
     * KAMIS 가격 응답 데이터 클래스
     */
    public static class KamisPriceResponse {
        private final String retailData;
        private final String wholesaleData;
        private final String itemCode;
        private final String kindCode;
        private final LocalDate startDate;
        private final LocalDate endDate;
        
        private KamisPriceResponse(Builder builder) {
            this.retailData = builder.retailData;
            this.wholesaleData = builder.wholesaleData;
            this.itemCode = builder.itemCode;
            this.kindCode = builder.kindCode;
            this.startDate = builder.startDate;
            this.endDate = builder.endDate;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String retailData;
            private String wholesaleData;
            private String itemCode;
            private String kindCode;
            private LocalDate startDate;
            private LocalDate endDate;
            
            public Builder retailData(String retailData) {
                this.retailData = retailData;
                return this;
            }
            
            public Builder wholesaleData(String wholesaleData) {
                this.wholesaleData = wholesaleData;
                return this;
            }
            
            public Builder itemCode(String itemCode) {
                this.itemCode = itemCode;
                return this;
            }
            
            public Builder kindCode(String kindCode) {
                this.kindCode = kindCode;
                return this;
            }
            
            public Builder startDate(LocalDate startDate) {
                this.startDate = startDate;
                return this;
            }
            
            public Builder endDate(LocalDate endDate) {
                this.endDate = endDate;
                return this;
            }
            
            public KamisPriceResponse build() {
                return new KamisPriceResponse(this);
            }
        }
        
        // Getters
        public String getRetailData() { return retailData; }
        public String getWholesaleData() { return wholesaleData; }
        public String getItemCode() { return itemCode; }
        public String getKindCode() { return kindCode; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
    }
} 