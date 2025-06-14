package org.fr.farmranding.repository;

import org.fr.farmranding.entity.pricing.KamisProductCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KamisProductCodeRepository extends JpaRepository<KamisProductCode, Long> {
    
    /**
     * 품목 그룹별 조회
     */
    List<KamisProductCode> findByGroupCodeOrderByItemNameAsc(String groupCode);
    
    /**
     * 품목명으로 검색
     */
    List<KamisProductCode> findByItemNameContainingIgnoreCaseOrderByItemNameAsc(String itemName);
    
    /**
     * 품목 코드와 품종 코드로 정확한 매칭
     */
    Optional<KamisProductCode> findByItemCodeAndKindCode(String itemCode, String kindCode);
    
    /**
     * 모든 품목 그룹 조회
     */
    @Query("SELECT DISTINCT k.groupCode, k.groupName FROM KamisProductCode k ORDER BY k.groupCode")
    List<Object[]> findDistinctGroups();
    
    /**
     * 특정 품목의 모든 품종 조회
     */
    List<KamisProductCode> findByItemCodeOrderByKindNameAsc(String itemCode);
    
    /**
     * 품목명과 품종명으로 검색 (퍼지 매칭)
     */
    @Query("SELECT k FROM KamisProductCode k WHERE " +
           "LOWER(k.itemName) LIKE LOWER(CONCAT('%', :itemName, '%')) OR " +
           "LOWER(k.kindName) LIKE LOWER(CONCAT('%', :kindName, '%')) " +
           "ORDER BY k.itemName, k.kindName")
    List<KamisProductCode> findByItemOrKindNameContaining(@Param("itemName") String itemName, 
                                                          @Param("kindName") String kindName);
    
    /**
     * 상위 50개 품목 조회 (품목명 순)
     */
    List<KamisProductCode> findTop50ByOrderByItemNameAsc();
} 