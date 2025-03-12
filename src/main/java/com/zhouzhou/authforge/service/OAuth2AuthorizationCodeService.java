package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.model.OAuthAuthorization;
import com.zhouzhou.authforge.model.OAuthClient;
import org.springframework.security.core.Authentication;

import java.util.Map;

/**
 * OAuth 2.0 授权码服务接口
 */
public interface OAuth2AuthorizationCodeService {

    /**
     * 生成并保存授权码
     * @param client 客户端信息
     * @param authentication 用户认证信息
     * @param scope 授权范围
     * @param state 状态参数
     * @param redirectUri 重定向URI
     * @param additionalParameters 额外参数
     * @return 授权信息
     */
    OAuthAuthorization createAuthorizationCode(
            OAuthClient client,
            Authentication authentication,
            String scope,
            String state,
            String redirectUri,
            Map<String, Object> additionalParameters);

    /**
     * 验证授权码
     * @param code 授权码
     * @param clientId 客户端ID
     * @param redirectUri 重定向URI
     * @param codeVerifier PKCE验证码
     */
    void validateAuthorizationCode(
            String code,
            String clientId,
            String redirectUri,
            String codeVerifier);
} 