package com.zhouzhou.authforge.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;

/**
 * OAuth 2.0 设备授权请求 DTO，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * 该 DTO 用于封装设备授权请求的参数，包括：
 * - client_id：客户端标识
 * - scope：请求的权限范围
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@Data
@Builder
public class DeviceAuthorizationRequest {
    private HttpServletRequest request;
    private String clientId;
    private String scope;
} 