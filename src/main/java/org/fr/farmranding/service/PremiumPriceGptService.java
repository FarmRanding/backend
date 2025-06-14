package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 프리미엄 가격 제안을 위한 GPT-4o 연동 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PremiumPriceGptService {
    
    private final ChatModel chatModel;
    
    /**
     * GPT-4o를 사용하여 프리미엄 가격 제안 및 산출 근거 생성
     */
    public PremiumPriceResult generatePremiumPrice(PremiumPriceRequest request) {
        try {
            log.info("프리미엄 가격 제안 GPT 요청 시작: itemCode={}, location={}", 
                request.getItemCode(), request.getLocation());
            
            String prompt = createPremiumPricePrompt(request);
            log.debug("GPT 프롬프트: {}", prompt);
            
            ChatResponse response = chatModel.call(
                new Prompt(prompt, OpenAiChatOptions.builder()
                    .model("gpt-4o") // o3 모델 사용
                    .maxTokens(1000)
                    .temperature(0.3) // 정확한 계산을 위해 낮은 온도
                    .build())
            );
            
            String gptResponse = response.getResult().getOutput().getText().trim();
            log.debug("GPT 응답: {}", gptResponse);
            
            return parseGptResponse(gptResponse, request);
            
        } catch (Exception e) {
            log.error("프리미엄 가격 제안 GPT 요청 실패: {}", e.getMessage(), e);
            
            // 폴백 응답 생성
            return createFallbackResponse(request);
        }
    }
    
    /**
     * 프리미엄 가격 제안 프롬프트 생성
     */
    private String createPremiumPricePrompt(PremiumPriceRequest request) {
        return String.format("""
            당신은 농산물 가격 전문가입니다. 아래 KAMIS API 데이터를 바탕으로 프리미엄 가격 제안을 해주세요.
            
            ## 가격 산출 공식
            직거래 제안가 = min( 소매 5일 평균 × 0.95, max( 도매 5일 평균 × 1.20, 도매 5일 평균 × α × 0.5 × 0.9 ) )
            α = 소매 5일 평균 ÷ 도매 5일 평균
            
            ## 품목 정보
            - 품목명: %s
            - 품종명: %s
            - 등급: %s
            - 거래 위치: %s
            - 조회 기간: %s ~ %s
            
            ## 소매 가격 데이터 (p_productclscode=01)
            %s
            
            ## 도매 가격 데이터 (p_productclscode=02)
            %s
            
            ## 분석 요청사항
            1. 위 데이터에서 거래 위치(%s)와 일치하는 지역 데이터가 있으면 해당 값을 우선 사용하세요.
            2. 해당 지역 데이터가 없으면 전체 평균값을 사용하여 계산하세요.
            3. 소매/도매 각각의 5일 평균 가격을 계산하세요.
            4. 위 공식을 정확히 적용하여 최종 직거래 제안가를 계산하세요.
            5. 계산 과정과 근거를 명확히 제시하세요.
            
            ## 응답 형식 (JSON)
            반드시 아래 JSON 형식으로만 응답해주세요:
            {
              "suggestedPrice": 최종_제안가격(숫자만),
              "retailAverage": 소매_5일_평균(숫자만),
              "wholesaleAverage": 도매_5일_평균(숫자만),
              "alphaRatio": α값(숫자만),
              "calculationReason": "계산 과정과 근거를 자세히 설명"
            }
            """,
            request.getItemName(),
            request.getKindName(),
            request.getProductRankCode(),
            request.getLocation(),
            request.getStartDate(),
            request.getEndDate(),
            request.getRetailData(),
            request.getWholesaleData(),
            request.getLocation()
        );
    }
    
    /**
     * GPT 응답 파싱
     */
    private PremiumPriceResult parseGptResponse(String gptResponse, PremiumPriceRequest request) {
        try {
            // JSON 추출 (```json 감싸진 경우 처리)
            String jsonContent = extractJsonFromResponse(gptResponse);
            
            // JSON 파싱은 간단하게 문자열 처리로 수행
            BigDecimal suggestedPrice = extractBigDecimalValue(jsonContent, "suggestedPrice");
            BigDecimal retailAverage = extractBigDecimalValue(jsonContent, "retailAverage");
            BigDecimal wholesaleAverage = extractBigDecimalValue(jsonContent, "wholesaleAverage");
            BigDecimal alphaRatio = extractBigDecimalValue(jsonContent, "alphaRatio");
            String calculationReason = extractStringValue(jsonContent, "calculationReason");
            
            return PremiumPriceResult.builder()
                    .suggestedPrice(suggestedPrice)
                    .retailAverage(retailAverage)
                    .wholesaleAverage(wholesaleAverage)
                    .alphaRatio(alphaRatio)
                    .calculationReason(calculationReason)
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("GPT 응답 파싱 실패: {}, 응답: {}", e.getMessage(), gptResponse);
            return createFallbackResponse(request);
        }
    }
    
    /**
     * JSON에서 특정 필드의 BigDecimal 값 추출
     */
    private BigDecimal extractBigDecimalValue(String json, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\"\\s*:\\s*([0-9]+\\.?[0-9]*)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return new BigDecimal(m.group(1));
            }
        } catch (Exception e) {
            log.warn("BigDecimal 값 추출 실패: field={}, error={}", fieldName, e.getMessage());
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * JSON에서 특정 필드의 문자열 값 추출
     */
    private String extractStringValue(String json, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.warn("문자열 값 추출 실패: field={}, error={}", fieldName, e.getMessage());
        }
        return "계산 근거를 가져올 수 없습니다.";
    }
    
    /**
     * 응답에서 JSON 부분만 추출
     */
    private String extractJsonFromResponse(String response) {
        // ```json으로 감싸진 경우 처리
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        
        // { }로 감싸진 JSON 찾기
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        
        return response;
    }
    
    /**
     * 폴백 응답 생성
     */
    private PremiumPriceResult createFallbackResponse(PremiumPriceRequest request) {
        return PremiumPriceResult.builder()
                .suggestedPrice(new BigDecimal("10000"))
                .retailAverage(new BigDecimal("12000"))
                .wholesaleAverage(new BigDecimal("8000"))
                .alphaRatio(new BigDecimal("1.5"))
                .calculationReason("GPT 분석에 실패하여 기본 가격을 제공합니다. 실제 시장 데이터를 확인해주세요.")
                .success(false)
                .build();
    }
    
    /**
     * 프리미엄 가격 제안 요청 데이터
     */
    public static class PremiumPriceRequest {
        private String itemCode;
        private String itemName;
        private String kindCode;
        private String kindName;
        private String productRankCode;
        private String location;
        private String startDate;
        private String endDate;
        private String retailData;
        private String wholesaleData;
        
        // Getters
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public String getKindCode() { return kindCode; }
        public String getKindName() { return kindName; }
        public String getProductRankCode() { return productRankCode; }
        public String getLocation() { return location; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getRetailData() { return retailData; }
        public String getWholesaleData() { return wholesaleData; }
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final PremiumPriceRequest request = new PremiumPriceRequest();
            
            public Builder itemCode(String itemCode) { request.itemCode = itemCode; return this; }
            public Builder itemName(String itemName) { request.itemName = itemName; return this; }
            public Builder kindCode(String kindCode) { request.kindCode = kindCode; return this; }
            public Builder kindName(String kindName) { request.kindName = kindName; return this; }
            public Builder productRankCode(String productRankCode) { request.productRankCode = productRankCode; return this; }
            public Builder location(String location) { request.location = location; return this; }
            public Builder startDate(String startDate) { request.startDate = startDate; return this; }
            public Builder endDate(String endDate) { request.endDate = endDate; return this; }
            public Builder retailData(String retailData) { request.retailData = retailData; return this; }
            public Builder wholesaleData(String wholesaleData) { request.wholesaleData = wholesaleData; return this; }
            
            public PremiumPriceRequest build() { return request; }
        }
    }
    
    /**
     * 프리미엄 가격 제안 결과
     */
    public static class PremiumPriceResult {
        private BigDecimal suggestedPrice;
        private BigDecimal retailAverage;
        private BigDecimal wholesaleAverage;
        private BigDecimal alphaRatio;
        private String calculationReason;
        private boolean success;
        
        // Getters
        public BigDecimal getSuggestedPrice() { return suggestedPrice; }
        public BigDecimal getRetailAverage() { return retailAverage; }
        public BigDecimal getWholesaleAverage() { return wholesaleAverage; }
        public BigDecimal getAlphaRatio() { return alphaRatio; }
        public String getCalculationReason() { return calculationReason; }
        public boolean isSuccess() { return success; }
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final PremiumPriceResult result = new PremiumPriceResult();
            
            public Builder suggestedPrice(BigDecimal suggestedPrice) { result.suggestedPrice = suggestedPrice; return this; }
            public Builder retailAverage(BigDecimal retailAverage) { result.retailAverage = retailAverage; return this; }
            public Builder wholesaleAverage(BigDecimal wholesaleAverage) { result.wholesaleAverage = wholesaleAverage; return this; }
            public Builder alphaRatio(BigDecimal alphaRatio) { result.alphaRatio = alphaRatio; return this; }
            public Builder calculationReason(String calculationReason) { result.calculationReason = calculationReason; return this; }
            public Builder success(boolean success) { result.success = success; return this; }
            
            public PremiumPriceResult build() { return result; }
        }
    }
} 