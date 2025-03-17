package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.dto.DeviceTokenRequest;
import com.zhouzhou.authforge.dto.DeviceTokenResponse;
import com.zhouzhou.authforge.service.OAuth2DeviceTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth 2.0 设备授权令牌控制器，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@RestController
@RequestMapping("/oauth2/deviceToken")
@RequiredArgsConstructor
@Slf4j
public class OAuth2DeviceTokenController {

    private final OAuth2DeviceTokenService deviceTokenService;

    /**
     * 处理设备授权令牌请求。
     * 遵循 RFC 8628 规范，支持设备授权流程。
     *
     * @param request HTTP 请求
     * @param grantType 授权类型，必须为 "urn:ietf:params:oauth:grant-type:device_code"
     * @param deviceCode 设备验证码
     * @param clientId 客户端ID
     * @return 令牌响应
     */
    @PostMapping
    public ResponseEntity<DeviceTokenResponse> getDeviceToken(
            HttpServletRequest request,
            @RequestParam("grant_type") String grantType,
            @RequestParam("device_code") String deviceCode,
            @RequestParam(value = "client_id", required = false) String clientId) {

        // 验证授权类型
        if (!"urn:ietf:params:oauth:grant-type:device_code".equals(grantType)) {
            log.warn("Invalid grant type: {}", grantType);
            return ResponseEntity.badRequest()
                .body(DeviceTokenResponse.builder()
                    .error("unsupported_grant_type")
                    .errorDescription("Only device_code grant type is supported")
                    .build());
        }

        // 构建令牌请求
        DeviceTokenRequest tokenRequest = DeviceTokenRequest.builder()
            .request(request)
            .deviceCode(deviceCode)
            .clientId(clientId)
            .build();

        // 处理令牌请求
        return deviceTokenService.getDeviceToken(tokenRequest);
    }
} 