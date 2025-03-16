package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.service.TokenGenerator;
import java.util.UUID;

/**
 * UUID 令牌生成器实现
 * 
 * 使用 UUID 生成刷新令牌和授权码
 */
public class UuidTokenGenerator implements TokenGenerator {

    @Override
    public String generateAccessToken(String subject, String scope, Integer validitySeconds) {
        return UUID.randomUUID().toString();
    }

    @Override
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String generateAuthorizationCode() {
        return UUID.randomUUID().toString();
    }
} 