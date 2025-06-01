package org.fr.farmranding.repository;

import org.fr.farmranding.entity.pricequote.PriceQuoteRequest;
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
    
    @Query("SELECT p FROM PriceQuoteRequest p WHERE p.user.id = :userId AND p.cropName LIKE %:keyword%")
    List<PriceQuoteRequest> findByUserIdAndCropNameContaining(@Param("userId") Long userId, 
                                                             @Param("keyword") String keyword);
    
    Optional<PriceQuoteRequest> findByIdAndUserId(Long id, Long userId);
    
    long countByUserId(Long userId);
} 