package com.zhouzhou.authforge.exception;

public class TemporaryServerErrorException extends OAuth2TokenException {
    
    public TemporaryServerErrorException(String message) {
        super("temporarily_unavailable", message);
    }
} 