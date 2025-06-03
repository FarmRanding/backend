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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    
    // 이미지 최적화 설정
    private static final int TARGET_SIZE = 512;
    private static final String OUTPUT_FORMAT = "jpeg";
    private static final String CONTENT_TYPE = "image/jpeg";
    
    @Override
    public String saveImage(byte[] imageBytes, String brandName) {
        try {
            // 이미지 리사이징 (512x512 JPEG)
            byte[] optimizedImageBytes = optimizeImage(imageBytes);
            
            // 고유한 파일명 생성 (JPEG 확장자)
            String fileName = String.format("brand-images/brand_%s_%s.jpg", 
                brandName.replaceAll("[^a-zA-Z0-9가-힣]", "_"), 
                UUID.randomUUID().toString().substring(0, 8));
            
            // S3에 퍼블릭 읽기 권한으로 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(CONTENT_TYPE)
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(optimizedImageBytes));
            
            // 퍼블릭 URL 생성
            String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, fileName);
            
            log.info("S3 최적화 이미지 업로드 완료: bucket={}, key={}, size={}KB, format=JPEG, url={}", 
                bucketName, fileName, optimizedImageBytes.length / 1024, imageUrl);
            return imageUrl;
            
        } catch (Exception e) {
            log.error("S3 이미지 업로드 실패: brandName={}, error={}", brandName, e.getMessage());
            throw new RuntimeException("이미지 저장에 실패했습니다", e);
        }
    }
    
    /**
     * 이미지 최적화: 512x512 리사이징 + JPEG 변환
     */
    private byte[] optimizeImage(byte[] originalImageBytes) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // 원본 이미지 로드
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageBytes));
        if (originalImage == null) {
            throw new IllegalArgumentException("이미지를 읽을 수 없습니다");
        }
        
        // 512x512로 리사이징
        BufferedImage resizedImage = resizeImage(originalImage, TARGET_SIZE, TARGET_SIZE);
        
        // JPEG 포맷으로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean written = ImageIO.write(resizedImage, OUTPUT_FORMAT, outputStream);
        
        if (!written) {
            throw new RuntimeException("JPEG 변환에 실패했습니다.");
        }
        
        byte[] optimizedBytes = outputStream.toByteArray();
        
        long processingTime = System.currentTimeMillis() - startTime;
        log.info("이미지 리사이징 완료: 원본={}KB, JPEG={}KB, 압축률={:.1f}%, 처리시간={}ms", 
            originalImageBytes.length / 1024, 
            optimizedBytes.length / 1024,
            (1 - (double) optimizedBytes.length / originalImageBytes.length) * 100,
            processingTime);
        
        return optimizedBytes;
    }
    
    /**
     * 이미지 리사이징 (고품질 알고리즘 사용)
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // RGB 타입으로 새 이미지 생성
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        
        // 고품질 리사이징을 위한 Graphics2D 설정
        Graphics2D g2d = resizedImage.createGraphics();
        try {
            // 안티앨리어싱 및 고품질 렌더링 설정
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 배경을 흰색으로 설정 (투명도 제거)
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, targetWidth, targetHeight);
            
            // 이미지 그리기
            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g2d.dispose();
        }
        
        return resizedImage;
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