package org.fr.farmranding.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.dto.request.ProductCodeSyncRequest;
import org.fr.farmranding.dto.response.ProductCodeSyncResponse;
import org.fr.farmranding.repository.ProductCodeRepository;
import org.fr.farmranding.service.ProductCodeService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCodeInitializationService implements ApplicationRunner {
    
    private final ProductCodeRepository productCodeRepository;
    private final ProductCodeService productCodeService;
    
    @Override
    public void run(ApplicationArguments args) {
        try {
            initializeProductCodes();
        } catch (Exception e) {
            log.error("품목 코드 초기화 중 오류가 발생했지만 애플리케이션은 계속 실행됩니다.", e);
        }
    }
    
    private void initializeProductCodes() {
        log.info("품목 코드 초기화를 시작합니다...");
        
        // 기존 데이터 확인
        long existingCount = productCodeRepository.count();
        
        if (existingCount > 0) {
            log.info("기존 품목 코드 {}개가 존재합니다. 초기화를 스킵합니다.", existingCount);
            return;
        }
        
        log.info("품목 코드가 존재하지 않습니다. 가락시장 API에서 동기화를 시작합니다...");
        
        try {
            ProductCodeSyncRequest request = new ProductCodeSyncRequest(703, 1, false);
            ProductCodeSyncResponse response = productCodeService.syncProductCodes(request);
            
            log.info("품목 코드 초기화 완료! 전체: {}개, 신규: {}개, 업데이트: {}개, 소요시간: {}ms",
                    response.totalCount(),
                    response.newCount(),
                    response.updatedCount(),
                    response.processingTimeMs());
                    
        } catch (Exception e) {
            log.error("품목 코드 초기화 중 오류 발생. 나중에 수동으로 /api/v1/product-codes/sync API를 호출해주세요.", e);
        }
    }
} 