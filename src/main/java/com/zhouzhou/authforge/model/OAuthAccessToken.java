package com.zhouzhou.authforge.model;

import com.zhouzhou.authforge.service.TokenGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * OAuth 2.0 访问令牌
 * 
 * 充血模型实现，包含：
 * 1. 令牌生成逻辑
 * 2. 令牌验证逻辑
 * 3. 过期检查逻辑
 */
@Entity
@Table(name = "oauth_access_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 客户端ID
     */
    @Column(name = "client_id", nullable = false)
    private String clientId;

    /**
     * 用户ID（可选，客户端凭证模式下为空）
     */
    @Column(name = "user_id")
    private String userId;

    /**
     * 访问令牌
     */
    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;

    /**
     * 刷新令牌
     */
    @Column(name = "refresh_token", unique = true)
    private String refreshToken;

    /**
     * 授权范围
     */
    @Column(name = "scope")
    private String scope;

    /**
     * 访问令牌过期时间
     */
    @Column(name = "access_token_expires_at", nullable = false)
    private LocalDateTime accessTokenExpiresAt;

    /**
     * 刷新令牌过期时间
     */
    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    /**
     * 令牌状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TokenStatus status = TokenStatus.ACTIVE;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 创建访问令牌
     *
     * @param authorization 授权信息
     * @param client 客户端信息
     * @param tokenGenerator 令牌生成器
     * @return 访问令牌
     */
    public static OAuthAccessToken createFrom(
            OAuthAuthorization authorization,
            OAuthClient client,
            TokenGenerator tokenGenerator) {
        
        String accessToken = tokenGenerator.generateAccessToken(
            authorization.getUserId(),
            authorization.getScope(),
            client.getAccessTokenValiditySeconds()
        );

        String refreshToken = null;
        if (client.isRefreshTokenEnabled()) {
            refreshToken = tokenGenerator.generateRefreshToken();
        }

        return OAuthAccessToken.builder()
            .clientId(client.getClientId())
            .userId(authorization.getUserId())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .scope(authorization.getScope())
            .accessTokenExpiresAt(LocalDateTime.now().plusSeconds(client.getAccessTokenValiditySeconds()))
            .refreshTokenExpiresAt(refreshToken != null ? LocalDateTime.now().plusSeconds(client.getRefreshTokenValiditySeconds()) : null)
            .build();
    }

    /**
     * 检查访问令牌是否已过期
     */
    public boolean isAccessTokenExpired() {
        return accessTokenExpiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 检查刷新令牌是否已过期
     */
    public boolean isRefreshTokenExpired() {
        return refreshTokenExpiresAt != null && 
               refreshTokenExpiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 检查令牌是否已失效
     */
    public boolean isInvalidated() {
        return TokenStatus.INVALIDATED.equals(this.status);
    }

    /**
     * 使令牌失效
     */
    public void markAsInvalidated() {
        this.status = TokenStatus.INVALIDATED;
    }

    /**
     * 检查令牌是否可用
     */
    public boolean isActive() {
        return status == TokenStatus.ACTIVE && !isAccessTokenExpired() && !isRefreshTokenExpired();
    }

    /**
     * 验证作用域
     */
    public boolean hasScope(String requiredScope) {
        if (scope == null) return false;
        return Set.of(scope.split(" ")).contains(requiredScope);
    }

    /**
     * 设置刷新令牌
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public enum TokenStatus {
        ACTIVE,        // 活跃状态
        INVALIDATED    // 已失效
    }
} 