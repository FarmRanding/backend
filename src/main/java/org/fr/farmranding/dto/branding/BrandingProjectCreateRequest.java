package org.fr.farmranding.dto.branding;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.fr.farmranding.entity.branding.Grade;

import java.util.List;

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
        Grade grade,
        
        @Schema(description = "GAP 인증 여부", example = "true")
        Boolean hasGapCertification,
        
        @NotEmpty(message = "브랜딩 키워드는 최소 1개 이상 선택해야 합니다.")
        @Size(max = 10, message = "브랜딩 키워드는 최대 10개까지 선택할 수 있습니다.")
        @Schema(description = "브랜딩 키워드 목록", example = "[\"프리미엄\", \"건강한\", \"신선한\"]", required = true)
        List<String> brandingKeywords,
        
        @NotEmpty(message = "작물 매력 키워드는 최소 1개 이상 선택해야 합니다.")
        @Size(max = 10, message = "작물 매력 키워드는 최대 10개까지 선택할 수 있습니다.")
        @Schema(description = "작물 매력 키워드 목록", example = "[\"달콤한\", \"육즙이 풍부한\", \"영양가 높은\"]", required = true)
        List<String> cropAppealKeywords,
        
        @NotEmpty(message = "로고 이미지 키워드는 최소 1개 이상 선택해야 합니다.")
        @Size(max = 10, message = "로고 이미지 키워드는 최대 10개까지 선택할 수 있습니다.")
        @Schema(description = "로고 이미지 키워드 목록", example = "[\"자연스러운\", \"모던한\", \"클래식한\"]", required = true)
        List<String> logoImageKeywords
) {} 