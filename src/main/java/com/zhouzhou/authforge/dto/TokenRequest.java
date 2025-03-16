package com.zhouzhou.authforge.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 令牌请求数据对象
 */
@Getter
@Builder
public class TokenRequest {
    private final HttpServletRequest request;
    private final String grantType;
    private final String code;
    private final String redirectUri;
    private final String codeVerifier;
    private final String refreshToken;
    private final String scope;

    public boolean isAuthorizationCodeGrant() {
        return "authorization_code".equals(grantType);
    }

    public boolean isRefreshTokenGrant() {
        return "refresh_token".equals(grantType);
    }

    public boolean isClientCredentialsGrant() {
        return "client_credentials".equals(grantType);
    }
}