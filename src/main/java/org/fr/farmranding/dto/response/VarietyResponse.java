package org.fr.farmranding.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.StandardCode;

@Schema(description = "품종 응답 DTO")
public record VarietyResponse(
    @Schema(description = "품종 코드", example = "030301")
    String varietyCode,
    
    @Schema(description = "품종명", example = "봄배추")
    String varietyName,
    
    @Schema(description = "작물 코드", example = "0303")
    String cropCode,
    
    @Schema(description = "작물명", example = "배추")
    String cropName
) {
    public static VarietyResponse from(StandardCode standardCode) {
        return new VarietyResponse(
            standardCode.getSclassCode(),
            standardCode.getSclassName(),
            standardCode.getMclassCode(),
            standardCode.getMclassName()
        );
    }
} 