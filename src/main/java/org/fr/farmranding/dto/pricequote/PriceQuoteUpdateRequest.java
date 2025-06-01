package org.fr.farmranding.dto.pricequote;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "가격 제안 요청 수정 DTO")
public record PriceQuoteUpdateRequest(
        
        @Size(min = 1, max = 50, message = "작물명은 1자 이상 50자 이하여야 합니다.")
        @Schema(description = "작물명", example = "토마토")
        String cropName,
        
        @Size(max = 50, message = "품종명은 50자 이하여야 합니다.")
        @Schema(description = "품종명", example = "방울토마토")
        String variety,
        
        @Size(max = 100, message = "재배방법은 100자 이하여야 합니다.")
        @Schema(description = "재배방법", example = "유기농 재배")
        String cultivationMethod,
        
        @Size(max = 100, message = "생산지역은 100자 이하여야 합니다.")
        @Schema(description = "생산지역", example = "경기도 화성시")
        String productionArea,
        
        @Size(max = 50, message = "수확시기는 50자 이하여야 합니다.")
        @Schema(description = "수확시기", example = "6월~8월")
        String harvestSeason,
        
        @Size(max = 20, message = "품질등급은 20자 이하여야 합니다.")
        @Schema(description = "품질등급", example = "특급")
        String qualityGrade,
        
        @Schema(description = "유기농 인증 여부", example = "true")
        Boolean organicCertification,
        
        @Schema(description = "GAP 인증 여부", example = "true")
        Boolean gapCertification,
        
        @Size(max = 200, message = "기타 인증은 200자 이하여야 합니다.")
        @Schema(description = "기타 인증", example = "HACCP, ISO22000")
        String otherCertifications,
        
        @DecimalMin(value = "0.0", inclusive = false, message = "생산량은 0보다 커야 합니다.")
        @Schema(description = "생산량", example = "1000.5")
        BigDecimal productionVolume,
        
        @Size(max = 20, message = "생산단위는 20자 이하여야 합니다.")
        @Schema(description = "생산단위", example = "kg")
        String productionUnit,
        
        @Size(max = 50, message = "포장형태는 50자 이하여야 합니다.")
        @Schema(description = "포장형태", example = "플라스틱 박스")
        String packagingType,
        
        @Size(max = 50, message = "포장크기는 50자 이하여야 합니다.")
        @Schema(description = "포장크기", example = "5kg 단위")
        String packagingSize,
        
        @Size(max = 100, message = "목표시장은 100자 이하여야 합니다.")
        @Schema(description = "목표시장", example = "대형마트, 온라인몰")
        String targetMarket,
        
        @Size(max = 100, message = "유통채널은 100자 이하여야 합니다.")
        @Schema(description = "유통채널", example = "직판, 도매시장")
        String distributionChannel,
        
        @DecimalMin(value = "0.0", inclusive = false, message = "현재 판매가격은 0보다 커야 합니다.")
        @Schema(description = "현재 판매가격", example = "15000")
        BigDecimal currentSellingPrice,
        
        @Size(max = 100, message = "희망가격대는 100자 이하여야 합니다.")
        @Schema(description = "희망가격대", example = "18000~22000원")
        String desiredPriceRange,
        
        @Size(max = 1000, message = "비고는 1000자 이하여야 합니다.")
        @Schema(description = "비고", example = "특별한 요구사항이나 추가 정보")
        String notes
) {} 