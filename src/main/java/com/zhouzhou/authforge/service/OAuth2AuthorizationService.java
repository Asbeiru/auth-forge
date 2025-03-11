package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.authentication.OAuth2Authorization;
import com.zhouzhou.authforge.dto.AuthorizationResult;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;

public interface OAuth2AuthorizationService {
    
    /**
     * 处理授权请求
     */
    AuthorizationResult handleAuthorizationRequest(
            String responseType,
            String clientId,
            String redirectUri,
            String scope,
            String state,
            Map<String, Object> additionalParameters,
            Authentication principal);

    /**
     * 处理授权同意
     */
    AuthorizationResult handleAuthorizationConsent(
            String clientId,
            String redirectUri,
            String scope,
            String state,
            String consent,
            Authentication principal);

    /**
     * 查找授权信息
     */
    Optional<OAuth2Authorization> findByAuthorizationCode(String code);
} 