package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.exception.OAuth2DeviceAuthorizationException;
import com.zhouzhou.authforge.model.DeviceAuthorizationEntity;
import com.zhouzhou.authforge.model.DeviceAuthorizationStatus;
import com.zhouzhou.authforge.repository.DeviceAuthorizationRepository;
import com.zhouzhou.authforge.service.DeviceVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 设备验证服务实现类。
 * 处理设备授权流程中的用户验证和授权确认。
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceVerificationServiceImpl implements DeviceVerificationService {

    private final DeviceAuthorizationRepository deviceAuthorizationRepository;

    @Override
    @Transactional
    public DeviceAuthorizationStatus verifyUserCode(String userCode, boolean approve) {
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

        // 更新设备授权状态
        DeviceAuthorizationStatus newStatus = approve ? 
            DeviceAuthorizationStatus.APPROVED : 
            DeviceAuthorizationStatus.DENIED;
        
        deviceAuth.setStatus(newStatus);
        deviceAuthorizationRepository.save(deviceAuth);

        log.debug("Device authorization status updated: userCode={}, status={}", userCode, newStatus);
        return newStatus;
    }

    @Override
    public DeviceAuthorizationStatus checkUserCodeStatus(String userCode) {
        return deviceAuthorizationRepository.findByUserCode(userCode)
            .map(deviceAuth -> {
                if (deviceAuth.isExpired() && deviceAuth.isPending()) {
                    deviceAuth.setStatus(DeviceAuthorizationStatus.EXPIRED);
                    deviceAuthorizationRepository.save(deviceAuth);
                    return DeviceAuthorizationStatus.EXPIRED;
                }
                return deviceAuth.getStatus();
            })
            .orElseThrow(() -> new OAuth2DeviceAuthorizationException("invalid_request", "无效的验证码"));
    }
} 