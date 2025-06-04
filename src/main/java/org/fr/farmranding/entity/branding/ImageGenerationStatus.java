package org.fr.farmranding.entity.branding;

/**
 * 이미지 생성 상태
 */
public enum ImageGenerationStatus {
    PENDING("대기중"),
    PROCESSING("생성중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String description;

    ImageGenerationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}