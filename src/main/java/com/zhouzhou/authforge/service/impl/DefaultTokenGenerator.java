package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.service.TokenGenerator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/**
 * 默认令牌生成器实现
 * 
 * 使用 JWT 生成访问令牌，UUID 生成刷新令牌和授权码
 */
@Service
public class DefaultTokenGenerator implements TokenGenerator {

    private final SecretKey secretKey;

    public DefaultTokenGenerator(@Value("${auth.jwt.secret-key:}") String configuredSecretKey) {
        // 如果没有配置密钥，则生成一个随机密钥
        this.secretKey = configuredSecretKey.isEmpty() ? 
            Keys.secretKeyFor(SignatureAlgorithm.HS256) :
            Keys.hmacShaKeyFor(configuredSecretKey.getBytes());
    }

    @Override
    public String generateAccessToken(String subject, String scope, Integer validitySeconds) {
        LocalDateTime expiryTime = calculateAccessTokenExpiryTime(validitySeconds);
        Date expirationDate = Date.from(expiryTime.atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setSubject(subject)
                .claim("scope", scope)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String generateAuthorizationCode() {
        // 生成一个随机的授权码
        return UUID.randomUUID().toString();
    }
} 