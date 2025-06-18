package org.fr.farmranding.service;

import org.fr.farmranding.dto.gap.GapCertificationResponse;
import org.fr.farmranding.dto.gap.GapSearchRequest;

/**
 * GAP(농산물우수관리) 인증 정보 조회 서비스
 * 농산물우수관리(GAP) 인증정보 OpenAPI를 연동하여 인증 정보를 조회
 */
public interface GapCertificationService {
    
    /**
     * GAP 인증 정보 검색
     */
    GapCertificationResponse searchGapCertification(GapSearchRequest request);
    
    /**
     * GAP 인증번호 형식 검증
     */
    boolean validateGapCertificationNumber(String certificationNumber);
    
    /**
     * GAP 인증번호 검증 및 정보 조회 (통합)
     */
    GapCertificationResponse validateAndSearchGapCertification(String certificationNumber);
} 