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
import org.fr.farmranding.dto.branding.BrandNameRequest;
import org.fr.farmranding.dto.branding.BrandNameResponse;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.service.BrandingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Tag(name = "브랜딩 API", description = "농산물 브랜딩 프로젝트 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/branding")
public class BrandingController {
    
    private final BrandingService brandingService;
    private static final Logger log = LoggerFactory.getLogger(BrandingController.class);

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

    @Operation(summary = "브랜드명 생성", description = "작물명, 키워드를 입력받아 빠른 모델로 브랜드명을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "브랜드명 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/brand-name")
    public ResponseEntity<FarmrandingResponseBody<BrandNameResponse>> generateBrandName(
            @CurrentUser User currentUser,
            @Valid @RequestBody BrandNameRequest request,
            @RequestParam(name = "prompt", required = false) String prompt) {
        String brandName = brandingService.generateBrandName(request, currentUser, prompt);
        return ResponseEntity.ok(FarmrandingResponseBody.success(new BrandNameResponse(brandName)));
    }

    @Operation(summary = "AI 기반 최종 브랜드 생성", description = "브랜드명, 로고, 컨셉, 스토리를 AI로 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "AI 기반 브랜드 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "사용량 한도 초과")
    })
    @PostMapping("/ai")
    public ResponseEntity<FarmrandingResponseBody<BrandingProjectResponse>> createBrandingProjectWithAi(
            @CurrentUser User currentUser,
            @Valid @RequestBody BrandingProjectCreateRequest request,
            @RequestParam(name = "brandName") String brandName,
            @RequestParam(name = "promptForLogo", required = false) String promptForLogo,
            @RequestParam(name = "promptForConcept", required = false) String promptForConcept,
            @RequestParam(name = "promptForStory", required = false) String promptForStory
    ) {
        BrandingProjectResponse response = brandingService.createBrandingProjectWithAi(request, currentUser, brandName, promptForLogo, promptForConcept, promptForStory);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FarmrandingResponseBody.success(response));
    }

    @Operation(summary = "🚀 점진적 AI 브랜딩 생성 (텍스트 먼저)", 
               description = "텍스트(홍보 문구/스토리)는 5초 내 즉시 반환하고, 이미지는 백그라운드에서 처리합니다. " +
                           "응답 받은 후 같은 프로젝트 ID로 조회하여 이미지 생성 상태를 확인할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "점진적 브랜딩 생성 성공 (텍스트 즉시 반환)"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "사용량 한도 초과")
    })
    @PostMapping("/ai/progressive")
    public ResponseEntity<FarmrandingResponseBody<BrandingProjectResponse>> createBrandingProjectProgressive(
            @CurrentUser User currentUser,
            @Valid @RequestBody BrandingProjectCreateRequest request,
            @RequestParam(name = "brandName") String brandName) {
        
        log.info("점진적 브랜딩 API 호출 - 사용자: {}, 브랜드명: {}", currentUser.getEmail(), brandName);
        log.info("요청 데이터 확인:");
        log.info("- title: {}", request.title());
        log.info("- cropName: {}", request.cropName());
        log.info("- variety: {}", request.variety());
        log.info("- brandingKeywords: {}", request.brandingKeywords());
        log.info("- cropAppealKeywords: {}", request.cropAppealKeywords());
        log.info("- logoImageKeywords: {}", request.logoImageKeywords());
        log.info("- hasGapCertification: {}", request.hasGapCertification());
        log.info("- gapCertificationNumber: {}", request.gapCertificationNumber());
        log.info("- 🏪 includeFarmName: {}", request.includeFarmName());
        log.info("- 사용자 농가명: {}", currentUser.getFarmName());
        
        BrandingProjectResponse response = brandingService.createBrandingProjectProgressive(request, currentUser, brandName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FarmrandingResponseBody.success(response));
    }
}