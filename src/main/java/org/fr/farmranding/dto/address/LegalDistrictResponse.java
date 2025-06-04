package org.fr.farmranding.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fr.farmranding.entity.address.LegalDistrict;

@Schema(description = "법정동 응답 DTO")
public record LegalDistrictResponse(
    @Schema(description = "법정동 코드", example = "1111010100")
    String code,
    
    @Schema(description = "시도명", example = "서울특별시")
    String sido,
    
    @Schema(description = "시군구명", example = "종로구")
    String sigungu,
    
    @Schema(description = "읍면동명", example = "청운동")
    String dong,
    
    @Schema(description = "리명", example = "")
    String ri,
    
    @Schema(description = "전체 주소", example = "서울특별시 종로구 청운동")
    String fullAddress
) {
    public static LegalDistrictResponse of(String code, String sido, String sigungu, String dong, String ri) {
        // 전체 주소 조합
        StringBuilder fullAddressBuilder = new StringBuilder();
        if (sido != null && !sido.trim().isEmpty()) {
            fullAddressBuilder.append(sido);
        }
        if (sigungu != null && !sigungu.trim().isEmpty()) {
            fullAddressBuilder.append(" ").append(sigungu);
        }
        if (dong != null && !dong.trim().isEmpty()) {
            fullAddressBuilder.append(" ").append(dong);
        }
        if (ri != null && !ri.trim().isEmpty()) {
            fullAddressBuilder.append(" ").append(ri);
        }
        
        return new LegalDistrictResponse(
            code,
            sido,
            sigungu,
            dong,
            ri,
            fullAddressBuilder.toString().trim()
        );
    }
    
    /**
     * 엔티티에서 DTO로 변환
     */
    public static LegalDistrictResponse from(LegalDistrict entity) {
        return new LegalDistrictResponse(
            entity.getDistrictCode(),
            entity.getSido(),
            entity.getSigungu(),
            entity.getDong(),
            entity.getRi(),
            entity.getFullAddress()
        );
    }
} 