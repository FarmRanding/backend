package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.entity.pricing.KamisProductCode;
import org.fr.farmranding.repository.KamisProductCodeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * KAMIS 품목 코드 CSV 파일을 DB에 초기화하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KamisCodeInitService implements CommandLineRunner {
    
    private final KamisProductCodeRepository kamisProductCodeRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 이미 데이터가 있으면 초기화 하지 않음
        if (kamisProductCodeRepository.count() > 0) {
            log.info("KAMIS 품목 코드 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }
        
        log.info("KAMIS 품목 코드 CSV 파일 초기화를 시작합니다.");
        
        try {
            ClassPathResource resource = new ClassPathResource("kamisCode.csv");
            List<KamisProductCode> codes = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), "UTF-8"))) {
                
                String line;
                int lineNum = 0;
                
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    
                    // 첫 번째 줄(헤더) 및 빈 줄 건너뛰기
                    if (lineNum <= 2 || line.trim().isEmpty()) {
                        continue;
                    }
                    
                    // CSV 파싱 (첫 번째 컬럼은 빈 값이므로 무시)
                    String[] columns = line.split(",");
                    if (columns.length < 7) {
                        log.warn("라인 {}: 컬럼 수가 부족합니다. 건너뜁니다: {}", lineNum, line);
                        continue;
                    }
                    
                    try {
                        String groupCode = columns[1].trim();
                        String groupName = columns[2].trim();
                        String itemCode = columns[3].trim();
                        String itemName = columns[4].trim();
                        String kindCode = columns[5].trim();
                        String kindName = columns[6].trim();
                        
                        // 빈 값 체크
                        if (groupCode.isEmpty() || groupName.isEmpty() || 
                            itemCode.isEmpty() || itemName.isEmpty() || 
                            kindCode.isEmpty() || kindName.isEmpty()) {
                            log.warn("라인 {}: 필수 값이 비어있습니다. 건너뜁니다: {}", lineNum, line);
                            continue;
                        }
                        
                        KamisProductCode code = KamisProductCode.builder()
                                .groupCode(groupCode)
                                .groupName(groupName)
                                .itemCode(itemCode)
                                .itemName(itemName)
                                .kindCode(kindCode)
                                .kindName(kindName)
                                .build();
                        
                        codes.add(code);
                        
                        // 배치로 저장 (1000개씩)
                        if (codes.size() >= 1000) {
                            kamisProductCodeRepository.saveAll(codes);
                            log.info("{}개 품목 코드 저장 완료", codes.size());
                            codes.clear();
                        }
                        
                    } catch (Exception e) {
                        log.error("라인 {} 처리 중 오류: {}, 라인: {}", lineNum, e.getMessage(), line);
                    }
                }
                
                // 남은 데이터 저장
                if (!codes.isEmpty()) {
                    kamisProductCodeRepository.saveAll(codes);
                    log.info("마지막 {}개 품목 코드 저장 완료", codes.size());
                }
            }
            
            long totalCount = kamisProductCodeRepository.count();
            log.info("KAMIS 품목 코드 초기화 완료. 총 {}개 품목 코드가 저장되었습니다.", totalCount);
            
        } catch (Exception e) {
            log.error("KAMIS 품목 코드 초기화 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
} 