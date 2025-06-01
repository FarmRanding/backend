package org.fr.farmranding.repository;

import org.fr.farmranding.entity.pricequote.PriceQuoteRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceQuoteRequestRepository extends JpaRepository<PriceQuoteRequest, Long> {
    
    List<PriceQuoteRequest> findByUserId(Long userId);
    
    List<PriceQuoteRequest> findByUserIdAndStatus(Long userId, PriceQuoteStatus status);
    
    Page<PriceQuoteRequest> findByUserId(Long userId, Pageable pageable);
    
    Page<PriceQuoteRequest> findByUserIdAndStatus(Long userId, PriceQuoteStatus status, Pageable pageable);
    
    @Query("SELECT p FROM PriceQuoteRequest p WHERE p.user.id = :userId AND p.cropName LIKE %:keyword%")
    List<PriceQuoteRequest> findByUserIdAndCropNameContaining(@Param("userId") Long userId, 
                                                             @Param("keyword") String keyword);
    
    @Query("SELECT pqr FROM PriceQuoteRequest pqr WHERE pqr.user.id = :userId AND pqr.productionArea LIKE %:area%")
    List<PriceQuoteRequest> findByUserIdAndProductionAreaContaining(@Param("userId") Long userId, @Param("area") String area);
    
    @Query("SELECT pqr FROM PriceQuoteRequest pqr WHERE pqr.user.id = :userId AND pqr.organicCertification = :organic")
    List<PriceQuoteRequest> findByUserIdAndOrganicCertification(@Param("userId") Long userId, @Param("organic") Boolean organic);
    
    @Query("SELECT pqr FROM PriceQuoteRequest pqr WHERE pqr.user.id = :userId AND pqr.gapCertification = :gap")
    List<PriceQuoteRequest> findByUserIdAndGapCertification(@Param("userId") Long userId, @Param("gap") Boolean gap);
    
    @Query("SELECT pqr FROM PriceQuoteRequest pqr WHERE pqr.user.id = :userId AND pqr.currentSellingPrice BETWEEN :minPrice AND :maxPrice")
    List<PriceQuoteRequest> findByUserIdAndCurrentSellingPriceBetween(@Param("userId") Long userId, 
                                                                     @Param("minPrice") BigDecimal minPrice, 
                                                                     @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT pqr FROM PriceQuoteRequest pqr WHERE pqr.user.id = :userId AND pqr.createdAt BETWEEN :startDate AND :endDate")
    List<PriceQuoteRequest> findByUserIdAndCreatedAtBetween(@Param("userId") Long userId, 
                                                           @Param("startDate") LocalDateTime startDate, 
                                                           @Param("endDate") LocalDateTime endDate);
    
    long countByUserId(Long userId);
    
    long countByUserIdAndStatus(Long userId, PriceQuoteStatus status);
    
    boolean existsByUserIdAndCropName(Long userId, String cropName);
    
    Optional<PriceQuoteRequest> findByIdAndUserId(Long id, Long userId);
    
    @Query("SELECT p FROM PriceQuoteRequest p WHERE p.user.id = :userId ORDER BY p.updatedAt DESC")
    List<PriceQuoteRequest> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT pqr FROM PriceQuoteRequest pqr WHERE pqr.status = :status AND pqr.updatedAt < :cutoffDate")
    List<PriceQuoteRequest> findStaleRequestsByStatus(@Param("status") PriceQuoteStatus status, 
                                                     @Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT DISTINCT pqr.cropName FROM PriceQuoteRequest pqr WHERE pqr.user.id = :userId")
    List<String> findDistinctCropNamesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT pqr.productionArea FROM PriceQuoteRequest pqr WHERE pqr.user.id = :userId")
    List<String> findDistinctProductionAreasByUserId(@Param("userId") Long userId);
} 