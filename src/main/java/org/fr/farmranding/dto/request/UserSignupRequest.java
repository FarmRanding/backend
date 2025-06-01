package org.fr.farmranding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "신규 유저 정보 저장 요청 DTO")
public record UserSignupRequest(
    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 30, message = "이름은 2자 이상 30자 이하여야 합니다.")
    @Schema(description = "사용자 이름", example = "김농부", required = true)
    String name,
    
    @NotBlank(message = "농장명은 필수입니다.")
    @Size(min = 2, max = 50, message = "농장명은 2자 이상 50자 이하여야 합니다.")
    @Schema(description = "농장명", example = "행복한 토마토 농장", required = true)
    String farmName,
    
    @NotBlank(message = "위치는 필수입니다.")
    @Size(min = 2, max = 100, message = "위치는 2자 이상 100자 이하여야 합니다.")
    @Schema(description = "농장 위치", example = "경기도 이천시", required = true)
    String location
) {} 