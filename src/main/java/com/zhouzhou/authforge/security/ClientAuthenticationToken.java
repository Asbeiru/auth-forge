package com.zhouzhou.authforge.security;

import lombok.Builder;
import lombok.Getter;

/**
 * 客户端认证信息
 * 不同的认证方法会提取不同的认证信息
 */
@Getter
@Builder
public class ClientAuthenticationToken {
    
    /**
     * 客户端ID
     */
    private final String clientId;

    /**
     * 客户端密钥（用于client_secret_basic和client_secret_post）
     */
    private final String clientSecret;

    /**
     * JWT断言（用于private_key_jwt和client_secret_jwt）
     */
    private final String clientAssertion;

    /**
     * JWT断言类型
     */
    private final String clientAssertionType;

    /**
     * 授权码（用于PKCE）
     */
    private final String code;

    /**
     * 代码验证器（用于PKCE）
     */
    private final String codeVerifier;

    /**
     * 代码挑战（用于PKCE）
     */
    private final String codeChallenge;

    /**
     * 代码挑战方法（用于PKCE）
     */
    private final String codeChallengeMethod;

    /**
     * 重定向URI
     */
    private final String redirectUri;

    /**
     * 授权范围
     */
    private final String scope;

    /**
     * 状态参数
     */
    private final String state;
} 