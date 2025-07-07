package org.fr.farmranding.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 응답 DTO")
public record AuthResponse(
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,
    
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String refreshToken,
    
    @Schema(description = "사용자 정보")
    UserResponse user,
    
    @Schema(description = "신규 사용자 여부", example = "false")
    boolean isNewUser
) {} 