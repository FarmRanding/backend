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
        
        @Schema(description = "작물명", example = "토마토")
        String cropName,
        
        @Schema(description = "품종명", example = "방울토마토")
        String variety,
        
        @Schema(description = "등급", example = "특급")
        String grade,
        
        @Schema(description = "수확 예정일", example = "2024-02-15")
        LocalDate harvestDate,
        
        @Schema(description = "예상 가격 (원)", example = "15000")
        BigDecimal estimatedPrice,
        
        @Schema(description = "최종 가격 (원)", example = "18000")
        BigDecimal finalPrice,
        
        @Schema(description = "분석 결과", example = "시장 상황을 고려할 때 kg당 18,000원이 적정 가격으로 분석됩니다.")
        String analysisResult,
        
        @Schema(description = "요청 상태", example = "COMPLETED")
        PriceQuoteStatus status,
        
        @Schema(description = "완료 여부", example = "true")
        Boolean isCompleted,
        
        @Schema(description = "예상 가격 보유 여부", example = "true")
        Boolean hasEstimatedPrice,
        
        @Schema(description = "최종 가격 보유 여부", example = "true")
        Boolean hasFinalPrice,
        
        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
        
        @Schema(description = "수정일시", example = "2024-01-15T15:20:00")
        LocalDateTime updatedAt
) {
    public static PriceQuoteResponse from(PriceQuoteRequest request) {
        return new PriceQuoteResponse(
                request.getId(),
                request.getUser().getId(),
                request.getCropName(),
                request.getVariety(),
                request.getGrade(),
                request.getHarvestDate(),
                request.getEstimatedPrice(),
                request.getFinalPrice(),
                request.getAnalysisResult(),
                request.getStatus(),
                request.isCompleted(),
                request.hasEstimatedPrice(),
                request.hasFinalPrice(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
} 