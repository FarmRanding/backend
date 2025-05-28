package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "OAuth2 소셜 로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    @Operation(summary = "카카오 로그인", description = "카카오 OAuth2 로그인을 시작합니다.")
    @GetMapping("/kakao")
    public ResponseEntity<FarmrandingResponseBody<String>> kakaoLogin() {
        String loginUrl = "/oauth2/authorization/kakao";
        return ResponseEntity.ok(FarmrandingResponseBody.success(loginUrl));
    }
} 