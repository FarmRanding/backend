package org.fr.farmranding.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.dto.premium.PremiumPriceRequest;
import org.fr.farmranding.dto.premium.PremiumPriceResponse;
import org.fr.farmranding.entity.pricing.KamisProductCode;
import org.fr.farmranding.entity.pricing.PremiumPriceSuggestion;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.repository.KamisProductCodeRepository;
import org.fr.farmranding.repository.PremiumPriceSuggestionRepository;
import org.fr.farmranding.service.KamisApiService;
import org.fr.farmranding.service.PremiumPriceGptService;
import org.fr.farmranding.service.PremiumPriceSuggestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 프리미엄 가격 제안 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PremiumPriceSuggestionServiceImpl implements PremiumPriceSuggestionService {
    
    private final PremiumPriceSuggestionRepository premiumPriceSuggestionRepository;
    private final KamisProductCodeRepository kamisProductCodeRepository;
    private final KamisApiService kamisApiService;
    private final PremiumPriceGptService premiumPriceGptService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public PremiumPriceResponse createPremiumPriceSuggestion(PremiumPriceRequest request, User currentUser) {
        // 1. 프리미엄 멤버십 확인
        validatePremiumMembership(currentUser);
        
        // 2. 품목 정보 조회 및 검증
        KamisProductCode productCode = validateAndGetProductCode(request);
        
        // 3. KAMIS API에서 5일간 가격 데이터 조회
        LocalDate baseDate = LocalDate.parse(request.date(), DATE_FORMATTER);
        String kindCode = (request.productVarietyCode() != null && !request.productVarietyCode().isEmpty()) 
                ? request.productVarietyCode() 
                : productCode.getKindCode();
        
        log.info("KAMIS API 호출 파라미터: groupCode={}, itemCode={}, kindCode={}, baseDate={}", 
                productCode.getGroupCode(), productCode.getItemCode(), kindCode, baseDate);
        
        // 등급 코드 설정 (요청에서 받은 값 사용, 없으면 기본값 04)
        String rankCode = (request.productRankCode() != null && !request.productRankCode().isEmpty()) 
                ? request.productRankCode() 
                : "04";
        
        // 지역별 데이터 포함하여 KAMIS API 호출
        KamisApiService.KamisPriceResponse priceData = kamisApiService.fetchBothPriceDataWithRegion(
                productCode.getGroupCode(),
                productCode.getItemCode(), // 실제 KAMIS 품목 코드 사용
                kindCode, // 품종 코드 명시적 설정
                rankCode, // 요청받은 등급 코드 사용
                baseDate.minusDays(4), // 시작일
                baseDate, // 종료일
                request.location() != null ? request.location() : "전국" // 지역 정보
        );
        
        // 3.1. KAMIS 데이터 유효성 검증
        validateKamisData(priceData, productCode.getItemName());
        
        // 4. GPT를 통한 가격 분석 및 제안 (지역 데이터 존재 여부 포함)
        PremiumPriceGptService.PremiumPriceResult gptResult = generatePriceWithGpt(
                request, productCode, priceData);
        
        // 5. 결과 저장
        PremiumPriceSuggestion entity = savePriceSuggestion(request, currentUser, productCode, priceData, gptResult);
        
        log.info("프리미엄 가격 제안 생성 완료: userId={}, itemCode={}, suggestedPrice={}", 
                currentUser.getId(), request.productItemCode(), gptResult.getSuggestedPrice());
        
        return PremiumPriceResponse.from(entity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PremiumPriceResponse> getMyPremiumPriceSuggestions(User currentUser, Pageable pageable) {
        Page<PremiumPriceSuggestion> suggestions = premiumPriceSuggestionRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        
        return suggestions.map(PremiumPriceResponse::from);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PremiumPriceResponse getPremiumPriceSuggestionById(Long suggestionId, User currentUser) {
        PremiumPriceSuggestion suggestion = premiumPriceSuggestionRepository
                .findById(suggestionId)
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.PREMIUM_PRICE_SUGGESTION_NOT_FOUND));
        
        // 소유자 확인
        if (!suggestion.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(FarmrandingResponseCode.PREMIUM_PRICE_SUGGESTION_ACCESS_DENIED);
        }
        
        return PremiumPriceResponse.from(suggestion);
    }
    
    /**
     * 프리미엄 멤버십 확인
     */
    private void validatePremiumMembership(User currentUser) {
        if (!currentUser.getMembershipType().isPremiumOrAbove()) {
            throw new BusinessException(FarmrandingResponseCode.PREMIUM_MEMBERSHIP_REQUIRED);
        }
    }
    
    /**
     * 품목 코드 검증 및 조회
     * 프론트엔드에서 가락시장 코드나 품목명을 보내면 KAMIS 품목 코드로 변환
     */
    private KamisProductCode validateAndGetProductCode(PremiumPriceRequest request) {
        // 1. 먼저 품목명으로 KAMIS 품목 코드 검색 시도
        List<KamisProductCode> productsByName = kamisProductCodeRepository
                .findByItemNameContainingIgnoreCaseOrderByItemNameAsc(request.productItemCode());
        
        if (!productsByName.isEmpty()) {
            // 정확히 일치하는 품목명이 있는지 확인
            for (KamisProductCode product : productsByName) {
                if (product.getItemName().equalsIgnoreCase(request.productItemCode())) {
                    log.info("품목명으로 KAMIS 코드 매핑 성공: {} -> {}", request.productItemCode(), product.getItemCode());
                    return product;
                }
            }
            // 정확히 일치하지 않으면 첫 번째 결과 사용
            KamisProductCode firstMatch = productsByName.get(0);
            log.info("품목명 부분 매칭으로 KAMIS 코드 사용: {} -> {}", request.productItemCode(), firstMatch.getItemCode());
            return firstMatch;
        }
        
        // 2. 품목명으로 찾지 못하면 직접 품목 코드로 검색 시도
        String kindCode = request.productVarietyCode() != null ? request.productVarietyCode() : "00";
        Optional<KamisProductCode> directMatch = kamisProductCodeRepository
                .findByItemCodeAndKindCode(request.productItemCode(), kindCode);
        
        if (directMatch.isPresent()) {
            log.info("직접 품목 코드로 KAMIS 코드 매핑 성공: {}", request.productItemCode());
            return directMatch.get();
        }
        
        // 3. 모든 방법으로 찾지 못하면 예외 발생
        log.error("KAMIS 품목 코드를 찾을 수 없음: productItemCode={}, productVarietyCode={}", 
                request.productItemCode(), request.productVarietyCode());
        throw new BusinessException(FarmrandingResponseCode.PRODUCT_CODE_NOT_FOUND);
    }
    
    /**
     * KAMIS 데이터 유효성 검증
     */
    private void validateKamisData(KamisApiService.KamisPriceResponse priceData, String itemName) {
        // KAMIS API에서 "001" (데이터 없음) 응답 체크
        if (priceData.getRetailData() != null && priceData.getRetailData().contains("\"001\"")) {
            log.error("KAMIS에서 소매 데이터 없음 응답: itemName={}", itemName);
            throw new BusinessException(FarmrandingResponseCode.KAMIS_DATA_NOT_AVAILABLE);
        }
        
        if (priceData.getWholesaleData() != null && priceData.getWholesaleData().contains("\"001\"")) {
            log.error("KAMIS에서 도매 데이터 없음 응답: itemName={}", itemName);
            throw new BusinessException(FarmrandingResponseCode.KAMIS_DATA_NOT_AVAILABLE);
        }
        
        // 소매 데이터 검증
        if (priceData.getRetailData() == null || priceData.getRetailData().trim().isEmpty() || 
            "{}".equals(priceData.getRetailData().trim())) {
            log.error("KAMIS 소매 데이터가 없음: itemName={}", itemName);
            throw new BusinessException(FarmrandingResponseCode.KAMIS_DATA_NOT_AVAILABLE);
        }
        
        // 도매 데이터 검증
        if (priceData.getWholesaleData() == null || priceData.getWholesaleData().trim().isEmpty() || 
            "{}".equals(priceData.getWholesaleData().trim())) {
            log.error("KAMIS 도매 데이터가 없음: itemName={}", itemName);
            throw new BusinessException(FarmrandingResponseCode.KAMIS_DATA_NOT_AVAILABLE);
        }
        
        log.info("KAMIS 데이터 검증 완료: itemName={}", itemName);
    }

    /**
     * GPT를 통한 가격 분석 및 제안
     */
    private PremiumPriceGptService.PremiumPriceResult generatePriceWithGpt(
            PremiumPriceRequest request, 
            KamisProductCode productCode, 
            KamisApiService.KamisPriceResponse priceData) {
        
        String kindCode = request.productVarietyCode() != null ? request.productVarietyCode() : productCode.getKindCode();
        String rankCode = (request.productRankCode() != null && !request.productRankCode().isEmpty()) 
                ? request.productRankCode() 
                : "04";
        
        PremiumPriceGptService.PremiumPriceRequest gptRequest = 
                PremiumPriceGptService.PremiumPriceRequest.builder()
                        .itemCode(request.productItemCode())
                        .itemName(request.productName() != null ? request.productName() : productCode.getItemName()) // 사용자 입력 품목명 우선 사용
                        .kindCode(kindCode)
                        .kindName(productCode.getKindName())
                        .productRankCode(rankCode) // 요청받은 등급 코드 사용
                        .location(request.location() != null ? request.location() : "전국")
                        .hasRegionalData(priceData.hasRegionalData()) // 지역 데이터 존재 여부
                        .startDate(priceData.getStartDate().format(DATE_FORMATTER))
                        .endDate(priceData.getEndDate().format(DATE_FORMATTER))
                        .retailData(priceData.getRetailData())
                        .wholesaleData(priceData.getWholesaleData())
                        .build();
        
        // PremiumPriceGptService에서 이미 에러 체크를 하므로 여기서는 단순히 호출만
        return premiumPriceGptService.generatePremiumPrice(gptRequest);
    }
    
    /**
     * 가격 제안 결과 저장
     */
    private PremiumPriceSuggestion savePriceSuggestion(
            PremiumPriceRequest request,
            User currentUser,
            KamisProductCode productCode,
            KamisApiService.KamisPriceResponse priceData,
            PremiumPriceGptService.PremiumPriceResult gptResult) {
        
        // 원시 데이터 결합 (저장용)
        String combinedRawData = String.format(
                "=== 소매 데이터 ===\n%s\n\n=== 도매 데이터 ===\n%s",
                priceData.getRetailData(),
                priceData.getWholesaleData()
        );
        
        String kindCode = request.productVarietyCode() != null ? request.productVarietyCode() : productCode.getKindCode();
        String rankCode = (request.productRankCode() != null && !request.productRankCode().isEmpty()) 
                ? request.productRankCode() 
                : "04";
        
        PremiumPriceSuggestion entity = PremiumPriceSuggestion.builder()
                .user(currentUser)
                .itemCategoryCode(productCode.getGroupCode())
                .itemCode(productCode.getItemCode()) // 실제 KAMIS 품목 코드 사용
                .itemName(request.productName() != null ? request.productName() : productCode.getItemName()) // 사용자가 입력한 품목명 우선 사용
                .kindCode(kindCode)
                .productRankCode(rankCode) // 요청받은 등급 코드 사용
                .location(request.location())
                .startDate(priceData.getStartDate())
                .endDate(priceData.getEndDate())
                .suggestedPrice(gptResult.getSuggestedPrice())
                .calculationReason(gptResult.getCalculationReason())
                .retail5DayAvg(gptResult.getRetailAverage())
                .wholesale5DayAvg(gptResult.getWholesaleAverage())
                .alphaRatio(gptResult.getAlphaRatio())
                .rawData(combinedRawData)
                .build();
        
        return premiumPriceSuggestionRepository.save(entity);
    }
} 