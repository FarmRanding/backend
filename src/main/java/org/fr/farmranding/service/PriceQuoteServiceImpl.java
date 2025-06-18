package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.dto.pricequote.*;
import org.fr.farmranding.entity.pricequote.PriceQuoteRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteStatus;
import org.fr.farmranding.entity.pricing.PremiumPriceSuggestion;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.repository.PriceQuoteRequestRepository;
import org.fr.farmranding.repository.PremiumPriceSuggestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PriceQuoteServiceImpl implements PriceQuoteService {
    
    private final PriceQuoteRequestRepository priceQuoteRequestRepository;
    private final PremiumPriceSuggestionRepository premiumPriceSuggestionRepository;
    
    @Override
    public PriceQuoteResponse createPriceQuote(PriceQuoteCreateRequest request, User currentUser) {
        // 멤버십 사용량 제한 확인
        if (!currentUser.canUsePricingSuggestion()) {
            throw new BusinessException(FarmrandingResponseCode.PRICING_USAGE_LIMIT_EXCEEDED);
        }
        
        PriceQuoteRequest priceQuote = PriceQuoteRequest.builder()
                .user(currentUser)
                .productId(request.productId())
                .garakCode(request.garakCode())
                .productName(request.productName())
                .grade(request.grade())
                .harvestDate(request.harvestDate())
                .estimatedPrice(request.estimatedPrice())
                .status(PriceQuoteStatus.DRAFT)
                .build();
        
        PriceQuoteRequest savedPriceQuote = priceQuoteRequestRepository.save(priceQuote);
        
        // 사용량 증가
        currentUser.incrementPricingSuggestionUsage();
        
        log.info("가격 견적 요청 생성 완료 - 사용자: {}, ID: {}", currentUser.getId(), savedPriceQuote.getId());
        
        return PriceQuoteResponse.from(savedPriceQuote);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PriceQuoteResponse> getMyPriceQuotes(User currentUser) {
        List<PriceQuoteRequest> priceQuotes = priceQuoteRequestRepository.findByUserId(currentUser.getId());
        return priceQuotes.stream()
                .map(PriceQuoteResponse::from)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PriceQuoteResponse> getMyPriceQuotes(User currentUser, Pageable pageable) {
        Page<PriceQuoteRequest> priceQuotes = priceQuoteRequestRepository.findByUserId(currentUser.getId(), pageable);
        return priceQuotes.map(PriceQuoteResponse::from);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UnifiedPriceHistoryResponse> getUnifiedPriceHistory(User currentUser) {
        List<UnifiedPriceHistoryResponse> unifiedHistory = new ArrayList<>();
        
        // 일반 가격 제안 조회
        List<PriceQuoteRequest> standardQuotes = priceQuoteRequestRepository.findByUserId(currentUser.getId());
        for (PriceQuoteRequest quote : standardQuotes) {
            unifiedHistory.add(UnifiedPriceHistoryResponse.fromStandard(quote));
        }
        
        // 프리미엄 가격 제안 조회
        List<PremiumPriceSuggestion> premiumSuggestions = premiumPriceSuggestionRepository.findByUserIdOrderByCreatedAtDesc(
                currentUser.getId(), 
                Pageable.unpaged()
        ).getContent();
        
        for (PremiumPriceSuggestion suggestion : premiumSuggestions) {
            unifiedHistory.add(UnifiedPriceHistoryResponse.fromPremium(suggestion));
        }
        
        // 생성일시 기준 최신순 정렬
        unifiedHistory.sort(Comparator.comparing(UnifiedPriceHistoryResponse::createdAt).reversed());
        
        return unifiedHistory;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PriceQuoteResponse> getMyPriceQuotesByStatus(User currentUser, PriceQuoteStatus status) {
        List<PriceQuoteRequest> priceQuotes = priceQuoteRequestRepository.findByUserIdAndStatus(currentUser.getId(), status);
        return priceQuotes.stream()
                .map(PriceQuoteResponse::from)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public PriceQuoteResponse getPriceQuote(Long priceQuoteId, User currentUser) {
        PriceQuoteRequest priceQuote = findPriceQuoteByIdAndUser(priceQuoteId, currentUser);
        return PriceQuoteResponse.from(priceQuote);
    }
    
    @Override
    public PriceQuoteResponse updatePriceQuote(Long priceQuoteId, PriceQuoteUpdateRequest request, User currentUser) {
        PriceQuoteRequest priceQuote = findPriceQuoteByIdAndUser(priceQuoteId, currentUser);
        
        // 수정 가능한 상태인지 확인
        if (!priceQuote.canEdit()) {
            throw new BusinessException(FarmrandingResponseCode.PRICE_QUOTE_CANNOT_EDIT);
        }
        
        priceQuote.updateBasicInfo(
                request.productId() != null ? request.productId() : priceQuote.getProductId(),
                request.garakCode() != null ? request.garakCode() : priceQuote.getGarakCode(),
                request.productName() != null ? request.productName() : priceQuote.getProductName(),
                request.grade() != null ? request.grade() : priceQuote.getGrade(),
                request.harvestDate() != null ? request.harvestDate() : priceQuote.getHarvestDate(),
                request.estimatedPrice() != null ? request.estimatedPrice() : priceQuote.getEstimatedPrice()
        );
        
        log.info("가격 견적 요청 수정 완료 - 사용자: {}, ID: {}", currentUser.getId(), priceQuoteId);
        
        return PriceQuoteResponse.from(priceQuote);
    }
    
    @Override
    public void deletePriceQuote(Long priceQuoteId, User currentUser) {
        PriceQuoteRequest priceQuote = findPriceQuoteByIdAndUser(priceQuoteId, currentUser);
        
        priceQuoteRequestRepository.delete(priceQuote);
        
        log.info("가격 견적 요청 삭제 완료 - 사용자: {}, ID: {}", currentUser.getId(), priceQuoteId);
    }
    
    @Override
    public void deleteUnifiedPriceQuote(Long id, String type, User currentUser) {
        if ("STANDARD".equals(type)) {
            // 일반 가격 제안 삭제
            PriceQuoteRequest priceQuote = priceQuoteRequestRepository.findByIdAndUserId(id, currentUser.getId())
                    .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.PRICE_QUOTE_NOT_FOUND));
            
            priceQuoteRequestRepository.delete(priceQuote);
            log.info("일반 가격 견적 삭제 완료 - 사용자: {}, ID: {}", currentUser.getId(), id);
            
        } else if ("PREMIUM".equals(type)) {
            // 프리미엄 가격 제안 삭제
            PremiumPriceSuggestion premiumSuggestion = premiumPriceSuggestionRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.PREMIUM_PRICE_SUGGESTION_NOT_FOUND));
            
            // 소유자 확인
            if (!premiumSuggestion.getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException(FarmrandingResponseCode.PREMIUM_PRICE_SUGGESTION_ACCESS_DENIED);
            }
            
            premiumPriceSuggestionRepository.delete(premiumSuggestion);
            log.info("프리미엄 가격 견적 삭제 완료 - 사용자: {}, ID: {}", currentUser.getId(), id);
            
        } else {
            throw new BusinessException(FarmrandingResponseCode.VALIDATION_ERROR);
        }
    }
    
    @Override
    public PriceQuoteResponse startAnalysis(Long priceQuoteId, User currentUser) {
        PriceQuoteRequest priceQuote = findPriceQuoteByIdAndUser(priceQuoteId, currentUser);
        
        if (!priceQuote.canEdit()) {
            throw new BusinessException(FarmrandingResponseCode.PRICE_QUOTE_CANNOT_ANALYZE);
        }
        
        priceQuote.updateStatus(PriceQuoteStatus.IN_PROGRESS);
        
        // TODO: 실제 AI 분석 로직 호출
        
        log.info("가격 분석 시작 - 사용자: {}, ID: {}", currentUser.getId(), priceQuoteId);
        
        return PriceQuoteResponse.from(priceQuote);
    }
    
    @Override
    public PriceQuoteResponse completeAnalysis(Long priceQuoteId, BigDecimal finalPrice, String analysisResult, User currentUser) {
        PriceQuoteRequest priceQuote = findPriceQuoteByIdAndUser(priceQuoteId, currentUser);
        
        priceQuote.updatePriceAnalysisComplete(finalPrice, null, null, null, null, analysisResult);
        
        log.info("가격 분석 완료 - 사용자: {}, ID: {}, 최종가격: {}", currentUser.getId(), priceQuoteId, finalPrice);
        
        return PriceQuoteResponse.from(priceQuote);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PriceQuoteResponse> searchPriceQuotes(String keyword, User currentUser) {
        List<PriceQuoteRequest> priceQuotes = priceQuoteRequestRepository.findByUserIdAndProductNameContaining(currentUser.getId(), keyword);
        return priceQuotes.stream()
                .map(PriceQuoteResponse::from)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PriceQuoteResponse> getRecentPriceQuotes(User currentUser, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<PriceQuoteRequest> priceQuotes = priceQuoteRequestRepository.findRecentByUserId(currentUser.getId(), pageable);
        return priceQuotes.stream()
                .map(PriceQuoteResponse::from)
                .toList();
    }
    
    @Override
    public PriceQuoteResponse savePriceQuoteResult(PriceQuoteSaveRequest request, User currentUser) {
        // 멤버십 사용량 제한 확인
        if (!currentUser.canUsePricingSuggestion()) {
            throw new BusinessException(FarmrandingResponseCode.PRICING_USAGE_LIMIT_EXCEEDED);
        }
        
        // 최대/최소값 계산 (yearlyPriceData JSON에서 추출)
        BigDecimal chartMinPrice = request.minPrice();
        BigDecimal chartMaxPrice = request.maxPrice();
        
        PriceQuoteRequest priceQuote = PriceQuoteRequest.builder()
                .user(currentUser)
                .productId(request.productId())
                .garakCode(request.garakCode())
                .productName(request.productName())
                .grade(request.grade())
                .harvestDate(request.harvestDate())
                .unit(request.unit())
                .quantity(request.quantity())
                .finalPrice(request.finalPrice())
                .minPrice(request.minPrice())
                .maxPrice(request.maxPrice())
                .avgPrice(request.avgPrice())
                .fairPrice(request.finalPrice()) // 최종 가격을 추천 가격으로 설정
                .yearlyPriceData(request.yearlyPriceData())
                .chartMinPrice(chartMinPrice)
                .chartMaxPrice(chartMaxPrice)
                .lookupDate(request.lookupDate())
                .analysisResult("가락시장 데이터 기반 가격 분석 완료")
                .status(PriceQuoteStatus.COMPLETED)
                .build();
        
        PriceQuoteRequest savedPriceQuote = priceQuoteRequestRepository.save(priceQuote);
        
        // 사용량 증가
        currentUser.incrementPricingSuggestionUsage();
        
        log.info("가격 제안 결과 저장 완료 - 사용자: {}, ID: {}, 품목: {}", 
                currentUser.getId(), savedPriceQuote.getId(), request.productName());
        
        return PriceQuoteResponse.from(savedPriceQuote);
    }

    // 내부 메서드
    private PriceQuoteRequest findPriceQuoteByIdAndUser(Long priceQuoteId, User currentUser) {
        return priceQuoteRequestRepository.findByIdAndUserId(priceQuoteId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.PRICE_QUOTE_NOT_FOUND));
    }
} 