package org.fr.farmranding.dto.premium;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.pricing.KamisProductCode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * KAMIS 품목 코드 응답 DTO
 */
@Schema(description = "KAMIS 품목 코드 정보")
public record KamisProductCodeResponse(
        
        @Schema(description = "품목 ID", example = "1")
        Long id,
        
        @Schema(description = "품목 그룹 코드", example = "100")
        String groupCode,
        
        @Schema(description = "품목 그룹명", example = "식량작물")
        String groupName,
        
        @Schema(description = "품목 코드", example = "211")
        String itemCode,
        
        @Schema(description = "품목명", example = "쌀")
        String itemName,
        
        @Schema(description = "품종 코드", example = "01")
        String kindCode,
        
        @Schema(description = "품종명", example = "일반계")
        String kindName
) {
    
    /**
     * Entity를 DTO로 변환하는 정적 팩토리 메서드
     */
    public static KamisProductCodeResponse from(KamisProductCode entity) {
        return new KamisProductCodeResponse(
                entity.getId(),
                entity.getGroupCode(),
                entity.getGroupName(),
                entity.getItemCode(),
                entity.getItemName(),
                entity.getKindCode(),
                entity.getKindName()
        );
    }
    
    /**
     * Entity 리스트를 DTO 리스트로 변환하는 정적 팩토리 메서드
     */
    public static List<KamisProductCodeResponse> fromList(List<KamisProductCode> entities) {
        return entities.stream()
                .map(KamisProductCodeResponse::from)
                .collect(Collectors.toList());
    }
}

/**
 * 품목 그룹 정보 DTO
 */
@Schema(description = "KAMIS 품목 그룹 정보")
record KamisProductGroupResponse(
        
        @Schema(description = "그룹 코드", example = "100")
        String groupCode,
        
        @Schema(description = "그룹명", example = "식량작물")
        String groupName
) {}