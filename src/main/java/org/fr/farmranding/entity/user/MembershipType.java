package org.fr.farmranding.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MembershipType {
    FREE("무료", 3, 5),
    PRO("프로", Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    private final String displayName;
    private final int aiBrandingLimit;
    private final int pricingSuggestionLimit;
    
    public boolean isFreeMembership() {
        return this == FREE;
    }
    
    public boolean isProMembership() {
        return this == PRO;
    }
} 