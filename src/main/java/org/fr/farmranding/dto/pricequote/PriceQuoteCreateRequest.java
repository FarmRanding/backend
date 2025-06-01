package org.fr.farmranding.dto.pricequote;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "가격 견적 생성 요청 DTO")
public record PriceQuoteCreateRequest(
        
        @NotBlank(message = "작물명은 필수입니다.")
        @Size(min = 1, max = 50, message = "작물명은 1자 이상 50자 이하여야 합니다.")
        @Schema(description = "작물명", example = "토마토", required = true)
        String cropName,
        
        @Size(max = 50, message = "품종명은 50자 이하여야 합니다.")
        @Schema(description = "품종명", example = "방울토마토")
        String variety,
        
        @NotBlank(message = "등급은 필수입니다.")
        @Schema(description = "등급", example = "특급", required = true)
        String grade,
        
        @Schema(description = "수확 예정일", example = "2024-02-15")
        LocalDate harvestDate,
        
        @NotBlank(message = "단위는 필수입니다.")
        @Schema(description = "단위", example = "kg", required = true)
        String unit,
        
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        @Schema(description = "수량", example = "1", required = true)
        Integer quantity,
        
        @DecimalMin(value = "0.0", inclusive = false, message = "예상 가격은 0보다 커야 합니다.")
        @Schema(description = "예상 가격", example = "15000")
        BigDecimal estimatedPrice
) {} 