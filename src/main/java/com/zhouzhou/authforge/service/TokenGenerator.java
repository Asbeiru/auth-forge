package com.zhouzhou.authforge.service;

import java.time.LocalDateTime;

/**
 * 令牌生成器接口
 * 
 * 用于生成访问令牌和刷新令牌
 */
public interface TokenGenerator {
    
    /**
     * 生成访问令牌
     *
     * @param subject 令牌主体（用户ID或客户端ID）
     * @param scope 授权范围
     * @param validitySeconds 有效期（秒）
     * @return 访问令牌
     */
    String generateAccessToken(String subject, String scope, Integer validitySeconds);
    
    /**
     * 生成刷新令牌
     */
    String generateRefreshToken();

    /**
     * 生成授权码
     */
    String generateAuthorizationCode();

    /**
     * 计算访问令牌的过期时间
     *
     * @param validitySeconds 有效期（秒）
     * @return 过期时间
     */
    default LocalDateTime calculateAccessTokenExpiryTime(Integer validitySeconds) {
        return LocalDateTime.now().plusSeconds(validitySeconds);
    }
} 