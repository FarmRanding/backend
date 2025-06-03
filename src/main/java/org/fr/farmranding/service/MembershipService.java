package org.fr.farmranding.service;

import org.fr.farmranding.dto.membership.MembershipPlanCreateRequest;
import org.fr.farmranding.dto.membership.MembershipPlanResponse;
import org.fr.farmranding.dto.membership.MembershipPlanUpdateRequest;
import org.fr.farmranding.entity.user.MembershipType;

import java.util.List;

public interface MembershipService {
    
    /**
     * 멤버십 플랜 생성 (관리자용)
     */
    MembershipPlanResponse createMembershipPlan(MembershipPlanCreateRequest request);
    
    /**
     * 멤버십 플랜 조회
     */
    MembershipPlanResponse getMembershipPlan(Long planId);
    
    /**
     * 멤버십 타입으로 플랜 조회
     */
    MembershipPlanResponse getMembershipPlanByType(MembershipType membershipType);
    
    /**
     * 모든 활성 멤버십 플랜 조회 (정렬 순서대로)
     */
    List<MembershipPlanResponse> getActiveMembershipPlans();
    
    /**
     * 모든 멤버십 플랜 조회 (관리자용)
     */
    List<MembershipPlanResponse> getAllMembershipPlans();
    
    /**
     * 인기 플랜 조회
     */
    MembershipPlanResponse getPopularPlan();
    
    /**
     * 멤버십 플랜 수정 (관리자용)
     */
    MembershipPlanResponse updateMembershipPlan(Long planId, MembershipPlanUpdateRequest request);
    
    /**
     * 멤버십 플랜 삭제 (관리자용)
     */
    void deleteMembershipPlan(Long planId);
    
    /**
     * 멤버십 플랜 활성화/비활성화 (관리자용)
     */
    MembershipPlanResponse togglePlanStatus(Long planId);
    
    /**
     * 인기 플랜 설정 (관리자용)
     */
    MembershipPlanResponse setPopularPlan(Long planId);
} 