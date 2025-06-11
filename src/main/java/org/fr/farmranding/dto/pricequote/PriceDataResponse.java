package org.fr.farmranding.dto.pricequote;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "가격 조회 응답 DTO")
public record PriceDataResponse(
        
        @Schema(description = "품목명", example = "고구마")
        String productName,
        
        @Schema(description = "등급", example = "특급")
        String grade,
        
        @Schema(description = "단위", example = "10키로상자(특)")
        String unit,
        
        @Schema(description = "조회 기간", example = "08월17일 ~ 08월17일")
        String period,
        
        @Schema(description = "5년 평균가격 (원)", example = "42500")
        BigDecimal averagePrice,
        
        @Schema(description = "추천 가격 (원)", example = "42500")
        BigDecimal recommendedPrice,
        
        @Schema(description = "년도별 가격 데이터")
        List<YearlyPriceData> yearlyPrices,
        
        @Schema(description = "표준 가격 (원)", example = "40245.68")
        BigDecimal standardPrice
) {
    
    @Schema(description = "년도별 가격 데이터")
    public record YearlyPriceData(
            @Schema(description = "년도", example = "2023")
            String year,
            
            @Schema(description = "가격 (원)", example = "42008")
            BigDecimal price
    ) {}
    
    /**
     * 가락시장 XML 데이터로부터 응답 생성
     */
    public static PriceDataResponse from(String productName, String grade, String unit, String period,
                                       List<YearlyPriceData> yearlyPrices, BigDecimal standardPrice) {
        
        // 5년 평균 계산 (0보다 큰 값만 계산)
        List<BigDecimal> validPrices = yearlyPrices.stream()
                .map(YearlyPriceData::price)
                .filter(price -> price.compareTo(BigDecimal.ZERO) > 0) // 0보다 큰 값만
                .toList();
        
        BigDecimal averagePrice = validPrices.isEmpty() 
                ? BigDecimal.ZERO 
                : validPrices.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(validPrices.size()), 2, java.math.RoundingMode.HALF_UP);
        
        return new PriceDataResponse(
                productName,
                grade,
                unit,
                period,
                averagePrice,
                averagePrice, // 추천 가격 = 평균 가격
                yearlyPrices,
                standardPrice
        );
    }
} 