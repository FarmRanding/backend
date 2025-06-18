package org.fr.farmranding.dto.gap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * GAP 인증 검색 요청 DTO
 */
@Schema(description = "GAP 인증 정보 검색 요청")
public record GapSearchRequest(
        @NotBlank(message = "GAP 인증번호는 필수입니다.")
        @Size(min = 7, max = 15, message = "GAP 인증번호는 7~15자리여야 합니다.")
        @Pattern(regexp = "^[0-9]+$", message = "GAP 인증번호는 숫자만 입력 가능합니다.")
        @Schema(description = "GAP 인증번호", example = "1001267", required = true)
        String certificationNumber,
        
        @Schema(description = "품목명 (선택사항)", example = "토마토")
        String productName
) {} 