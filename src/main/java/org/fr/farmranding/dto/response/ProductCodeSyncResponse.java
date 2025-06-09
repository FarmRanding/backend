package org.fr.farmranding.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "품목 코드 동기화 응답 DTO")
public record ProductCodeSyncResponse(
    @Schema(description = "전체 데이터 수", example = "703")
    Integer totalCount,
    
    @Schema(description = "신규 추가된 품목 수", example = "10")
    Integer newCount,
    
    @Schema(description = "업데이트된 품목 수", example = "5")
    Integer updatedCount,
    
    @Schema(description = "비활성화된 품목 수", example = "2")
    Integer deactivatedCount,
    
    @Schema(description = "동기화 완료 시간", example = "2024-01-15T10:30:00")
    LocalDateTime syncedAt,
    
    @Schema(description = "동기화 소요 시간(밀리초)", example = "1500")
    Long processingTimeMs
) {
    public static ProductCodeSyncResponse of(
            Integer totalCount, 
            Integer newCount, 
            Integer updatedCount, 
            Integer deactivatedCount,
            LocalDateTime syncedAt,
            Long processingTimeMs) {
        return new ProductCodeSyncResponse(
                totalCount,
                newCount,
                updatedCount,
                deactivatedCount,
                syncedAt,
                processingTimeMs
        );
    }
} 