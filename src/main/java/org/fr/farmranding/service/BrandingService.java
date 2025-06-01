package org.fr.farmranding.service;

import org.fr.farmranding.dto.branding.BrandingProjectCreateRequest;
import org.fr.farmranding.dto.branding.BrandingProjectResponse;
import org.fr.farmranding.dto.branding.BrandingProjectUpdateRequest;
import org.fr.farmranding.entity.branding.BrandingStatus;
import org.fr.farmranding.entity.user.User;

import java.util.List;

public interface BrandingService {
    
    /**
     * 브랜딩 프로젝트 생성
     */
    BrandingProjectResponse createBrandingProject(BrandingProjectCreateRequest request, User currentUser);
    
    /**
     * 브랜딩 프로젝트 조회
     */
    BrandingProjectResponse getBrandingProject(Long projectId, User currentUser);
    
    /**
     * 사용자의 모든 브랜딩 프로젝트 조회
     */
    List<BrandingProjectResponse> getUserBrandingProjects(User currentUser);
    
    /**
     * 상태별 브랜딩 프로젝트 조회
     */
    List<BrandingProjectResponse> getBrandingProjectsByStatus(BrandingStatus status, User currentUser);
    
    /**
     * 브랜딩 프로젝트 수정
     */
    BrandingProjectResponse updateBrandingProject(Long projectId, BrandingProjectUpdateRequest request, User currentUser);
    
    /**
     * 브랜딩 프로젝트 삭제
     */
    void deleteBrandingProject(Long projectId, User currentUser);
    
    /**
     * 브랜딩 키워드 업데이트 (단계별 진행)
     */
    BrandingProjectResponse updateBrandingKeywords(Long projectId, String keywords, User currentUser);
    
    /**
     * 작물 매력 키워드 업데이트
     */
    BrandingProjectResponse updateCropAppealKeywords(Long projectId, String keywords, User currentUser);
    
    /**
     * 로고 이미지 키워드 업데이트
     */
    BrandingProjectResponse updateLogoImageKeywords(Long projectId, String keywords, User currentUser);
    
    /**
     * GAP 인증 정보 업데이트
     */
    BrandingProjectResponse updateGapInfo(Long projectId, String gapNumber, Boolean isVerified, User currentUser);
    
    /**
     * AI 브랜딩 완료 (실제 AI 로직은 나중에 구현)
     */
    BrandingProjectResponse completeBranding(Long projectId, User currentUser);
    
    /**
     * 프로젝트 상태 변경
     */
    BrandingProjectResponse updateProjectStatus(Long projectId, BrandingStatus status, User currentUser);
} 