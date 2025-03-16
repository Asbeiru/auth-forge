package com.zhouzhou.authforge.repository;

import com.zhouzhou.authforge.model.OAuthAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OAuth 2.0 授权记录仓库
 */
@Repository
public interface OAuthAuthorizationRepository extends JpaRepository<OAuthAuthorization, Long> {
    
    /**
     * Find authorization by client ID and user ID
     * @param clientId the client ID
     * @param userId the user ID
     * @return List of authorizations for the client and user
     */
    List<OAuthAuthorization> findByClientIdAndUserId(String clientId, String userId);
    
    /**
     * Find authorization by authorization code
     * @param authorizationCode the authorization code
     * @return Optional containing the authorization if found
     */
    Optional<OAuthAuthorization> findByAuthorizationCode(String authorizationCode);
    
    /**
     * Delete expired authorization codes
     * @param now current timestamp
     * @return number of deleted records
     */
    long deleteByAuthorizationCodeExpiresAtLessThan(LocalDateTime now);


    // 添加根据 state 查询的方法
    Optional<OAuthAuthorization> findByState(String state);

    Optional<OAuthAuthorization> findByTraceId(String traceId);

    /**
     * 根据刷新令牌查找授权信息
     */
    Optional<OAuthAuthorization> findByRefreshToken(String refreshToken);
} 