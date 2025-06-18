package org.fr.farmranding.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "작물 응답 DTO")
public record CropResponse(
    @Schema(description = "작물 코드", example = "0303")
    String cropCode,
    
    @Schema(description = "작물명", example = "배추")
    String cropName
) {
    public static CropResponse of(String cropCode, String cropName) {
        return new CropResponse(cropCode, cropName);
    }
} 