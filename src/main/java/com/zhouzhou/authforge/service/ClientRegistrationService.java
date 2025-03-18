package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.dto.ClientRegistrationRequest;
import com.zhouzhou.authforge.dto.ClientRegistrationResponse;

/**
 * OAuth 2.0 客户端注册服务接口
 */
public interface ClientRegistrationService {
    
    /**
     * 注册新的客户端
     *
     * @param request 客户端注册请求
     * @param initialAccessToken 可选的初始访问令牌
     * @return 客户端注册响应
     */
    ClientRegistrationResponse registerClient(ClientRegistrationRequest request, String initialAccessToken);
    
    /**
     * 验证重定向URI
     *
     * @param redirectUris 重定向URI列表
     * @return 如果所有URI都有效则返回true
     */
    boolean validateRedirectUris(Iterable<String> redirectUris);
    
    /**
     * 生成客户端标识符
     *
     * @return 唯一的客户端标识符
     */
    String generateClientId();
    
    /**
     * 生成客户端密钥
     *
     * @return 客户端密钥
     */
    String generateClientSecret();
    
    /**
     * 生成注册访问令牌
     *
     * @param clientId 客户端标识符
     * @return 注册访问令牌
     */
    String generateRegistrationAccessToken(String clientId);
} 