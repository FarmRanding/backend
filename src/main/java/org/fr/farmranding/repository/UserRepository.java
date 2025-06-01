package org.fr.farmranding.repository;

import org.fr.farmranding.entity.user.SocialProvider;
import org.fr.farmranding.entity.user.User;
import org.fr.farmranding.entity.user.MembershipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByProviderAndProviderId(SocialProvider provider, String providerId);
    
    List<User> findByMembershipType(MembershipType membershipType);
    
    long countByMembershipType(MembershipType membershipType);
    
    boolean existsByEmail(String email);
    
    boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);
    
    @Query("SELECT u FROM User u WHERE u.farmName LIKE %:keyword% OR u.location LIKE %:keyword%")
    List<User> findByFarmNameOrLocationContaining(@Param("keyword") String keyword);
    
    @Query("SELECT u FROM User u WHERE u.establishedYear >= :fromYear AND u.establishedYear <= :toYear")
    List<User> findByEstablishedYearBetween(@Param("fromYear") Integer fromYear, @Param("toYear") Integer toYear);
} 