package org.fr.farmranding.auth;

import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.common.exception.BusinessException;

import java.util.Map;

public class OAuth2UserInfoFactory {
    
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return new KakaoOAuth2UserInfo(attributes);
    }
} 