package org.fr.farmranding.dto.branding;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.branding.BrandingProject;
import org.fr.farmranding.entity.branding.Grade;
import org.fr.farmranding.entity.branding.ImageGenerationStatus;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "브랜딩 응답 DTO")
public record BrandingProjectResponse(
        
        @Schema(description = "브랜딩 ID", example = "1")
        Long id,
        
        @Schema(description = "브랜딩 제목", example = "김씨농장 유기농 토마토 브랜딩")
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
        
        @Schema(description = "브랜딩 키워드", example = "[\"프리미엄\", \"신선한\", \"유기농\"]")
        List<String> brandingKeywords,
        
        @Schema(description = "작물 매력 키워드", example = "[\"달콤한\", \"과즙이 풍부한\", \"비타민 함유\"]")
        List<String> cropAppealKeywords,
        
        @Schema(description = "로고 이미지 키워드", example = "[\"심플한\", \"모던한\", \"밝은\"]")
        List<String> logoImageKeywords,
        
        @Schema(description = "GAP 인증번호", example = "GAP-2024-001")
        String gapNumber,
        
        @Schema(description = "GAP 인증 여부", example = "true")
        Boolean isGapVerified,
        
        @Schema(description = "생성된 브랜드명", example = "뽀사과")
        String generatedBrandName,
        
        @Schema(description = "홍보 문구", example = "김씨농장 프리미엄 토마토와 함께하는 건강한 삶")
        String brandConcept,
        
        @Schema(description = "브랜드 스토리", example = "3대째 이어온 전통 농법으로 정성스럽게 키운 프리미엄 토마토입니다.")
        String brandStory,
        
        @Schema(description = "브랜드 이미지 URL", example = "https://storage.farmranding.com/brands/12345.png")
        String brandImageUrl,
        
        @Schema(description = "이미지 생성 상태", example = "COMPLETED", 
               allowableValues = {"PENDING", "PROCESSING", "COMPLETED", "FAILED"})
        ImageGenerationStatus imageGenerationStatus,
        
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
                project.getBrandConcept(),
                project.getBrandStory(),
                project.getBrandImageUrl(),
                project.getImageGenerationStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
} 