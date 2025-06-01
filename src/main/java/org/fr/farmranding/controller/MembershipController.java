package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.membership.MembershipPlanCreateRequest;
import org.fr.farmranding.dto.membership.MembershipPlanResponse;
import org.fr.farmranding.dto.membership.MembershipPlanUpdateRequest;
import org.fr.farmranding.entity.user.MembershipType;
import org.fr.farmranding.service.MembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "멤버십 API", description = "멤버십 플랜 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/membership")
public class MembershipController {
    
    private final MembershipService membershipService;
    
    @Operation(summary = "활성 멤버십 플랜 목록 조회", description = "사용자에게 보여줄 활성화된 멤버십 플랜들을 정렬 순서대로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플랜 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/plans")
    public ResponseEntity<FarmrandingResponseBody<List<MembershipPlanResponse>>> getActiveMembershipPlans() {
        
        List<MembershipPlanResponse> response = membershipService.getActiveMembershipPlans();
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "멤버십 타입으로 플랜 조회", description = "특정 멤버십 타입의 플랜 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플랜 조회 성공"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/plans/type/{membershipType}")
    public ResponseEntity<FarmrandingResponseBody<MembershipPlanResponse>> getMembershipPlanByType(
            @PathVariable @Parameter(description = "멤버십 타입") MembershipType membershipType) {
        
        MembershipPlanResponse response = membershipService.getMembershipPlanByType(membershipType);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "인기 플랜 조회", description = "현재 인기 플랜으로 설정된 멤버십 플랜을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 플랜 조회 성공"),
            @ApiResponse(responseCode = "404", description = "인기 플랜을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/plans/popular")
    public ResponseEntity<FarmrandingResponseBody<MembershipPlanResponse>> getPopularPlan() {
        
        MembershipPlanResponse response = membershipService.getPopularPlan();
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    // === 관리자용 API ===
    
    @Operation(summary = "[관리자] 멤버십 플랜 생성", description = "새로운 멤버십 플랜을 생성합니다. (관리자 권한 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "플랜 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/admin/plans")
    public ResponseEntity<FarmrandingResponseBody<MembershipPlanResponse>> createMembershipPlan(
            @Valid @RequestBody MembershipPlanCreateRequest request) {
        
        MembershipPlanResponse response = membershipService.createMembershipPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "[관리자] 멤버십 플랜 조회", description = "특정 멤버십 플랜의 상세 정보를 조회합니다. (관리자 권한 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플랜 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @GetMapping("/admin/plans/{planId}")
    public ResponseEntity<FarmrandingResponseBody<MembershipPlanResponse>> getMembershipPlan(
            @PathVariable @Parameter(description = "플랜 ID") Long planId) {
        
        MembershipPlanResponse response = membershipService.getMembershipPlan(planId);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "[관리자] 모든 멤버십 플랜 조회", description = "모든 멤버십 플랜을 조회합니다. (관리자 권한 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플랜 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/admin/plans")
    public ResponseEntity<FarmrandingResponseBody<List<MembershipPlanResponse>>> getAllMembershipPlans() {
        
        List<MembershipPlanResponse> response = membershipService.getAllMembershipPlans();
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "[관리자] 멤버십 플랜 수정", description = "멤버십 플랜의 정보를 수정합니다. (관리자 권한 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플랜 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @PutMapping("/admin/plans/{planId}")
    public ResponseEntity<FarmrandingResponseBody<MembershipPlanResponse>> updateMembershipPlan(
            @PathVariable @Parameter(description = "플랜 ID") Long planId,
            @Valid @RequestBody MembershipPlanUpdateRequest request) {
        
        MembershipPlanResponse response = membershipService.updateMembershipPlan(planId, request);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "[관리자] 멤버십 플랜 삭제", description = "멤버십 플랜을 삭제합니다. (관리자 권한 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "플랜 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @DeleteMapping("/admin/plans/{planId}")
    public ResponseEntity<FarmrandingResponseBody<Void>> deleteMembershipPlan(
            @PathVariable @Parameter(description = "플랜 ID") Long planId) {
        
        membershipService.deleteMembershipPlan(planId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(FarmrandingResponseBody.success());
    }
    
    @Operation(summary = "[관리자] 플랜 활성화/비활성화", description = "멤버십 플랜의 활성화 상태를 토글합니다. (관리자 권한 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @PatchMapping("/admin/plans/{planId}/toggle-status")
    public ResponseEntity<FarmrandingResponseBody<MembershipPlanResponse>> togglePlanStatus(
            @PathVariable @Parameter(description = "플랜 ID") Long planId) {
        
        MembershipPlanResponse response = membershipService.togglePlanStatus(planId);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "[관리자] 인기 플랜 설정", description = "특정 플랜을 인기 플랜으로 설정합니다. (관리자 권한 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 플랜 설정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @PatchMapping("/admin/plans/{planId}/set-popular")
    public ResponseEntity<FarmrandingResponseBody<MembershipPlanResponse>> setPopularPlan(
            @PathVariable @Parameter(description = "플랜 ID") Long planId) {
        
        MembershipPlanResponse response = membershipService.setPopularPlan(planId);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
} 