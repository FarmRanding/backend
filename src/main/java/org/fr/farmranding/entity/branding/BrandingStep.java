package org.fr.farmranding.entity.branding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 브랜딩 프로젝트 진행 단계
 */
@Getter
@RequiredArgsConstructor
public enum BrandingStep {
    BASIC_INFO(0, "기본정보", "작물 기본 정보 입력"),
    GAP_VERIFICATION(1, "GAP인증", "GAP 인증번호 확인"),
    BRANDING_KEYWORDS(2, "브랜드키워드", "브랜드 이미지 키워드 선택"),
    CROP_APPEAL_KEYWORDS(3, "작물키워드", "작물 매력 키워드 선택"),
    LOGO_IMAGE_KEYWORDS(4, "로고키워드", "로고 이미지 키워드 선택"),
    BRAND_NAME_GENERATION(5, "브랜드명생성", "브랜드명 생성 및 선택"),
    COMPLETE(6, "완료", "브랜딩 작업 완료");
    
    private final int order;
    private final String displayName;
    private final String description;
    
    public static BrandingStep fromOrder(int order) {
        for (BrandingStep step : values()) {
            if (step.order == order) {
                return step;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 단계입니다: " + order);
    }
    
    public BrandingStep getNextStep() {
        if (this == COMPLETE) {
            return COMPLETE;
        }
        return fromOrder(this.order + 1);
    }
    
    public BrandingStep getPreviousStep() {
        if (this == BASIC_INFO) {
            return BASIC_INFO;
        }
        return fromOrder(this.order - 1);
    }
    
    public boolean isCompleted() {
        return this == COMPLETE;
    }
    
    public int getProgressPercentage() {
        return (order * 100) / (COMPLETE.order);
    }
} 