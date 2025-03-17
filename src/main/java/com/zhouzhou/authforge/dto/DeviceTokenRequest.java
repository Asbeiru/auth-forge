package com.zhouzhou.authforge.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * OAuth 2.0 设备授权令牌请求 DTO，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * 该 DTO 用于封装设备授权令牌请求的参数，包括：
 * - device_code：设备码
 * - client_id：客户端标识
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@Getter
@Builder
public class DeviceTokenRequest {
    
    /**
     * HTTP 请求对象
     */
    private final HttpServletRequest request;
    
    /**
     * 设备码
     */
    private final String deviceCode;
    
    /**
     * 客户端ID
     */
    private final String clientId;
} 