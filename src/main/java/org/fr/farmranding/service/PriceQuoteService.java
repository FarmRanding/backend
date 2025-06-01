package org.fr.farmranding.service;

import org.fr.farmranding.dto.pricequote.PriceQuoteCreateRequest;
import org.fr.farmranding.dto.pricequote.PriceQuoteResponse;
import org.fr.farmranding.dto.pricequote.PriceQuoteUpdateRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteStatus;
import org.fr.farmranding.entity.user.User;

import java.util.List;

public interface PriceQuoteService {
    
    /**
     * 가격 제안 요청 생성
     */
    PriceQuoteResponse createPriceQuoteRequest(PriceQuoteCreateRequest request, User currentUser);
    
    /**
     * 가격 제안 요청 조회
     */
    PriceQuoteResponse getPriceQuoteRequest(Long requestId, User currentUser);
    
    /**
     * 사용자의 모든 가격 제안 요청 조회
     */
    List<PriceQuoteResponse> getUserPriceQuoteRequests(User currentUser);
    
    /**
     * 상태별 가격 제안 요청 조회
     */
    List<PriceQuoteResponse> getPriceQuoteRequestsByStatus(PriceQuoteStatus status, User currentUser);
    
    /**
     * 가격 제안 요청 수정
     */
    PriceQuoteResponse updatePriceQuoteRequest(Long requestId, PriceQuoteUpdateRequest request, User currentUser);
    
    /**
     * 가격 제안 요청 삭제
     */
    void deletePriceQuoteRequest(Long requestId, User currentUser);
    
    /**
     * AI 가격 분석 실행 (실제 AI 로직은 나중에 구현)
     */
    PriceQuoteResponse analyzePriceQuote(Long requestId, User currentUser);
    
    /**
     * 요청 상태 변경
     */
    PriceQuoteResponse updateRequestStatus(Long requestId, PriceQuoteStatus status, User currentUser);
    
    /**
     * 작물별 가격 제안 요청 조회
     */
    List<PriceQuoteResponse> getPriceQuoteRequestsByCrop(String cropName, User currentUser);
} 