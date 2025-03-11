package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.model.OAuthAuthorization;
import com.zhouzhou.authforge.model.OAuthClient;
import org.springframework.security.core.Authentication;

import java.util.Set;

public interface OAuthAuthorizationService {
    
    /**
     * 检查是否需要用户授权
     */
    boolean isAuthorizationConsentRequired(OAuthClient client, String userId, Set<String> scopes);
    
    /**
     * 生成授权码
     */
    String generateAuthorizationCode(OAuthClient client, String userId, Set<String> scopes);
    
    /**
     * 保存用户授权同意
     */
    void saveAuthorizationConsent(String clientId, String userId, Set<String> scopes);
    
    /**
     * 验证授权码
     */
    OAuthAuthorization validateAuthorizationCode(String code, String clientId);
} 