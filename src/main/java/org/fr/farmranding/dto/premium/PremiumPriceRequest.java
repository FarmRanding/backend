package org.fr.farmranding.dto.premium;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 프리미엄 가격 제안 요청 DTO
 */
@Schema(description = "프리미엄 가격 제안 요청")
public record PremiumPriceRequest(
        
        @Schema(description = "품목 그룹 코드 (사용하지 않음)", example = "", required = false)
        String productGroupCode,
        
        @NotBlank(message = "품목 코드는 필수입니다.")
        @Size(max = 10, message = "품목 코드는 10자 이하여야 합니다.")
        @Schema(description = "품목 코드", example = "241", required = true)
        String productItemCode,
        
        @Size(max = 10, message = "품종 코드는 10자 이하여야 합니다.")
        @Schema(description = "품종 코드", example = "00", required = false)
        String productVarietyCode,
        
        @Size(max = 50, message = "거래 위치는 50자 이하여야 합니다.")
        @Schema(description = "거래 위치 (지역명)", example = "서울", required = false)
        String location,
        
        @NotBlank(message = "출하 예정일는 필수입니다.")
        @Schema(description = "출하 예정일 (YYYY-MM-DD)", example = "2025-01-15", required = true)
        String date
) {} 