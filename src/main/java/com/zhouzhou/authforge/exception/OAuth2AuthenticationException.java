package com.zhouzhou.authforge.exception;

/**
 * OAuth 2.0 认证异常
 */
public class OAuth2AuthenticationException extends RuntimeException {
    private final String error;
    private final String errorDescription;

    public OAuth2AuthenticationException(String error, String errorDescription) {
        super(errorDescription);
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public OAuth2AuthenticationException(String error, String errorDescription, Throwable cause) {
        super(errorDescription, cause);
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
} 