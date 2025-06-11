package org.fr.farmranding.service;

import org.fr.farmranding.dto.pricequote.PriceDataRequest;
import org.fr.farmranding.dto.pricequote.PriceDataResponse;

public interface GarakPriceService {
    
    /**
     * 가락시장 가격 정보 조회
     * 
     * @param request 가격 조회 요청 정보
     * @return 5년간 가격 데이터 및 평균가격
     */
    PriceDataResponse getPriceData(PriceDataRequest request);
} 