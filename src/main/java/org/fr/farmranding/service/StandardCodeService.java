package org.fr.farmranding.service;

import org.fr.farmranding.dto.response.CropResponse;
import org.fr.farmranding.dto.response.VarietyResponse;

import java.util.List;

public interface StandardCodeService {
    
    /**
     * 공공API에서 전체 표준코드 데이터를 가져와 DB에 저장
     */
    void syncStandardCodeData();
    
    /**
     * 작물 검색 (MCLASSNAME 기준)
     */
    List<CropResponse> searchCrops(String query, int limit);
    
    /**
     * 인기 작물 조회
     */
    List<CropResponse> getPopularCrops(int limit);
    
    /**
     * 특정 작물의 품종 검색 (SCLASSNAME 기준)
     */
    List<VarietyResponse> searchVarieties(String cropCode, String query, int limit);
} 