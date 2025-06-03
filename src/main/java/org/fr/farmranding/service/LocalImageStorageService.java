package org.fr.farmranding.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 로컬 파일 시스템용 이미지 저장 서비스 (개발용)
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "farmranding.image.storage-type", havingValue = "local")
public class LocalImageStorageService implements ImageStorageService {
    
    @Value("${farmranding.image.upload-dir}")
    private String uploadDir;
    
    @Value("${farmranding.image.base-url}")
    private String baseUrl;
    
    @Override
    public String saveImage(byte[] imageBytes, String brandName) {
        try {
            // 업로드 디렉토리 생성
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
                log.info("업로드 디렉토리 생성: {}", uploadDirectory.getAbsolutePath());
            }
            
            // 고유한 파일명 생성
            String fileName = String.format("brand_%s_%s.png", 
                brandName.replaceAll("[^a-zA-Z0-9가-힣]", "_"), 
                UUID.randomUUID().toString().substring(0, 8));
            
            File imageFile = new File(uploadDirectory, fileName);
            
            // 파일 저장
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(imageBytes);
            }
            
            // 접근 가능한 URL 생성
            String imageUrl = String.format("%s/%s", baseUrl, fileName);
            
            log.info("로컬 이미지 저장 완료: path={}, url={}", imageFile.getAbsolutePath(), imageUrl);
            return imageUrl;
            
        } catch (IOException e) {
            log.error("로컬 이미지 저장 실패: brandName={}, error={}", brandName, e.getMessage());
            throw new RuntimeException("이미지 저장에 실패했습니다", e);
        }
    }
    
    @Override
    public void deleteImage(String imageUrl) {
        try {
            // URL에서 파일명 추출
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            File imageFile = new File(uploadDir, fileName);
            
            if (imageFile.exists() && imageFile.delete()) {
                log.info("로컬 이미지 삭제 완료: {}", imageFile.getAbsolutePath());
            } else {
                log.warn("로컬 이미지 삭제 실패 또는 파일이 존재하지 않음: {}", imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("로컬 이미지 삭제 중 오류: imageUrl={}, error={}", imageUrl, e.getMessage());
        }
    }
} 