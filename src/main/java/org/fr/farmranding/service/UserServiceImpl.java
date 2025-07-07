package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.dto.request.UserSignupRequest;
import org.fr.farmranding.dto.response.UserResponse;
import org.fr.farmranding.dto.response.AuthResponse;
import org.fr.farmranding.dto.user.UserProfileResponse;
import org.fr.farmranding.dto.user.UserProfileUpdateRequest;
import org.fr.farmranding.dto.user.UserUsageResponse;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.entity.user.MembershipType;
import org.fr.farmranding.repository.UserRepository;
import org.fr.farmranding.jwt.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    private static final String TEST_PASSWORD = "gongmoTest";
    private static final String TEST_EMAIL = "test@farmranding.com";
    
    @Override
    public AuthResponse testLogin(String password) {
        // 암호 검증
        if (!TEST_PASSWORD.equals(password)) {
            log.warn("테스트 로그인 실패 - 잘못된 암호");
            throw new BusinessException(FarmrandingResponseCode.VALIDATION_ERROR, "잘못된 암호입니다.");
        }
        
        // 기존 테스트 계정 조회 또는 생성
        User testUser = userRepository.findByEmail(TEST_EMAIL)
                .orElseGet(() -> {
                    log.info("테스트 계정 생성 중...");
                    User newTestUser = User.builder()
                            .email(TEST_EMAIL)
                            .name("테스트 사용자")
                            .providerId("test-user-001")
                            .membershipType(MembershipType.PREMIUM_PLUS) // 모든 기능 사용 가능
                            .farmName("팜랜딩 테스트 농장")
                            .location("서울특별시 강남구")
                            .build();
                    return userRepository.save(newTestUser);
                });
        
        // 기존 사용자면 농장 정보가 있으므로 신규 사용자가 아님
        boolean isNewUser = testUser.getFarmName() == null || testUser.getFarmName().trim().isEmpty();
        
        // JWT 토큰 생성
        String accessToken = jwtService.generateAccessToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);
        
        log.info("테스트 로그인 성공 - userId: {}, email: {}", testUser.getId(), testUser.getEmail());
        
        return new AuthResponse(
                accessToken,
                refreshToken,
                UserResponse.from(testUser),
                isNewUser
        );
    }
    
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