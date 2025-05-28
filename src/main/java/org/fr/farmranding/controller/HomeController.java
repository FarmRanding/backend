package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "홈", description = "팜랜딩 메인 API")
@RestController
@RequestMapping("/api")
public class HomeController {
    
    @Operation(summary = "API 상태 확인", description = "팜랜딩 API 서버 상태를 확인합니다.")
    @GetMapping("/home")
    public ResponseEntity<FarmrandingResponseBody<Map<String, String>>> home() {
        Map<String, String> status = Map.of(
                "service", "Farmranding API",
                "version", "1.0.0",
                "status", "running",
                "message", "팜랜딩 API 서버가 정상 작동 중입니다."
        );
        return ResponseEntity.ok(FarmrandingResponseBody.success(status));
    }
} 