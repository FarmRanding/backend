package org.fr.farmranding.jwt;

import org.fr.farmranding.entity.user.User;

public interface JwtService {
    
    /**
     * Access Token 생성
     */
    String generateAccessToken(User user);
    
    /**
     * Refresh Token 생성
     */
    String generateRefreshToken(User user);
    
    /**
     * 토큰에서 사용자 ID 추출
     */
    Long getUserIdFromToken(String token);
    
    /**
     * 토큰 유효성 검증
     */
    boolean isTokenValid(String token);
    
    /**
     * 토큰 만료 여부 확인
     */
    boolean isTokenExpired(String token);
} 