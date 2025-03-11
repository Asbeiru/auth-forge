package com.zhouzhou.authforge.repository;

import com.zhouzhou.authforge.model.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    
    /**
     * Find token by access token
     * @param accessToken the access token
     * @return Optional containing the token if found
     */
    Optional<OAuthToken> findByAccessToken(String accessToken);
    
    /**
     * Find token by refresh token
     * @param refreshToken the refresh token
     * @return Optional containing the token if found
     */
    Optional<OAuthToken> findByRefreshToken(String refreshToken);
    
    /**
     * Find all tokens for a specific client and user
     * @param clientId the client ID
     * @param userId the user ID
     * @return List of tokens
     */
    List<OAuthToken> findByClientIdAndUserId(String clientId, String userId);
    
    /**
     * Delete expired tokens
     * @param now current timestamp
     * @return number of deleted records
     */
    long deleteByExpiresAtLessThan(LocalDateTime now);
    
    /**
     * Delete expired refresh tokens
     * @param now current timestamp
     * @return number of deleted records
     */
    long deleteByRefreshTokenExpiresAtLessThan(LocalDateTime now);
} 