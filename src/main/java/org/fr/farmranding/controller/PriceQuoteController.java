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
import org.fr.farmranding.dto.pricequote.PriceQuoteCreateRequest;
import org.fr.farmranding.dto.pricequote.PriceQuoteResponse;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.service.PriceQuoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "가격 견적 API", description = "농산물 가격 견적 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/price-quotes")
public class PriceQuoteController {
    
    private final PriceQuoteService priceQuoteService;
    
    @Operation(summary = "가격 견적 요청 생성", description = "새로운 가격 견적 요청을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "가격 견적 요청 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "사용량 한도 초과")
    })
    @PostMapping
    public ResponseEntity<FarmrandingResponseBody<PriceQuoteResponse>> createPriceQuote(
            @CurrentUser User currentUser,
            @Valid @RequestBody PriceQuoteCreateRequest request) {
        
        PriceQuoteResponse response = priceQuoteService.createPriceQuote(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "내 가격 견적 요청 목록 조회", description = "현재 사용자의 모든 가격 견적 요청을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<FarmrandingResponseBody<List<PriceQuoteResponse>>> getMyPriceQuotes(
            @CurrentUser User currentUser) {
        
        List<PriceQuoteResponse> responses = priceQuoteService.getMyPriceQuotes(currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(responses));
    }
    
    @Operation(summary = "가격 견적 요청 상세 조회", description = "특정 가격 견적 요청의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "견적 요청을 찾을 수 없음")
    })
    @GetMapping("/{priceQuoteId}")
    public ResponseEntity<FarmrandingResponseBody<PriceQuoteResponse>> getPriceQuote(
            @CurrentUser User currentUser,
            @Parameter(description = "가격 견적 요청 ID", example = "1")
            @PathVariable Long priceQuoteId) {
        
        PriceQuoteResponse response = priceQuoteService.getPriceQuote(priceQuoteId, currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "가격 견적 요청 삭제", description = "가격 견적 요청을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "견적 요청을 찾을 수 없음")
    })
    @DeleteMapping("/{priceQuoteId}")
    public ResponseEntity<Void> deletePriceQuote(
            @CurrentUser User currentUser,
            @Parameter(description = "가격 견적 요청 ID", example = "1")
            @PathVariable Long priceQuoteId) {
        
        priceQuoteService.deletePriceQuote(priceQuoteId, currentUser);
        return ResponseEntity.noContent().build();
    }
} 