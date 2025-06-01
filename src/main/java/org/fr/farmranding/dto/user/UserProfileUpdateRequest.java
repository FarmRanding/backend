package org.fr.farmranding.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "사용자 프로필 수정 요청 DTO")
public record UserProfileUpdateRequest(
        
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
        @Schema(description = "닉네임", example = "농부김씨", required = true)
        String nickname,
        
        @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
        @Schema(description = "이름", example = "김농부")
        String name,
        
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImage,
        
        @Size(max = 50, message = "농장명은 50자 이하여야 합니다.")
        @Schema(description = "농장명", example = "김씨농장")
        String farmName,
        
        @Size(max = 100, message = "위치는 100자 이하여야 합니다.")
        @Schema(description = "위치", example = "경기도 안산시")
        String location,
        
        @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,
        
        @Size(max = 500, message = "농장 소개는 500자 이하여야 합니다.")
        @Schema(description = "농장 소개", example = "3대째 이어온 유기농 농장입니다.")
        String farmDescription,
        
        @Min(value = 1900, message = "설립년도는 1900년 이후여야 합니다.")
        @Max(value = 2024, message = "설립년도는 현재 년도를 초과할 수 없습니다.")
        @Schema(description = "농장 설립년도", example = "1995")
        Integer establishedYear
) {} 