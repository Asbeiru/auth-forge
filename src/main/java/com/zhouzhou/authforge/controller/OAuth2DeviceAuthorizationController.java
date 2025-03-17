package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.dto.DeviceAuthorizationRequest;
import com.zhouzhou.authforge.service.OAuth2DeviceAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth 2.0 设备授权端点的 Spring 实现，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * 该端点用于处理设备授权请求，生成设备验证码和用户验证码。
 * 端点需要客户端认证以防止未授权的设备授权请求。
 *
 * 主要特性：
 * <ul>
 *     <li>支持设备授权流程</li>
 *     <li>要求客户端认证（Basic Auth 或 Bearer Token）</li>
 *     <li>生成设备验证码和用户验证码</li>
 *     <li>提供验证 URI</li>
 *     <li>完全符合 RFC 8628 规范</li>
 * </ul>
 *
 * 请求示例：
 * <pre>
 * POST /oauth2/device_authorization HTTP/1.1
 * Host: server.example.com
 * Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
 * Content-Type: application/x-www-form-urlencoded
 *
 * client_id=1406020730&scope=example_scope
 * </pre>
 *
 * 响应示例：
 * <pre>
 * HTTP/1.1 200 OK
 * Content-Type: application/json
 * Cache-Control: no-store
 *
 * {
 *     "device_code": "GmRhmhcxhwAzkoEqiMEg_DnyEysNkuNhszIySk9eS",
 *     "user_code": "WDJB-MJHT",
 *     "verification_uri": "https://example.com/device",
 *     "verification_uri_complete": "https://example.com/device?user_code=WDJB-MJHT",
 *     "expires_in": 1800,
 *     "interval": 5
 * }
 * </pre>
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@RestController
@RequestMapping("/oauth2/device_authorization")
@RequiredArgsConstructor
@Slf4j
public class OAuth2DeviceAuthorizationController {

    private final OAuth2DeviceAuthorizationService deviceAuthorizationService;

    /**
     * 处理设备授权请求。
     *
     * 该端点验证客户端凭据并生成设备验证码和用户验证码。
     * 如果客户端认证失败，则返回相应的错误响应。
     *
     * @param request HTTP 请求对象
     * @param clientId 客户端标识
     * @param scope 请求的权限范围
     * @return 包含设备验证码和用户验证码的响应
     */
    @PostMapping
    public ResponseEntity<?> authorizeDevice(
            HttpServletRequest request,
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "scope", required = false) String scope) {
        
        DeviceAuthorizationRequest deviceRequest = DeviceAuthorizationRequest.builder()
            .request(request)
            .clientId(clientId)
            .scope(scope)
            .build();
        
        return deviceAuthorizationService.authorizeDevice(deviceRequest);
    }
} 