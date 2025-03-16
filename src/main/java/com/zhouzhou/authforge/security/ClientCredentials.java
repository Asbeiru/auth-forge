package com.zhouzhou.authforge.security;

import lombok.Builder;
import lombok.Getter;

/**
 * 客户端凭证
 */
@Getter
@Builder
public class ClientCredentials {

    /**
     * 客户端ID
     */
    private final String clientId;

    /**
     * 客户端密钥
     */
    private final String clientSecret;

    /**
     * 授权码
     */
    private final String code;

    /**
     * PKCE code_verifier
     */
    private final String codeVerifier;

    /**
     * JWT 断言
     */
    private final String assertion;

    /**
     * JWT 断言类型
     */
    private final String assertionType;
} 