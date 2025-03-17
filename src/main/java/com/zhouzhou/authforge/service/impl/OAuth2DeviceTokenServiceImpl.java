package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.dto.DeviceTokenRequest;
import com.zhouzhou.authforge.dto.DeviceTokenResponse;
import com.zhouzhou.authforge.exception.OAuth2DeviceAuthorizationException;
import com.zhouzhou.authforge.model.DeviceAuthorizationEntity;
import com.zhouzhou.authforge.model.DeviceAuthorizationStatus;
import com.zhouzhou.authforge.model.OAuthAccessToken;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.DeviceAuthorizationRepository;
import com.zhouzhou.authforge.repository.OAuthAccessTokenRepository;
import com.zhouzhou.authforge.security.ClientAuthenticatorChain;
import com.zhouzhou.authforge.service.OAuth2DeviceTokenService;
import com.zhouzhou.authforge.service.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * OAuth 2.0 设备授权令牌服务实现类，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2DeviceTokenServiceImpl implements OAuth2DeviceTokenService {

    private final DeviceAuthorizationRepository deviceAuthorizationRepository;
    private final ClientAuthenticatorChain clientAuthenticatorChain;
    private final OAuthAccessTokenRepository accessTokenRepository;
    private final TokenGenerator tokenGenerator;

    @Value("${auth.token.access-token.expires-in:3600}")
    private Integer accessTokenExpiresIn;

    @Override
    @Transactional
    public ResponseEntity<DeviceTokenResponse> getDeviceToken(DeviceTokenRequest request) {
        try {
            // 1. 验证客户端凭据
            OAuthClient client = clientAuthenticatorChain.authenticate(request.getRequest());

            // 2. 查找设备授权记录
            DeviceAuthorizationEntity deviceAuth = deviceAuthorizationRepository.findByDeviceCode(request.getDeviceCode())
                .orElseThrow(() -> new OAuth2DeviceAuthorizationException(
                    "invalid_grant",
                    "Invalid device code"
                ));

            // 3. 验证客户端ID
            if (!deviceAuth.getClientId().equals(request.getClientId())) {
                log.warn("Client ID mismatch: requested={}, stored={}", 
                    request.getClientId(), deviceAuth.getClientId());
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("invalid_client", "Client ID mismatch"));
            }

            // 4. 检查是否已使用
            if (deviceAuth.isUsed()) {
                log.warn("Device code already used: {}", request.getDeviceCode());
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("invalid_grant", "Device code already used"));
            }

            // 5. 检查是否已过期
            if (deviceAuth.isExpired()) {
                deviceAuth.setStatus(DeviceAuthorizationStatus.EXPIRED);
                deviceAuthorizationRepository.save(deviceAuth);
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("expired_token", "The device code has expired"));
            }

            // 6. 检查轮询间隔
            if (!deviceAuth.canPoll(deviceAuth.getInterval())) {
                log.warn("Polling too frequently for device code: {}", request.getDeviceCode());
                deviceAuth.increasePollingInterval();
                deviceAuthorizationRepository.save(deviceAuth);
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("slow_down", "Polling too frequently"));
            }

            // 7. 检查授权状态
            if (deviceAuth.isPending()) {
                deviceAuth.updateLastPolledAt();
                deviceAuthorizationRepository.save(deviceAuth);
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("authorization_pending", "User has not approved the request yet"));
            }

            if (deviceAuth.isDenied()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("access_denied", "User denied the request"));
            }

            // 8. 验证和处理作用域
            Set<String> validScopes = validateAndFilterScopes(client, deviceAuth.getScope());
            String scopeString = String.join(" ", validScopes);

            // 9. 生成访问令牌
            LocalDateTime accessTokenExpiresAt = LocalDateTime.now()
                .plusSeconds(client.getAccessTokenValiditySeconds());

            OAuthAccessToken accessToken = OAuthAccessToken.builder()
                .clientId(client.getClientId())
                .accessToken(tokenGenerator.generateAccessToken(
                    "device_authorization",  // 使用device_authorization作为subject
                    scopeString,
                    client.getAccessTokenValiditySeconds()
                ))
                .scopes(scopeString)
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .status(OAuthAccessToken.TokenStatus.ACTIVE)
                .build();

            // 10. 保存访问令牌
            accessTokenRepository.save(accessToken);

            // 11. 更新设备授权状态
            deviceAuth.setStatus(DeviceAuthorizationStatus.COMPLETED);
            deviceAuth.setAccessToken(accessToken.getAccessToken());
            deviceAuth.setAccessTokenExpiresAt(accessTokenExpiresAt.toInstant(ZoneOffset.UTC));
            deviceAuth.updateLastPolledAt();
            deviceAuthorizationRepository.save(deviceAuth);

            // 12. 返回访问令牌
            return ResponseEntity.ok(DeviceTokenResponse.builder()
                .accessToken(accessToken.getAccessToken())
                .tokenType("Bearer")
                .expiresIn(client.getAccessTokenValiditySeconds())
                .build());

        } catch (OAuth2DeviceAuthorizationException e) {
            log.warn("Device token request failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getError(), e.getErrorDescription()));
        }
    }

    /**
     * 创建错误响应
     */
    private DeviceTokenResponse createErrorResponse(String error, String description) {
        return DeviceTokenResponse.builder()
            .error(error)
            .errorDescription(description)
            .build();
    }

    /**
     * 验证和处理作用域
     */
    private Set<String> validateAndFilterScopes(OAuthClient client, String requestedScope) {
        Set<String> validScopes = new HashSet<>();
        
        // 如果请求的作用域为空，使用客户端默认作用域
        if (!StringUtils.hasText(requestedScope)) {
            if (client.getScopes() != null) {
                validScopes.addAll(Arrays.asList(client.getScopes().split(" ")));
            }
            return validScopes;
        }

        // 验证请求的作用域
        Set<String> requestedScopes = new HashSet<>(Arrays.asList(requestedScope.split(" ")));
        Set<String> allowedScopes = new HashSet<>(Arrays.asList(client.getScopes().split(" ")));
        
        // 过滤出客户端允许的作用域
        for (String scope : requestedScopes) {
            if (allowedScopes.contains(scope)) {
                validScopes.add(scope);
            }
        }

        return validScopes;
    }
} 