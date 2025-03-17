package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.dto.DeviceAuthorizationRequest;
import com.zhouzhou.authforge.dto.DeviceAuthorizationResponse;
import com.zhouzhou.authforge.model.DeviceAuthorizationStatus;
import org.springframework.http.ResponseEntity;

/**
 * OAuth 2.0 设备授权服务接口，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * 该服务负责处理设备授权流程，包括：
 * 1. 生成设备验证码和用户验证码
 * 2. 创建验证 URI
 * 3. 管理验证码的生命周期
 * 4. 处理客户端认证
 *
 * @author zhouzhou
 * @since 1.0.0
 */
public interface OAuth2DeviceAuthorizationService {

    /**
     * 处理设备授权请求。
     *
     * 该方法验证客户端凭据并生成设备验证码和用户验证码。
     * 如果客户端认证失败，则返回相应的错误响应。
     *
     * @param request 设备授权请求
     * @return 包含设备验证码和用户验证码的响应
     */
    ResponseEntity<DeviceAuthorizationResponse> authorizeDevice(DeviceAuthorizationRequest request);

    /**
     * 验证用户提供的验证码并更新设备授权状态。
     *
     * @param userCode 用户验证码
     * @param approve 是否批准授权
     * @param interval 轮询间隔（秒）
     * @return 更新后的授权状态
     * @throws com.zhouzhou.authforge.exception.OAuth2DeviceAuthorizationException 当验证码无效、已过期或已被使用时
     */
    DeviceAuthorizationStatus verifyUserCode(String userCode, boolean approve, int interval);

    /**
     * 检查验证码状态。
     *
     * @param userCode 用户验证码
     * @param interval 轮询间隔（秒）
     * @return 当前的授权状态
     * @throws com.zhouzhou.authforge.exception.OAuth2DeviceAuthorizationException 当验证码无效或需要限制轮询频率时
     */
    DeviceAuthorizationStatus checkUserCodeStatus(String userCode, int interval);
} 