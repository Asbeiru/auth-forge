package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.dto.DeviceTokenRequest;
import com.zhouzhou.authforge.dto.DeviceTokenResponse;
import org.springframework.http.ResponseEntity;

/**
 * OAuth 2.0 设备授权令牌服务接口，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * 该服务负责处理设备授权令牌请求，包括：
 * 1. 验证设备码和客户端ID
 * 2. 检查授权状态
 * 3. 生成访问令牌
 * 4. 处理轮询限制
 *
 * @author zhouzhou
 * @since 1.0.0
 */
public interface OAuth2DeviceTokenService {

    /**
     * 处理设备授权令牌请求。
     *
     * 该方法验证设备码并返回访问令牌或相应的错误响应。
     * 如果设备码尚未授权，则返回 authorization_pending 错误。
     * 如果设备码已过期，则返回 expired_token 错误。
     * 如果轮询过于频繁，则返回 slow_down 错误。
     *
     * @param request 设备令牌请求
     * @return 包含访问令牌或错误信息的响应
     */
    ResponseEntity<DeviceTokenResponse> getDeviceToken(DeviceTokenRequest request);
} 