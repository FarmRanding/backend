package org.fr.farmranding.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.jwt.JwtService;
import org.fr.farmranding.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private static final String FRONTEND_URL = "http://localhost:5174";
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException {
        
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        
        // 사용자 정보 조회
        User user = userRepository.findByProviderId(
                oAuth2User.getProviderId()
        ).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 신규 유저 여부 확인 (농장 정보가 없으면 신규 유저로 간주)
        boolean isNewUser = user.getFarmName() == null || user.getFarmName().trim().isEmpty();
        
        // JWT 토큰 생성
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        // URL 파라미터로 토큰과 신규 유저 정보 전달하여 프론트엔드로 리다이렉트
        String redirectUrl = String.format(
                "%s/auth/callback?accessToken=%s&refreshToken=%s&userId=%d&email=%s&name=%s&membershipType=%s&isNewUser=%s",
                FRONTEND_URL,
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
                URLEncoder.encode(refreshToken, StandardCharsets.UTF_8),
                user.getId(),
                URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8),
                URLEncoder.encode(user.getName(), StandardCharsets.UTF_8),
                user.getMembershipType().name(),
                isNewUser
        );
        
        response.sendRedirect(redirectUrl);
        
        log.info("OAuth2 로그인 성공 - 사용자 ID: {}, 이메일: {}, 신규 유저: {}, 리다이렉트: {}", 
                user.getId(), user.getEmail(), isNewUser, FRONTEND_URL);
    }
} 