package com.zhouzhou.authforge.security.impl;

import com.zhouzhou.authforge.exception.OAuth2AuthenticationException;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthClientRepository;
import com.zhouzhou.authforge.security.AbstractClientAuthenticator;
import com.zhouzhou.authforge.security.ClientAuthenticationMethod;
import com.zhouzhou.authforge.security.ClientAuthenticationToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * JWT断言认证器实现
 * 
 * 使用JWT断言进行客户端认证
 * 参考OAuth 2.0规范：
 * https://datatracker.ietf.org/doc/html/rfc7523
 */
@Slf4j
@Component
public class JwtAssertionAuthenticator extends AbstractClientAuthenticator {

    private static final String CLIENT_ASSERTION_TYPE_PARAM = "client_assertion_type";
    private static final String CLIENT_ASSERTION_PARAM = "client_assertion";
    private static final String EXPECTED_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    public JwtAssertionAuthenticator(OAuthClientRepository clientRepository) {
        super(clientRepository);
    }

    @Override
    public ClientAuthenticationMethod getAuthenticationMethod() {
        return ClientAuthenticationMethod.PRIVATE_KEY_JWT;
    }

    @Override
    public ClientAuthenticationToken tryExtractCredentials(HttpServletRequest request) {
        // 1. 获取断言类型和JWT
        String assertionType = request.getParameter(CLIENT_ASSERTION_TYPE_PARAM);
        String assertion = request.getParameter(CLIENT_ASSERTION_PARAM);

        if (!StringUtils.hasText(assertionType) || !StringUtils.hasText(assertion)) {
            return null;
        }

        if (!EXPECTED_ASSERTION_TYPE.equals(assertionType)) {
            return null;
        }

        // 2. 从JWT中提取客户端ID（不验证签名）
        String clientId;
        try {
            Claims claims = Jwts.parserBuilder()
                .build()
                .parseClaimsJws(assertion)
                .getBody();
            clientId = claims.getIssuer();
        } catch (Exception e) {
            return null;
        }

        if (!StringUtils.hasText(clientId)) {
            return null;
        }

        return ClientAuthenticationToken.builder()
            .clientId(clientId)
            .clientAssertion(assertion)
            .clientAssertionType(assertionType)
            .build();
    }

    @Override
    public void validateCredentials(ClientAuthenticationToken token, OAuthClient client) throws OAuth2AuthenticationException {
        String assertion = token.getClientAssertion();
        if (assertion == null) {
            throw new OAuth2AuthenticationException("invalid_client", "Missing client_assertion");
        }

        try {
            // 验证JWT签名和声明
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(client.getClientSecret().getBytes())
                .build()
                .parseClaimsJws(assertion)
                .getBody();

            // 验证声明
            validateClaims(claims, client);

        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw new OAuth2AuthenticationException("invalid_client", "Invalid JWT: " + e.getMessage());
        }
    }

    private void validateClaims(Claims claims, OAuthClient client) {
        // 验证过期时间
        if (claims.getExpiration() == null) {
            throw new OAuth2AuthenticationException("invalid_client", "JWT must have an expiration time");
        }
        if (claims.getExpiration().before(new Date())) {
            throw new OAuth2AuthenticationException("invalid_client", "JWT has expired");
        }

        // 验证签发时间
        if (claims.getIssuedAt() == null) {
            throw new OAuth2AuthenticationException("invalid_client", "JWT must have an issued at time");
        }
        if (claims.getIssuedAt().after(new Date())) {
            throw new OAuth2AuthenticationException("invalid_client", "JWT issued at future time");
        }

        // 验证受众
        String audience = claims.getAudience();
        if (audience == null || !audience.equals(client.getClientId())) {
            throw new OAuth2AuthenticationException("invalid_client", "Invalid audience");
        }

        // 验证签发者
        String issuer = claims.getIssuer();
        if (issuer == null || !issuer.equals(client.getClientId())) {
            throw new OAuth2AuthenticationException("invalid_client", "Invalid issuer");
        }
    }
} 