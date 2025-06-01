package org.fr.farmranding.common.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * JSON 배열을 TEXT 필드로 변환하는 JPA 컨버터
 * MySQL JSON 타입 호환성 문제 해결을 위해 TEXT로 저장
 */
@Slf4j
@Converter
public class JsonConverter implements AttributeConverter<List<String>, String> {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: {}", attribute, e);
            return null;
        }
    }
    
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON 역직렬화 실패: {}", dbData, e);
            return List.of();
        }
    }
} 