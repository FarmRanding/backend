package org.fr.farmranding.repository;

import org.fr.farmranding.entity.StandardCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StandardCodeRepository extends JpaRepository<StandardCode, Long> {
    
    Optional<StandardCode> findBySclassCode(String sclassCode);
    
    boolean existsBySclassCode(String sclassCode);
    
    // 작물명(MCLASSNAME) 기준 검색 - 중복 제거
    @Query("SELECT DISTINCT sc.mclassCode, sc.mclassName FROM StandardCode sc " +
           "WHERE sc.isActive = true AND sc.mclassName LIKE %:query% " +
           "ORDER BY sc.mclassName ASC")
    List<Object[]> findDistinctCropsByMclassNameContaining(@Param("query") String query);
    
    // 인기 작물 조회 (등록된 품종 수 기준)
    @Query("SELECT sc.mclassCode, sc.mclassName, COUNT(sc) as varietyCount FROM StandardCode sc " +
           "WHERE sc.isActive = true " +
           "GROUP BY sc.mclassCode, sc.mclassName " +
           "ORDER BY varietyCount DESC, sc.mclassName ASC")
    List<Object[]> findPopularCrops();
    
    // 특정 작물의 품종 조회
    @Query("SELECT sc FROM StandardCode sc " +
           "WHERE sc.isActive = true AND sc.mclassCode = :mclassCode " +
           "AND sc.sclassName LIKE %:query% " +
           "ORDER BY sc.sclassName ASC")
    List<StandardCode> findVarietiesByMclassCodeAndSclassNameContaining(
            @Param("mclassCode") String mclassCode, 
            @Param("query") String query);
    
    // 특정 작물의 모든 품종 조회
    List<StandardCode> findByMclassCodeAndIsActiveOrderBySclassNameAsc(String mclassCode, Boolean isActive);
    
    // 전체 데이터 삭제 (초기화용)
    void deleteAllByIsActive(Boolean isActive);
    
    // 활성화된 전체 데이터 개수
    long countByIsActive(Boolean isActive);
} 