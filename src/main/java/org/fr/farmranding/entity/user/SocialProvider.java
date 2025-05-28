package org.fr.farmranding.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {
    KAKAO("kakao", "카카오"),
    GOOGLE("google", "구글");
    
    private final String providerName;
    private final String displayName;
    
    public static SocialProvider fromString(String provider) {
        for (SocialProvider socialProvider : values()) {
            if (socialProvider.providerName.equalsIgnoreCase(provider)) {
                return socialProvider;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + provider);
    }
} 