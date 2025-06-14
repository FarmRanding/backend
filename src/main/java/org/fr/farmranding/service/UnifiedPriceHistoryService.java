package org.fr.farmranding.service;

import org.fr.farmranding.dto.pricequote.UnifiedPriceHistoryResponse;
import org.fr.farmranding.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 통합 가격 이력 서비스 인터페이스
 * 일반 가격 제안과 프리미엄 가격 제안을 통합하여 제공
 */
public interface UnifiedPriceHistoryService {
    
    /**
     * 사용자의 통합 가격 이력 조회 (일반 + 프리미엄)
     */
    Page<UnifiedPriceHistoryResponse> getUnifiedPriceHistory(User currentUser, Pageable pageable);
} 