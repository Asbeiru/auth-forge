package com.zhouzhou.authforge.repository;

import com.zhouzhou.authforge.model.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthClientRepository extends JpaRepository<OAuthClient, Long> {
    
    /**
     * Find OAuth client by client ID
     * @param clientId the client ID
     * @return Optional containing the OAuth client if found
     */
    Optional<OAuthClient> findByClientId(String clientId);
    
    /**
     * Check if client exists by client ID
     * @param clientId the client ID
     * @return true if client exists
     */
    boolean existsByClientId(String clientId);
} 