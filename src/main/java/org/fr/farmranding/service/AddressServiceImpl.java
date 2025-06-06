package org.fr.farmranding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fr.farmranding.config.AddressProperties;
import org.fr.farmranding.dto.address.LegalDistrictResponse;
import org.fr.farmranding.entity.address.LegalDistrict;
import org.fr.farmranding.repository.LegalDistrictRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final LegalDistrictRepository legalDistrictRepository;
    private final AddressProperties addressProperties;

    @PostConstruct
    public void initializeData() {
        long count = legalDistrictRepository.count();
        if (count == 0) {
            log.info("법정동 데이터가 없습니다. CSV 파일에서 초기 데이터를 로드합니다.");
            loadLegalDistrictsFromCsvToDb();
        } else {
            log.info("법정동 데이터가 이미 존재합니다. 총 {}개", count);
        }
    }

    /**
     * CSV 파일에서 DB로 초기 데이터 마이그레이션 (classpath 리소스 사용)
     */
    private void loadLegalDistrictsFromCsvToDb() {
        // resources/ 아래에 위치한 파일명을 가져옴
        String csvFileName = addressProperties.getCsvFilename();
        ClassPathResource resource = new ClassPathResource(csvFileName);

        if (!resource.exists()) {
            log.error("CSV 파일을 찾을 수 없습니다 (classpath): {}", csvFileName);
            return;
        }

        log.info("법정동 CSV 파일 DB 마이그레이션 시작 (classpath): {}", csvFileName);

        List<LegalDistrict> batchList = new ArrayList<>();
        int batchSize = 1000;
        int totalCount = 0;
        int skipCount = 0;

        // ClassPathResource로 InputStream을 얻고, 명시적으로 UTF-8 인코딩 지정
        try (InputStream is = resource.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // 헤더 스킵
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length >= 4) {
                    String code = fields[0].trim();
                    String sido = fields[1].trim();
                    String sigungu = fields[2].trim();
                    String dong = fields[3].trim();
                    String ri = fields.length > 4 ? fields[4].trim() : "";

                    // 삭제된 데이터 제외 (삭제일자가 있는 경우)
                    if (fields.length > 7 && !fields[7].trim().isEmpty()) {
                        continue;
                    }

                    // 의미있는 데이터만 추가 (시도만 있는 데이터 제외)
                    if (!sigungu.isEmpty() || !dong.isEmpty()) {
                        // 중복 체크
                        if (legalDistrictRepository.existsByDistrictCode(code)) {
                            skipCount++;
                            continue;
                        }

                        LegalDistrict district = LegalDistrict.builder()
                                .districtCode(code)
                                .sido(sido)
                                .sigungu(sigungu.isEmpty() ? null : sigungu)
                                .dong(dong.isEmpty() ? null : dong)
                                .ri(ri.isEmpty() ? null : ri)
                                .build();

                        batchList.add(district);
                        totalCount++;

                        // 배치 저장
                        if (batchList.size() >= batchSize) {
                            try {
                                legalDistrictRepository.saveAll(batchList);
                                log.info("배치 저장 완료: {}개 처리됨, {}개 중복 스킵", totalCount, skipCount);
                            } catch (Exception e) {
                                log.error("배치 저장 실패: {}", e.getMessage());
                                // 개별 저장 시도
                                for (LegalDistrict d : batchList) {
                                    try {
                                        if (!legalDistrictRepository.existsByDistrictCode(d.getDistrictCode())) {
                                            legalDistrictRepository.save(d);
                                        }
                                    } catch (Exception ex) {
                                        log.warn("개별 저장 실패 - 코드: {}, 에러: {}", d.getDistrictCode(), ex.getMessage());
                                    }
                                }
                            }
                            batchList.clear();
                        }
                    }
                }
            }

            // 남은 데이터 저장
            if (!batchList.isEmpty()) {
                try {
                    legalDistrictRepository.saveAll(batchList);
                } catch (Exception e) {
                    log.error("최종 배치 저장 실패: {}", e.getMessage());
                    // 개별 저장 시도
                    for (LegalDistrict d : batchList) {
                        try {
                            if (!legalDistrictRepository.existsByDistrictCode(d.getDistrictCode())) {
                                legalDistrictRepository.save(d);
                            }
                        } catch (Exception ex) {
                            log.warn("최종 개별 저장 실패 - 코드: {}, 에러: {}", d.getDistrictCode(), ex.getMessage());
                        }
                    }
                }
            }

            log.info("법정동 데이터 DB 마이그레이션 완료: 총 {}개 저장, {}개 중복 스킵", totalCount, skipCount);

        } catch (IOException e) {
            log.error("CSV 파일 읽기 실패 (classpath): {}", csvFileName, e);
        }
    }

    /**
     * 최적화된 법정동 검색: 3단계 검색 전략
     * 1단계: 정확 일치 (커버링 인덱스 활용)
     * 2단계: 접두사 매칭 (커버링 인덱스 활용) 
     * 3단계: Full-Text 검색 (ngram 인덱스 활용)
     */
    @Override
    public List<LegalDistrictResponse> searchLegalDistricts(String keyword, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchKeyword = keyword.trim();
        Set<LegalDistrict> resultSet = new LinkedHashSet<>();
        
        log.debug("법정동 검색 시작: keyword='{}', limit={}", searchKeyword, limit);
        
        // 1단계: 정확 일치 검색 (가장 빠른 검색)
        try {
            Pageable pageExact = PageRequest.of(0, limit);
            List<LegalDistrict> exactMatches = legalDistrictRepository.findExactMatch(searchKeyword, pageExact);
            resultSet.addAll(exactMatches);
            
            log.debug("1단계 정확 일치 검색 완료: {}개 결과", exactMatches.size());
        } catch (Exception e) {
            log.warn("1단계 정확 일치 검색 실패: {}", e.getMessage());
        }
        
        // 2단계: 접두사 매칭 검색 (아직 결과가 부족하면)
        if (resultSet.size() < limit) {
            try {
                int remainingCount = limit - resultSet.size();
                Pageable pagePrefix = PageRequest.of(0, remainingCount);
                List<LegalDistrict> prefixMatches = legalDistrictRepository.findPrefixMatch(searchKeyword, pagePrefix);
                
                for (LegalDistrict district : prefixMatches) {
                    if (resultSet.size() >= limit) break;
                    resultSet.add(district);
                }
                
                log.debug("2단계 접두사 매칭 검색 완료: {}개 추가 (총 {}개)", 
                    prefixMatches.size(), resultSet.size());
            } catch (Exception e) {
                log.warn("2단계 접두사 매칭 검색 실패: {}", e.getMessage());
            }
        }
        
        // 3단계: 부분 매칭 검색 (여전히 결과가 부족하면)
        if (resultSet.size() < limit) {
            try {
                int remainingCount = limit - resultSet.size();
                List<LegalDistrict> fallbackMatches = legalDistrictRepository
                    .searchByKeywordFallback(searchKeyword, remainingCount);
                
                for (LegalDistrict district : fallbackMatches) {
                    if (resultSet.size() >= limit) break;
                    resultSet.add(district);
                }
                
                log.debug("3단계 부분 매칭 검색 완료: {}개 추가 (총 {}개)", 
                    fallbackMatches.size(), resultSet.size());
            } catch (Exception e) {
                log.warn("3단계 부분 매칭 검색 실패, 기본 레거시 검색으로 대체: {}", e.getMessage());
                
                // 최후의 수단으로 기본 레거시 검색
                try {
                    int remainingCount = limit - resultSet.size();
                    Pageable pageable = PageRequest.of(0, remainingCount);
                    List<LegalDistrict> legacyMatches = legalDistrictRepository
                        .searchByKeywordWithRelevance(searchKeyword, pageable);
                    
                    for (LegalDistrict district : legacyMatches) {
                        if (resultSet.size() >= limit) break;
                        resultSet.add(district);
                    }
                    
                    log.debug("기본 레거시 검색 완료: {}개 추가 (총 {}개)", 
                        legacyMatches.size(), resultSet.size());
                } catch (Exception legacyEx) {
                    log.error("모든 검색 방법 실패: {}", legacyEx.getMessage());
                }
            }
        }
        
        // 최종 결과를 DTO로 변환
        List<LegalDistrictResponse> responseList = new ArrayList<>();
        int count = 0;
        for (LegalDistrict district : resultSet) {
            if (count++ >= limit) break;
            responseList.add(LegalDistrictResponse.from(district));
        }
        
        log.debug("법정동 검색 완료: 최종 {}개 결과 반환", responseList.size());
        return responseList;
    }

    /**
     * 빠른 시도별 검색 (최적화된 정확 일치)
     */
    public List<LegalDistrictResponse> getDistrictsBySido(String sido, int limit) {
        if (sido == null || sido.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String searchSido = sido.trim();
        Pageable pageable = PageRequest.of(0, limit);
        
        // 먼저 정확 일치로 시도
        List<LegalDistrict> districts = legalDistrictRepository.findBySidoExact(searchSido, pageable);
        
        // 정확 일치 결과가 없으면 부분 일치로 시도
        if (districts.isEmpty()) {
            districts = legalDistrictRepository.findBySidoContainingOrderByFullAddress(sido, pageable);
        }

        return districts.stream()
                .map(LegalDistrictResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 법정동 코드로 단일 검색 (최적화)
     */
    public LegalDistrictResponse getDistrictByCode(String districtCode) {
        if (districtCode == null || districtCode.trim().isEmpty()) {
            return null;
        }
        
        LegalDistrict district = legalDistrictRepository.findByDistrictCode(districtCode.trim());
        return district != null ? LegalDistrictResponse.from(district) : null;
    }
    
    /**
     * 시도 + 시군구 조합 검색 (최적화)
     */
    public List<LegalDistrictResponse> getDistrictsBySidoAndSigungu(String sido, String sigungu, int limit) {
        if (sido == null || sido.trim().isEmpty() || sigungu == null || sigungu.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        Pageable pageable = PageRequest.of(0, limit);
        List<LegalDistrict> districts = legalDistrictRepository
            .findBySidoAndSigunguExact(sido.trim(), sigungu.trim(), pageable);
        
        return districts.stream()
                .map(LegalDistrictResponse::from)
                .collect(Collectors.toList());
    }
}
