package com.zhouzhou.authforge.exception;

/**
 * OAuth 2.0 设备授权异常类，用于处理设备授权过程中的错误。
 *
 * @author zhouzhou
 * @since 1.0.0
 */
public class OAuth2DeviceAuthorizationException extends RuntimeException {
    private final String error;
    private final String errorDescription;

    /**
     * 构造函数。
     *
     * @param error 错误代码
     * @param errorDescription 错误描述
     */
    public OAuth2DeviceAuthorizationException(String error, String errorDescription) {
        super(errorDescription);
        this.error = error;
        this.errorDescription = errorDescription;
    }

    /**
     * 获取错误代码。
     *
     * @return 错误代码
     */
    public String getError() {
        return error;
    }

    /**
     * 获取错误描述。
     *
     * @return 错误描述
     */
    public String getErrorDescription() {
        return errorDescription;
    }
} 