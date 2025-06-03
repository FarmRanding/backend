package org.fr.farmranding.entity.branding;

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
    
    public static Grade fromShortName(String shortName) {
        for (Grade grade : values()) {
            if (grade.shortName.equals(shortName)) {
                return grade;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 등급입니다: " + shortName);
    }
    
    public boolean isHighGrade() {
        return this == SPECIAL || this == FIRST;
    }
} 