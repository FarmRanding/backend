package org.fr.farmranding.service;

import org.fr.farmranding.dto.request.ProductCodeSyncRequest;
import org.fr.farmranding.dto.response.ProductCodeResponse;
import org.fr.farmranding.dto.response.ProductCodeSyncResponse;

import java.util.List;

public interface ProductCodeService {
    
    /**
     * 가락시장 API로부터 품목 코드를 동기화합니다.
     */
    ProductCodeSyncResponse syncProductCodes(ProductCodeSyncRequest request);
    
    /**
     * 모든 활성 품목 코드를 조회합니다.
     */
    List<ProductCodeResponse> getAllActiveProductCodes();
    
    /**
     * 키워드로 품목 코드를 검색합니다.
     */
    List<ProductCodeResponse> searchProductCodes(String keyword);
    
    /**
     * 가락시장 코드로 품목 코드를 조회합니다.
     */
    ProductCodeResponse getProductCodeByGarakCode(String garakCode);
    
    /**
     * 활성 품목 코드 총 개수를 조회합니다.
     */
    long getActiveProductCodeCount();
} 