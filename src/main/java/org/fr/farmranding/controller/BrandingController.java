package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fr.farmranding.auth.CurrentUser;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.branding.BrandingProjectCreateRequest;
import org.fr.farmranding.dto.branding.BrandingProjectResponse;
import org.fr.farmranding.dto.branding.BrandingProjectUpdateRequest;
import org.fr.farmranding.entity.branding.BrandingStatus;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.service.BrandingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "브랜딩 API", description = "농산물 브랜딩 프로젝트 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/branding")
public class BrandingController {
    
    private final BrandingService brandingService;
    
    @Operation(summary = "브랜딩 API 헬스체크", description = "브랜딩 API의 상태를 확인합니다.")
    @GetMapping("/health")
    public ResponseEntity<FarmrandingResponseBody<String>> healthCheck() {
        return ResponseEntity.ok(FarmrandingResponseBody.success("브랜딩 API가 정상적으로 동작중입니다."));
    }
    
    @Operation(summary = "브랜딩 프로젝트 생성", description = "새로운 브랜딩 프로젝트를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "프로젝트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<FarmrandingResponseBody<BrandingProjectResponse>> createBrandingProject(
            @CurrentUser User currentUser,
            @Valid @RequestBody BrandingProjectCreateRequest request) {
        
        BrandingProjectResponse response = brandingService.createBrandingProject(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "브랜딩 프로젝트 조회", description = "특정 브랜딩 프로젝트의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    @GetMapping("/{projectId}")
    public ResponseEntity<FarmrandingResponseBody<BrandingProjectResponse>> getBrandingProject(
            @CurrentUser User currentUser,
            @PathVariable("projectId") Long projectId) {
        
        BrandingProjectResponse response = brandingService.getBrandingProject(projectId, currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "내 브랜딩 프로젝트 목록 조회", description = "현재 사용자의 모든 브랜딩 프로젝트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<FarmrandingResponseBody<List<BrandingProjectResponse>>> getUserBrandingProjects(
            @CurrentUser User currentUser) {
        
        List<BrandingProjectResponse> response = brandingService.getUserBrandingProjects(currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "상태별 프로젝트 조회", description = "특정 상태의 브랜딩 프로젝트들을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<FarmrandingResponseBody<List<BrandingProjectResponse>>> getBrandingProjectsByStatus(
            @CurrentUser User currentUser,
            @PathVariable("status") BrandingStatus status) {
        
        List<BrandingProjectResponse> response = brandingService.getBrandingProjectsByStatus(status, currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "브랜딩 프로젝트 수정", description = "브랜딩 프로젝트의 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    @PutMapping("/{projectId}")
    public ResponseEntity<FarmrandingResponseBody<BrandingProjectResponse>> updateBrandingProject(
            @CurrentUser User currentUser,
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody BrandingProjectUpdateRequest request) {
        
        BrandingProjectResponse response = brandingService.updateBrandingProject(projectId, request, currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "브랜딩 프로젝트 삭제", description = "브랜딩 프로젝트를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "프로젝트 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    @DeleteMapping("/{projectId}")
    public ResponseEntity<FarmrandingResponseBody<Void>> deleteBrandingProject(
            @CurrentUser User currentUser,
            @PathVariable("projectId") Long projectId) {
        
        brandingService.deleteBrandingProject(projectId, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(FarmrandingResponseBody.success());
    }
    
    @Operation(summary = "브랜딩 키워드 업데이트", description = "브랜딩 키워드를 업데이트하고 다음 단계로 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "키워드 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping("/{projectId}/branding-keywords")
    public ResponseEntity<FarmrandingResponseBody<BrandingProjectResponse>> updateBrandingKeywords(
            @CurrentUser User currentUser,
            @PathVariable("projectId") Long projectId,
            @RequestBody List<String> keywords) {
        
        BrandingProjectResponse response = brandingService.updateBrandingKeywords(projectId, keywords, currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "작물 매력 키워드 업데이트", description = "작물 매력 키워드를 업데이트하고 다음 단계로 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "키워드 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping("/{projectId}/crop-appeal-keywords")
    public ResponseEntity<FarmrandingResponseBody<BrandingProjectResponse>> updateCropAppealKeywords(
            @CurrentUser User currentUser,
            @PathVariable("projectId") Long projectId,
            @RequestBody List<String> keywords) {
        
        BrandingProjectResponse response = brandingService.updateCropAppealKeywords(projectId, keywords, currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "로고 이미지 키워드 업데이트", description = "로고 이미지 키워드를 업데이트하고 다음 단계로 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "키워드 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping("/{projectId}/logo-image-keywords")
    public ResponseEntity<FarmrandingResponseBody<BrandingProjectResponse>> updateLogoImageKeywords(
            @CurrentUser User currentUser,
            @PathVariable("projectId") Long projectId,
            @RequestBody List<String> keywords) {
        
        BrandingProjectResponse response = brandingService.updateLogoImageKeywords(projectId, keywords, currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "AI 브랜딩 완료", description = "AI를 통해 브랜딩을 완료하고 결과를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "브랜딩 완료 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/{projectId}/complete")
    public ResponseEntity<FarmrandingResponseBody<BrandingProjectResponse>> completeBranding(
            @CurrentUser User currentUser,
            @PathVariable("projectId") Long projectId) {
        
        BrandingProjectResponse response = brandingService.completeBranding(projectId, currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
} 