package org.fr.farmranding.repository;

import org.fr.farmranding.entity.pricing.PremiumPriceSuggestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PremiumPriceSuggestionRepository extends JpaRepository<PremiumPriceSuggestion, Long> {
    
    /**
     * 사용자별 프리미엄 가격 제안 이력 조회 (최신순)
     */
    Page<PremiumPriceSuggestion> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 사용자별 프리미엄 가격 제안 개수
     */
    long countByUserId(Long userId);
    
    /**
     * 특정 품목의 최근 가격 제안들
     */
    @Query("SELECT p FROM PremiumPriceSuggestion p WHERE " +
           "p.itemCode = :itemCode AND p.kindCode = :kindCode " +
           "ORDER BY p.createdAt DESC")
    List<PremiumPriceSuggestion> findRecentSuggestionsByProduct(@Param("itemCode") String itemCode, 
                                                               @Param("kindCode") String kindCode, 
                                                               Pageable pageable);
    
    /**
     * 특정 날짜 범위의 가격 제안들
     */
    @Query("SELECT p FROM PremiumPriceSuggestion p WHERE " +
           "p.startDate >= :fromDate AND p.endDate <= :toDate " +
           "ORDER BY p.createdAt DESC")
    List<PremiumPriceSuggestion> findByDateRange(@Param("fromDate") LocalDate fromDate, 
                                                @Param("toDate") LocalDate toDate);
}
 