package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.dto.pricequote.PriceQuoteCreateRequest;
import org.fr.farmranding.dto.pricequote.PriceQuoteResponse;
import org.fr.farmranding.dto.pricequote.PriceQuoteUpdateRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteStatus;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.repository.PriceQuoteRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        
        PriceQuoteRequest priceQuote = PriceQuoteRequest.builder()
                .user(currentUser)
                .cropName(request.cropName())
                .variety(request.variety())
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
                request.cropName() != null ? request.cropName() : priceQuote.getCropName(),
                request.variety() != null ? request.variety() : priceQuote.getVariety(),
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
        
        priceQuote.completeAnalysis(finalPrice, analysisResult);
        
        log.info("가격 분석 완료 - 사용자: {}, ID: {}, 최종가격: {}", currentUser.getId(), priceQuoteId, finalPrice);
        
        return PriceQuoteResponse.from(priceQuote);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PriceQuoteResponse> searchPriceQuotes(String keyword, User currentUser) {
        List<PriceQuoteRequest> priceQuotes = priceQuoteRequestRepository.findByUserIdAndCropNameContaining(currentUser.getId(), keyword);
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
    
    // 내부 메서드
    private PriceQuoteRequest findPriceQuoteByIdAndUser(Long priceQuoteId, User currentUser) {
        return priceQuoteRequestRepository.findByIdAndUserId(priceQuoteId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(FarmrandingResponseCode.PRICE_QUOTE_NOT_FOUND));
    }
} 