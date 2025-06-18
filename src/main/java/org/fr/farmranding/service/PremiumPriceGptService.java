package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
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
                    .model("gpt-4o") // 4o 모델 사용
                    .maxTokens(1000)
                    .temperature(0.3) // 정확한 계산을 위해 낮은 온도
                    .build())
            );
            
            String gptResponse = response.getResult().getOutput().getText().trim();
            log.debug("GPT 응답: {}", gptResponse);
            
            return parseGptResponse(gptResponse, request);
            
        } catch (Exception e) {
            log.error("프리미엄 가격 제안 GPT 요청 실패: {}", e.getMessage(), e);
            
            // BusinessException으로 변경
            throw new BusinessException(FarmrandingResponseCode.AI_SERVICE_ERROR);
        }
    }
    
    /**
     * 프리미엄 가격 제안 프롬프트 생성
     */
    private String createPremiumPricePrompt(PremiumPriceRequest request) {
        // 등급 설명 매핑
        String gradeDescription = switch (request.getProductRankCode()) {
            case "04" -> "상급 (최고 품질)";
            case "05" -> "중급 (일반 품질)";
            default -> "중급 (일반 품질)";
        };
        
        return String.format("""
            당신은 농산물 가격 전문가입니다. 아래 KAMIS(한국농수산식품유통공사) 공식 데이터를 바탕으로 신뢰성 있는 직거래 가격을 제안해주세요.
            
            ## 가격 산출 공식
            직거래 제안가 = min( 소매 5일 평균 × 0.95, max( 도매 5일 평균 × 1.20, 도매 5일 평균 × α × 0.5 × 0.9 ) )
            α = 소매 5일 평균 ÷ 도매 5일 평균
            
            ## 품목 정보
            - 품목명: %s
            - 품종명: %s
            - 등급: %s
            - 거래 위치: %s
            - 분석 기간: 작년 동일날짜 앞뒤 기준 %s ~ %s (5일간)
            - **중요: 품목명에 포함된 단위 정보를 반드시 고려하여 1개 기준으로 정규화해주세요**
            - 도매 데이터: 품목명이 "애호박(20개)"처럼 묶음 단위인 경우, 가격을 20으로 나누어 1개당 가격 계산
            - 소매 데이터: 품목명이 "애호박(1개)"인 경우, 그대로 1개당 가격으로 사용
            - 반드시 모든 가격을 1개 기준으로 환산한 후 평균을 계산하세요
            - 예시: 도매 "애호박(20개) 14,820원" → 1개당 741원, 소매 "애호박(1개) 1,065원" → 1개당 1,065원
            
            ## 소매 가격 데이터 (p_productclscode=01)
            %s
            
            ## 도매 가격 데이터 (p_productclscode=02)
            %s
            
            ## 분석 요청사항
            1. **1단계 - 단위 정규화 (필수)**: 
               - 도매/소매 데이터의 모든 가격을 1개 기준으로 환산
               - 품목명의 "(숫자개)" 부분을 확인하여 개수로 나누기
               - 예: "애호박(20개) 14,820원" → 14,820 ÷ 20 = 741원/개
            2. 지역 데이터 존재 여부: %s
            3. 위 데이터에서 거래 위치(%s)와 일치하는 지역 데이터가 있으면 해당 값을 우선 사용하세요.
            4. 해당 지역 데이터가 없으면 전국 평균값을 사용하여 계산하세요.
            5. **정규화된 1개당 가격**으로 작년 동일한 시기 앞뒤 5일간의 소매/도매 각각 평균 가격을 계산하세요.
            6. 위 공식을 정확히 적용하여 최종 직거래 제안가를 계산하세요.
            7. calculationReason은 일반인이 이해하기 쉽고 신뢰성을 느낄 수 있도록 작성하세요.
            8. kamis 공식 데이터를 사용했다는 문구는 이미 존재하므로 별도로 언급하지 마세요.
            
            ## calculationReason 작성 가이드라인
            - **자연스러운 대화체**: 농부와 대화하듯 친근하고 쉬운 말로 설명
            - **지역 언급**: 만약 주어진 지역 데이터를 사용했으면 해당 지역을 언급하고, 해당 지역 데이터가 존재하지 않아 전국 데이터를 사용했다면 전국 데이터를 사용했다고 언급하세요.
            - **핵심만 간단히**: 복잡한 수식 대신 "왜 이 가격인지" 이유를 쉽게 설명
            - **실용적 관점**: 시장 상황과 판매 전략 관점에서 설명
            - **창의적 표현**: 매번 다른 방식으로 설명하되, 이해하기 쉽게
            - **금지사항**: 
              * "~~습니다", "~~됩니다" 등 딱딱한 존댓말 금지
              * "min()", "max()" 등 수학 공식 표현 금지
              * "α", "계수", "공식" 등 어려운 용어 금지
              * 반말 사용 금지 (~~해, ~~야, ~~지, ~~네, ~~거든 등)
            - **권장 표현**: 
              * "요즘 시장에서 ~해요", "이 정도면 괜찮을 것 같아요"
              * "소매가가 높았어요", "도매가 대비 ~배 정도예요"
         지     * "지금 시세를 보니까요", "이런 이유로 추천해요"
              * 모든 문장은 "~해요", "~예요", "~이에요" 등 부드러운 존댓말로 마무리
            - **예시 톤**: "작년 같은 시기 도매시장에서 애호박(20개) 14,820원이었는데 1개로 환산하면 741원, 마트에서는 1개당 1,065원 정도에 거래됐었어요. 소매가가 도매가보다 거의 1.4배 정도 높았던 상황이라서, 직거래로는 960원 정도가 적당할 것 같아요. 너무 비싸지도 않고 농부님께도 손해 안 되는 선이에요."
            - **창의성"": 예시는 예시일 뿐이지, 항상 그런 형식을 따르지는 말고 적절한 창의를 발휘하여 설명 작성해
            
            ## 응답 형식 (JSON)
            반드시 아래 JSON 형식으로만 응답해주세요:
            {
              "suggestedPrice": 최종_제안가격(1개_기준_숫자만),
              "retailAverage": 소매_5일_평균(1개_기준_숫자만),
              "wholesaleAverage": 도매_5일_평균(1개_기준_숫자만),
              "alphaRatio": α값(숫자만),
              "calculationReason": "KAMIS 공식 데이터를 기반으로 한 신뢰성 있는 설명 (일반인이 이해하기 쉽게)"
            }
            
            **주의**: 모든 가격 값은 반드시 1개 기준으로 정규화된 값이어야 합니다.
            """,
            request.getItemName(),
            request.getKindName(),
            gradeDescription,
            request.getLocation(),
            request.getStartDate(),
            request.getEndDate(),
            request.getRetailData(),
            request.getWholesaleData(),
            request.hasRegionalData() ? "있음" : "없음",
            request.getLocation()
        );
    }
    
    /**
     * GPT 응답 파싱
     */
    private PremiumPriceResult parseGptResponse(String gptResponse, PremiumPriceRequest request) {
        try {
            // 먼저 에러 패턴 체크
            String responseLower = gptResponse.toLowerCase();
            if (responseLower.contains("죄송하지만") || 
                responseLower.contains("제공된 데이터") || 
                responseLower.contains("구체적인 숫자가 포함되어 있지 않아") ||
                responseLower.contains("정확한 계산을 수행할 수 없습니다") ||
                responseLower.contains("데이터가 없") ||
                responseLower.contains("분석할 수 없")) {
                log.error("GPT에서 데이터 부족 응답: response={}", gptResponse);
                throw new BusinessException(FarmrandingResponseCode.KAMIS_DATA_NOT_AVAILABLE);
            }
            
            // JSON 추출 (```json 감싸진 경우 처리)
            String jsonContent = extractJsonFromResponse(gptResponse);
            
            // JSON 파싱은 간단하게 문자열 처리로 수행
            BigDecimal suggestedPrice = extractBigDecimalValue(jsonContent, "suggestedPrice");
            BigDecimal retailAverage = extractBigDecimalValue(jsonContent, "retailAverage");
            BigDecimal wholesaleAverage = extractBigDecimalValue(jsonContent, "wholesaleAverage");
            BigDecimal alphaRatio = extractBigDecimalValue(jsonContent, "alphaRatio");
            String calculationReason = extractStringValue(jsonContent, "calculationReason");
            
            // 추출된 값들이 유효한지 검증
            if (suggestedPrice.compareTo(BigDecimal.ZERO) <= 0 || 
                retailAverage.compareTo(BigDecimal.ZERO) <= 0 || 
                wholesaleAverage.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("GPT 응답에서 유효하지 않은 가격 데이터: suggestedPrice={}, retailAverage={}, wholesaleAverage={}", 
                    suggestedPrice, retailAverage, wholesaleAverage);
                throw new BusinessException(FarmrandingResponseCode.KAMIS_DATA_NOT_AVAILABLE);
            }
            
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
            throw new BusinessException(FarmrandingResponseCode.AI_SERVICE_ERROR);
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
        log.debug("원본 GPT 응답: {}", response);
        
        // ```json으로 감싸진 경우 처리
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                String extracted = response.substring(start, end).trim();
                log.debug("```json 방식으로 추출된 JSON: {}", extracted);
                return extracted;
            }
        }
        
        // ```로만 감싸진 경우 처리
        if (response.contains("```")) {
            int start = response.indexOf("```") + 3;
            int end = response.indexOf("```", start);
            if (end > start) {
                String content = response.substring(start, end).trim();
                // json으로 시작하면 제거
                if (content.startsWith("json")) {
                    content = content.substring(4).trim();
                }
                log.debug("``` 방식으로 추출된 JSON: {}", content);
                return content;
            }
        }
        
        // { }로 감싸진 JSON 찾기
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        if (start >= 0 && end > start) {
            String extracted = response.substring(start, end + 1);
            log.debug("{} 방식으로 추출된 JSON: {}", extracted);
            return extracted;
        }
        
        log.debug("JSON 추출 실패, 원본 반환: {}", response);
        return response;
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
        private boolean hasRegionalData;
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
        public boolean hasRegionalData() { return hasRegionalData; }
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
            public Builder hasRegionalData(boolean hasRegionalData) { request.hasRegionalData = hasRegionalData; return this; }
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