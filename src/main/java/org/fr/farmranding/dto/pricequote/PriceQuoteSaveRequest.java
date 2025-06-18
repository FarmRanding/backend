package org.fr.farmranding.dto.pricequote;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "가격 제안 결과 저장 DTO")
public record PriceQuoteSaveRequest(
        
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
        
        @Schema(description = "수량", example = "1")
        Integer quantity,
        
        @NotNull(message = "최종 가격은 필수입니다.")
        @Schema(description = "최종 추천 가격 (원)", example = "45531", required = true)
        BigDecimal finalPrice,
        
        @Schema(description = "최저가 (원)", example = "31113")
        BigDecimal minPrice,
        
        @Schema(description = "최고가 (원)", example = "64863")
        BigDecimal maxPrice,
        
        @Schema(description = "평균가 (원)", example = "45531")
        BigDecimal avgPrice,
        
        @NotBlank(message = "5년간 가격 데이터는 필수입니다.")
        @Schema(description = "5년간 가격 추이 데이터 (JSON)", example = "[{\"year\":\"2020\",\"price\":64863},{\"year\":\"2021\",\"price\":51798}]", required = true)
        String yearlyPriceData,
        
        @NotNull(message = "조회 기준일은 필수입니다.")
        @Schema(description = "조회 기준일", example = "2024-06-09", required = true)
        LocalDate lookupDate
) {} 