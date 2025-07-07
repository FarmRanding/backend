package org.fr.farmranding.repository;

import org.fr.farmranding.entity.pricequote.PriceQuoteRequest;
import org.fr.farmranding.entity.pricequote.PriceQuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceQuoteRequestRepository extends JpaRepository<PriceQuoteRequest, Long> {
    
    List<PriceQuoteRequest> findByUserId(Long userId);
    
    Page<PriceQuoteRequest> findByUserId(Long userId, Pageable pageable);
    
    List<PriceQuoteRequest> findByUserIdAndStatus(Long userId, PriceQuoteStatus status);
    
    @Query("SELECT p FROM PriceQuoteRequest p WHERE p.user.id = :userId AND p.productName LIKE %:keyword%")
    List<PriceQuoteRequest> findByUserIdAndProductNameContaining(@Param("userId") Long userId, 
                                                                @Param("keyword") String keyword);
    
    @Query("SELECT p FROM PriceQuoteRequest p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<PriceQuoteRequest> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
    
    Optional<PriceQuoteRequest> findByIdAndUserId(Long id, Long userId);
    
    long countByUserId(Long userId);
} 