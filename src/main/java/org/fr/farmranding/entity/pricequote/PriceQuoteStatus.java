package org.fr.farmranding.entity.pricequote;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 가격 제안 요청 상태
 */
@Getter
@RequiredArgsConstructor
public enum PriceQuoteStatus {
    DRAFT("임시저장", "작성 중인 상태"),
    IN_PROGRESS("분석중", "AI 가격 분석 진행 중"),
    COMPLETED("완료", "가격 분석 완료"),
    ARCHIVED("보관", "사용하지 않는 요청");
    
    private final String displayName;
    private final String description;
    
    public boolean isActive() {
        return this == DRAFT || this == IN_PROGRESS || this == COMPLETED;
    }
    
    public boolean canEdit() {
        return this == DRAFT || this == IN_PROGRESS;
    }
    
    public boolean canAnalyze() {
        return this == DRAFT || this == IN_PROGRESS;
    }
} 