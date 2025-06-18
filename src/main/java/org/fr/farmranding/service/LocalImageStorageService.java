package org.fr.farmranding.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    
    // 이미지 최적화 설정
    private static final int TARGET_SIZE = 512;
    private static final String OUTPUT_FORMAT = "jpeg";
    
    @Override
    public String saveImage(byte[] imageBytes, String brandName) {
        try {
            // 업로드 디렉토리 생성
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
                log.info("업로드 디렉토리 생성: {}", uploadDirectory.getAbsolutePath());
            }
            
            // 이미지 리사이징 (512x512 JPEG)
            byte[] optimizedImageBytes = optimizeImage(imageBytes);
            
            // 고유한 파일명 생성 (JPEG 확장자)
            String fileName = String.format("brand_%s_%s.jpg", 
                brandName.replaceAll("[^a-zA-Z0-9가-힣]", "_"), 
                UUID.randomUUID().toString().substring(0, 8));
            
            File imageFile = new File(uploadDirectory, fileName);
            
            // 파일 저장
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(optimizedImageBytes);
            }
            
            // 접근 가능한 URL 생성
            String imageUrl = String.format("%s/%s", baseUrl, fileName);
            
            log.info("로컬 최적화 이미지 저장 완료: path={}, size={}KB, format=JPEG, url={}", 
                imageFile.getAbsolutePath(), optimizedImageBytes.length / 1024, imageUrl);
            return imageUrl;
            
        } catch (IOException e) {
            log.error("로컬 이미지 저장 실패: brandName={}, error={}", brandName, e.getMessage());
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