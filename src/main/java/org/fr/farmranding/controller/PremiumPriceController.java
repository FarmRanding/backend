package org.fr.farmranding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.fr.farmranding.auth.CurrentUser;
import org.fr.farmranding.common.dto.FarmrandingResponseBody;
import org.fr.farmranding.dto.premium.KamisProductCodeResponse;
import org.fr.farmranding.dto.premium.PremiumPriceRequest;
import org.fr.farmranding.dto.premium.PremiumPriceResponse;
import org.fr.farmranding.entity.pricing.KamisProductCode;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.repository.KamisProductCodeRepository;
import org.fr.farmranding.service.PremiumPriceSuggestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 프리미엄 가격 제안 API 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/premium-price")
public class PremiumPriceController implements PremiumPriceApi {
    
    private final PremiumPriceSuggestionService premiumPriceSuggestionService;
    private final KamisProductCodeRepository kamisProductCodeRepository;
    
    @PostMapping("/suggestions")
    @Override
    public ResponseEntity<FarmrandingResponseBody<PremiumPriceResponse>> createPremiumPriceSuggestion(
            @CurrentUser User currentUser,
            @Valid @RequestBody PremiumPriceRequest request) {
        
        PremiumPriceResponse response = premiumPriceSuggestionService
                .createPremiumPriceSuggestion(request, currentUser);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FarmrandingResponseBody.success(response));
    }
    
    @GetMapping("/history")
    @Override
    public ResponseEntity<FarmrandingResponseBody<Page<PremiumPriceResponse>>> getMyPremiumPriceSuggestions(
            @CurrentUser User currentUser,
            Pageable pageable) {
        
        Page<PremiumPriceResponse> responses = premiumPriceSuggestionService
                .getMyPremiumPriceSuggestions(currentUser, pageable);
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(responses));
    }
    
    @GetMapping("/{suggestionId}")
    @Override
    public ResponseEntity<FarmrandingResponseBody<PremiumPriceResponse>> getPremiumPriceSuggestionById(
            @CurrentUser User currentUser,
            @PathVariable Long suggestionId) {
        
        PremiumPriceResponse response = premiumPriceSuggestionService
                .getPremiumPriceSuggestionById(suggestionId, currentUser);
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(response));
    }
    
    @GetMapping("/products/search")
    @Override
    public ResponseEntity<FarmrandingResponseBody<List<KamisProductCodeResponse>>> searchProductCodes(
            @CurrentUser User currentUser,
            @RequestParam(value = "keyword") String keyword) {
        
        List<KamisProductCode> products = kamisProductCodeRepository
                .findByItemNameContainingIgnoreCaseOrderByItemNameAsc(keyword);
        
        List<KamisProductCodeResponse> responses = KamisProductCodeResponse.fromList(products);
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(responses));
    }
    
    @GetMapping("/products/groups")
    @Override
    public ResponseEntity<FarmrandingResponseBody<List<KamisProductCodeResponse>>> getProductGroups(
            @CurrentUser User currentUser) {
        
        // 품목 그룹 조회를 위한 별도 로직 필요
        // 현재는 전체 품목 중에서 그룹별로 대표 하나씩 선택
        List<Object[]> groups = kamisProductCodeRepository.findDistinctGroups();
        
        List<KamisProductCodeResponse> responses = groups.stream()
                .map(group -> new KamisProductCodeResponse(
                        null,
                        (String) group[0], // groupCode
                        (String) group[1], // groupName
                        null, null, null, null
                ))
                .toList();
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(responses));
    }
    
    @GetMapping("/products/varieties")
    @Override
    public ResponseEntity<FarmrandingResponseBody<List<KamisProductCodeResponse>>> getProductVarieties(
            @CurrentUser User currentUser,
            @RequestParam(value = "itemCode") String itemCode) {
        
        List<KamisProductCode> varieties = kamisProductCodeRepository
                .findByItemCodeOrderByKindNameAsc(itemCode);
        
        List<KamisProductCodeResponse> responses = KamisProductCodeResponse.fromList(varieties);
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(responses));
    }

    /**
     * KAMIS 품목 코드 조회
     */
    @GetMapping("/kamis-products")
    @Operation(summary = "KAMIS 품목 코드 조회", description = "검색어로 KAMIS 품목 코드를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<FarmrandingResponseBody<List<KamisProductDto>>> getKamisProducts(
            @RequestParam(value = "search", required = false) String search) {
        
        List<KamisProductCode> products;
        
        if (search != null && !search.trim().isEmpty()) {
            // 검색어가 있으면 품목명으로 검색
            products = kamisProductCodeRepository
                    .findByItemNameContainingIgnoreCaseOrderByItemNameAsc(search.trim());
        } else {
            // 검색어가 없으면 전체 조회 (최대 50개)
            products = kamisProductCodeRepository
                    .findTop50ByOrderByItemNameAsc();
        }
        
        List<KamisProductDto> result = products.stream()
                .map(KamisProductDto::from)
                .toList();
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(result));
    }

    /**
     * KAMIS 품목 코드 DTO
     */
    @Schema(description = "KAMIS 품목 코드 정보")
    public record KamisProductDto(
            @Schema(description = "품목 코드", example = "245")
            String itemCode,
            
            @Schema(description = "품목명", example = "양파")
            String itemName,
            
            @Schema(description = "품종 코드", example = "00")
            String kindCode,
            
            @Schema(description = "품종명", example = "양파")
            String kindName
    ) {
        public static KamisProductDto from(KamisProductCode entity) {
            return new KamisProductDto(
                    entity.getItemCode(),
                    entity.getItemName(),
                    entity.getKindCode(),
                    entity.getKindName()
            );
        }
    }
} 