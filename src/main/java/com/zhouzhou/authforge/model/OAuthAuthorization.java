package com.zhouzhou.authforge.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * OAuth 2.0 授权信息实体
 * 
 * 存储 OAuth 2.0 授权码授权类型的授权信息，实现 RFC 6749 Section 4.1.2
 * 
 * 核心属性：
 * 1. 授权码 (authorization_code)：
 *    - 随机生成的字符串
 *    - 用于交换访问令牌
 *    - 短期有效（通常10分钟）
 * 
 * 2. PKCE 支持 (RFC 7636)：
 *    - code_challenge：客户端提供的代码挑战
 *    - code_challenge_method：代码挑战方法（plain 或 S256）
 * 
 * 3. 授权信息：
 *    - client_id：关联的客户端标识
 *    - user_id：授权的用户标识
 *    - scopes：授权的权限范围
 *    - state：客户端状态参数
 * 
 * 4. 时间控制：
 *    - authorization_code_expires_at：授权码过期时间
 *    - created_at：创建时间
 *    - updated_at：更新时间
 * 
 * @see RFC 6749 https://tools.ietf.org/html/rfc6749#section-4.1.2
 * @see RFC 7636 https://tools.ietf.org/html/rfc7636
 */
@Data
@Entity
@Table(name = "oauth_authorizations")
public class OAuthAuthorization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "scopes", columnDefinition = "TEXT")
    private String scopes;

    @Column(name = "authorization_code", length = 256)
    private String authorizationCode;

    @Column(name = "code_challenge", length = 256)
    private String codeChallenge;

    @Column(name = "code_challenge_method", length = 32)
    private String codeChallengeMethod;

    @Column(name = "state", length = 256)
    private String state;

    @Column(name = "redirect_uri", length = 1024)
    private String redirectUri;

    @Column(name = "response_type", length = 32)
    private String responseType;

    @Column(name = "authorization_code_expires_at")
    private LocalDateTime authorizationCodeExpiresAt;

    @Column(name = "trace_id", length = 256)
    private String traceId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "client_id", insertable = false, updatable = false)
    private OAuthClient oauthClient;
} 