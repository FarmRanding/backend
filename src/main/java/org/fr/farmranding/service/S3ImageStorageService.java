package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

/**
 * AWS S3용 이미지 저장 서비스 (운영용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "farmranding.image.storage-type", havingValue = "s3")
public class S3ImageStorageService implements ImageStorageService {
    
    private final S3Client s3Client;
    
    @Value("${farmranding.image.s3.bucket-name}")
    private String bucketName;
    
    @Value("${farmranding.image.s3.region}")
    private String region;
    
    @Override
    public String saveImage(byte[] imageBytes, String brandName) {
        try {
            // 고유한 파일명 생성
            String fileName = String.format("brand-images/brand_%s_%s.png", 
                brandName.replaceAll("[^a-zA-Z0-9가-힣]", "_"), 
                UUID.randomUUID().toString().substring(0, 8));
            
            // S3에 퍼블릭 읽기 권한으로 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("image/png")
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
            
            // 퍼블릭 URL 생성
            String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, fileName);
            
            log.info("S3 이미지 업로드 완료: bucket={}, key={}, url={}", bucketName, fileName, imageUrl);
            return imageUrl;
            
        } catch (Exception e) {
            log.error("S3 이미지 업로드 실패: brandName={}, error={}", brandName, e.getMessage());
            throw new RuntimeException("이미지 저장에 실패했습니다", e);
        }
    }
    
    @Override
    public void deleteImage(String imageUrl) {
        try {
            // URL에서 키 추출
            String key;
            if (imageUrl.contains("amazonaws.com/")) {
                key = imageUrl.substring(imageUrl.indexOf("amazonaws.com/") + 14);
            } else {
                log.warn("S3 URL 형식이 올바르지 않습니다: {}", imageUrl);
                return;
            }
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            
            log.info("S3 이미지 삭제 완료: bucket={}, key={}", bucketName, key);
            
        } catch (Exception e) {
            log.error("S3 이미지 삭제 중 오류: imageUrl={}, error={}", imageUrl, e.getMessage());
        }
    }
} 