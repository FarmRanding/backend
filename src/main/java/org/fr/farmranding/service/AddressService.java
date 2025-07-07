package org.fr.farmranding.service;

import org.fr.farmranding.dto.address.LegalDistrictResponse;

import java.util.List;

public interface AddressService {
    
    /**
     * 키워드로 법정동 검색
     * @param keyword 검색 키워드 (시도명, 시군구명, 읍면동명 포함)
     * @param limit 최대 결과 수 (기본 20개)
     * @return 검색된 법정동 목록
     */
    List<LegalDistrictResponse> searchLegalDistricts(String keyword, int limit);
} 