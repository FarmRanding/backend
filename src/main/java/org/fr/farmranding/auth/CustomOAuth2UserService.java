package org.fr.farmranding.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception e) {
            log.error("OAuth2 사용자 처리 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(FarmrandingResponseCode.OAUTH2_AUTHENTICATION_FAILED);
        }
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        
        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            throw new BusinessException(FarmrandingResponseCode.OAUTH2_USER_INFO_ERROR, 
                    "OAuth2 제공자로부터 이메일 정보를 가져올 수 없습니다.");
        }
        
        User user = userRepository.findByProviderId(userInfo.getId())
                .orElseGet(() -> createUser(userInfo));
        
        return new CustomOAuth2User(
                oAuth2User,
                user.getProviderId(),
                user.getName(),
                user.getEmail()
        );
    }
    
    private User createUser(OAuth2UserInfo userInfo) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(userInfo.getEmail())) {
            throw new BusinessException(FarmrandingResponseCode.USER_ALREADY_EXISTS, 
                    "이미 다른 소셜 계정으로 가입된 이메일입니다.");
        }
        
        User user = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .providerId(userInfo.getId())
                .build();
        
        return userRepository.save(user);
    }
} 