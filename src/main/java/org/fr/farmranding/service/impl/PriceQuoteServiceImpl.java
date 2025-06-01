package org.fr.farmranding.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.dto.pricequote.PriceQuoteCreateRequest;
import org.fr.farmranding.dto.pricequote.PriceQuoteResponse;
import org.fr.farmranding.entity.pricequote.PriceQuoteRequest;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.repository.PriceQuoteRequestRepository;
import org.fr.farmranding.service.PriceQuoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PriceQuoteServiceImpl implements PriceQuoteService {
    
    private final PriceQuoteRequestRepository priceQuoteRequestRepository;
    
    @Override
    public PriceQuoteResponse createPriceQuote(PriceQuoteCreateRequest request, User currentUser) {
        // 멤버십 사용량 제한 확인
        if (!currentUser.canUsePricingSuggestion()) {
            throw new BusinessException(FarmrandingResponseCode.PRICING_USAGE_LIMIT_EXCEEDED);
        }
        
        // AI 가격 분석을 통해 가격 정보 생성 (임시로 더미 데이터)
        BigDecimal minPrice = new BigDecimal("12000");
        BigDecimal maxPrice = new BigDecimal("20000"); 
        BigDecimal avgPrice = new BigDecimal("16000");
        BigDecimal fairPrice = new BigDecimal("18000");
        String analysisResult = String.format(
            "현재 %s(%s) %s 등급 시장 분석 결과:\n" +
            "• 시장 최저가: %s원/%s\n" +
            "• 시장 최고가: %s원/%s\n" +
            "• 시장 평균가: %s원/%s\n" +
            "• AI 추천가격: %s원/%s\n\n" +
            "수확 시기와 품질을 고려할 때, %s원이 적정 판매가격으로 분석됩니다.",
            request.cropName(), 
            request.variety() != null ? request.variety() : "일반",
            request.grade(),
            minPrice, request.unit(),
            maxPrice, request.unit(),
            avgPrice, request.unit(),
            fairPrice, request.unit(),
            fairPrice
        );
        
        PriceQuoteRequest priceQuote = PriceQuoteRequest.builder()
                .user(currentUser)
                .cropName(request.cropName())
                .variety(request.variety())
                .grade(request.grade())
                .harvestDate(request.harvestDate())
                .unit(request.unit())
                .quantity(request.quantity())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .avgPrice(avgPrice)
                .fairPrice(fairPrice)
                .analysisResult(analysisResult)
                .build();
        
        PriceQuoteRequest savedPriceQuote = priceQuoteRequestRepository.save(priceQuote);
        
        // 사용량 증가
        currentUser.incrementPricingSuggestionUsage();
        
        log.info("가격 견적 요청 생성 완료 - 사용자: {}, ID: {}, 추천가격: {}", currentUser.getId(), savedPriceQuote.getId(), fairPrice);
        
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
    public PriceQuoteResponse getPriceQuote(Long priceQuoteId, User currentUser) {
        PriceQuoteRequest priceQuote = findPriceQuoteByIdAndUser(priceQuoteId, currentUser);
        return PriceQuoteResponse.from(priceQuote);
    }
    
    @Override
    public void deletePriceQuote(Long priceQuoteId, User currentUser) {
        PriceQuoteRequest priceQuote = findPriceQuoteByIdAndUser(priceQuoteId, currentUser);
        
        priceQuoteRequestRepository.delete(priceQuote);
        
        log.info("가격 견적 요청 삭제 완료 - 사용자: {}, ID: {}", currentUser.getId(), priceQuoteId);
    }
    
    // 내부 메서드
    private PriceQuoteRequest findPriceQuoteByIdAndUser(Long priceQuoteId, User currentUser) {
        return priceQuoteRequestRepository.findByIdAndUserId(priceQuoteId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.PRICE_QUOTE_NOT_FOUND));
    }
} 