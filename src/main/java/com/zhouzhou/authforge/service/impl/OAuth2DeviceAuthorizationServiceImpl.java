package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.dto.DeviceAuthorizationRequest;
import com.zhouzhou.authforge.dto.DeviceAuthorizationResponse;
import com.zhouzhou.authforge.exception.OAuth2DeviceAuthorizationException;
import com.zhouzhou.authforge.model.DeviceAuthorizationEntity;
import com.zhouzhou.authforge.model.DeviceAuthorizationStatus;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.DeviceAuthorizationRepository;
import com.zhouzhou.authforge.security.ClientAuthenticatorChain;
import com.zhouzhou.authforge.service.OAuth2DeviceAuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * OAuth 2.0 设备授权服务实现类，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@Service
@Slf4j
public class OAuth2DeviceAuthorizationServiceImpl implements OAuth2DeviceAuthorizationService {

    private final ClientAuthenticatorChain clientAuthenticatorChain;
    private final DeviceAuthorizationRepository deviceAuthorizationRepository;
    private final SecureRandom secureRandom;

    // 设备验证码字符集：去除了容易混淆的字符
    private static final char[] DEVICE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    // 用户验证码字符集：仅使用大写字母，避免混淆
    private static final char[] USER_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    @Value("${auth.device.verification-uri}")
    private String verificationUri;

    @Value("${auth.device.expires-in:1800}")
    private Integer expiresIn;

    @Value("${auth.device.interval:5}")
    private Integer defaultInterval;

    public OAuth2DeviceAuthorizationServiceImpl(
            ClientAuthenticatorChain clientAuthenticatorChain,
            DeviceAuthorizationRepository deviceAuthorizationRepository) {
        this.clientAuthenticatorChain = clientAuthenticatorChain;
        this.deviceAuthorizationRepository = deviceAuthorizationRepository;
        // 使用强随机数生成器初始化
        this.secureRandom = new SecureRandom();
        // 预热 SecureRandom
        this.secureRandom.nextBytes(new byte[64]);
    }

    @Override
    @Transactional
    public ResponseEntity<DeviceAuthorizationResponse> authorizeDevice(DeviceAuthorizationRequest request) {
        try {
            // 1. 验证客户端凭据
            OAuthClient client = clientAuthenticatorChain.authenticate(request.getRequest());
            
            // 2. 验证请求中的 client_id 与认证的客户端是否匹配
            if (!client.getClientId().equals(request.getClientId())) {
                throw new OAuth2DeviceAuthorizationException("invalid_client", "Client ID mismatch");
            }

            // 3. 生成设备验证码和用户验证码
            String deviceCode = generateDeviceCode();
            String userCode = generateUserCode();

            // 4. 存储验证码信息
            DeviceAuthorizationEntity entity = new DeviceAuthorizationEntity();
            entity.setDeviceCode(deviceCode);
            entity.setUserCode(userCode);
            entity.setClientId(client.getClientId());
            entity.setScope(request.getScope());
            entity.setVerificationUri(verificationUri);
            entity.setStatus(DeviceAuthorizationStatus.PENDING);
            entity.setExpiresAt(Instant.now().plusSeconds(expiresIn));
            
            deviceAuthorizationRepository.save(entity);

            // 5. 构建响应
            DeviceAuthorizationResponse.DeviceAuthorizationResponseBuilder builder = 
                DeviceAuthorizationResponse.builder()
                    .deviceCode(deviceCode)
                    .userCode(userCode)
                    .verificationUri(verificationUri)
                    .expiresIn(expiresIn)
                    .interval(defaultInterval);

            return ResponseEntity.ok(builder.build());
        } catch (OAuth2DeviceAuthorizationException e) {
            log.warn("Device authorization failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(DeviceAuthorizationResponse.builder()
                    .error(e.getError())
                    .errorDescription(e.getErrorDescription())
                    .build());
        }
    }

    @Override
    @Transactional
    public DeviceAuthorizationStatus verifyUserCode(String userCode, boolean approve, int interval) {
        // 查找设备授权记录
        DeviceAuthorizationEntity deviceAuth = deviceAuthorizationRepository.findByUserCode(userCode)
            .orElseThrow(() -> new OAuth2DeviceAuthorizationException("invalid_request", "无效的验证码"));

        // 检查是否已过期
        if (deviceAuth.isExpired()) {
            deviceAuth.setStatus(DeviceAuthorizationStatus.EXPIRED);
            deviceAuthorizationRepository.save(deviceAuth);
            throw new OAuth2DeviceAuthorizationException("expired_token", "验证码已过期");
        }

        // 检查是否已经处理过
        if (!deviceAuth.isPending()) {
            throw new OAuth2DeviceAuthorizationException("invalid_request", "该验证码已被使用");
        }

        // 检查轮询间隔
        if (!deviceAuth.canPoll(interval)) {
            throw new OAuth2DeviceAuthorizationException("slow_down", "请稍后再试");
        }

        // 更新设备授权状态
        DeviceAuthorizationStatus newStatus = approve ? 
            DeviceAuthorizationStatus.APPROVED : 
            DeviceAuthorizationStatus.DENIED;
        
        deviceAuth.setStatus(newStatus);
        deviceAuth.updateLastPolledAt();
        deviceAuthorizationRepository.save(deviceAuth);

        log.debug("Device authorization status updated: userCode={}, status={}", userCode, newStatus);
        return newStatus;
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceAuthorizationStatus checkUserCodeStatus(String userCode, int interval) {
        return deviceAuthorizationRepository.findByUserCode(userCode)
            .map(deviceAuth -> {
                // 检查轮询间隔
                if (!deviceAuth.canPoll(interval)) {
                    throw new OAuth2DeviceAuthorizationException("slow_down", "请稍后再试");
                }

                // 检查是否已过期
                if (deviceAuth.isExpired() && deviceAuth.isPending()) {
                    deviceAuth.setStatus(DeviceAuthorizationStatus.EXPIRED);
                    deviceAuthorizationRepository.save(deviceAuth);
                    return DeviceAuthorizationStatus.EXPIRED;
                }

                // 更新最后轮询时间
                deviceAuth.updateLastPolledAt();
                deviceAuthorizationRepository.save(deviceAuth);

                return deviceAuth.getStatus();
            })
            .orElseThrow(() -> new OAuth2DeviceAuthorizationException("invalid_request", "无效的验证码"));
    }

    /**
     * 生成设备验证码。
     * 使用 SecureRandom 生成 40 位字符的设备验证码。
     * 字符集包含大写字母和数字，去除了容易混淆的字符（0,1,I,O）。
     *
     * @return 设备验证码
     */
    private String generateDeviceCode() {
        // 生成 40 位字符的设备验证码
        StringBuilder code = new StringBuilder(40);
        for (int i = 0; i < 40; i++) {
            code.append(DEVICE_CODE_CHARS[secureRandom.nextInt(DEVICE_CODE_CHARS.length)]);
            // 每 8 位添加一个分隔符，提高可读性
            if (i < 39 && (i + 1) % 8 == 0) {
                code.append('-');
            }
        }
        return code.toString();
    }

    /**
     * 生成用户验证码。
     * 使用 SecureRandom 生成 8 位字符的用户验证码，格式为：XXXX-XXXX。
     * 字符集仅包含大写字母，去除了容易混淆的字符（I,O）。
     *
     * @return 用户验证码
     */
    private String generateUserCode() {
        // 生成 8 位字符的用户验证码
        StringBuilder code = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            code.append(USER_CODE_CHARS[secureRandom.nextInt(USER_CODE_CHARS.length)]);
            // 4 位后添加分隔符
            if (i == 3) {
                code.append('-');
            }
        }
        return code.toString();
    }
} 