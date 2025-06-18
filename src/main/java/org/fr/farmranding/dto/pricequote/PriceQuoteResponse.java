package org.fr.farmranding.dto.pricequote;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.pricequote.PriceQuoteRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "가격 견적 응답 DTO")
public record PriceQuoteResponse(
        
        @Schema(description = "요청 ID", example = "1")
        Long id,
        
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        
        @Schema(description = "품목 ID", example = "1")
        Long productId,
        
        @Schema(description = "가락시장 품목 코드", example = "73003")
        String garakCode,
        
        @Schema(description = "품목명", example = "가공 게지")
        String productName,
        
        @Schema(description = "등급", example = "특급")
        String grade,
        
        @Schema(description = "수확 예정일", example = "2024-02-15")
        LocalDate harvestDate,
        
        @Schema(description = "단위", example = "kg")
        String unit,
        
        @Schema(description = "수량", example = "1")
        Integer quantity,
        
        @Schema(description = "예상 가격 (원)", example = "15000")
        BigDecimal estimatedPrice,
        
        @Schema(description = "상태", example = "DRAFT")
        PriceQuoteStatus status,
        
        @Schema(description = "최저가 (원)", example = "12000")
        BigDecimal minPrice,
        
        @Schema(description = "최고가 (원)", example = "20000")
        BigDecimal maxPrice,
        
        @Schema(description = "평균가 (원)", example = "16000")
        BigDecimal avgPrice,
        
        @Schema(description = "AI 추천가격 (원)", example = "18000")
        BigDecimal fairPrice,
        
        @Schema(description = "최종 분석 가격 (원)", example = "17500")
        BigDecimal finalPrice,
        
        @Schema(description = "5년간 가격 추이 데이터 (JSON)", example = "[{\"year\":\"2020\",\"price\":15000},{\"year\":\"2021\",\"price\":16500}]")
        String yearlyPriceData,
        
        @Schema(description = "차트 최저가 (원)", example = "12000")
        BigDecimal chartMinPrice,
        
        @Schema(description = "차트 최고가 (원)", example = "20000")
        BigDecimal chartMaxPrice,
        
        @Schema(description = "조회 기준일", example = "2024-02-15")
        LocalDate lookupDate,
        
        @Schema(description = "AI 분석 결과", example = "현재 시장 상황을 고려할 때 kg당 18,000원이 적정 가격으로 분석됩니다.")
        String analysisResult,
        
        @Schema(description = "분석 완료 여부", example = "true")
        Boolean hasAnalysisResult,
        
        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
        
        @Schema(description = "수정일시", example = "2024-01-15T15:20:00")
        LocalDateTime updatedAt
) {
    public static PriceQuoteResponse from(PriceQuoteRequest request) {
        return new PriceQuoteResponse(
                request.getId(),
                request.getUser().getId(),
                request.getProductId(),
                request.getGarakCode(),
                request.getProductName(),
                request.getGrade(),
                request.getHarvestDate(),
                request.getUnit(),
                request.getQuantity(),
                request.getEstimatedPrice(),
                request.getStatus(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getAvgPrice(),
                request.getFairPrice(),
                request.getFinalPrice(),
                request.getYearlyPriceData(),
                request.getChartMinPrice(),
                request.getChartMaxPrice(),
                request.getLookupDate(),
                request.getAnalysisResult(),
                request.hasAnalysisResult(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
} 