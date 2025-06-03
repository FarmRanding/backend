package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationServiceImpl implements ImageGenerationService {
    
    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;
    
    private final RestTemplate restTemplate;
    
    private static final String OPENAI_IMAGE_API_URL = "https://api.openai.com/v1/images/generations";

    @Override
    public String generateBrandLogo(String brandName, List<String> keywords, String prompt) {
        try {
            log.info("브랜드 로고 이미지 생성 시작 (gpt-image-1): brandName={}, keywords={}", brandName, keywords);
            
            // 프롬프트 길이 체크 (1000자 제한)
            if (prompt.length() > 1000) {
                log.error("프롬프트가 너무 깁니다: {}자 (최대 1000자)", prompt.length());
                throw new BusinessException(FarmrandingResponseCode.AI_PROMPT_TOO_LONG);
            }
            
            // OpenAI API 직접 호출 - gpt-image-1 모델 사용
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            
            Map<String, Object> requestBody = Map.of(
                "model", "gpt-image-1",  // 최신 gpt-image-1 모델 사용
                "prompt", prompt,
                "n", 1,
                "size", "1024x1024",
                "quality", "hd",
                "response_format", "url"
            );
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_IMAGE_API_URL,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
                
                if (data != null && !data.isEmpty()) {
                    String imageUrl = (String) data.get(0).get("url");
                    log.info("브랜드 로고 이미지 생성 완료 (gpt-image-1): brandName={}, imageUrl={}", brandName, imageUrl);
                    return imageUrl;
                }
            }
            
            log.error("OpenAI API 응답에서 이미지 URL을 찾을 수 없습니다: brandName={}", brandName);
            throw new BusinessException(FarmrandingResponseCode.AI_GENERATION_FAILED);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("브랜드 로고 이미지 생성 실패 (gpt-image-1): brandName={}, error={}", brandName, e.getMessage(), e);
            throw new BusinessException(FarmrandingResponseCode.AI_GENERATION_FAILED);
        }
    }
} 