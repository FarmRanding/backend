package org.fr.farmranding.entity.branding;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.fr.farmranding.common.entity.BaseEntity;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.common.converter.JsonConverter;

import java.util.List;

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
    
    // 키워드 정보 (TEXT로 저장, JSON 변환)
    @Convert(converter = JsonConverter.class)
    @Column(name = "branding_keywords", columnDefinition = "TEXT")
    private List<String> brandingKeywords;
    
    @Convert(converter = JsonConverter.class)
    @Column(name = "crop_appeal_keywords", columnDefinition = "TEXT")
    private List<String> cropAppealKeywords;
    
    @Convert(converter = JsonConverter.class)
    @Column(name = "logo_image_keywords", columnDefinition = "TEXT")
    private List<String> logoImageKeywords;
    
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
    
    // 비즈니스 메서드
    public void updateBasicInfo(String title, String cropName, String variety, 
                               String cultivationMethod, Grade grade) {
        this.title = title;
        this.cropName = cropName;
        this.variety = variety;
        this.cultivationMethod = cultivationMethod;
        this.grade = grade;
    }
    
    public void updateGapInfo(String gapNumber, Boolean isGapVerified) {
        this.gapNumber = gapNumber;
        this.isGapVerified = isGapVerified;
    }
    
    public void updateBrandingKeywords(List<String> brandingKeywords) {
        this.brandingKeywords = brandingKeywords;
    }
    
    public void updateCropAppealKeywords(List<String> cropAppealKeywords) {
        this.cropAppealKeywords = cropAppealKeywords;
    }
    
    public void updateLogoImageKeywords(List<String> logoImageKeywords) {
        this.logoImageKeywords = logoImageKeywords;
    }
    
    public void completeBranding(String generatedBrandName, String promotionText, 
                               String brandStory, String brandConcept, String brandImageUrl) {
        this.generatedBrandName = generatedBrandName;
        this.promotionText = promotionText;
        this.brandStory = brandStory;
        this.brandConcept = brandConcept;
        this.brandImageUrl = brandImageUrl;
    }
    
    /**
     * 새로운 플로우: 모든 정보를 한 번에 설정하고 GPT 처리 후 완료
     */
    public void createCompleteProject(String title, String cropName, String variety, 
                                    String cultivationMethod, Grade grade, Boolean hasGapCertification,
                                    List<String> brandingKeywords, List<String> cropAppealKeywords, 
                                    List<String> logoImageKeywords,
                                    String generatedBrandName, String promotionText, 
                                    String brandStory, String brandConcept, String brandImageUrl) {
        // 기본 정보 설정
        this.title = title;
        this.cropName = cropName;
        this.variety = variety;
        this.cultivationMethod = cultivationMethod;
        this.grade = grade;
        
        // GAP 인증 정보 설정
        this.isGapVerified = hasGapCertification;
        
        // 키워드 정보 설정
        this.brandingKeywords = brandingKeywords;
        this.cropAppealKeywords = cropAppealKeywords;
        this.logoImageKeywords = logoImageKeywords;
        
        // GPT 생성 결과 설정
        this.generatedBrandName = generatedBrandName;
        this.promotionText = promotionText;
        this.brandStory = brandStory;
        this.brandConcept = brandConcept;
        this.brandImageUrl = brandImageUrl;

    }
} 