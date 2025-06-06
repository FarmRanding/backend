package org.fr.farmranding.dto.branding;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import java.util.List;

@Schema(description = "브랜드명 생성 요청 DTO")
public record BrandNameRequest(
    @NotBlank(message = "작물명은 필수입니다.")
    @Schema(description = "작물명", example = "사과", required = true)
    String cropName,

    @Schema(description = "품종", example = "홍로")
    String variety,

    @NotEmpty(message = "브랜딩 키워드는 최소 1개 이상 선택해야 합니다.")
    @Size(max = 10, message = "브랜딩 키워드는 최대 10개까지 선택할 수 있습니다.")
    @Schema(description = "브랜드 이미지 키워드 목록", example = "[\"프리미엄\", \"건강한\", \"신선한\"]", required = true)
    List<String> brandingKeywords,

    @Size(max = 10, message = "작물 매력 키워드는 최대 10개까지 선택할 수 있습니다.")
    @Schema(description = "작물의 매력 키워드 목록", example = "[\"달콤한\", \"아삭한\", \"과즙이 풍부한\"]")
    List<String> cropAppealKeywords,
    
    @Size(max = 20, message = "이전 브랜드명은 최대 20개까지 전송할 수 있습니다.")
    @Schema(description = "이전에 생성된 브랜드명 목록 (중복 방지용)", example = "[\"달콤사과\", \"프리미엄사과\"]")
    List<String> previousBrandNames,
    
    @Min(value = 0, message = "재생성 횟수는 0 이상이어야 합니다.")
    @Schema(description = "현재까지의 재생성 횟수", example = "1")
    Integer regenerationCount
) {} 