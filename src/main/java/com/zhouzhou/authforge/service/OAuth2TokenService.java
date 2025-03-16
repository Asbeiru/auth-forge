package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.dto.TokenResponse;
import com.zhouzhou.authforge.model.OAuthClient;
import jakarta.servlet.http.HttpServletRequest;

/**
 * OAuth 2.0 令牌服务接口
 * 
 * 定义令牌端点的核心业务逻辑，包括：
 * 1. 客户端认证
 * 2. 授权码验证
 * 3. 令牌生成
 * 4. 令牌响应
 */
public interface OAuth2TokenService {

    /**
     * 处理令牌请求
     *
     * @param request HTTP请求
     * @param grantType 授权类型
     * @param code 授权码
     * @param redirectUri 重定向URI
     * @param codeVerifier PKCE验证码
     * @param refreshToken 刷新令牌
     * @return 令牌响应
     */
    TokenResponse handleTokenRequest(
            HttpServletRequest request,
            String grantType,
            String code,
            String redirectUri,
            String codeVerifier,
            String refreshToken);

    /**
     * 处理刷新令牌请求
     * 
     * @param refreshToken 刷新令牌
     * @param client 客户端信息
     * @return 令牌响应
     */
    TokenResponse handleRefreshTokenGrant(String refreshToken, OAuthClient client);
} 