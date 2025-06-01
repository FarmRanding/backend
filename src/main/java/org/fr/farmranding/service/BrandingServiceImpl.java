package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.dto.branding.BrandingProjectCreateRequest;
import org.fr.farmranding.dto.branding.BrandingProjectResponse;
import org.fr.farmranding.dto.branding.BrandingProjectUpdateRequest;
import org.fr.farmranding.entity.branding.BrandingProject;
import org.fr.farmranding.entity.branding.BrandingStatus;
import org.fr.farmranding.entity.branding.BrandingStep;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.repository.BrandingProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BrandingServiceImpl implements BrandingService {
    
    private final BrandingProjectRepository brandingProjectRepository;
    private final UserService userService;
    
    @Override
    public BrandingProjectResponse createBrandingProject(BrandingProjectCreateRequest request, User currentUser) {
        // AI 브랜딩 사용량 체크
        userService.incrementAiBrandingUsage(currentUser.getId());
        
        BrandingProject project = BrandingProject.builder()
                .title(request.title())
                .user(currentUser)
                .cropName(request.cropName())
                .variety(request.variety())
                .cultivationMethod(request.cultivationMethod())
                .grade(request.grade())
                .status(BrandingStatus.DRAFT)
                .currentStep(BrandingStep.BASIC_INFO)
                .build();
        
        BrandingProject savedProject = brandingProjectRepository.save(project);
        log.info("브랜딩 프로젝트 생성 완료: projectId={}, userId={}", savedProject.getId(), currentUser.getId());
        
        return BrandingProjectResponse.from(savedProject);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BrandingProjectResponse getBrandingProject(Long projectId, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());
        return BrandingProjectResponse.from(project);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BrandingProjectResponse> getUserBrandingProjects(User currentUser) {
        List<BrandingProject> projects = brandingProjectRepository.findByUserId(currentUser.getId());
        return projects.stream()
                .map(BrandingProjectResponse::from)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BrandingProjectResponse> getBrandingProjectsByStatus(BrandingStatus status, User currentUser) {
        List<BrandingProject> projects = brandingProjectRepository.findByUserIdAndStatus(currentUser.getId(), status);
        return projects.stream()
                .map(BrandingProjectResponse::from)
                .toList();
    }
    
    @Override
    public BrandingProjectResponse updateBrandingProject(Long projectId, BrandingProjectUpdateRequest request, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());
        
        if (!project.canEdit()) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
        
        // 기본 정보 업데이트
        if (request.title() != null || request.cropName() != null) {
            project.updateBasicInfo(
                    request.title() != null ? request.title() : project.getTitle(),
                    request.cropName() != null ? request.cropName() : project.getCropName(),
                    request.variety(),
                    request.cultivationMethod(),
                    request.grade()
            );
        }
        
        // GAP 정보 업데이트
        if (request.gapNumber() != null || request.isGapVerified() != null) {
            project.updateGapInfo(request.gapNumber(), request.isGapVerified());
        }
        
        // 키워드 정보 업데이트
        if (request.brandingKeywords() != null) {
            project.updateBrandingKeywords(request.brandingKeywords());
        }
        if (request.cropAppealKeywords() != null) {
            project.updateCropAppealKeywords(request.cropAppealKeywords());
        }
        if (request.logoImageKeywords() != null) {
            project.updateLogoImageKeywords(request.logoImageKeywords());
        }
        
        BrandingProject savedProject = brandingProjectRepository.save(project);
        log.info("브랜딩 프로젝트 수정 완료: projectId={}", projectId);
        
        return BrandingProjectResponse.from(savedProject);
    }
    
    @Override
    public void deleteBrandingProject(Long projectId, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());
        
        brandingProjectRepository.delete(project);
        log.info("브랜딩 프로젝트 삭제 완료: projectId={}", projectId);
    }
    
    @Override
    public BrandingProjectResponse updateBrandingKeywords(Long projectId, String keywords, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());
        
        if (!project.canEdit()) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
        
        project.updateBrandingKeywords(keywords);
        BrandingProject savedProject = brandingProjectRepository.save(project);
        
        log.info("브랜딩 키워드 업데이트 완료: projectId={}", projectId);
        return BrandingProjectResponse.from(savedProject);
    }
    
    @Override
    public BrandingProjectResponse updateCropAppealKeywords(Long projectId, String keywords, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());
        
        if (!project.canEdit()) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
        
        project.updateCropAppealKeywords(keywords);
        BrandingProject savedProject = brandingProjectRepository.save(project);
        
        log.info("작물 매력 키워드 업데이트 완료: projectId={}", projectId);
        return BrandingProjectResponse.from(savedProject);
    }
    
    @Override
    public BrandingProjectResponse updateLogoImageKeywords(Long projectId, String keywords, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());
        
        if (!project.canEdit()) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
        
        project.updateLogoImageKeywords(keywords);
        BrandingProject savedProject = brandingProjectRepository.save(project);
        
        log.info("로고 이미지 키워드 업데이트 완료: projectId={}", projectId);
        return BrandingProjectResponse.from(savedProject);
    }
    
    @Override
    public BrandingProjectResponse updateGapInfo(Long projectId, String gapNumber, Boolean isVerified, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());
        
        if (!project.canEdit()) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
        
        project.updateGapInfo(gapNumber, isVerified);
        BrandingProject savedProject = brandingProjectRepository.save(project);
        
        log.info("GAP 인증 정보 업데이트 완료: projectId={}", projectId);
        return BrandingProjectResponse.from(savedProject);
    }
    
    @Override
    public BrandingProjectResponse completeBranding(Long projectId, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());
        
        if (!project.canEdit()) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
        
        // TODO: 실제 AI 브랜딩 로직 구현 (외부 API 연동)
        // 현재는 더미 데이터로 완료 처리
        String generatedBrandName = project.getCropName() + " 프리미엄";
        String brandStory = "{\"story\": \"AI가 생성한 브랜드 스토리\"}";
        String brandConcept = "{\"concept\": \"AI가 생성한 브랜드 컨셉\"}";
        
        project.completeBranding(generatedBrandName, brandStory, brandConcept);
        BrandingProject savedProject = brandingProjectRepository.save(project);
        
        log.info("브랜딩 완료 처리: projectId={}", projectId);
        return BrandingProjectResponse.from(savedProject);
    }
    
    @Override
    public BrandingProjectResponse updateProjectStatus(Long projectId, BrandingStatus status, User currentUser) {
        BrandingProject project = findProjectByIdAndUser(projectId, currentUser.getId());
        
        project.updateStatus(status);
        BrandingProject savedProject = brandingProjectRepository.save(project);
        
        log.info("프로젝트 상태 변경 완료: projectId={}, status={}", projectId, status);
        return BrandingProjectResponse.from(savedProject);
    }
    
    private BrandingProject findProjectByIdAndUser(Long projectId, Long userId) {
        return brandingProjectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.USER_NOT_FOUND));
    }
} 