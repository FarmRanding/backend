package org.fr.farmranding.entity.branding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 브랜딩 프로젝트 상태
 */
@Getter
@RequiredArgsConstructor
public enum BrandingStatus {
    DRAFT("임시저장", "작성 중인 상태"),
    IN_PROGRESS("진행중", "브랜딩 작업 진행 중"),
    COMPLETED("완료", "브랜딩 작업 완료"),
    ARCHIVED("보관", "사용하지 않는 프로젝트");
    
    private final String displayName;
    private final String description;
    
    public boolean isActive() {
        return this == DRAFT || this == IN_PROGRESS || this == COMPLETED;
    }
    
    public boolean canEdit() {
        return this == DRAFT || this == IN_PROGRESS;
    }
    
    public boolean canComplete() {
        return this == IN_PROGRESS;
    }
} 