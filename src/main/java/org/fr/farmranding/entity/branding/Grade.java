package org.fr.farmranding.entity.branding;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 농산물 등급 
 * 농산물의 품질 등급을 나타냅니다.
 */
@Getter
@RequiredArgsConstructor
public enum Grade {
    SPECIAL("특", "특급", "최고 품질의 농산물"),
    FIRST("상", "1급", "우수한 품질의 농산물"),
    SECOND("중", "2급", "보통 품질의 농산물"),
    THIRD("하", "3급", "기본 품질의 농산물");
    
    private final String shortName;
    private final String displayName;
    private final String description;
    
    @JsonCreator
    public static Grade fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return SECOND; // 기본값: 중급
        }
        
        // enum 이름으로 조회 시도 (SPECIAL, FIRST 등)
        try {
            return Grade.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // enum 이름이 아닌 경우 한글/영문으로 매핑 시도
        }
        
        // 한글 등급으로 매핑
        switch (value.trim()) {
            case "특":
            case "특급":
                return SPECIAL;
            case "상":
            case "1급":
            case "상급":
                return FIRST;
            case "중":
            case "2급":
            case "중급":
                return SECOND;
            case "하":
            case "3급":
            case "하급":
                return THIRD;
            default:
                return SECOND; // 알 수 없는 값은 중급으로 설정
        }
    }
    
    public static Grade fromShortName(String shortName) {
        for (Grade grade : values()) {
            if (grade.shortName.equals(shortName)) {
                return grade;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 등급입니다: " + shortName);
    }
    
    /**
     * 프롬프팅에 사용할 한글 등급명을 반환합니다.
     * @return 한글 등급명 ("특", "상", "중", "하", "프리미엄")
     */
    public String getKoreanName() {
        return this.shortName;
    }
    
    public boolean isHighGrade() {
        return this == SPECIAL || this == FIRST;
    }
} 