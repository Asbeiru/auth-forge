package com.zhouzhou.authforge.model;

import com.zhouzhou.authforge.security.ClientAuthenticationMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
@Entity
@Table(name = "oauth_clients")
@Getter
@Setter
public class OAuthClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 客户端ID
     */
    @Column(name = "client_id", nullable = false, unique = true, length = 100)
    private String clientId;

    /**
     * 客户端密钥
     */
    @Column(name = "client_secret", nullable = false, length = 200)
    private String clientSecret;

    /**
     * 客户端名称
     */
    @Column(name = "client_name", length = 200)
    private String clientName;

    /**
     * 客户端描述
     */
    @Column(name = "description")
    private String description;

    /**
     * 客户端类型
     */
    @Column(name = "client_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ClientType clientType = ClientType.CONFIDENTIAL;

    /**
     * 令牌端点认证方法
     */
    @Column(name = "token_endpoint_auth_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenEndpointAuthMethod tokenEndpointAuthMethod = TokenEndpointAuthMethod.CLIENT_SECRET_BASIC;

    /**
     * 是否需要 PKCE
     */
    @Column(name = "require_proof_key", nullable = false)
    private Boolean requireProofKey = false;

    /**
     * 默认的 PKCE 挑战方法
     */
    @Column(name = "default_code_challenge_method")
    private String defaultCodeChallengeMethod;

    /**
     * JWKS URI
     */
    @Column(name = "jwks_uri")
    private String jwksUri;

    /**
     * 令牌端点
     */
    @Column(name = "token_endpoint")
    private String tokenEndpoint;

    /**
     * 重定向URI列表，以空格分隔
     */
    @Column(name = "redirect_uris", nullable = false, columnDefinition = "TEXT")
    private String redirectUris;

    /**
     * 授权范围列表，以空格分隔
     */
    @Column(name = "scopes", columnDefinition = "TEXT")
    private String scopes;

    /**
     * 授权类型列表，以逗号分隔
     */
    @Column(name = "authorized_grant_types", nullable = false)
    private String authorizedGrantTypes;

    /**
     * 访问令牌有效期
     */
    @Column(name = "access_token_validity_seconds")
    private Integer accessTokenValiditySeconds = 3600;

    /**
     * 刷新令牌有效期
     */
    @Column(name = "refresh_token_validity_seconds")
    private Integer refreshTokenValiditySeconds = 86400;

    /**
     * 自动批准授权
     */
    @Column(name = "auto_approve", nullable = false)
    private Boolean autoApprove = false;

    /**
     * 客户端是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 客户端认证方法，多个方法用逗号分隔
     */
    @Column(name = "client_authentication_methods", nullable = false)
    private String clientAuthenticationMethods = "client_secret_basic";

    /**
     * 默认的客户端认证方法
     */
    @Column(name = "default_authentication_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private ClientAuthenticationMethod defaultAuthenticationMethod = ClientAuthenticationMethod.CLIENT_SECRET_BASIC;

    /**
     * 获取重定向URI集合
     */
    public Set<String> getRedirectUriSet() {
        if (StringUtils.hasText(redirectUris)) {
            return new HashSet<>(Arrays.asList(redirectUris.split(" ")));
        }
        return Collections.emptySet();
    }

    /**
     * 设置重定向URI集合
     */
    public void setRedirectUriSet(Set<String> uris) {
        this.redirectUris = String.join(" ", uris);
    }

    /**
     * 获取授权范围集合
     */
    public Set<String> getScopeSet() {
        if (StringUtils.hasText(scopes)) {
            return new HashSet<>(Arrays.asList(scopes.split(" ")));
        }
        return Collections.emptySet();
    }

    /**
     * 设置授权范围集合
     */
    public void setScopeSet(Set<String> scopes) {
        this.scopes = String.join(" ", scopes);
    }

    /**
     * 获取授权类型集合
     */
    public Set<String> getAuthorizedGrantTypeSet() {
        if (StringUtils.hasText(authorizedGrantTypes)) {
            return new HashSet<>(Arrays.asList(authorizedGrantTypes.split(",")));
        }
        return Collections.emptySet();
    }

    /**
     * 设置授权类型集合
     */
    public void setAuthorizedGrantTypeSet(Set<String> grantTypes) {
        this.authorizedGrantTypes = String.join(",", grantTypes);
    }

    /**
     * 获取支持的认证方法集合
     */
    public Set<ClientAuthenticationMethod> getClientAuthenticationMethodSet() {
        return Arrays.stream(clientAuthenticationMethods.split(","))
                .map(String::trim)
                .map(ClientAuthenticationMethod::valueOf)
                .collect(Collectors.toSet());
    }

    /**
     * 设置支持的认证方法集合
     */
    public void setClientAuthenticationMethodSet(Set<ClientAuthenticationMethod> methods) {
        this.clientAuthenticationMethods = methods.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 公钥（用于JWT验证）
     */
    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;

    /**
     * 是否重用刷新令牌
     */
    @Column(name = "reuse_refresh_tokens", nullable = false)
    private Boolean reuseRefreshTokens = true;

    /**
     * 检查客户端是否支持刷新令牌
     */
    public boolean isRefreshTokenEnabled() {
        return authorizedGrantTypes != null && 
               authorizedGrantTypes.contains("refresh_token");
    }

    /**
     * 检查客户端是否支持指定的授权类型
     */
    public boolean isGrantTypeAllowed(String grantType) {
        return authorizedGrantTypes != null && 
               authorizedGrantTypes.contains(grantType);
    }

    /**
     * 检查重定向URI是否有效
     */
    public boolean isRedirectUriValid(String redirectUri) {
        return redirectUris != null && 
               redirectUris.contains(redirectUri);
    }

    /**
     * 检查是否允许重复使用refresh_token
     */
    public boolean isReuseRefreshTokens() {
        return Boolean.TRUE.equals(this.reuseRefreshTokens);
    }

    /**
     * 获取访问令牌有效期
     * @return Duration 访问令牌有效期
     */
    public Duration getAccessTokenValidity() {
        return Duration.ofSeconds(accessTokenValiditySeconds);
    }

    /**
     * 获取刷新令牌有效期
     * @return Duration 刷新令牌有效期
     */
    public Duration getRefreshTokenValidity() {
        return Duration.ofSeconds(refreshTokenValiditySeconds);
    }

    public enum ClientType {
        CONFIDENTIAL,
        PUBLIC
    }

    public enum TokenEndpointAuthMethod {
        NONE,
        CLIENT_SECRET_BASIC,
        CLIENT_SECRET_POST,
        CLIENT_SECRET_JWT,
        PRIVATE_KEY_JWT
    }
} 