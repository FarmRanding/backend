package org.fr.farmranding.dto.pricequote;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "가격 조회 요청 DTO")
public record PriceDataRequest(
        
        @NotBlank(message = "가락시장 품목 코드는 필수입니다.")
        @Schema(description = "가락시장 품목 코드", example = "15100", required = true)
        String garakCode,
        
        @NotNull(message = "조회 기준일은 필수입니다.")
        @Schema(description = "조회 기준일 (출하 예정일)", example = "2024-02-15", required = true)
        LocalDate targetDate,
        
        @NotBlank(message = "등급은 필수입니다.")
        @Pattern(regexp = "^[특상중하]$", message = "등급은 특, 상, 중, 하 중 하나여야 합니다.")
        @Schema(description = "등급", example = "특", required = true)
        String grade
) {
    
    /**
     * 등급을 가락시장 API 코드로 변환
     * 특-0, 상-1, 중-2, 하-3
     */
    public String getGradeCode() {
        return switch (grade) {
            case "특" -> "0";
            case "상" -> "1";
            case "중" -> "2";
            case "하" -> "3";
            default -> "2"; // 기본값: 중급
        };
    }
    
    /**
     * 날짜를 가락시장 API 형식(YYYYMMDD)으로 변환
     */
    public String getFormattedDate() {
        return targetDate.toString().replace("-", "");
    }
} 