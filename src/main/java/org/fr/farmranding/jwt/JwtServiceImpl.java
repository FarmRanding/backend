package org.fr.farmranding.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.config.JwtProperties;
import org.fr.farmranding.entity.user.User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    
    private final JwtProperties jwtProperties;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    
    @Override
    public String generateAccessToken(User user) {
        return createToken(user, jwtProperties.getAccessTokenExpiration());
    }
    
    @Override
    public String generateRefreshToken(User user) {
        return createToken(user, jwtProperties.getRefreshTokenExpiration());
    }
    
    private String createToken(User user, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("membershipType", user.getMembershipType().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    @Override
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.error("토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            throw new BusinessException(FarmrandingResponseCode.INVALID_TOKEN);
        }
    }
    
    @Override
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.debug("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.debug("토큰 만료 확인 실패: {}", e.getMessage());
            return true;
        }
    }
    
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰: {}", e.getMessage());
            throw new BusinessException(FarmrandingResponseCode.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 토큰: {}", e.getMessage());
            throw new BusinessException(FarmrandingResponseCode.INVALID_TOKEN);
        } catch (MalformedJwtException e) {
            log.error("잘못된 토큰 형식: {}", e.getMessage());
            throw new BusinessException(FarmrandingResponseCode.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            log.error("토큰이 비어있음: {}", e.getMessage());
            throw new BusinessException(FarmrandingResponseCode.INVALID_TOKEN);
        } catch (Exception e) {
            log.error("토큰 파싱 오류: {}", e.getMessage());
            throw new BusinessException(FarmrandingResponseCode.INVALID_TOKEN);
        }
    }
} 