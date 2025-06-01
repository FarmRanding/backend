package org.fr.farmranding.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "사용자 프로필 수정 요청 DTO")
public record UserProfileUpdateRequest(
        
        @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
        @Schema(description = "이름", example = "김농부")
        String name,
        
        @Size(max = 50, message = "농가명은 50자 이하여야 합니다.")
        @Schema(description = "농가명", example = "김씨농장")
        String farmName,
        
        @Size(max = 100, message = "위치는 100자 이하여야 합니다.")
        @Schema(description = "위치", example = "경기도 안산시")
        String location

) {} 