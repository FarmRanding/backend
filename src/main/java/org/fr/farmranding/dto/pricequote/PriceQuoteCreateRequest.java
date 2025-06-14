package org.fr.farmranding.dto.pricequote;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "가격 견적 요청 생성 DTO")
public record PriceQuoteCreateRequest(
        
        @Schema(description = "품목 ID", example = "1")
        Long productId,
        
        @NotBlank(message = "가락시장 품목 코드는 필수입니다.")
        @Schema(description = "가락시장 품목 코드", example = "15100", required = true)
        String garakCode,
        
        @NotBlank(message = "품목명은 필수입니다.")
        @Schema(description = "품목명", example = "고구마", required = true)
        String productName,
        
        @NotBlank(message = "등급은 필수입니다.")
        @Schema(description = "등급", example = "특", required = true)
        String grade,
        
        @NotNull(message = "수확일은 필수입니다.")
        @Schema(description = "수확일", example = "2024-02-15", required = true)
        LocalDate harvestDate,
        
        @Schema(description = "단위", example = "10kg")
        String unit,
        
        @Positive(message = "수량은 양수여야 합니다.")
        @Schema(description = "수량", example = "1")
        Integer quantity,
        
        @Schema(description = "예상 가격 (원)", example = "15000")
        BigDecimal estimatedPrice
) {} 