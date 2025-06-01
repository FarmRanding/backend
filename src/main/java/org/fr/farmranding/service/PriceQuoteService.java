package org.fr.farmranding.service;

import org.fr.farmranding.dto.pricequote.PriceQuoteCreateRequest;
import org.fr.farmranding.dto.pricequote.PriceQuoteResponse;
import org.fr.farmranding.entity.user.User;

import java.util.List;

public interface PriceQuoteService {
    
    /**
     * 가격 견적 요청 생성
     */
    PriceQuoteResponse createPriceQuote(PriceQuoteCreateRequest request, User currentUser);
    
    /**
     * 내 가격 견적 요청 목록 조회
     */
    List<PriceQuoteResponse> getMyPriceQuotes(User currentUser);
    
    /**
     * 가격 견적 요청 상세 조회
     */
    PriceQuoteResponse getPriceQuote(Long priceQuoteId, User currentUser);
    
    /**
     * 가격 견적 요청 삭제
     */
    void deletePriceQuote(Long priceQuoteId, User currentUser);
} 