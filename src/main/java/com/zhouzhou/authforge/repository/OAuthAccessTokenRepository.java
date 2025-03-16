package com.zhouzhou.authforge.repository;

import com.zhouzhou.authforge.model.OAuthAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * OAuth 2.0 访问令牌仓库
 */
@Repository
public interface OAuthAccessTokenRepository extends JpaRepository<OAuthAccessToken, Long> {

    /**
     * 根据访问令牌查找
     */
    Optional<OAuthAccessToken> findByAccessToken(String accessToken);

    /**
     * 根据刷新令牌查找
     */
    Optional<OAuthAccessToken> findByRefreshToken(String refreshToken);

    /**
     * 根据客户端ID和用户ID查找
     */
    Optional<OAuthAccessToken> findByClientIdAndUserId(String clientId, String userId);

    /**
     * 删除过期的访问令牌
     */
    long deleteByAccessTokenExpiresAtLessThan(LocalDateTime now);

    /**
     * 删除过期的刷新令牌
     */
    long deleteByRefreshTokenExpiresAtLessThan(LocalDateTime now);
} 