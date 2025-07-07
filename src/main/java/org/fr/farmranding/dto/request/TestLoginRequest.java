package org.fr.farmranding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "테스트 로그인 요청 DTO")
public record TestLoginRequest(
    @NotBlank(message = "암호는 필수입니다.")
    @Schema(description = "테스트 계정 암호", example = "gongmoTest", required = true)
    String password
) {} 