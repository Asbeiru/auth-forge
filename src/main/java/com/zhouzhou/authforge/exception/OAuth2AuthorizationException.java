package com.zhouzhou.authforge.exception;

import lombok.Getter;

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