package org.fr.farmranding.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.product.ProductCode;

import java.time.LocalDateTime;

@Schema(description = "품목 코드 응답 DTO")
public record ProductCodeResponse(
    @Schema(description = "품목 코드 ID", example = "1")
    Long id,
    
    @Schema(description = "가락시장 품목 코드", example = "73003")
    String garakCode,
    
    @Schema(description = "품목명", example = "가공 게지")
    String productName,
    
    @Schema(description = "활성화 여부", example = "true")
    Boolean isActive,
    
    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt,
    
    @Schema(description = "수정일시", example = "2024-01-15T10:30:00")
    LocalDateTime updatedAt
) {
    public static ProductCodeResponse from(ProductCode productCode) {
        return new ProductCodeResponse(
                productCode.getId(),
                productCode.getGarakCode(),
                productCode.getProductName(),
                productCode.getIsActive(),
                productCode.getCreatedAt(),
                productCode.getUpdatedAt()
        );
    }
} 