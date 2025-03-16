package com.zhouzhou.authforge.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * OAuth 2.0 授权记录
 */
@Entity
@Table(name = "oauth_authorizations")
@Getter
@Setter
public class OAuthAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 客户端ID
     */
    @Column(name = "client_id", nullable = false)
    private String clientId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * 授权码
     */
    @Column(name = "authorization_code", unique = true)
    private String authorizationCode;

    /**
     * 授权码过期时间
     */
    @Column(name = "authorization_code_expires_at")
    private LocalDateTime authorizationCodeExpiresAt;

    /**
     * PKCE code_challenge
     */
    @Column(name = "code_challenge")
    private String codeChallenge;

    /**
     * PKCE code_challenge_method
     */
    @Column(name = "code_challenge_method")
    private String codeChallengeMethod;

    /**
     * 授权范围
     */
    @Column(name = "scopes")
    private String scopes;

    /**
     * 重定向URI
     */
    @Column(name = "redirect_uri", nullable = false)
    private String redirectUri;

    /**
     * 状态参数
     */
    @Column(name = "state")
    private String state;


    /**
     * 追踪ID
     */
    @Column(name = "trace_id")
    private String traceId;

    /**
     * 响应类型
     */
    @Column(name = "response_type")
    private String responseType;

    /**
     * 授权码状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AuthorizationStatus status = AuthorizationStatus.ACTIVE;

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
     * 检查refresh_token是否已失效
     */
    public boolean isInvalidated() {
        return AuthorizationStatus.INVALIDATED.equals(this.status);
    }


    /**
     * 使授权码失效
     * <p>
     * 注意：调用此方法后，需要在服务层保存实体的更改
     */
    public void markAsInvalidated() {
        this.status = AuthorizationStatus.INVALIDATED;
        this.authorizationCode = null;  // 清除授权码
        this.authorizationCodeExpiresAt = LocalDateTime.now();  // 设置为当前时间，确保过期
    }

    /**
     * 检查授权码是否有效
     */
    public boolean isValid() {
        return status == AuthorizationStatus.ACTIVE &&
                authorizationCode != null &&
                authorizationCodeExpiresAt != null &&
                authorizationCodeExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * 获取授权范围
     */
    public String getScope() {
        return scopes;
    }


    public enum AuthorizationStatus {
        ACTIVE,        // 活跃状态
        INVALIDATED    // 已失效
    }
} 