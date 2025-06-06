package org.fr.farmranding.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MembershipType {
    FREE("무료", 3, 5, 3),
    PRO("프로", Integer.MAX_VALUE, Integer.MAX_VALUE, 10);
    
    private final String displayName;
    private final int aiBrandingLimit;
    private final int pricingSuggestionLimit;
    private final int brandNameRegenerationLimit;
    
    public boolean isFreeMembership() {
        return this == FREE;
    }
    
    public boolean isProMembership() {
        return this == PRO;
    }
    
    public boolean isPro() {
        return this == PRO;
    }
} 