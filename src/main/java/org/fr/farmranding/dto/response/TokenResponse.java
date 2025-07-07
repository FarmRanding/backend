package org.fr.farmranding.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 응답 DTO")
public record TokenResponse(
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,
    
    @Schema(description = "토큰 타입", example = "Bearer")
    String tokenType
) {
    public static TokenResponseBuilder builder() {
        return new TokenResponseBuilder();
    }
    
    public static class TokenResponseBuilder {
        private String accessToken;
        private String tokenType;
        
        public TokenResponseBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        
        public TokenResponseBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }
        
        public TokenResponse build() {
            return new TokenResponse(accessToken, tokenType);
        }
    }
} 