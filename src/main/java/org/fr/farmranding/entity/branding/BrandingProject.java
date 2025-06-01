package org.fr.farmranding.entity.branding;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.fr.farmranding.common.entity.BaseEntity;
import org.fr.farmranding.entity.user.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "branding_projects")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BrandingProject extends BaseEntity {
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // 작물 기본 정보
    @Column(name = "crop_name", nullable = false)
    private String cropName;
    
    @Column(name = "variety")
    private String variety;
    
    @Column(name = "cultivation_method")
    private String cultivationMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "grade")
    private Grade grade;
    
    // 키워드 정보 (JSON으로 저장)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "branding_keywords", columnDefinition = "JSON")
    private String brandingKeywords;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "crop_appeal_keywords", columnDefinition = "JSON")
    private String cropAppealKeywords;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "logo_image_keywords", columnDefinition = "JSON")
    private String logoImageKeywords;
    
    // GAP 인증 정보
    @Column(name = "gap_number")
    private String gapNumber;
    
    @Column(name = "is_gap_verified")
    private Boolean isGapVerified;
    
    // 생성된 브랜드 정보
    @Column(name = "generated_brand_name")
    private String generatedBrandName;
    
    @Column(name = "promotion_text", columnDefinition = "TEXT")
    private String promotionText;
    
    @Column(name = "brand_story", columnDefinition = "TEXT")
    private String brandStory;
    
    @Column(name = "brand_concept", columnDefinition = "TEXT")
    private String brandConcept;
    
    // 생성된 브랜드 이미지
    @Column(name = "brand_image_url")
    private String brandImageUrl;
    
    // 프로젝트 상태 및 진행 정보
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BrandingStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false)
    private BrandingStep currentStep;
    
    // 비즈니스 메서드
    public void updateBasicInfo(String title, String cropName, String variety, 
                               String cultivationMethod, Grade grade) {
        this.title = title;
        this.cropName = cropName;
        this.variety = variety;
        this.cultivationMethod = cultivationMethod;
        this.grade = grade;
        this.currentStep = BrandingStep.GAP_VERIFICATION;
    }
    
    public void updateGapInfo(String gapNumber, Boolean isGapVerified) {
        this.gapNumber = gapNumber;
        this.isGapVerified = isGapVerified;
        this.currentStep = BrandingStep.BRANDING_KEYWORDS;
    }
    
    public void updateBrandingKeywords(String brandingKeywords) {
        this.brandingKeywords = brandingKeywords;
        this.currentStep = BrandingStep.CROP_APPEAL_KEYWORDS;
    }
    
    public void updateCropAppealKeywords(String cropAppealKeywords) {
        this.cropAppealKeywords = cropAppealKeywords;
        this.currentStep = BrandingStep.LOGO_IMAGE_KEYWORDS;
    }
    
    public void updateLogoImageKeywords(String logoImageKeywords) {
        this.logoImageKeywords = logoImageKeywords;
        this.currentStep = BrandingStep.BRAND_NAME_GENERATION;
    }
    
    public void completeBranding(String generatedBrandName, String promotionText, 
                               String brandStory, String brandConcept, String brandImageUrl) {
        this.generatedBrandName = generatedBrandName;
        this.promotionText = promotionText;
        this.brandStory = brandStory;
        this.brandConcept = brandConcept;
        this.brandImageUrl = brandImageUrl;
        this.currentStep = BrandingStep.COMPLETE;
        this.status = BrandingStatus.COMPLETED;
    }
    
    public void updateBrandTexts(String promotionText, String brandStory, String brandConcept) {
        this.promotionText = promotionText;
        this.brandStory = brandStory;
        this.brandConcept = brandConcept;
    }
    
    public void updateBrandImage(String brandImageUrl) {
        this.brandImageUrl = brandImageUrl;
    }
    
    public void updateStatus(BrandingStatus status) {
        this.status = status;
    }
    
    public void moveToNextStep() {
        this.currentStep = this.currentStep.getNextStep();
        
        if (this.currentStep.isCompleted()) {
            this.status = BrandingStatus.COMPLETED;
        } else if (this.status == BrandingStatus.DRAFT) {
            this.status = BrandingStatus.IN_PROGRESS;
        }
    }
    
    public boolean canEdit() {
        return this.status.canEdit();
    }
    
    public int getProgressPercentage() {
        return this.currentStep.getProgressPercentage();
    }
    
    public boolean hasImage() {
        return brandImageUrl != null;
    }
    
    public boolean isFullyCompleted() {
        return status == BrandingStatus.COMPLETED && 
               generatedBrandName != null && 
               promotionText != null && 
               brandStory != null;
    }
} 