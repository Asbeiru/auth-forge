package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.model.DeviceAuthorizationStatus;

/**
 * 设备验证服务接口。
 * 处理设备授权流程中的用户验证和授权确认。
 *
 * @author zhouzhou
 * @since 1.0.0
 */
public interface DeviceVerificationService {

    /**
     * 验证用户提供的验证码并更新设备授权状态。
     *
     * @param userCode 用户验证码
     * @param approve 是否批准授权
     * @return 更新后的授权状态
     * @throws com.zhouzhou.authforge.exception.OAuth2DeviceAuthorizationException 当验证码无效、已过期或已被使用时
     */
    DeviceAuthorizationStatus verifyUserCode(String userCode, boolean approve);

    /**
     * 检查验证码状态。
     *
     * @param userCode 用户验证码
     * @return 当前的授权状态
     * @throws com.zhouzhou.authforge.exception.OAuth2DeviceAuthorizationException 当验证码无效时
     */
    DeviceAuthorizationStatus checkUserCodeStatus(String userCode);
} 