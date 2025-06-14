package org.fr.farmranding.dto.pricequote;

import lombok.Builder;
import org.fr.farmranding.entity.pricequote.PriceQuoteRequest;
import org.fr.farmranding.entity.pricing.PremiumPriceSuggestion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 통합된 가격 제안 이력 응답 DTO
 * 일반 가격 제안과 프리미엄 가격 제안을 통합하여 표시
 */
public record UnifiedPriceHistoryResponse(
        Long id,
        String type, // "STANDARD" 또는 "PREMIUM"
        String productName,
        String grade,
        String location,
        BigDecimal suggestedPrice,
        String unit,
        Integer quantity,
        LocalDate harvestDate, // 일반 가격 제안용
        LocalDate analysisDate, // 프리미엄 가격 제안용
        LocalDateTime createdAt,
        
        // 프리미엄 전용 필드
        BigDecimal retailAverage,
        BigDecimal wholesaleAverage,
        String calculationReason
) {
    
    /**
     * 일반 가격 제안에서 변환
     */
    public static UnifiedPriceHistoryResponse fromStandard(PriceQuoteRequest priceQuote) {
        return new UnifiedPriceHistoryResponse(
                priceQuote.getId(),
                "STANDARD",
                priceQuote.getProductName(),
                priceQuote.getGrade(),
                null, // 일반 가격 제안에는 지역 정보 없음
                priceQuote.getFairPrice() != null ? priceQuote.getFairPrice() : priceQuote.getFinalPrice(),
                priceQuote.getUnit(),
                priceQuote.getQuantity(),
                priceQuote.getHarvestDate(),
                null,
                priceQuote.getCreatedAt(),
                null,
                null,
                null
        );
    }
    
    /**
     * 프리미엄 가격 제안에서 변환
     */
    public static UnifiedPriceHistoryResponse fromPremium(PremiumPriceSuggestion premium) {
        return new UnifiedPriceHistoryResponse(
                premium.getId(),
                "PREMIUM",
                premium.getItemName() != null ? premium.getItemName() : "농산물", // 실제 품목명 사용
                getGradeDisplayName(premium.getProductRankCode()),
                premium.getLocation(),
                premium.getSuggestedPrice(),
                "kg", // 프리미엄은 기본적으로 1kg 단위
                1, // 프리미엄은 기본적으로 1kg 기준
                null,
                premium.getEndDate(), // 분석 기준일
                premium.getCreatedAt(),
                premium.getRetail5DayAvg(),
                premium.getWholesale5DayAvg(),
                premium.getCalculationReason()
        );
    }
    
    /**
     * 등급 코드를 표시명으로 변환
     */
    private static String getGradeDisplayName(String rankCode) {
        return switch (rankCode) {
            case "01" -> "특급";
            case "02" -> "상급";
            case "03" -> "보통";
            case "04" -> "중급";
            case "05" -> "하급";
            default -> "중급";
        };
    }
} 