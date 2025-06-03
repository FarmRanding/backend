package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationServiceImpl implements ImageGenerationService {
    
    @Value("${ai.openai.api-key-verify}")
    private String openaiApiKey;
    
    private final RestTemplate restTemplate;
    private final ImageStorageService imageStorageService;  // 추상화된 서비스 사용
    
    private static final String OPENAI_IMAGE_API_URL = "https://api.openai.com/v1/images/generations";

    @Override
    public String generateBrandLogo(String brandName, List<String> keywords, String prompt) {
        try {
            log.info("브랜드 로고 이미지 생성 시작 (gpt-image-1): brandName={}, keywords={}", brandName, keywords);
            
            // 프롬프트 길이 체크 (1000자 제한)
            if (prompt.length() > 1000) {
                log.warn("프롬프트가 너무 깁니다: {}자 (최대 1000자), 잘라서 사용합니다", prompt.length());
                prompt = prompt.substring(0, 1000);
            }
            
            HttpHeaders headers = createHeaders();
            
            // gpt-image-1 API 요청 (기본 base64 형식, medium 품질)
            Map<String, Object> requestBody = Map.of(
                "model", "gpt-image-1",
                "prompt", prompt,
                "n", 1,
                "size", "1024x1024",
                "quality", "medium"
            );
            
            log.debug("gpt-image-1 요청 데이터: {}", requestBody);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_IMAGE_API_URL,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            return processImageResponse(response, brandName);
            
        } catch (Exception e) {
            log.error("브랜드 로고 이미지 생성 실패: brandName={}, error={}", brandName, e.getMessage(), e);
            throw new BusinessException(FarmrandingResponseCode.AI_GENERATION_FAILED);
        }
    }
    
    /**
     * HTTP 헤더 생성
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);
        return headers;
    }
    
    /**
     * OpenAI API 응답 처리 (base64 이미지를 저장소에 저장 후 URL 반환)
     */
    @SuppressWarnings("unchecked")
    private String processImageResponse(ResponseEntity<Map> response, String brandName) {
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            log.debug("gpt-image-1 API 응답: {}", responseBody);
            
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
            
            if (data != null && !data.isEmpty()) {
                String base64Image = (String) data.get(0).get("b64_json");
                if (base64Image != null && !base64Image.isEmpty()) {
                    try {
                        // base64 이미지를 바이트 배열로 변환 후 저장소에 저장
                        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                        String imageUrl = imageStorageService.saveImage(imageBytes, brandName);
                        
                        log.info("브랜드 로고 이미지 생성 완료 (gpt-image-1): brandName={}, imageUrl={}", brandName, imageUrl);
                        return imageUrl;
                    } catch (Exception e) {
                        log.error("이미지 저장 실패: brandName={}, error={}", brandName, e.getMessage());
                        throw new BusinessException(FarmrandingResponseCode.AI_GENERATION_FAILED);
                    }
                }
            }
            
            log.error("gpt-image-1 API 응답에서 이미지 데이터를 찾을 수 없습니다: brandName={}, response={}", brandName, responseBody);
            throw new BusinessException(FarmrandingResponseCode.AI_GENERATION_FAILED);
        } else {
            log.error("gpt-image-1 API 호출 실패: brandName={}, status={}, body={}", brandName, response.getStatusCode(), response.getBody());
            throw new BusinessException(FarmrandingResponseCode.AI_GENERATION_FAILED);
        }
    }
} 