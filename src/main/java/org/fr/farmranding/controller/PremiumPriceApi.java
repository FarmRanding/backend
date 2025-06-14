package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.fr.farmranding.auth.CurrentUser;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.premium.KamisProductCodeResponse;
import org.fr.farmranding.dto.premium.PremiumPriceRequest;
import org.fr.farmranding.dto.premium.PremiumPriceResponse;
import org.fr.farmranding.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "프리미엄 가격 제안 API", description = "프리미엄 회원 전용 고급 가격 제안 서비스")
@SecurityRequirement(name = "bearerAuth")
public interface PremiumPriceApi {
    
    @Operation(
        summary = "프리미엄 가격 제안 생성",
        description = "KAMIS API 데이터와 GPT-4o 분석을 통한 프리미엄 가격 제안을 생성합니다. (프리미엄 이상 멤버십 필요)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "프리미엄 가격 제안 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "프리미엄 멤버십 필요"),
        @ApiResponse(responseCode = "404", description = "품목 코드를 찾을 수 없음"),
        @ApiResponse(responseCode = "503", description = "외부 API 호출 실패")
    })
    ResponseEntity<FarmrandingResponseBody<PremiumPriceResponse>> createPremiumPriceSuggestion(
            @CurrentUser User currentUser,
            @Valid @RequestBody PremiumPriceRequest request
    );
    
    @Operation(
        summary = "내 프리미엄 가격 제안 이력 조회",
        description = "사용자가 요청한 프리미엄 가격 제안 이력을 최신순으로 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<FarmrandingResponseBody<Page<PremiumPriceResponse>>> getMyPremiumPriceSuggestions(
            @CurrentUser User currentUser,
            @Parameter(description = "페이지 정보") Pageable pageable
    );
    
    @Operation(
        summary = "프리미엄 가격 제안 상세 조회",
        description = "특정 프리미엄 가격 제안의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "프리미엄 가격 제안을 찾을 수 없음")
    })
    ResponseEntity<FarmrandingResponseBody<PremiumPriceResponse>> getPremiumPriceSuggestionById(
            @CurrentUser User currentUser,
            @Parameter(description = "프리미엄 가격 제안 ID", required = true) Long suggestionId
    );
    
    @Operation(
        summary = "KAMIS 품목 코드 검색",
        description = "품목명으로 KAMIS 품목 코드를 검색합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<FarmrandingResponseBody<List<KamisProductCodeResponse>>> searchProductCodes(
            @CurrentUser User currentUser,
            @Parameter(description = "검색할 품목명", required = true) 
            @RequestParam String keyword
    );
    
    @Operation(
        summary = "KAMIS 품목 그룹 목록 조회",
        description = "KAMIS 품목 그룹 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<FarmrandingResponseBody<List<KamisProductCodeResponse>>> getProductGroups(
            @CurrentUser User currentUser
    );
    
    @Operation(
        summary = "특정 품목의 품종 목록 조회",
        description = "특정 품목의 모든 품종을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<FarmrandingResponseBody<List<KamisProductCodeResponse>>> getProductVarieties(
            @CurrentUser User currentUser,
            @Parameter(description = "품목 코드", required = true) 
            @RequestParam String itemCode
    );
} 
