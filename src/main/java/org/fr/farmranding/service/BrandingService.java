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

}
