package org.fr.farmranding.common.validator;

import lombok.RequiredArgsConstructor;
import org.fr.farmranding.common.code.FarmrandingResponseCode;
import org.fr.farmranding.common.exception.BusinessException;
import org.fr.farmranding.dto.request.ProductCodeSyncRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductCodeValidator {
    
    @Transactional(readOnly = true)
    public void validateSyncRequest(ProductCodeSyncRequest request) {
        if (request.pageSize() != null && (request.pageSize() < 1 || request.pageSize() > 1000)) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
        
        if (request.pageIndex() != null && request.pageIndex() < 1) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
    }
    
    @Transactional(readOnly = true)
    public void validateGarakCode(String garakCode) {
        if (garakCode == null || garakCode.trim().isEmpty()) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
        
        // 가락시장 코드는 일반적으로 숫자 형태
        if (!garakCode.matches("\\d+")) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
    }
    
    @Transactional(readOnly = true)
    public void validateSearchKeyword(String keyword) {
        if (keyword != null && keyword.trim().length() > 100) {
            throw new BusinessException(FarmrandingResponseCode.INVALID_INPUT);
        }
    }
} 