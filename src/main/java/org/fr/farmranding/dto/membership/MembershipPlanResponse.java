package org.fr.farmranding.dto.membership;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.membership.MembershipPlan;
import org.fr.farmranding.entity.user.MembershipType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "멤버십 플랜 응답 DTO")
public record MembershipPlanResponse(
        
        @Schema(description = "플랜 ID", example = "1")
        Long id,
        
        @Schema(description = "멤버십 타입", example = "PRO")
        MembershipType membershipType,
        
        @Schema(description = "플랜명", example = "프로 플랜")
        String planName,
        
        @Schema(description = "플랜 설명", example = "모든 기능을 무제한으로 사용할 수 있는 프리미엄 플랜")
        String description,
        
        @Schema(description = "월 가격", example = "29000")
        BigDecimal monthlyPrice,
        
        @Schema(description = "연 가격", example = "290000")
        BigDecimal yearlyPrice,
        
        @Schema(description = "연간 할인 금액", example = "58000")
        BigDecimal yearlyDiscount,
        
        @Schema(description = "연간 할인율 (%)", example = "20")
        Integer yearlyDiscountPercentage,
        
        @Schema(description = "AI 브랜딩 한도", example = "999999")
        Integer aiBrandingLimit,
        
        @Schema(description = "가격 제안 한도", example = "999999")
        Integer pricingSuggestionLimit,
        
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
        
        @Schema(description = "정렬 순서", example = "1")
        Integer sortOrder,
        
        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
        
        @Schema(description = "수정일시", example = "2024-01-15T15:20:00")
        LocalDateTime updatedAt
) {
    public static MembershipPlanResponse from(MembershipPlan plan) {
        return new MembershipPlanResponse(
                plan.getId(),
                plan.getMembershipType(),
                plan.getPlanName(),
                plan.getDescription(),
                plan.getMonthlyPrice(),
                plan.getYearlyPrice(),
                plan.getDiscountedYearlyPrice(),
                plan.getYearlyDiscountPercentage(),
                plan.getAiBrandingLimit(),
                plan.getPricingSuggestionLimit(),
                plan.getProjectStorageLimit(),
                plan.getAdvancedAnalytics(),
                plan.getPrioritySupport(),
                plan.getCustomBranding(),
                plan.getApiAccess(),
                plan.getExportFeatures(),
                plan.getIsActive(),
                plan.getIsPopular(),
                plan.getSortOrder(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
} 