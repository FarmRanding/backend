package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.dto.user.UserProfileResponse;
import org.fr.farmranding.dto.user.UserProfileUpdateRequest;
import org.fr.farmranding.dto.user.UserUsageResponse;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = findUserById(userId);
        return UserProfileResponse.from(user);
    }
    
    @Override
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = findUserById(userId);
        
        // 프로필 기본 정보 업데이트 (nickname, name, profileImage)
        user.updateProfile(request.nickname(), request.name(), request.profileImage());
        
        // 농장 정보 업데이트
        user.updateFarmInfo(
                request.farmName(),
                request.location(),
                request.phoneNumber(),
                request.farmDescription(),
                request.establishedYear()
        );
        
        User savedUser = userRepository.save(user);
        log.info("사용자 프로필 수정 완료: userId={}", userId);
        
        return UserProfileResponse.from(savedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserUsageResponse getUserUsage(Long userId) {
        User user = findUserById(userId);
        return UserUsageResponse.from(user);
    }
    
    @Override
    public UserProfileResponse upgradeToProMembership(Long userId) {
        User user = findUserById(userId);
        
        if (user.getMembershipType().isPro()) {
            throw new BusinessException(FarmrandingResponseCode.ALREADY_PRO_MEMBERSHIP);
        }
        
        user.upgradeToProMembership();
        User savedUser = userRepository.save(user);
        
        log.info("프로 멤버십 업그레이드 완료: userId={}", userId);
        
        return UserProfileResponse.from(savedUser);
    }
    
    @Override
    public void incrementAiBrandingUsage(Long userId) {
        User user = findUserById(userId);
        
        if (!user.canUseAiBranding()) {
            throw new BusinessException(FarmrandingResponseCode.AI_BRANDING_USAGE_LIMIT_EXCEEDED);
        }
        
        user.incrementAiBrandingUsage();
        userRepository.save(user);
        
        log.info("AI 브랜딩 사용량 증가: userId={}, count={}", userId, user.getAiBrandingUsageCount());
    }
    
    @Override
    public void incrementPricingSuggestionUsage(Long userId) {
        User user = findUserById(userId);
        
        if (!user.canUsePricingSuggestion()) {
            throw new BusinessException(FarmrandingResponseCode.PRICING_USAGE_LIMIT_EXCEEDED);
        }
        
        user.incrementPricingSuggestionUsage();
        userRepository.save(user);
        
        log.info("가격 제안 사용량 증가: userId={}, count={}", userId, user.getPricingSuggestionUsageCount());
    }
    
    @Override
    public void resetUsageCounts(Long userId) {
        User user = findUserById(userId);
        
        user.resetUsageCounts();
        userRepository.save(user);
        
        log.info("사용량 초기화 완료: userId={}", userId);
    }
    
    @Override
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        
        userRepository.delete(user);
        
        log.info("회원 탈퇴 완료: userId={}", userId);
    }
    
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.USER_NOT_FOUND));
    }
} 