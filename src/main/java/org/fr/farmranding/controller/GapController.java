package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fr.farmranding.auth.CurrentUser;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.gap.GapCertificationResponse;
import org.fr.farmranding.dto.gap.GapSearchRequest;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.service.GapCertificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "GAP 인증 API", description = "농산물우수관리(GAP) 인증 정보 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/gap")
public class GapController {
    
    private final GapCertificationService gapCertificationService;

    @Operation(summary = "GAP 인증 정보 조회", 
               description = "GAP 인증번호로 농산물우수관리 인증 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "GAP 인증 정보 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "GAP 인증 정보를 찾을 수 없음")
    })
    @PostMapping("/search")
    public ResponseEntity<FarmrandingResponseBody<GapCertificationResponse>> searchGapCertification(
            @CurrentUser User currentUser,
            @Valid @RequestBody GapSearchRequest request) {
        
        GapCertificationResponse gapInfo = gapCertificationService.searchGapCertification(request);
        return ResponseEntity.ok(FarmrandingResponseBody.success(gapInfo));
    }
    
    @Operation(summary = "GAP 인증번호 검증 및 정보 조회", 
               description = "GAP 인증번호를 검증하고 실제 인증 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "GAP 인증 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "GAP 인증 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/validate/{certificationNumber}")
    public ResponseEntity<FarmrandingResponseBody<GapCertificationResponse>> validateGapCertificationNumber(
            @PathVariable("certificationNumber") String certificationNumber) {
        
        GapCertificationResponse gapInfo = gapCertificationService.validateAndSearchGapCertification(certificationNumber);
        return ResponseEntity.ok(FarmrandingResponseBody.success(gapInfo));
    }
} 