package org.fr.farmranding.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MembershipType {
    FREE("일반 회원", 5, 5, 3, false),
    PREMIUM("프리미엄", Integer.MAX_VALUE, Integer.MAX_VALUE, 10, false),
    PREMIUM_PLUS("프리미엄 플러스", Integer.MAX_VALUE, Integer.MAX_VALUE, 10, true);
    
    private final String displayName;
    private final int aiBrandingLimit;
    private final int pricingSuggestionLimit;
    private final int brandNameRegenerationLimit;
    private final boolean canAccessSalesContent;
    
    public boolean isFreeMembership() {
        return this == FREE;
    }
    
    public boolean isPremiumMembership() {
        return this == PREMIUM;
    }
    
    public boolean isPremiumPlusMembership() {
        return this == PREMIUM_PLUS;
    }
    
    public boolean isPaidMembership() {
        return this == PREMIUM || this == PREMIUM_PLUS;
    }
    
    public boolean hasUnlimitedBranding() {
        return this == PREMIUM || this == PREMIUM_PLUS;
    }
    
    public boolean hasUnlimitedPricing() {
        return this == PREMIUM || this == PREMIUM_PLUS;
    }
    
    public boolean isPremiumOrAbove() {
        return this == PREMIUM || this == PREMIUM_PLUS;
    }
} 