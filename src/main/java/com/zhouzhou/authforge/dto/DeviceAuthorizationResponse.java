package com.zhouzhou.authforge.dto;

import lombok.Builder;
import lombok.Data;

/**
 * OAuth 2.0 设备授权响应 DTO，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * 该 DTO 用于封装设备授权响应的参数，包括：
 * - device_code：设备验证码
 * - user_code：用户验证码
 * - verification_uri：验证 URI
 * - verification_uri_complete：完整的验证 URI
 * - expires_in：过期时间
 * - interval：轮询间隔
 * - error：错误代码（可选）
 * - error_description：错误描述（可选）
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@Data
@Builder
public class DeviceAuthorizationResponse {
    private String deviceCode;
    private String userCode;
    private String verificationUri;
    private String verificationUriComplete;
    private Integer expiresIn;
    private Integer interval;
    private String error;
    private String errorDescription;
} 