package org.fr.farmranding.controller;

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
            @RequestParam String keyword) {
        
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
            @RequestParam String itemCode) {
        
        List<KamisProductCode> varieties = kamisProductCodeRepository
                .findByItemCodeOrderByKindNameAsc(itemCode);
        
        List<KamisProductCodeResponse> responses = KamisProductCodeResponse.fromList(varieties);
        
        return ResponseEntity.ok(FarmrandingResponseBody.success(responses));
    }
} 