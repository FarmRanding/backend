package org.fr.farmranding.dto.branding;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.fr.farmranding.entity.branding.BrandingStatus;
import org.fr.farmranding.entity.branding.BrandingStep;
import org.fr.farmranding.entity.branding.Grade;

import java.util.List;

@Schema(description = "브랜딩 프로젝트 수정 요청 DTO")
public record BrandingProjectUpdateRequest(
        
        @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다.")
        @Schema(description = "프로젝트 제목", example = "김씨농장 유기농 토마토 브랜딩")
        String title,
        
        @Size(min = 1, max = 50, message = "작물명은 1자 이상 50자 이하여야 합니다.")
        @Schema(description = "작물명", example = "토마토")
        String cropName,
        
        @Size(max = 50, message = "품종명은 50자 이하여야 합니다.")
        @Schema(description = "품종명", example = "방울토마토")
        String variety,
        
        @Size(max = 100, message = "재배방법은 100자 이하여야 합니다.")
        @Schema(description = "재배방법", example = "유기농 재배")
        String cultivationMethod,
        
        @Schema(description = "등급", example = "SPECIAL")
        Grade grade,
        
        @Schema(description = "브랜딩 키워드", example = "[\"프리미엄\", \"신선한\", \"유기농\"]")
        List<String> brandingKeywords,
        
        @Schema(description = "작물 매력 키워드", example = "[\"달콤한\", \"과즙이 풍부한\", \"비타민 함유\"]")
        List<String> cropAppealKeywords,
        
        @Schema(description = "로고 이미지 키워드", example = "[\"심플한\", \"모던한\", \"밝은\"]")
        List<String> logoImageKeywords,
        
        @Size(max = 50, message = "GAP 인증번호는 50자 이하여야 합니다.")
        @Schema(description = "GAP 인증번호", example = "GAP-2024-001")
        String gapNumber,
        
        @Schema(description = "GAP 인증 여부", example = "true")
        Boolean isGapVerified,
        
        @Size(max = 100, message = "생성된 브랜드명은 100자 이하여야 합니다.")
        @Schema(description = "생성된 브랜드명", example = "김씨농장 프리미엄 토마토")
        String generatedBrandName,
        
        @Size(max = 500, message = "홍보 문구는 500자 이하여야 합니다.")
        @Schema(description = "홍보 문구", example = "김씨농장 프리미엄 토마토와 함께하는 건강한 삶")
        String promotionText,
        
        @Size(max = 1000, message = "브랜드 스토리는 1000자 이하여야 합니다.")
        @Schema(description = "브랜드 스토리", example = "3대째 이어온 전통 농법으로 정성스럽게 키운 프리미엄 토마토입니다.")
        String brandStory,
        
        @Size(max = 500, message = "브랜드 컨셉은 500자 이하여야 합니다.")
        @Schema(description = "브랜드 컨셉", example = "자연 그대로의 맛과 영양을 담은 프리미엄 농산물 브랜드")
        String brandConcept,
        
        @Schema(description = "브랜드 이미지 URL", example = "https://storage.farmranding.com/brands/12345.png")
        String brandImageUrl,
        
        @Schema(description = "프로젝트 상태", example = "IN_PROGRESS")
        BrandingStatus status,
        
        @Schema(description = "현재 진행 단계", example = "BRANDING_KEYWORDS")
        BrandingStep currentStep
) {} 