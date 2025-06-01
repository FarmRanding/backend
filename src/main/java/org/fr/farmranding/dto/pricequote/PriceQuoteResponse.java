package org.fr.farmranding.dto.pricequote;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.pricequote.PriceQuoteRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "가격 제안 요청 응답 DTO")
public record PriceQuoteResponse(
        
        @Schema(description = "요청 ID", example = "1")
        Long id,
        
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        
        @Schema(description = "작물명", example = "토마토")
        String cropName,
        
        @Schema(description = "품종명", example = "방울토마토")
        String variety,
        
        @Schema(description = "재배방법", example = "유기농 재배")
        String cultivationMethod,
        
        @Schema(description = "생산지역", example = "경기도 화성시")
        String productionArea,
        
        @Schema(description = "수확시기", example = "6월~8월")
        String harvestSeason,
        
        @Schema(description = "품질등급", example = "특급")
        String qualityGrade,
        
        @Schema(description = "유기농 인증 여부", example = "true")
        Boolean organicCertification,
        
        @Schema(description = "GAP 인증 여부", example = "true")
        Boolean gapCertification,
        
        @Schema(description = "기타 인증", example = "HACCP, ISO22000")
        String otherCertifications,
        
        @Schema(description = "생산량", example = "1000.5")
        BigDecimal productionVolume,
        
        @Schema(description = "생산단위", example = "kg")
        String productionUnit,
        
        @Schema(description = "포장형태", example = "플라스틱 박스")
        String packagingType,
        
        @Schema(description = "포장크기", example = "5kg 단위")
        String packagingSize,
        
        @Schema(description = "목표시장", example = "대형마트, 온라인몰")
        String targetMarket,
        
        @Schema(description = "유통채널", example = "직판, 도매시장")
        String distributionChannel,
        
        @Schema(description = "현재 판매가격", example = "15000")
        BigDecimal currentSellingPrice,
        
        @Schema(description = "희망가격대", example = "18000~22000원")
        String desiredPriceRange,
        
        @Schema(description = "시장 분석 결과 (JSON)")
        String marketAnalysis,
        
        @Schema(description = "가격 제안 결과 (JSON)")
        String priceSuggestion,
        
        @Schema(description = "경쟁사 분석 결과 (JSON)")
        String competitiveAnalysis,
        
        @Schema(description = "요청 상태", example = "COMPLETED")
        PriceQuoteStatus status,
        
        @Schema(description = "비고", example = "특별한 요구사항이나 추가 정보")
        String notes,
        
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
                request.getCultivationMethod(),
                request.getProductionArea(),
                request.getHarvestSeason(),
                request.getQualityGrade(),
                request.getOrganicCertification(),
                request.getGapCertification(),
                request.getOtherCertifications(),
                request.getProductionVolume(),
                request.getProductionUnit(),
                request.getPackagingType(),
                request.getPackagingSize(),
                request.getTargetMarket(),
                request.getDistributionChannel(),
                request.getCurrentSellingPrice(),
                request.getDesiredPriceRange(),
                request.getMarketAnalysis(),
                request.getPriceSuggestion(),
                request.getCompetitiveAnalysis(),
                request.getStatus(),
                request.getNotes(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
} 