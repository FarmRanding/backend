package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fr.farmranding.auth.CurrentUser;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.request.RefreshTokenRequest;
import org.fr.farmranding.dto.request.UserSignupRequest;
import org.fr.farmranding.dto.response.TokenResponse;
import org.fr.farmranding.dto.response.UserResponse;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.jwt.JwtService;
import org.fr.farmranding.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "OAuth2 소셜 로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    @Operation(summary = "카카오 로그인", description = "카카오 OAuth2 로그인을 시작합니다.")
    @GetMapping("/kakao")
    public ResponseEntity<FarmrandingResponseBody<String>> kakaoLogin() {
        String loginUrl = "/oauth2/authorization/kakao";
        return ResponseEntity.ok(FarmrandingResponseBody.success(loginUrl));
    }
    
    @Operation(summary = "신규 유저 정보 저장", description = "OAuth2 로그인 후 신규 유저의 농장 정보를 저장합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/signup")
    public ResponseEntity<FarmrandingResponseBody<UserResponse>> completeSignup(
            @CurrentUser User currentUser,
            @Valid @RequestBody UserSignupRequest request) {
        
        UserResponse response = userService.completeSignup(currentUser, request);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<FarmrandingResponseBody<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        // JWT 서비스에서 토큰 검증 및 새 토큰 발급
        String newAccessToken = jwtService.refreshAccessToken(request.refreshToken());
        
        TokenResponse response = TokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .build();
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "현재 사용자 정보", description = "현재 로그인한 사용자의 기본 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me")
    public ResponseEntity<FarmrandingResponseBody<UserResponse>> getCurrentUser(
            @CurrentUser User currentUser) {
        
        log.info("🔍 /api/auth/me 호출: userId={}, membershipType={}", 
                currentUser.getId(), currentUser.getMembershipType());
        
        UserResponse response = userService.getUserInfo(currentUser);
        
        log.info("🔍 응답 데이터: membershipType={}", response.membershipType());
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
} 