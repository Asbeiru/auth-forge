package com.zhouzhou.authforge.security;

import com.zhouzhou.authforge.exception.OAuth2AuthenticationException;
import com.zhouzhou.authforge.model.OAuthClient;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端认证器接口
 * 
 * 定义客户端认证的标准流程，包括：
 * 1. 提取认证信息
 * 2. 验证认证信息
 * 3. 获取支持的认证方法
 */
public interface ClientAuthenticator {

    /**
     * 执行客户端认证
     * 
     * @param request HTTP请求
     * @return 认证成功的客户端信息，如果认证失败则返回null
     * @throws OAuth2AuthenticationException 如果认证过程中发生错误
     */
    OAuthClient doAuthenticate(HttpServletRequest request) throws OAuth2AuthenticationException;

    /**
     * 尝试从请求中提取客户端认证信息
     * 
     * @param request HTTP请求
     * @return 认证信息令牌，如果无法提取则返回null
     */
    ClientAuthenticationToken tryExtractCredentials(HttpServletRequest request);

    /**
     * 验证客户端认证信息
     * 
     * @param token 认证信息令牌
     * @param client 客户端信息
     * @throws OAuth2AuthenticationException 如果验证失败
     */
    void validateCredentials(ClientAuthenticationToken token, OAuthClient client) throws OAuth2AuthenticationException;

    /**
     * 获取此认证器支持的认证方法
     * 
     * @return 认证方法
     */
    ClientAuthenticationMethod getAuthenticationMethod();
} 