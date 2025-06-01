package org.fr.farmranding.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.user.MembershipType;
import org.fr.farmranding.entity.user.User;

import java.time.LocalDateTime;

@Schema(description = "사용자 프로필 응답 DTO")
public record UserProfileResponse(
        
        @Schema(description = "사용자 ID", example = "1")
        Long id,
        
        @Schema(description = "이메일", example = "user@example.com")
        String email,
        
        @Schema(description = "이름", example = "김농부")
        String name,
        
        @Schema(description = "멤버십 타입", example = "FREE")
        MembershipType membershipType,
        
        @Schema(description = "농가명", example = "김씨농장")
        String farmName,
        
        @Schema(description = "위치", example = "경기도 안산시")
        String location,

        @Schema(description = "가입일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getMembershipType(),
                user.getFarmName(),
                user.getLocation(),
                user.getCreatedAt()
        );
    }
} 