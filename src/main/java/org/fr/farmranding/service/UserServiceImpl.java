package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.dto.request.UserSignupRequest;
import org.fr.farmranding.dto.response.UserResponse;
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
    public UserResponse completeSignup(User currentUser, UserSignupRequest request) {
        // 사용자 이름 업데이트
        currentUser.updateProfile(currentUser.getName(), request.farmName(), request.location());
        
        User savedUser = userRepository.save(currentUser);
        log.info("신규 유저 정보 저장 완료: userId={}, name={}, farmName={}", 
                currentUser.getId(), request.name(), request.farmName());
        
        return UserResponse.from(savedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(User currentUser) {
        return UserResponse.from(currentUser);
    }
    
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
        user.updateProfile(request.name(), request.farmName(), request.location());
        
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
    public UserProfileResponse upgradeToPremiumMembership(Long userId) {
        User user = findUserById(userId);
        
        // 🔥 이미 프리미엄인 경우 현재 상태 그대로 성공 반환
        if (user.getMembershipType().isPremiumMembership()) {
            log.info("이미 프리미엄 멤버십 사용자: userId={}, 현재상태유지", userId);
            return UserProfileResponse.from(user);
        }
        
        // 프리미엄 플러스는 다운그레이드가 아니므로 예외
        if (user.getMembershipType().isPremiumPlusMembership()) {
            throw new BusinessException(FarmrandingResponseCode.ALREADY_PRO_MEMBERSHIP);
        }
        
        user.upgradeToPremiumMembership();
        User savedUser = userRepository.save(user);
        
        log.info("프리미엄 멤버십 업그레이드 완료: userId={} (FREE→PREMIUM)", userId);
        
        return UserProfileResponse.from(savedUser);
    }
    
    @Override
    public UserProfileResponse upgradeToPremiumPlusMembership(Long userId) {
        User user = findUserById(userId);
        
        // 🔥 이미 프리미엄 플러스인 경우 현재 상태 그대로 성공 반환
        if (user.getMembershipType().isPremiumPlusMembership()) {
            log.info("이미 프리미엄 플러스 멤버십 사용자: userId={}, 현재상태유지", userId);
            return UserProfileResponse.from(user);
        }
        
        user.upgradeToPremiumPlusMembership();
        User savedUser = userRepository.save(user);
        
        log.info("프리미엄 플러스 멤버십 업그레이드 완료: userId={} ({}→PREMIUM_PLUS)", userId, user.getMembershipType());
        
        return UserProfileResponse.from(savedUser);
    }
    
    @Override
    public UserProfileResponse downgradeToPremiumMembership(Long userId) {
        User user = findUserById(userId);
        
        // 🔥 이미 프리미엄인 경우 현재 상태 그대로 성공 반환
        if (user.getMembershipType().isPremiumMembership()) {
            log.info("이미 프리미엄 멤버십 사용자: userId={}, 현재상태유지", userId);
            return UserProfileResponse.from(user);
        }
        
        // 프리미엄 플러스가 아닌 경우에만 예외 (FREE→PREMIUM은 업그레이드로 처리)
        if (!user.getMembershipType().isPremiumPlusMembership()) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_MEMBERSHIP_DOWNGRADE);
        }
        
        user.downgradeToPremiumMembership();
        User savedUser = userRepository.save(user);
        
        log.info("프리미엄 멤버십 다운그레이드 완료: userId={} (PREMIUM_PLUS→PREMIUM)", userId);
        
        return UserProfileResponse.from(savedUser);
    }
    
    @Override
    public UserProfileResponse downgradeToFreeMembership(Long userId) {
        User user = findUserById(userId);
        
        // 🔥 이미 무료 멤버십인 경우 현재 상태 그대로 성공 반환
        if (user.getMembershipType().isFreeMembership()) {
            log.info("이미 무료 멤버십 사용자: userId={}, 현재상태유지", userId);
            return UserProfileResponse.from(user);
        }
        
        user.downgradeToFreeMembership();
        User savedUser = userRepository.save(user);
        
        log.info("무료 멤버십 다운그레이드 완료: userId={} ({}→FREE)", userId, user.getMembershipType());
        
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
    public void validateAiBrandingUsage(Long userId) {
        User user = findUserById(userId);
        
        if (!user.canUseAiBranding()) {
            throw new BusinessException(FarmrandingResponseCode.AI_BRANDING_USAGE_LIMIT_EXCEEDED);
        }
        
        log.debug("AI 브랜딩 사용량 검증 통과: userId={}, remaining={}", 
                userId, user.getMembershipType().getAiBrandingLimit() - user.getAiBrandingUsageCount());
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