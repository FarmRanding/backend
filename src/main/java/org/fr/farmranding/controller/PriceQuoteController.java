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
import org.fr.farmranding.dto.pricequote.PriceQuoteUpdateRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteStatus;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.service.PriceQuoteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    
    @Operation(summary = "내 가격 견적 요청 목록 조회 (페이징)", description = "현재 사용자의 가격 견적 요청을 페이징하여 조회합니다.")
    @GetMapping("/paged")
    public ResponseEntity<FarmrandingResponseBody<Page<PriceQuoteResponse>>> getMyPriceQuotesPaged(
            @CurrentUser User currentUser,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<PriceQuoteResponse> responses = priceQuoteService.getMyPriceQuotes(currentUser, pageable);
        return ResponseEntity.ok(FarmrandingResponseBody.success(responses));
    }
    
    @Operation(summary = "상태별 가격 견적 요청 조회", description = "특정 상태의 가격 견적 요청들을 조회합니다.")
    @GetMapping("/status/{status}")
    public ResponseEntity<FarmrandingResponseBody<List<PriceQuoteResponse>>> getPriceQuotesByStatus(
            @CurrentUser User currentUser,
            @Parameter(description = "가격 견적 상태", example = "COMPLETED")
            @PathVariable PriceQuoteStatus status) {
        
        List<PriceQuoteResponse> responses = priceQuoteService.getMyPriceQuotesByStatus(currentUser, status);
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
    
    @Operation(summary = "가격 견적 요청 수정", description = "기존 가격 견적 요청을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "견적 요청을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "수정할 수 없는 상태")
    })
    @PutMapping("/{priceQuoteId}")
    public ResponseEntity<FarmrandingResponseBody<PriceQuoteResponse>> updatePriceQuote(
            @CurrentUser User currentUser,
            @Parameter(description = "가격 견적 요청 ID", example = "1")
            @PathVariable Long priceQuoteId,
            @Valid @RequestBody PriceQuoteUpdateRequest request) {
        
        PriceQuoteResponse response = priceQuoteService.updatePriceQuote(priceQuoteId, request, currentUser);
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
    
    @Operation(summary = "가격 분석 시작", description = "AI 기반 가격 분석을 시작합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "분석 시작 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "견적 요청을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "분석할 수 없는 상태")
    })
    @PostMapping("/{priceQuoteId}/analyze")
    public ResponseEntity<FarmrandingResponseBody<PriceQuoteResponse>> startAnalysis(
            @CurrentUser User currentUser,
            @Parameter(description = "가격 견적 요청 ID", example = "1")
            @PathVariable Long priceQuoteId) {
        
        PriceQuoteResponse response = priceQuoteService.startAnalysis(priceQuoteId, currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "가격 분석 완료", description = "AI 분석 결과를 저장하고 분석을 완료합니다.")
    @PostMapping("/{priceQuoteId}/complete")
    public ResponseEntity<FarmrandingResponseBody<PriceQuoteResponse>> completeAnalysis(
            @CurrentUser User currentUser,
            @Parameter(description = "가격 견적 요청 ID", example = "1")
            @PathVariable Long priceQuoteId,
            @Parameter(description = "최종 가격", example = "18000")
            @RequestParam BigDecimal finalPrice,
            @Parameter(description = "분석 결과", example = "시장 상황을 고려할 때...")
            @RequestParam String analysisResult) {
        
        PriceQuoteResponse response = priceQuoteService.completeAnalysis(priceQuoteId, finalPrice, analysisResult, currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @Operation(summary = "가격 견적 요청 검색", description = "키워드로 가격 견적 요청을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<FarmrandingResponseBody<List<PriceQuoteResponse>>> searchPriceQuotes(
            @CurrentUser User currentUser,
            @Parameter(description = "검색 키워드", example = "토마토")
            @RequestParam String keyword) {
        
        List<PriceQuoteResponse> responses = priceQuoteService.searchPriceQuotes(keyword, currentUser);
        return ResponseEntity.ok(FarmrandingResponseBody.success(responses));
    }
    
    @Operation(summary = "최근 가격 견적 요청 조회", description = "최근 생성된 가격 견적 요청들을 조회합니다.")
    @GetMapping("/recent")
    public ResponseEntity<FarmrandingResponseBody<List<PriceQuoteResponse>>> getRecentPriceQuotes(
            @CurrentUser User currentUser,
            @Parameter(description = "조회할 개수", example = "5")
            @RequestParam(defaultValue = "5") int limit) {
        
        List<PriceQuoteResponse> responses = priceQuoteService.getRecentPriceQuotes(currentUser, limit);
        return ResponseEntity.ok(FarmrandingResponseBody.success(responses));
    }
} 