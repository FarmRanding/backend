package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fr.farmranding.auth.CurrentUser;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.user.UserProfileResponse;
import org.fr.farmranding.dto.user.UserProfileUpdateRequest;
import org.fr.farmranding.dto.user.UserUsageResponse;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 API", description = "사용자 프로필 및 사용량 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    
    private final UserService userService;
    
    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/profile")
    public ResponseEntity<FarmrandingResponseBody<UserProfileResponse>> getUserProfile(
            @CurrentUser User currentUser) {
        
        UserProfileResponse response = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "내 프로필 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping("/profile")
    public ResponseEntity<FarmrandingResponseBody<UserProfileResponse>> updateUserProfile(
            @CurrentUser User currentUser,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        
        UserProfileResponse response = userService.updateUserProfile(currentUser.getId(), request);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "내 사용량 조회", description = "현재 로그인한 사용자의 서비스 사용량을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용량 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/usage")
    public ResponseEntity<FarmrandingResponseBody<UserUsageResponse>> getUserUsage(
            @CurrentUser User currentUser) {
        
        UserUsageResponse response = userService.getUserUsage(currentUser.getId());
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "프리미엄 멤버십 업그레이드", description = "무료 멤버십에서 프리미엄 멤버십으로 업그레이드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업그레이드 성공"),
            @ApiResponse(responseCode = "400", description = "이미 프리미엄 이상 멤버십 사용자"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/upgrade/premium")
    public ResponseEntity<FarmrandingResponseBody<UserProfileResponse>> upgradeToPremiumMembership(
            @CurrentUser User currentUser) {
        
        UserProfileResponse response = userService.upgradeToPremiumMembership(currentUser.getId());
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "프리미엄 플러스 멤버십 업그레이드", description = "기존 멤버십에서 프리미엄 플러스 멤버십으로 업그레이드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업그레이드 성공"),
            @ApiResponse(responseCode = "400", description = "이미 프리미엄 플러스 멤버십 사용자"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/upgrade/premium-plus")
    public ResponseEntity<FarmrandingResponseBody<UserProfileResponse>> upgradeToPremiumPlusMembership(
            @CurrentUser User currentUser) {
        
        UserProfileResponse response = userService.upgradeToPremiumPlusMembership(currentUser.getId());
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "프리미엄 멤버십 다운그레이드", description = "프리미엄 플러스에서 프리미엄 멤버십으로 다운그레이드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다운그레이드 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 다운그레이드"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/downgrade/premium")
    public ResponseEntity<FarmrandingResponseBody<UserProfileResponse>> downgradeToPremiumMembership(
            @CurrentUser User currentUser) {
        
        UserProfileResponse response = userService.downgradeToPremiumMembership(currentUser.getId());
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "무료 멤버십 다운그레이드", description = "기존 멤버십에서 무료 멤버십으로 다운그레이드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다운그레이드 성공"),
            @ApiResponse(responseCode = "400", description = "이미 무료 멤버십 사용자"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/downgrade/free")
    public ResponseEntity<FarmrandingResponseBody<UserProfileResponse>> downgradeToFreeMembership(
            @CurrentUser User currentUser) {
        
        UserProfileResponse response = userService.downgradeToFreeMembership(currentUser.getId());
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @DeleteMapping
    public ResponseEntity<FarmrandingResponseBody<Void>> deleteUser(
            @CurrentUser User currentUser) {
        
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(FarmrandingResponseBody.success());
    }
} 