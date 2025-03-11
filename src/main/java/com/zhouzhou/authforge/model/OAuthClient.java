package com.zhouzhou.authforge.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * OAuth 2.0 客户端实体
 * 
 * 存储 OAuth 2.0 客户端注册信息，实现 RFC 6749 Section 2
 * 
 * 核心属性：
 * 1. 客户端标识：
 *    - client_id：客户端唯一标识
 *    - client_secret：客户端密钥
 *    - client_name：客户端名称
 * 
 * 2. 授权配置：
 *    - redirect_uris：允许的重定向URI列表
 *    - authorized_grant_types：支持的授权类型
 *    - scopes：允许的权限范围
 * 
 * 3. 令牌配置：
 *    - access_token_validity_seconds：访问令牌有效期
 *    - refresh_token_validity_seconds：刷新令牌有效期
 * 
 * 4. 安全配置：
 *    - auto_approve：是否自动批准授权
 *    - enabled：客户端是否启用
 * 
 * 客户端类型（RFC 6749 Section 2.1）：
 * - 机密客户端：能够安全存储客户端凭据
 * - 公开客户端：无法安全存储客户端凭据
 * 
 * @see RFC 6749 https://tools.ietf.org/html/rfc6749#section-2
 */
@Data
@Entity
@Table(name = "oauth_clients")
public class OAuthClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, unique = true, length = 100)
    private String clientId;

    @Column(name = "client_secret", nullable = false, length = 200)
    private String clientSecret;

    @Column(name = "client_name", length = 200)
    private String clientName;

    @Column(name = "redirect_uris", nullable = false, columnDefinition = "TEXT")
    private String redirectUris;

    @Column(name = "scopes", columnDefinition = "TEXT")
    private String scopes;

    @Column(name = "authorized_grant_types", nullable = false)
    private String authorizedGrantTypes;

    @Column(name = "access_token_validity_seconds")
    private Integer accessTokenValiditySeconds = 3600;

    @Column(name = "refresh_token_validity_seconds")
    private Integer refreshTokenValiditySeconds = 86400;

    @Column(name = "auto_approve")
    private Boolean autoApprove = false;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 