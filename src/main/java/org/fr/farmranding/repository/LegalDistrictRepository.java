package org.fr.farmranding.repository;

import org.fr.farmranding.entity.address.LegalDistrict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LegalDistrictRepository extends JpaRepository<LegalDistrict, Long> {
    
    // ===== 최적화된 3단계 검색 메서드 =====
    
    /**
     * 1단계: 정확 일치 검색 (커버링 인덱스 활용)
     * 시도/시군구/동/리 중 하나라도 키워드와 정확히 일치하는 경우
     */
    @Query("SELECT ld FROM LegalDistrict ld " +
           "WHERE ld.sido = :keyword " +
           "   OR ld.sigungu = :keyword " +
           "   OR ld.dong = :keyword " +
           "   OR ld.ri = :keyword " +
           "ORDER BY FUNCTION('CHAR_LENGTH', ld.fullAddress)")
    List<LegalDistrict> findExactMatch(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 2단계: 접두사 매칭 검색 (커버링 인덱스 활용)
     * 시도/시군구/동/리 중에 '키워드%' 형태로 시작하는 경우
     */
    @Query("SELECT ld FROM LegalDistrict ld " +
           "WHERE ld.sido LIKE CONCAT(:keyword, '%') " +
           "   OR ld.sigungu LIKE CONCAT(:keyword, '%') " +
           "   OR ld.dong LIKE CONCAT(:keyword, '%') " +
           "   OR ld.ri LIKE CONCAT(:keyword, '%') " +
           "ORDER BY FUNCTION('CHAR_LENGTH', ld.fullAddress)")
    List<LegalDistrict> findPrefixMatch(@Param("keyword") String keyword, Pageable pageable);


    
    // ===== 성능 최적화된 특별 검색 메서드 =====
    
    /**
     * 빠른 중복 체크용 (커버링 인덱스 활용)
     */
    @Query("SELECT COUNT(ld) > 0 FROM LegalDistrict ld WHERE ld.districtCode = :districtCode")
    boolean existsByDistrictCodeOptimized(@Param("districtCode") String districtCode);
    
    /**
     * 시도별 빠른 검색 (커버링 인덱스 활용)
     */
    @Query("SELECT ld FROM LegalDistrict ld " +
           "WHERE ld.sido = :sido " +
           "ORDER BY ld.fullAddress")
    List<LegalDistrict> findBySidoExact(@Param("sido") String sido, Pageable pageable);
    
    /**
     * 시도 + 시군구 조합 빠른 검색
     */
    @Query("SELECT ld FROM LegalDistrict ld " +
           "WHERE ld.sido = :sido AND ld.sigungu = :sigungu " +
           "ORDER BY ld.fullAddress")
    List<LegalDistrict> findBySidoAndSigunguExact(@Param("sido") String sido, @Param("sigungu") String sigungu, Pageable pageable);
    
    // ===== 기존 메서드 (하위 호환성 유지) =====
    
    /**
     * 키워드로 법정동 검색 (전체 주소 기준) - 레거시
     */
    @Query("SELECT ld FROM LegalDistrict ld WHERE ld.fullAddress LIKE %:keyword% ORDER BY LENGTH(ld.fullAddress)")
    List<LegalDistrict> findByFullAddressContainingOrderByLength(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 복합 검색 (시도, 시군구, 동 개별 검색) - 레거시
     */
    @Query("SELECT ld FROM LegalDistrict ld WHERE " +
           "ld.sido LIKE %:keyword% OR " +
           "ld.sigungu LIKE %:keyword% OR " +
           "ld.dong LIKE %:keyword% OR " +
           "ld.ri LIKE %:keyword% OR " +
           "ld.fullAddress LIKE %:keyword% " +
           "ORDER BY " +
           "CASE " +
           "  WHEN ld.sido = :keyword THEN 1 " +
           "  WHEN ld.sigungu = :keyword THEN 2 " +
           "  WHEN ld.dong = :keyword THEN 3 " +
           "  WHEN ld.ri = :keyword THEN 4 " +
           "  WHEN ld.sido LIKE :keyword% THEN 5 " +
           "  WHEN ld.sigungu LIKE :keyword% THEN 6 " +
           "  WHEN ld.dong LIKE :keyword% THEN 7 " +
           "  ELSE 8 " +
           "END, " +
           "LENGTH(ld.fullAddress)")
    List<LegalDistrict> searchByKeywordWithRelevance(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 강화된 부분 매칭 검색 (Full-Text 인덱스 없이도 작동)
     * 정확도 기반 정렬과 함께 부분 문자열 검색
     */
    @Query(value = "SELECT * FROM legal_districts ld WHERE " +
                   "ld.sido LIKE CONCAT('%', :keyword, '%') OR " +
                   "ld.sigungu LIKE CONCAT('%', :keyword, '%') OR " +
                   "ld.dong LIKE CONCAT('%', :keyword, '%') OR " +
                   "ld.ri LIKE CONCAT('%', :keyword, '%') OR " +
                   "ld.full_address LIKE CONCAT('%', :keyword, '%') " +
                   "ORDER BY " +
                   "CASE " +
                   "  WHEN ld.sido = :keyword THEN 1 " +
                   "  WHEN ld.sigungu = :keyword THEN 2 " +
                   "  WHEN ld.dong = :keyword THEN 3 " +
                   "  WHEN ld.ri = :keyword THEN 4 " +
                   "  WHEN ld.sido LIKE CONCAT(:keyword, '%') THEN 5 " +
                   "  WHEN ld.sigungu LIKE CONCAT(:keyword, '%') THEN 6 " +
                   "  WHEN ld.dong LIKE CONCAT(:keyword, '%') THEN 7 " +
                   "  WHEN ld.ri LIKE CONCAT(:keyword, '%') THEN 8 " +
                   "  WHEN ld.full_address LIKE CONCAT(:keyword, '%') THEN 9 " +
                   "  ELSE 10 " +
                   "END, " +
                   "CHAR_LENGTH(ld.full_address) ASC " +
                   "LIMIT :limit", nativeQuery = true)
    List<LegalDistrict> searchByKeywordFallback(@Param("keyword") String keyword, @Param("limit") int limit);
    
    /**
     * 시도로 검색 - 레거시
     */
    List<LegalDistrict> findBySidoContainingOrderByFullAddress(String sido, Pageable pageable);
    
    /**
     * 시도와 시군구로 검색 - 레거시
     */
    List<LegalDistrict> findBySidoAndSigunguContainingOrderByFullAddress(String sido, String sigungu, Pageable pageable);
    
    /**
     * 법정동 코드로 검색
     */
    LegalDistrict findByDistrictCode(String districtCode);
    
    /**
     * 데이터 존재 여부 확인 (초기 데이터 로드용)
     */
    boolean existsByDistrictCode(String districtCode);
    
    /**
     * 전체 데이터 수 조회
     */
    long count();
} 