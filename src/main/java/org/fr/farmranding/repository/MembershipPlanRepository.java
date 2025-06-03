package org.fr.farmranding.repository;

import org.fr.farmranding.entity.membership.MembershipPlan;
import org.fr.farmranding.entity.user.MembershipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
    
    Optional<MembershipPlan> findByMembershipType(MembershipType membershipType);
    
    List<MembershipPlan> findByIsActiveTrue();
    
    List<MembershipPlan> findByIsActiveTrueOrderBySortOrder();
    
    Optional<MembershipPlan> findByIsPopularTrue();
    
    @Query("SELECT mp FROM MembershipPlan mp WHERE mp.isActive = true ORDER BY mp.sortOrder ASC")
    List<MembershipPlan> findActivePlansOrderedBySortOrder();
    
    @Query("SELECT mp FROM MembershipPlan mp WHERE mp.membershipType = :membershipType AND mp.isActive = true")
    Optional<MembershipPlan> findActiveByMembershipType(MembershipType membershipType);
    
    boolean existsByMembershipType(MembershipType membershipType);
    
    long countByIsActiveTrue();
} 