package org.fr.farmranding.service;

/**
 * 이미지 저장을 위한 추상화된 서비스 인터페이스
 * 구현체: LocalImageStorageService (개발용), S3ImageStorageService (운영용)
 */
public interface ImageStorageService {
    
    /**
     * 이미지를 저장하고 접근 가능한 URL을 반환
     * 
     * @param imageBytes 이미지 바이트 데이터
     * @param brandName 브랜드명
     * @return 접근 가능한 이미지 URL
     */
    String saveImage(byte[] imageBytes, String brandName);
    
    /**
     * 이미지를 삭제
     * 
     * @param imageUrl 삭제할 이미지 URL
     */
    void deleteImage(String imageUrl);
} 