package org.fr.farmranding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${farmranding.image.upload-dir:/tmp/farmranding/images}")
    private String uploadDir;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 이미지 파일들을 정적 파일로 서빙
        registry.addResourceHandler("/api/v1/images/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
} 