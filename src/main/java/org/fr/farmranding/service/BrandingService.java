package org.fr.farmranding.service;

import org.fr.farmranding.dto.branding.BrandingProjectCreateRequest;
import org.fr.farmranding.dto.branding.BrandingProjectResponse;
import org.fr.farmranding.dto.branding.BrandingProjectUpdateRequest;
import org.fr.farmranding.entity.branding.BrandingStatus;
import org.fr.farmranding.entity.user.User;

import java.util.List;

/**
 * 브랜딩 프로젝트 관리 서비스
 * 농산물 브랜딩 프로젝트의 생성, 조회, 수정, 삭제를 담당
 */
public interface BrandingService {
    
    /**
     * 브랜딩 프로젝트 생성
     * @param request 생성 요청 DTO
     * @param currentUser 현재 사용자
     * @return 생성된 프로젝트 응답 DTO
     */
    BrandingProjectResponse createBrandingProject(BrandingProjectCreateRequest request, User currentUser);
    
    /**
     * 브랜딩 프로젝트 단건 조회
     * @param projectId 프로젝트 ID
     * @param currentUser 현재 사용자
     * @return 프로젝트 응답 DTO
     */
    BrandingProjectResponse getBrandingProject(Long projectId, User currentUser);
    
    /**
     * 사용자의 브랜딩 프로젝트 목록 조회
     * @param currentUser 현재 사용자
     * @return 프로젝트 목록
     */
    List<BrandingProjectResponse> getUserBrandingProjects(User currentUser);
    
    /**
     * 상태별 브랜딩 프로젝트 목록 조회
     * @param status 프로젝트 상태
     * @param currentUser 현재 사용자
     * @return 프로젝트 목록
     */
    List<BrandingProjectResponse> getBrandingProjectsByStatus(BrandingStatus status, User currentUser);
    
    /**
     * 브랜딩 프로젝트 수정
     * @param projectId 프로젝트 ID
     * @param request 수정 요청 DTO
     * @param currentUser 현재 사용자
     * @return 수정된 프로젝트 응답 DTO
     */
    BrandingProjectResponse updateBrandingProject(Long projectId, BrandingProjectUpdateRequest request, User currentUser);
    
    /**
     * 브랜딩 프로젝트 삭제
     * @param projectId 프로젝트 ID
     * @param currentUser 현재 사용자
     */
    void deleteBrandingProject(Long projectId, User currentUser);
    
    /**
     * 브랜딩 키워드 업데이트
     * @param projectId 프로젝트 ID
     * @param keywords 키워드 목록
     * @param currentUser 현재 사용자
     * @return 수정된 프로젝트 응답 DTO
     */
    BrandingProjectResponse updateBrandingKeywords(Long projectId, List<String> keywords, User currentUser);
    
    /**
     * 작물 매력 키워드 업데이트
     * @param projectId 프로젝트 ID
     * @param keywords 키워드 목록
     * @param currentUser 현재 사용자
     * @return 수정된 프로젝트 응답 DTO
     */
    BrandingProjectResponse updateCropAppealKeywords(Long projectId, List<String> keywords, User currentUser);
    
    /**
     * 로고 이미지 키워드 업데이트
     * @param projectId 프로젝트 ID
     * @param keywords 키워드 목록
     * @param currentUser 현재 사용자
     * @return 수정된 프로젝트 응답 DTO
     */
    BrandingProjectResponse updateLogoImageKeywords(Long projectId, List<String> keywords, User currentUser);
    
    /**
     * GAP 인증 정보 업데이트
     * @param projectId 프로젝트 ID
     * @param gapNumber GAP 인증번호
     * @param isVerified 인증 여부
     * @param currentUser 현재 사용자
     * @return 수정된 프로젝트 응답 DTO
     */
    BrandingProjectResponse updateGapInfo(Long projectId, String gapNumber, Boolean isVerified, User currentUser);
    
    /**
     * 브랜딩 완료 처리
     * @param projectId 프로젝트 ID
     * @param currentUser 현재 사용자
     * @return 완료된 프로젝트 응답 DTO
     */
    BrandingProjectResponse completeBranding(Long projectId, User currentUser);
    
    /**
     * 프로젝트 상태 변경
     * @param projectId 프로젝트 ID
     * @param status 변경할 상태
     * @param currentUser 현재 사용자
     * @return 수정된 프로젝트 응답 DTO
     */
    BrandingProjectResponse updateProjectStatus(Long projectId, BrandingStatus status, User currentUser);
} 