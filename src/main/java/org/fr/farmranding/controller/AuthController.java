package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.request.UserSignupRequest;
import org.fr.farmranding.dto.response.UserResponse;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "OAuth2 소셜 로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
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
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UserSignupRequest request) {
        
        UserResponse response = userService.completeSignup(currentUser, request);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
} 