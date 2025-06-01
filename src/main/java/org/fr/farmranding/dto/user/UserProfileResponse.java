package org.fr.farmranding.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.user.MembershipType;
import org.fr.farmranding.entity.user.SocialProvider;
import org.fr.farmranding.entity.user.User;

import java.time.LocalDateTime;

@Schema(description = "사용자 프로필 응답 DTO")
public record UserProfileResponse(
        
        @Schema(description = "사용자 ID", example = "1")
        Long id,
        
        @Schema(description = "이메일", example = "user@example.com")
        String email,
        
        @Schema(description = "닉네임", example = "농부김씨")
        String nickname,
        
        @Schema(description = "이름", example = "김농부")
        String name,
        
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImage,
        
        @Schema(description = "소셜 로그인 제공자", example = "KAKAO")
        SocialProvider provider,
        
        @Schema(description = "멤버십 타입", example = "FREE")
        MembershipType membershipType,
        
        @Schema(description = "농장명", example = "김씨농장")
        String farmName,
        
        @Schema(description = "위치", example = "경기도 안산시")
        String location,
        
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,
        
        @Schema(description = "농장 소개", example = "3대째 이어온 유기농 농장입니다.")
        String farmDescription,
        
        @Schema(description = "농장 설립년도", example = "1995")
        Integer establishedYear,
        
        @Schema(description = "가입일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getProfileImage(),
                user.getProvider(),
                user.getMembershipType(),
                user.getFarmName(),
                user.getLocation(),
                user.getPhoneNumber(),
                user.getFarmDescription(),
                user.getEstablishedYear(),
                user.getCreatedAt()
        );
    }
} 