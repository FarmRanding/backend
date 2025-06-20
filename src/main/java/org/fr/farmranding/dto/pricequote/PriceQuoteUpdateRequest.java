package org.fr.farmranding.dto.pricequote;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "가격 견적 수정 요청 DTO")
public record PriceQuoteUpdateRequest(
        
        @Size(min = 1, max = 50, message = "작물명은 1자 이상 50자 이하여야 합니다.")
        @Schema(description = "작물명", example = "토마토")
        String cropName,
        
        @Size(max = 50, message = "품종명은 50자 이하여야 합니다.")
        @Schema(description = "품종명", example = "방울토마토")
        String variety,
        
        @Schema(description = "등급", example = "특급")
        String grade,
        
        @Schema(description = "수확 예정일", example = "2024-02-15")
        LocalDate harvestDate,
        
        @DecimalMin(value = "0.0", inclusive = false, message = "예상 가격은 0보다 커야 합니다.")
        @Schema(description = "예상 가격 (원)", example = "15000")
        BigDecimal estimatedPrice
) {} 