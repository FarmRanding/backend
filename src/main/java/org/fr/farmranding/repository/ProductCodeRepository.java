package org.fr.farmranding.repository;

import org.fr.farmranding.entity.product.ProductCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCodeRepository extends JpaRepository<ProductCode, Long> {
    
    Optional<ProductCode> findByGarakCode(String garakCode);
    
    List<ProductCode> findByIsActiveTrue();
    
    List<ProductCode> findByProductNameContainingIgnoreCase(String keyword);
    
    boolean existsByGarakCode(String garakCode);
    
    @Query("SELECT p FROM ProductCode p WHERE p.productName LIKE %:keyword% AND p.isActive = true")
    List<ProductCode> findActiveProductsByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT COUNT(p) FROM ProductCode p WHERE p.isActive = true")
    long countActiveProducts();
} 