package org.fr.farmranding.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.user.MembershipType;
import org.fr.farmranding.entity.user.User;

import java.time.LocalDateTime;

@Schema(description = "사용자 정보 응답 DTO")
public record UserResponse(
    @Schema(description = "사용자 ID", example = "1")
    Long id,
    
    @Schema(description = "이메일", example = "user@example.com")
    String email,
    
    @Schema(description = "이름", example = "김농부")
    String name,
    
    @Schema(description = "멤버십 타입")
    MembershipType membershipType,
    
    @Schema(description = "농장명", example = "행복한 토마토 농장")
    String farmName,
    
    @Schema(description = "농장 위치", example = "경기도 이천시")
    String location,
    
    @Schema(description = "생성일시")
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
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