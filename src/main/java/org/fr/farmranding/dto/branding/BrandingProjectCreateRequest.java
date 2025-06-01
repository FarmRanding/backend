package org.fr.farmranding.dto.branding;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.fr.farmranding.entity.branding.Grade;

@Schema(description = "브랜딩 프로젝트 생성 요청 DTO")
public record BrandingProjectCreateRequest(
        
        @NotBlank(message = "프로젝트 제목은 필수입니다.")
        @Size(min = 2, max = 100, message = "프로젝트 제목은 2자 이상 100자 이하여야 합니다.")
        @Schema(description = "프로젝트 제목", example = "김씨농장 유기농 토마토 브랜딩", required = true)
        String title,
        
        @NotBlank(message = "작물명은 필수입니다.")
        @Size(min = 1, max = 50, message = "작물명은 1자 이상 50자 이하여야 합니다.")
        @Schema(description = "작물명", example = "토마토", required = true)
        String cropName,
        
        @Size(max = 50, message = "품종명은 50자 이하여야 합니다.")
        @Schema(description = "품종명", example = "방울토마토")
        String variety,
        
        @Size(max = 100, message = "재배방법은 100자 이하여야 합니다.")
        @Schema(description = "재배방법", example = "유기농 재배")
        String cultivationMethod,
        
        @Schema(description = "등급", example = "SPECIAL")
        Grade grade
) {} 