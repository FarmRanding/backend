package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "브랜딩 프로젝트 생성", 
               description = "작물정보, GAP인증여부, 키워드를 입력받아 GPT로 브랜딩을 완료하고 프로젝트를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "프로젝트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "사용량 한도 초과")
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
            @Parameter(description = "브랜딩 프로젝트 ID", example = "1")
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
            @Parameter(description = "브랜딩 프로젝트 ID", example = "1")
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
            @Parameter(description = "브랜딩 프로젝트 ID", example = "1")
            @PathVariable("projectId") Long projectId) {
        
        brandingService.deleteBrandingProject(projectId, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(FarmrandingResponseBody.success());
    }
} 