package org.fr.farmranding.dto.premium;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.pricing.PremiumPriceSuggestion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 프리미엄 가격 제안 응답 DTO
 */
@Schema(description = "프리미엄 가격 제안 응답")
public record PremiumPriceResponse(
        
        @Schema(description = "제안 ID", example = "1")
        Long id,
        
        @Schema(description = "제안 가격 (원)", example = "15000")
        BigDecimal suggestedPrice,
        
        @Schema(description = "소매 5일 평균 (원)", example = "18000")
        BigDecimal retail5DayAvg,
        
        @Schema(description = "도매 5일 평균 (원)", example = "12000")
        BigDecimal wholesale5DayAvg,
        
        @Schema(description = "α값 (소매/도매 비율)", example = "1.5")
        BigDecimal alphaRatio,
        
        @Schema(description = "계산 근거", example = "소매 5일 평균 18,000원, 도매 5일 평균 12,000원을 기준으로...")
        String calculationReason,
        
        @Schema(description = "품목 정보")
        ProductInfo productInfo,
        
        @Schema(description = "분석 기간")
        AnalysisPeriod analysisPeriod,
        
        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt
) {
    
    @Schema(description = "품목 정보")
    public record ProductInfo(
            @Schema(description = "품목 카테고리 코드", example = "200")
            String itemCategoryCode,
            
            @Schema(description = "품목 코드", example = "241")
            String itemCode,
            
            @Schema(description = "품종 코드", example = "00")
            String kindCode,
            
            @Schema(description = "등급 코드", example = "04")
            String productRankCode,
            
            @Schema(description = "거래 위치", example = "서울")
            String location
    ) {}
    
    @Schema(description = "분석 기간")
    public record AnalysisPeriod(
            @Schema(description = "시작일", example = "2024-01-10")
            LocalDate startDate,
            
            @Schema(description = "종료일", example = "2024-01-15")
            LocalDate endDate
    ) {}
    
    /**
     * Entity를 DTO로 변환하는 정적 팩토리 메서드
     */
    public static PremiumPriceResponse from(PremiumPriceSuggestion entity) {
        return new PremiumPriceResponse(
                entity.getId(),
                entity.getSuggestedPrice(),
                entity.getRetail5DayAvg(),
                entity.getWholesale5DayAvg(),
                entity.getAlphaRatio(),
                entity.getCalculationReason(),
                new ProductInfo(
                        entity.getItemCategoryCode(),
                        entity.getItemCode(),
                        entity.getKindCode(),
                        entity.getProductRankCode(),
                        entity.getLocation()
                ),
                new AnalysisPeriod(
                        entity.getStartDate(),
                        entity.getEndDate()
                ),
                entity.getCreatedAt()
        );
    }
} 