package org.fr.farmranding.dto.membership;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.fr.farmranding.entity.user.MembershipType;

import java.math.BigDecimal;

@Schema(description = "멤버십 플랜 생성 요청 DTO")
public record MembershipPlanCreateRequest(
        
        @NotNull(message = "멤버십 타입은 필수입니다.")
        @Schema(description = "멤버십 타입", example = "PRO", required = true)
        MembershipType membershipType,
        
        @NotBlank(message = "플랜명은 필수입니다.")
        @Size(min = 2, max = 50, message = "플랜명은 2자 이상 50자 이하여야 합니다.")
        @Schema(description = "플랜명", example = "프로 플랜", required = true)
        String planName,
        
        @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
        @Schema(description = "플랜 설명", example = "모든 기능을 무제한으로 사용할 수 있는 프리미엄 플랜")
        String description,
        
        @NotNull(message = "월 가격은 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = true, message = "월 가격은 0 이상이어야 합니다.")
        @Schema(description = "월 가격", example = "29000", required = true)
        BigDecimal monthlyPrice,
        
        @DecimalMin(value = "0.0", inclusive = true, message = "연 가격은 0 이상이어야 합니다.")
        @Schema(description = "연 가격", example = "290000")
        BigDecimal yearlyPrice,
        
        @NotNull(message = "AI 브랜딩 한도는 필수입니다.")
        @Min(value = 0, message = "AI 브랜딩 한도는 0 이상이어야 합니다.")
        @Schema(description = "AI 브랜딩 한도", example = "999999", required = true)
        Integer aiBrandingLimit,
        
        @NotNull(message = "가격 제안 한도는 필수입니다.")
        @Min(value = 0, message = "가격 제안 한도는 0 이상이어야 합니다.")
        @Schema(description = "가격 제안 한도", example = "999999", required = true)
        Integer pricingSuggestionLimit,
        
        @NotNull(message = "프로젝트 저장 한도는 필수입니다.")
        @Min(value = 0, message = "프로젝트 저장 한도는 0 이상이어야 합니다.")
        @Schema(description = "프로젝트 저장 한도", example = "999999", required = true)
        Integer projectStorageLimit,
        
        @NotNull(message = "고급 분석 지원 여부는 필수입니다.")
        @Schema(description = "고급 분석 지원 여부", example = "true", required = true)
        Boolean advancedAnalytics,
        
        @NotNull(message = "우선 지원 여부는 필수입니다.")
        @Schema(description = "우선 지원 여부", example = "true", required = true)
        Boolean prioritySupport,
        
        @NotNull(message = "커스텀 브랜딩 지원 여부는 필수입니다.")
        @Schema(description = "커스텀 브랜딩 지원 여부", example = "true", required = true)
        Boolean customBranding,
        
        @NotNull(message = "API 접근 지원 여부는 필수입니다.")
        @Schema(description = "API 접근 지원 여부", example = "true", required = true)
        Boolean apiAccess,
        
        @NotNull(message = "내보내기 기능 지원 여부는 필수입니다.")
        @Schema(description = "내보내기 기능 지원 여부", example = "true", required = true)
        Boolean exportFeatures,
        
        @NotNull(message = "활성화 여부는 필수입니다.")
        @Schema(description = "활성화 여부", example = "true", required = true)
        Boolean isActive,
        
        @NotNull(message = "인기 플랜 여부는 필수입니다.")
        @Schema(description = "인기 플랜 여부", example = "false", required = true)
        Boolean isPopular,
        
        @NotNull(message = "정렬 순서는 필수입니다.")
        @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다.")
        @Schema(description = "정렬 순서", example = "1", required = true)
        Integer sortOrder
) {} 