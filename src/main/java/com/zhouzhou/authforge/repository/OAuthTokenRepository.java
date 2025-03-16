package com.zhouzhou.authforge.repository;

import com.zhouzhou.authforge.model.OAuthAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthAccessToken, Long> {
    
    /**
     * Find token by access token
     * @param accessToken the access token
     * @return Optional containing the token if found
     */
    Optional<OAuthAccessToken> findByAccessToken(String accessToken);
    
    /**
     * Find token by refresh token
     * @param refreshToken the refresh token
     * @return Optional containing the token if found
     */
    Optional<OAuthAccessToken> findByRefreshToken(String refreshToken);
    
    /**
     * Find all tokens for a specific client and user
     * @param clientId the client ID
     * @param userId the user ID
     * @return List of tokens
     */
    List<OAuthAccessToken> findByClientIdAndUserId(String clientId, String userId);
    
    /**
     * Delete expired tokens
     * @param now current timestamp
     * @return number of deleted records
     */
    long deleteByAccessTokenExpiresAtLessThan(LocalDateTime now);
    
    /**
     * Delete expired refresh tokens
     * @param now current timestamp
     * @return number of deleted records
     */
    long deleteByRefreshTokenExpiresAtLessThan(LocalDateTime now);
} 