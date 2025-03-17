package com.zhouzhou.authforge.model;

/**
 * 设备授权状态枚举。
 * 定义了设备授权流程中可能的状态。
 *
 * @author zhouzhou
 * @since 1.0.0
 */
public enum DeviceAuthorizationStatus {
    /**
     * 待处理状态
     * 表示设备授权请求已创建但尚未被用户处理
     */
    PENDING,

    /**
     * 已批准状态
     * 表示用户已同意设备授权请求
     */
    APPROVED,

    /**
     * 已拒绝状态
     * 表示用户已拒绝设备授权请求
     */
    DENIED,

    /**
     * 已过期状态
     * 表示设备授权请求已超过有效期
     */
    EXPIRED
} 