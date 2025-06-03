package org.fr.farmranding.dto.membership;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "멤버십 플랜 수정 요청 DTO")
public record MembershipPlanUpdateRequest(
        
        @Size(min = 2, max = 50, message = "플랜명은 2자 이상 50자 이하여야 합니다.")
        @Schema(description = "플랜명", example = "프로 플랜")
        String planName,
        
        @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
        @Schema(description = "플랜 설명", example = "모든 기능을 무제한으로 사용할 수 있는 프리미엄 플랜")
        String description,
        
        @DecimalMin(value = "0.0", inclusive = true, message = "월 가격은 0 이상이어야 합니다.")
        @Schema(description = "월 가격", example = "29000")
        BigDecimal monthlyPrice,
        
        @DecimalMin(value = "0.0", inclusive = true, message = "연 가격은 0 이상이어야 합니다.")
        @Schema(description = "연 가격", example = "290000")
        BigDecimal yearlyPrice,
        
        @Min(value = 0, message = "AI 브랜딩 한도는 0 이상이어야 합니다.")
        @Schema(description = "AI 브랜딩 한도", example = "999999")
        Integer aiBrandingLimit,
        
        @Min(value = 0, message = "가격 제안 한도는 0 이상이어야 합니다.")
        @Schema(description = "가격 제안 한도", example = "999999")
        Integer pricingSuggestionLimit,
        
        @Min(value = 0, message = "프로젝트 저장 한도는 0 이상이어야 합니다.")
        @Schema(description = "프로젝트 저장 한도", example = "999999")
        Integer projectStorageLimit,
        
        @Schema(description = "고급 분석 지원 여부", example = "true")
        Boolean advancedAnalytics,
        
        @Schema(description = "우선 지원 여부", example = "true")
        Boolean prioritySupport,
        
        @Schema(description = "커스텀 브랜딩 지원 여부", example = "true")
        Boolean customBranding,
        
        @Schema(description = "API 접근 지원 여부", example = "true")
        Boolean apiAccess,
        
        @Schema(description = "내보내기 기능 지원 여부", example = "true")
        Boolean exportFeatures,
        
        @Schema(description = "활성화 여부", example = "true")
        Boolean isActive,
        
        @Schema(description = "인기 플랜 여부", example = "false")
        Boolean isPopular,
        
        @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다.")
        @Schema(description = "정렬 순서", example = "1")
        Integer sortOrder
) {} 