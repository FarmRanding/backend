package org.fr.farmranding.dto.branding;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.branding.BrandingProject;
import org.fr.farmranding.entity.branding.BrandingStatus;
import org.fr.farmranding.entity.branding.BrandingStep;
import org.fr.farmranding.entity.branding.Grade;

import java.time.LocalDateTime;

@Schema(description = "브랜딩 프로젝트 응답 DTO")
public record BrandingProjectResponse(
        
        @Schema(description = "프로젝트 ID", example = "1")
        Long id,
        
        @Schema(description = "프로젝트 제목", example = "김씨농장 유기농 토마토 브랜딩")
        String title,
        
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        
        @Schema(description = "작물명", example = "토마토")
        String cropName,
        
        @Schema(description = "품종명", example = "방울토마토")
        String variety,
        
        @Schema(description = "재배방법", example = "유기농 재배")
        String cultivationMethod,
        
        @Schema(description = "등급", example = "SPECIAL")
        Grade grade,
        
        @Schema(description = "브랜딩 키워드 (JSON)", example = "[\"프리미엄\", \"신선한\", \"유기농\"]")
        String brandingKeywords,
        
        @Schema(description = "작물 매력 키워드 (JSON)", example = "[\"달콤한\", \"과즙이 풍부한\", \"비타민 함유\"]")
        String cropAppealKeywords,
        
        @Schema(description = "로고 이미지 키워드 (JSON)", example = "[\"심플한\", \"모던한\", \"밝은\"]")
        String logoImageKeywords,
        
        @Schema(description = "GAP 인증번호", example = "GAP-2024-001")
        String gapNumber,
        
        @Schema(description = "GAP 인증 여부", example = "true")
        Boolean isGapVerified,
        
        @Schema(description = "생성된 브랜드명", example = "김씨농장 프리미엄 토마토")
        String generatedBrandName,
        
        @Schema(description = "홍보 문구", example = "김씨농장 프리미엄 토마토와 함께하는 건강한 삶")
        String promotionText,
        
        @Schema(description = "브랜드 스토리", example = "3대째 이어온 전통 농법으로 정성스럽게 키운 프리미엄 토마토입니다.")
        String brandStory,
        
        @Schema(description = "브랜드 컨셉", example = "자연 그대로의 맛과 영양을 담은 프리미엄 농산물 브랜드")
        String brandConcept,
        
        @Schema(description = "브랜드 이미지 URL", example = "https://storage.farmranding.com/brands/12345.png")
        String brandImageUrl,
        
        @Schema(description = "프로젝트 상태", example = "IN_PROGRESS")
        BrandingStatus status,
        
        @Schema(description = "현재 진행 단계", example = "BRANDING_KEYWORDS")
        BrandingStep currentStep,
        
        @Schema(description = "진행률 (%)", example = "30")
        Integer progressPercentage,
        
        @Schema(description = "이미지 보유 여부", example = "true")
        Boolean hasImage,
        
        @Schema(description = "완전 완료 여부", example = "false")
        Boolean isFullyCompleted,
        
        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
        
        @Schema(description = "수정일시", example = "2024-01-15T15:20:00")
        LocalDateTime updatedAt
) {
    public static BrandingProjectResponse from(BrandingProject project) {
        return new BrandingProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getUser().getId(),
                project.getCropName(),
                project.getVariety(),
                project.getCultivationMethod(),
                project.getGrade(),
                project.getBrandingKeywords(),
                project.getCropAppealKeywords(),
                project.getLogoImageKeywords(),
                project.getGapNumber(),
                project.getIsGapVerified(),
                project.getGeneratedBrandName(),
                project.getPromotionText(),
                project.getBrandStory(),
                project.getBrandConcept(),
                project.getBrandImageUrl(),
                project.getStatus(),
                project.getCurrentStep(),
                project.getProgressPercentage(),
                project.hasImage(),
                project.isFullyCompleted(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
} 