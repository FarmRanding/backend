package org.fr.farmranding.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.user.MembershipType;
import org.fr.farmranding.entity.user.User;

@Schema(description = "사용자 사용량 조회 응답 DTO")
public record UserUsageResponse(
        
        @Schema(description = "멤버십 타입", example = "FREE")
        MembershipType membershipType,
        
        @Schema(description = "AI 브랜딩 사용 횟수", example = "3")
        Integer aiBrandingUsageCount,
        
        @Schema(description = "AI 브랜딩 사용 한도", example = "5")
        Integer aiBrandingLimit,
        
        @Schema(description = "가격 제안 사용 횟수", example = "2")
        Integer pricingSuggestionUsageCount,
        
        @Schema(description = "가격 제안 사용 한도", example = "10")
        Integer pricingSuggestionLimit,
        
        @Schema(description = "AI 브랜딩 사용 가능 여부", example = "true")
        Boolean canUseAiBranding,
        
        @Schema(description = "가격 제안 사용 가능 여부", example = "true")
        Boolean canUsePricingSuggestion,
        
        @Schema(description = "AI 브랜딩 남은 횟수", example = "2")
        Integer remainingAiBrandingCount,
        
        @Schema(description = "가격 제안 남은 횟수", example = "8")
        Integer remainingPricingSuggestionCount
) {
    public static UserUsageResponse from(User user) {
        int aiBrandingLimit = user.getMembershipType().getAiBrandingLimit();
        int pricingLimit = user.getMembershipType().getPricingSuggestionLimit();
        
        return new UserUsageResponse(
                user.getMembershipType(),
                user.getAiBrandingUsageCount(),
                aiBrandingLimit,
                user.getPricingSuggestionUsageCount(),
                pricingLimit,
                user.canUseAiBranding(),
                user.canUsePricingSuggestion(),
                Math.max(0, aiBrandingLimit - user.getAiBrandingUsageCount()),
                Math.max(0, pricingLimit - user.getPricingSuggestionUsageCount())
        );
    }
} 