package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.model.OAuthClient;

import java.util.Optional;

/**
 * OAuth 2.0 客户端服务接口
 * 
 * 定义 OAuth 2.0 客户端管理的核心业务逻辑
 */
public interface OAuth2ClientService {
    
    /**
     * 根据客户端ID查找客户端
     * @param clientId 客户端ID
     * @return 客户端信息
     */
    Optional<OAuthClient> findByClientId(String clientId);

    /**
     * 验证客户端密钥
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @return 验证是否通过
     */
    boolean validateClientSecret(String clientId, String clientSecret);

    /**
     * 验证重定向URI是否合法
     * @param clientId 客户端ID
     * @param redirectUri 重定向URI
     * @return 验证是否通过
     */
    boolean validateRedirectUri(String clientId, String redirectUri);
} 