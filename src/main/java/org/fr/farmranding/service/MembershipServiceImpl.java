package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.dto.membership.MembershipPlanCreateRequest;
import org.fr.farmranding.dto.membership.MembershipPlanResponse;
import org.fr.farmranding.dto.membership.MembershipPlanUpdateRequest;
import org.fr.farmranding.entity.membership.MembershipPlan;
import org.fr.farmranding.entity.user.MembershipType;
import org.fr.farmranding.repository.MembershipPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MembershipServiceImpl implements MembershipService {
    
    private final MembershipPlanRepository membershipPlanRepository;
    
    @Override
    public MembershipPlanResponse createMembershipPlan(MembershipPlanCreateRequest request) {
        // 동일한 멤버십 타입이 이미 존재하는지 확인
        if (membershipPlanRepository.existsByMembershipType(request.membershipType())) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
        
        MembershipPlan plan = MembershipPlan.builder()
                .membershipType(request.membershipType())
                .planName(request.planName())
                .description(request.description())
                .monthlyPrice(request.monthlyPrice())
                .yearlyPrice(request.yearlyPrice())
                .aiBrandingLimit(request.aiBrandingLimit())
                .pricingSuggestionLimit(request.pricingSuggestionLimit())
                .projectStorageLimit(request.projectStorageLimit())
                .advancedAnalytics(request.advancedAnalytics())
                .prioritySupport(request.prioritySupport())
                .customBranding(request.customBranding())
                .apiAccess(request.apiAccess())
                .exportFeatures(request.exportFeatures())
                .isActive(request.isActive())
                .isPopular(request.isPopular())
                .sortOrder(request.sortOrder())
                .build();
        
        MembershipPlan savedPlan = membershipPlanRepository.save(plan);
        log.info("멤버십 플랜 생성 완료: planId={}, membershipType={}", savedPlan.getId(), savedPlan.getMembershipType());
        
        return MembershipPlanResponse.from(savedPlan);
    }
    
    @Override
    @Transactional(readOnly = true)
    public MembershipPlanResponse getMembershipPlan(Long planId) {
        MembershipPlan plan = findPlanById(planId);
        return MembershipPlanResponse.from(plan);
    }
    
    @Override
    @Transactional(readOnly = true)
    public MembershipPlanResponse getMembershipPlanByType(MembershipType membershipType) {
        MembershipPlan plan = membershipPlanRepository.findActiveByMembershipType(membershipType)
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.USER_NOT_FOUND));
        return MembershipPlanResponse.from(plan);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> getActiveMembershipPlans() {
        List<MembershipPlan> plans = membershipPlanRepository.findActivePlansOrderedBySortOrder();
        return plans.stream()
                .map(MembershipPlanResponse::from)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> getAllMembershipPlans() {
        List<MembershipPlan> plans = membershipPlanRepository.findAll();
        return plans.stream()
                .map(MembershipPlanResponse::from)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public MembershipPlanResponse getPopularPlan() {
        MembershipPlan plan = membershipPlanRepository.findByIsPopularTrue()
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.USER_NOT_FOUND));
        return MembershipPlanResponse.from(plan);
    }
    
    @Override
    public MembershipPlanResponse updateMembershipPlan(Long planId, MembershipPlanUpdateRequest request) {
        MembershipPlan plan = findPlanById(planId);
        
        // 기본 정보 업데이트
        if (request.planName() != null || request.description() != null ||
            request.monthlyPrice() != null || request.yearlyPrice() != null) {
            plan.updatePlanInfo(
                    request.planName() != null ? request.planName() : plan.getPlanName(),
                    request.description() != null ? request.description() : plan.getDescription(),
                    request.monthlyPrice() != null ? request.monthlyPrice() : plan.getMonthlyPrice(),
                    request.yearlyPrice() != null ? request.yearlyPrice() : plan.getYearlyPrice()
            );
        }
        
        // 제한 사항 업데이트
        if (request.aiBrandingLimit() != null || request.pricingSuggestionLimit() != null ||
            request.projectStorageLimit() != null) {
            plan.updateLimits(
                    request.aiBrandingLimit() != null ? request.aiBrandingLimit() : plan.getAiBrandingLimit(),
                    request.pricingSuggestionLimit() != null ? request.pricingSuggestionLimit() : plan.getPricingSuggestionLimit(),
                    request.projectStorageLimit() != null ? request.projectStorageLimit() : plan.getProjectStorageLimit()
            );
        }
        
        // 기능 지원 업데이트
        if (request.advancedAnalytics() != null || request.prioritySupport() != null ||
            request.customBranding() != null || request.apiAccess() != null || request.exportFeatures() != null) {
            plan.updateFeatures(
                    request.advancedAnalytics() != null ? request.advancedAnalytics() : plan.getAdvancedAnalytics(),
                    request.prioritySupport() != null ? request.prioritySupport() : plan.getPrioritySupport(),
                    request.customBranding() != null ? request.customBranding() : plan.getCustomBranding(),
                    request.apiAccess() != null ? request.apiAccess() : plan.getApiAccess(),
                    request.exportFeatures() != null ? request.exportFeatures() : plan.getExportFeatures()
            );
        }
        
        // 상태 업데이트
        if (request.isActive() != null || request.isPopular() != null || request.sortOrder() != null) {
            plan.updateStatus(
                    request.isActive() != null ? request.isActive() : plan.getIsActive(),
                    request.isPopular() != null ? request.isPopular() : plan.getIsPopular(),
                    request.sortOrder() != null ? request.sortOrder() : plan.getSortOrder()
            );
        }
        
        MembershipPlan savedPlan = membershipPlanRepository.save(plan);
        log.info("멤버십 플랜 수정 완료: planId={}", planId);
        
        return MembershipPlanResponse.from(savedPlan);
    }
    
    @Override
    public void deleteMembershipPlan(Long planId) {
        MembershipPlan plan = findPlanById(planId);
        
        membershipPlanRepository.delete(plan);
        log.info("멤버십 플랜 삭제 완료: planId={}", planId);
    }
    
    @Override
    public MembershipPlanResponse togglePlanStatus(Long planId) {
        MembershipPlan plan = findPlanById(planId);
        
        plan.updateStatus(!plan.getIsActive(), plan.getIsPopular(), plan.getSortOrder());
        MembershipPlan savedPlan = membershipPlanRepository.save(plan);
        
        log.info("멤버십 플랜 상태 변경 완료: planId={}, isActive={}", planId, savedPlan.getIsActive());
        return MembershipPlanResponse.from(savedPlan);
    }
    
    @Override
    public MembershipPlanResponse setPopularPlan(Long planId) {
        // 기존 인기 플랜 해제
        membershipPlanRepository.findByIsPopularTrue()
                .ifPresent(existingPopular -> {
                    existingPopular.updateStatus(existingPopular.getIsActive(), false, existingPopular.getSortOrder());
                    membershipPlanRepository.save(existingPopular);
                });
        
        // 새로운 인기 플랜 설정
        MembershipPlan plan = findPlanById(planId);
        plan.updateStatus(plan.getIsActive(), true, plan.getSortOrder());
        MembershipPlan savedPlan = membershipPlanRepository.save(plan);
        
        log.info("인기 플랜 설정 완료: planId={}", planId);
        return MembershipPlanResponse.from(savedPlan);
    }
    
    private MembershipPlan findPlanById(Long planId) {
        return membershipPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.USER_NOT_FOUND));
    }
} 