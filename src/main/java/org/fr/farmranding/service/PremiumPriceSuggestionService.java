package org.fr.farmranding.service;

import org.fr.farmranding.dto.premium.PremiumPriceRequest;
import org.fr.farmranding.dto.premium.PremiumPriceResponse;
import org.fr.farmranding.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 프리미엄 가격 제안 서비스 인터페이스
 */
public interface PremiumPriceSuggestionService {
    
    /**
     * 프리미엄 가격 제안 생성
     */
    PremiumPriceResponse createPremiumPriceSuggestion(PremiumPriceRequest request, User currentUser);
    
    /**
     * 사용자의 프리미엄 가격 제안 이력 조회
     */
    Page<PremiumPriceResponse> getMyPremiumPriceSuggestions(User currentUser, Pageable pageable);
    
    /**
     * 특정 프리미엄 가격 제안 상세 조회
     */
    PremiumPriceResponse getPremiumPriceSuggestionById(Long suggestionId, User currentUser);
} 