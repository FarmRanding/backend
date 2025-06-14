package org.fr.farmranding.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.dto.pricequote.UnifiedPriceHistoryResponse;
import org.fr.farmranding.entity.pricequote.PriceQuoteRequest;
import org.fr.farmranding.entity.pricing.PremiumPriceSuggestion;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.repository.PriceQuoteRequestRepository;
import org.fr.farmranding.repository.PremiumPriceSuggestionRepository;
import org.fr.farmranding.service.UnifiedPriceHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 통합 가격 이력 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UnifiedPriceHistoryServiceImpl implements UnifiedPriceHistoryService {
    
    private final PriceQuoteRequestRepository priceQuoteRequestRepository;
    private final PremiumPriceSuggestionRepository premiumPriceSuggestionRepository;
    
    @Override
    public Page<UnifiedPriceHistoryResponse> getUnifiedPriceHistory(User currentUser, Pageable pageable) {
        // 1. 일반 가격 제안 조회
        List<PriceQuoteRequest> standardRequests = priceQuoteRequestRepository
                .findByUserId(currentUser.getId());
        
        // 2. 프리미엄 가격 제안 조회 (페이징 없이 전체 조회)
        List<PremiumPriceSuggestion> premiumSuggestions = premiumPriceSuggestionRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId(), Pageable.unpaged()).getContent();
        
        // 3. 통합 리스트 생성
        List<UnifiedPriceHistoryResponse> unifiedList = new ArrayList<>();
        
        // 일반 가격 제안 변환
        standardRequests.forEach(request -> {
            unifiedList.add(UnifiedPriceHistoryResponse.fromStandard(request));
        });
        
        // 프리미엄 가격 제안 변환
        premiumSuggestions.forEach(suggestion -> {
            unifiedList.add(UnifiedPriceHistoryResponse.fromPremium(suggestion));
        });
        
        // 4. 생성일시 기준 내림차순 정렬
        unifiedList.sort(Comparator.comparing(UnifiedPriceHistoryResponse::createdAt).reversed());
        
        // 5. 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), unifiedList.size());
        
        List<UnifiedPriceHistoryResponse> pagedList = unifiedList.subList(start, end);
        
        log.info("통합 가격 이력 조회 완료: userId={}, total={}, page={}, size={}", 
                currentUser.getId(), unifiedList.size(), pageable.getPageNumber(), pagedList.size());
        
        return new PageImpl<>(pagedList, pageable, unifiedList.size());
    }
} 