package com.zhouzhou.authforge.model;

/**
 * OAuth 2.0 设备授权状态枚举，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * @author zhouzhou
 * @since 1.0.0
 */
public enum DeviceAuthorizationStatus {
    
    /**
     * 待处理状态
     * 设备授权请求已创建，等待用户输入验证码进行授权
     */
    PENDING,
    
    /**
     * 已批准状态
     * 用户已输入验证码并批准授权
     */
    APPROVED,
    
    /**
     * 已拒绝状态
     * 用户已拒绝授权请求
     */
    DENIED,
    
    /**
     * 已过期状态
     * 设备码或用户码已过期
     */
    EXPIRED,
    
    /**
     * 已完成状态
     * 用户已批准授权，且访问令牌已生成
     */
    COMPLETED
} 