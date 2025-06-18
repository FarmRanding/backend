package org.fr.farmranding.dto.gap;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * GAP 인증 정보 응답 DTO
 */
@Schema(description = "GAP(농산물우수관리) 인증 정보")
public record GapCertificationResponse(
        @Schema(description = "GAP 인증번호", example = "1001267")
        String certificationNumber,
        
        @Schema(description = "인증기관명", example = "글로벌유농인")
        String certificationInstitution,
        
        @Schema(description = "인증기관 코드", example = "C000050176")
        String institutionCode,
        
        @Schema(description = "개인/단체 구분", example = "개인")
        String individualGroupType,
        
        @Schema(description = "생산자단체명", example = "가교버섯영농조합법인")
        String producerGroupName,
        
        @Schema(description = "유효기간 시작일", example = "2023-06-12")
        LocalDate validPeriodStart,
        
        @Schema(description = "유효기간 종료일", example = "2025-06-11")
        LocalDate validPeriodEnd,
        
        @Schema(description = "품목명", example = "토마토")
        String productName,
        
        @Schema(description = "품목코드", example = "080300")
        String productCode,
        
        @Schema(description = "등록 농가 수", example = "1")
        Integer registeredFarmCount,
        
        @Schema(description = "등록 필지 수", example = "2")
        Integer registeredLotCount,
        
        @Schema(description = "재배면적(㎡)", example = "1500.5")
        Double cultivationArea,
        
        @Schema(description = "생산계획량(kg)", example = "3000.0")
        Double productionPlanQuantity,
        
        @Schema(description = "지정 일자", example = "2023-06-12")
        LocalDate designationDate,
        
        @Schema(description = "인증 유효 여부", example = "true")
        boolean isValid
) {
    /**
     * 현재 날짜 기준으로 인증이 유효한지 확인
     */
    public boolean isCurrentlyValid() {
        LocalDate now = LocalDate.now();
        return validPeriodStart != null && validPeriodEnd != null &&
               !now.isBefore(validPeriodStart) && !now.isAfter(validPeriodEnd);
    }
    
    /**
     * 만료일까지 남은 일수 계산
     */
    public long getDaysUntilExpiry() {
        if (validPeriodEnd == null) {
            return -1;
        }
        return LocalDate.now().until(validPeriodEnd).getDays();
    }
} 