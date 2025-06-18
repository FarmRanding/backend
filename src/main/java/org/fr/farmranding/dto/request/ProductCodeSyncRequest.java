package org.fr.farmranding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "품목 코드 동기화 요청 DTO")
public record ProductCodeSyncRequest(
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 1000, message = "페이지 크기는 1000 이하여야 합니다.")
    @Schema(description = "한 번에 가져올 데이터 수", example = "703", defaultValue = "703")
    Integer pageSize,
    
    @Min(value = 1, message = "페이지 인덱스는 1 이상이어야 합니다.")
    @Schema(description = "페이지 인덱스", example = "1", defaultValue = "1")
    Integer pageIndex,
    
    @Schema(description = "강제 업데이트 여부", example = "false", defaultValue = "false")
    Boolean forceUpdate
) {
    public ProductCodeSyncRequest {
        if (pageSize == null) pageSize = 703;
        if (pageIndex == null) pageIndex = 1;
        if (forceUpdate == null) forceUpdate = false;
    }
} 