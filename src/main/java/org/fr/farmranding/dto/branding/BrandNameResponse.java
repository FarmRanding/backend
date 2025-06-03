package org.fr.farmranding.dto.branding;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "브랜드명 생성 응답 DTO")
public record BrandNameResponse(
    @Schema(description = "생성된 브랜드명", example = "뽀사과")
    String brandName
) {} 