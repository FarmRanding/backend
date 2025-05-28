package org.fr.farmranding.repository;

import org.fr.farmranding.entity.user.SocialProvider;
import org.fr.farmranding.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByProviderAndProviderId(SocialProvider provider, String providerId);
    
    boolean existsByEmail(String email);
    
    boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);
} 