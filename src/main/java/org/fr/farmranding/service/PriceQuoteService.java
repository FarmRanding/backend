package org.fr.farmranding.service;

import org.fr.farmranding.dto.pricequote.PriceQuoteCreateRequest;
import org.fr.farmranding.dto.pricequote.PriceQuoteResponse;
import org.fr.farmranding.dto.pricequote.PriceQuoteSaveRequest;
import org.fr.farmranding.dto.pricequote.PriceQuoteUpdateRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteStatus;
import org.fr.farmranding.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PriceQuoteService {
    
    /**
     * 가격 견적 요청 생성
     */
    PriceQuoteResponse createPriceQuote(PriceQuoteCreateRequest request, User currentUser);
    
    /**
     * 가격 제안 결과 저장 (완전한 데이터)
     */
    PriceQuoteResponse savePriceQuoteResult(PriceQuoteSaveRequest request, User currentUser);
    
    /**
     * 내 가격 견적 요청 목록 조회
     */
    List<PriceQuoteResponse> getMyPriceQuotes(User currentUser);
    
    /**
     * 내 가격 견적 요청 목록 조회 (페이징)
     */
    Page<PriceQuoteResponse> getMyPriceQuotes(User currentUser, Pageable pageable);
    
    /**
     * 상태별 가격 견적 요청 목록 조회
     */
    List<PriceQuoteResponse> getMyPriceQuotesByStatus(User currentUser, PriceQuoteStatus status);
    
    /**
     * 가격 견적 요청 상세 조회
     */
    PriceQuoteResponse getPriceQuote(Long priceQuoteId, User currentUser);
    
    /**
     * 가격 견적 요청 수정
     */
    PriceQuoteResponse updatePriceQuote(Long priceQuoteId, PriceQuoteUpdateRequest request, User currentUser);
    
    /**
     * 가격 견적 요청 삭제
     */
    void deletePriceQuote(Long priceQuoteId, User currentUser);
    
    /**
     * 가격 분석 시작
     */
    PriceQuoteResponse startAnalysis(Long priceQuoteId, User currentUser);
    
    /**
     * 가격 분석 완료
     */
    PriceQuoteResponse completeAnalysis(Long priceQuoteId, BigDecimal finalPrice, String analysisResult, User currentUser);
    
    /**
     * 가격 견적 요청 검색
     */
    List<PriceQuoteResponse> searchPriceQuotes(String keyword, User currentUser);
    
    /**
     * 최근 가격 견적 요청 조회
     */
    List<PriceQuoteResponse> getRecentPriceQuotes(User currentUser, int limit);
} 