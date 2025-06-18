package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fr.farmranding.auth.CurrentUser;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.pricequote.PriceDataRequest;
import org.fr.farmranding.dto.pricequote.PriceDataResponse;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.service.GarakPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "가격 조회 API", description = "가락시장 기반 실시간 가격 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/price-data")
public class PriceDataController {
    
    private final GarakPriceService garakPriceService;
    
    @Operation(summary = "가격 조회", description = "가락시장 API를 통해 품목별 5년간 가격 데이터를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "가격 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "503", description = "가락시장 API 오류")
    })
    @PostMapping("/lookup")
    public ResponseEntity<FarmrandingResponseBody<PriceDataResponse>> lookupPrice(
            @CurrentUser User currentUser,
            @Valid @RequestBody PriceDataRequest request) {
        
        PriceDataResponse response = garakPriceService.getPriceData(request);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
} 