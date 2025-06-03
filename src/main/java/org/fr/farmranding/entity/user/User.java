package org.fr.farmranding.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.fr.farmranding.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Column(name = "name")
    private String name;

    @Column(name = "provider_id", nullable = false)
    private String providerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", nullable = false)
    @Builder.Default
    private MembershipType membershipType = MembershipType.FREE;
    
    @Column(name = "ai_branding_usage_count", nullable = false)
    @Builder.Default
    private Integer aiBrandingUsageCount = 0;
    
    @Column(name = "pricing_suggestion_usage_count", nullable = false)
    @Builder.Default
    private Integer pricingSuggestionUsageCount = 0;
    
    @Column(name = "farm_name")
    private String farmName;
    
    @Column(name = "location")
    private String location;
    
    public void updateProfile(String name, String farmName, String location) {
        this.name = name;
        this.farmName = farmName;
        this.location = location;
    }

    
    public void upgradeToProMembership() {
        this.membershipType = MembershipType.PRO;
    }
    
    public void incrementAiBrandingUsage() {
        this.aiBrandingUsageCount++;
    }
    
    public void incrementPricingSuggestionUsage() {
        this.pricingSuggestionUsageCount++;
    }
    
    public boolean canUseAiBranding() {
        if (membershipType == MembershipType.PRO) {
            return true;
        }
        return aiBrandingUsageCount < MembershipType.FREE.getAiBrandingLimit();
    }
    
    public boolean canUsePricingSuggestion() {
        if (membershipType == MembershipType.PRO) {
            return true;
        }
        return pricingSuggestionUsageCount < MembershipType.FREE.getPricingSuggestionLimit();
    }
    
    public void resetUsageCounts() {
        this.aiBrandingUsageCount = 0;
        this.pricingSuggestionUsageCount = 0;
    }
} 