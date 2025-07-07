package org.fr.farmranding.entity.membership;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.fr.farmranding.common.entity.BaseEntity;
import org.fr.farmranding.entity.user.MembershipType;

import java.math.BigDecimal;

@Entity
@Table(name = "membership_plans")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlan extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", nullable = false, unique = true)
    private MembershipType membershipType;
    
    @Column(name = "plan_name", nullable = false)
    private String planName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "monthly_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal monthlyPrice;
    
    @Column(name = "yearly_price", precision = 10, scale = 2)
    private BigDecimal yearlyPrice;
    
    // 기능 제한
    @Column(name = "ai_branding_limit", nullable = false)
    private Integer aiBrandingLimit;
    
    @Column(name = "pricing_suggestion_limit", nullable = false)
    private Integer pricingSuggestionLimit;
    
    @Column(name = "project_storage_limit", nullable = false)
    private Integer projectStorageLimit;
    
    // 기능 지원 여부
    @Column(name = "advanced_analytics", nullable = false)
    private Boolean advancedAnalytics;
    
    @Column(name = "priority_support", nullable = false)
    private Boolean prioritySupport;
    
    @Column(name = "custom_branding", nullable = false)
    private Boolean customBranding;
    
    @Column(name = "api_access", nullable = false)
    private Boolean apiAccess;
    
    @Column(name = "export_features", nullable = false)
    private Boolean exportFeatures;
    
    // 플랜 상태
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "is_popular", nullable = false)
    private Boolean isPopular;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    
    // 비즈니스 메서드
    public void updatePlanInfo(String planName, String description, BigDecimal monthlyPrice, BigDecimal yearlyPrice) {
        this.planName = planName;
        this.description = description;
        this.monthlyPrice = monthlyPrice;
        this.yearlyPrice = yearlyPrice;
    }
    
    public void updateLimits(Integer aiBrandingLimit, Integer pricingSuggestionLimit, Integer projectStorageLimit) {
        this.aiBrandingLimit = aiBrandingLimit;
        this.pricingSuggestionLimit = pricingSuggestionLimit;
        this.projectStorageLimit = projectStorageLimit;
    }
    
    public void updateFeatures(Boolean advancedAnalytics, Boolean prioritySupport, Boolean customBranding,
                              Boolean apiAccess, Boolean exportFeatures) {
        this.advancedAnalytics = advancedAnalytics;
        this.prioritySupport = prioritySupport;
        this.customBranding = customBranding;
        this.apiAccess = apiAccess;
        this.exportFeatures = exportFeatures;
    }
    
    public void updateStatus(Boolean isActive, Boolean isPopular, Integer sortOrder) {
        this.isActive = isActive;
        this.isPopular = isPopular;
        this.sortOrder = sortOrder;
    }
    
    public boolean isFree() {
        return this.membershipType == MembershipType.FREE;
    }
    
    public boolean isPremium() {
        return this.membershipType == MembershipType.PREMIUM;
    }
    
    public boolean isPremiumPlus() {
        return this.membershipType == MembershipType.PREMIUM_PLUS;
    }
    
    public BigDecimal getDiscountedYearlyPrice() {
        if (yearlyPrice != null && monthlyPrice != null) {
            BigDecimal yearlyFromMonthly = monthlyPrice.multiply(BigDecimal.valueOf(12));
            return yearlyFromMonthly.subtract(yearlyPrice);
        }
        return BigDecimal.ZERO;
    }
    
    public int getYearlyDiscountPercentage() {
        if (yearlyPrice != null && monthlyPrice != null) {
            BigDecimal yearlyFromMonthly = monthlyPrice.multiply(BigDecimal.valueOf(12));
            BigDecimal discount = yearlyFromMonthly.subtract(yearlyPrice);
            return discount.multiply(BigDecimal.valueOf(100))
                    .divide(yearlyFromMonthly, 0, java.math.RoundingMode.HALF_UP)
                    .intValue();
        }
        return 0;
    }
} 