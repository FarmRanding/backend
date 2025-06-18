package org.fr.farmranding.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "표준코드 응답 DTO")
public record StandardCodeResponse(
    @Schema(description = "표준코드 (소분류 코드)", example = "040101")
    String code,
    
    @Schema(description = "표준코드명 (소분류명)", example = "토마토")
    String name
) {
    public static StandardCodeResponse of(String code, String name) {
        return new StandardCodeResponse(code, name);
    }
} 