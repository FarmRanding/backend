package org.fr.farmranding.auth;

import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.entity.user.SocialProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {
    
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        SocialProvider provider = SocialProvider.fromString(registrationId);
        
        switch (provider) {
            case KAKAO:
                return new KakaoOAuth2UserInfo(attributes);
            default:
                throw new BusinessException(FarmrandingResponseCode.OAUTH2_USER_INFO_ERROR, 
                        "지원하지 않는 OAuth2 제공자입니다: " + registrationId);
        }
    }
} 