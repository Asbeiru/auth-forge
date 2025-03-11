package com.zhouzhou.authforge.exception;

import lombok.Getter;

/**
 * OAuth 2.0 授权异常类
 * 
 * 处理 OAuth 2.0 授权流程中的异常情况：
 * 1. 无效的请求参数
 * 2. 未授权的客户端
 * 3. 访问被拒绝
 * 4. 无效的授权范围
 * 
 * 包含：
 * - 错误码
 * - 重定向URI
 * - 状态参数
 */
@Getter
public class OAuth2AuthorizationException extends RuntimeException {
    private final String error;
    private final String redirectUri;
    private final String state;

    public OAuth2AuthorizationException(String error, String message, String redirectUri, String state) {
        super(message);
        this.error = error;
        this.redirectUri = redirectUri;
        this.state = state;
    }
} 