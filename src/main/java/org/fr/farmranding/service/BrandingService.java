package org.fr.farmranding.service;

import org.fr.farmranding.dto.branding.BrandingProjectCreateRequest;
import org.fr.farmranding.dto.branding.BrandingProjectResponse;
import org.fr.farmranding.dto.branding.BrandingProjectUpdateRequest;
import org.fr.farmranding.entity.user.User;

import java.util.List;

/**
 * 브랜딩 프로젝트 관리 서비스
 * 농산물 브랜딩 프로젝트의 생성, 조회, 수정, 삭제 및 키워드·GAP 정보·상태 관리를 담당
 */
public interface BrandingService {

    /**
     * 브랜딩 프로젝트 생성
     * 작물정보, GAP 인증 여부, 키워드를 입력받아 GPT로 브랜딩을 완료하고 프로젝트를 생성합니다.
     */
    BrandingProjectResponse createBrandingProject(BrandingProjectCreateRequest request, User currentUser);

    /**
     * 브랜딩 프로젝트 단건 조회
     */
    BrandingProjectResponse getBrandingProject(Long projectId, User currentUser);

    /**
     * 사용자의 모든 브랜딩 프로젝트 목록 조회
     */
    List<BrandingProjectResponse> getUserBrandingProjects(User currentUser);

    /**
     * 브랜딩 프로젝트 수정
     * 프로젝트 기본 정보, GAP 정보, 키워드 등을 수정합니다.
     */
    BrandingProjectResponse updateBrandingProject(Long projectId, BrandingProjectUpdateRequest request, User currentUser);

    /**
     * 브랜딩 프로젝트 삭제
     */
    void deleteBrandingProject(Long projectId, User currentUser);

    /**
     * 브랜드명만 빠르게 생성 (빠른 모델 사용)
     */
    String generateBrandName(org.fr.farmranding.dto.branding.BrandNameRequest request, org.fr.farmranding.entity.user.User currentUser, String prompt);

    /**
     * 최종 브랜드 생성 (로고, 컨셉, 스토리 포함, 프롬프트는 파라미터)
     */
    BrandingProjectResponse createBrandingProjectWithAi(org.fr.farmranding.dto.branding.BrandingProjectCreateRequest request, org.fr.farmranding.entity.user.User currentUser, String brandName, String promptForLogo, String promptForConcept, String promptForStory);

    /**
     * 🚀 점진적 브랜딩 생성 (텍스트 먼저 반환, 이미지는 백그라운드 처리)
     * 텍스트(홍보 문구/스토리)는 5초 내 즉시 반환하고, 이미지는 백그라운드에서 처리하여 나중에 업데이트
     */
    BrandingProjectResponse createBrandingProjectProgressive(BrandingProjectCreateRequest request, User currentUser, String brandName);

}
