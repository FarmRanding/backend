package org.fr.farmranding.repository;

import org.fr.farmranding.entity.branding.BrandingProject;

import org.fr.farmranding.entity.branding.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BrandingProjectRepository extends JpaRepository<BrandingProject, Long> {
    
    List<BrandingProject> findByUserId(Long userId);

    Page<BrandingProject> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT bp FROM BrandingProject bp WHERE bp.user.id = :userId AND bp.title LIKE %:keyword%")
    List<BrandingProject> findByUserIdAndTitleContaining(@Param("userId") Long userId, @Param("keyword") String keyword);
    
    @Query("SELECT bp FROM BrandingProject bp WHERE bp.user.id = :userId AND bp.cropName LIKE %:cropName%")
    List<BrandingProject> findByUserIdAndCropNameContaining(@Param("userId") Long userId, @Param("cropName") String cropName);
    
    @Query("SELECT bp FROM BrandingProject bp WHERE bp.user.id = :userId AND bp.grade = :grade")
    List<BrandingProject> findByUserIdAndGrade(@Param("userId") Long userId, @Param("grade") Grade grade);
    
    @Query("SELECT bp FROM BrandingProject bp WHERE bp.user.id = :userId AND bp.createdAt BETWEEN :startDate AND :endDate")
    List<BrandingProject> findByUserIdAndCreatedAtBetween(@Param("userId") Long userId, 
                                                         @Param("startDate") LocalDateTime startDate, 
                                                         @Param("endDate") LocalDateTime endDate);
    
    long countByUserId(Long userId);

    boolean existsByUserIdAndTitle(Long userId, String title);
    
    Optional<BrandingProject> findByIdAndUserId(Long id, Long userId);
    
    @Query("SELECT bp FROM BrandingProject bp WHERE bp.user.id = :userId ORDER BY bp.updatedAt DESC")
    List<BrandingProject> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

} 