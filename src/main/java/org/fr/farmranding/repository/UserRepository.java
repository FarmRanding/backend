package org.fr.farmranding.repository;

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
    
    Optional<User> findByProviderId(String providerId);
    
    List<User> findByMembershipType(MembershipType membershipType);
    
    long countByMembershipType(MembershipType membershipType);
    
    boolean existsByEmail(String email);
    
    boolean existsByProviderId(String providerId);

} 