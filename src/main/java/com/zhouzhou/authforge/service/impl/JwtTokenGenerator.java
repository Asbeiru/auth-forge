package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.service.TokenGenerator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 令牌生成器实现
 * 
 * 使用 JWT 格式生成访问令牌
 */
public class JwtTokenGenerator implements TokenGenerator {

    private final SecretKey secretKey;

    public JwtTokenGenerator(String configuredSecretKey) {
        // 如果没有配置密钥，则生成一个随机密钥
        this.secretKey = !StringUtils.hasText(configuredSecretKey) ? 
            Keys.secretKeyFor(SignatureAlgorithm.HS256) :
            Keys.hmacShaKeyFor(configuredSecretKey.getBytes());
    }

    @Override
    public String generateAccessToken(String subject, String scope, Integer validitySeconds) {
        LocalDateTime expiryTime = calculateAccessTokenExpiryTime(validitySeconds);
        Date expirationDate = Date.from(expiryTime.atZone(ZoneId.systemDefault()).toInstant());

        // 创建JWT header
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");

        return Jwts.builder()
                .setHeader(header)  // 设置header
                .setSubject(subject)
                .claim("scope", scope)
                .setId(UUID.randomUUID().toString())  // JWT ID
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)  // 显式指定算法
                .compact();
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