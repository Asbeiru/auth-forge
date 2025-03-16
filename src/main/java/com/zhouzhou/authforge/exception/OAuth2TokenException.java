package com.zhouzhou.authforge.exception;

/**
 * OAuth 2.0 令牌异常
 */
public class OAuth2TokenException extends RuntimeException {
    private final String error;
    private final String errorDescription;

    public OAuth2TokenException(String error, String errorDescription) {
        super(errorDescription);
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