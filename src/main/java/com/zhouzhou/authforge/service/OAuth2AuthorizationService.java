package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.dto.AuthorizationResult;
import com.zhouzhou.authforge.model.OAuthAuthorization;
import org.springframework.security.core.Authentication;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface OAuth2AuthorizationService {
    
    /**
     * 处理授权请求
     * @param responseType 响应类型
     * @param clientId 客户端ID
     * @param redirectUri 重定向URI
     * @param scope 请求的权限范围
     * @param state 状态参数
     * @param additionalParameters 额外参数
     * @param authentication 用户认证信息
     * @return 授权结果
     */
    AuthorizationResult handleAuthorizationRequest(
            String responseType,
            String clientId,
            String redirectUri,
            String scope,
            String state,
            Map<String, Object> additionalParameters,
            Authentication authentication);

    /**
     * 处理用户同意授权
     * @param clientId 客户端ID
     * @param redirectUri 重定向URI
     * @param scope 请求的权限范围
     * @param state 状态参数
     * @param consent 用户同意结果
     * @param authentication 用户认证信息
     * @return 授权结果
     */
    AuthorizationResult handleAuthorizationConsent(
            String clientId,
            String redirectUri,
            String scope,
            String state,
            String consent,
            Authentication authentication);

    /**
     * 检查是否需要用户同意
     */
    boolean isConsentRequired(String clientId, String userId, Set<String> requestedScopes);

    /**
     * 根据授权码查找授权信息
     */
    Optional<OAuthAuthorization> findByCode(String code);

    /**
     * 验证授权码
     */
    void validateAuthorizationCode(
            String code,
            String clientId,
            String redirectUri,
            String codeVerifier);
} 