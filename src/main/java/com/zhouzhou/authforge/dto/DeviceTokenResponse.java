package com.zhouzhou.authforge.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * OAuth 2.0 设备授权令牌响应 DTO，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * 该 DTO 用于封装设备授权令牌响应的数据，包括：
 * - 成功响应：访问令牌、令牌类型、过期时间等
 * - 错误响应：错误代码、错误描述等
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@Getter
@Builder
public class DeviceTokenResponse {
    
    /**
     * 访问令牌
     */
    private final String accessToken;
    
    /**
     * 令牌类型（通常是 "Bearer"）
     */
    private final String tokenType;
    
    /**
     * 令牌过期时间（秒）
     */
    private final Integer expiresIn;
    
    /**
     * 错误代码
     */
    private final String error;
    
    /**
     * 错误描述
     */
    private final String errorDescription;
} 