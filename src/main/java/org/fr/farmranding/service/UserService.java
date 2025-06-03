package org.fr.farmranding.service;

import org.fr.farmranding.dto.request.UserSignupRequest;
import org.fr.farmranding.dto.response.UserResponse;
import org.fr.farmranding.dto.user.UserProfileResponse;
import org.fr.farmranding.dto.user.UserProfileUpdateRequest;
import org.fr.farmranding.dto.user.UserUsageResponse;
import org.fr.farmranding.entity.user.User;

public interface UserService {
    
    /**
     * 신규 유저 정보 저장 (OAuth2 로그인 후)
     */
    UserResponse completeSignup(User currentUser, UserSignupRequest request);
    
    /**
     * 사용자 프로필 조회
     */
    UserProfileResponse getUserProfile(Long userId);
    
    /**
     * 사용자 프로필 수정
     */
    UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request);
    
    /**
     * 사용자 사용량 조회
     */
    UserUsageResponse getUserUsage(Long userId);
    
    /**
     * 프로 멤버십으로 업그레이드
     */
    UserProfileResponse upgradeToProMembership(Long userId);
    
    /**
     * AI 브랜딩 사용량 증가
     */
    void incrementAiBrandingUsage(Long userId);
    
    /**
     * 가격 제안 사용량 증가
     */
    void incrementPricingSuggestionUsage(Long userId);
    
    /**
     * 사용량 초기화 (월간 리셋용)
     */
    void resetUsageCounts(Long userId);
    
    /**
     * 회원 탈퇴
     */
    void deleteUser(Long userId);
} 