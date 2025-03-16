package com.zhouzhou.authforge.security.impl;

import com.zhouzhou.authforge.exception.OAuth2AuthenticationException;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthClientRepository;
import com.zhouzhou.authforge.security.AbstractClientAuthenticator;
import com.zhouzhou.authforge.security.ClientAuthenticationMethod;
import com.zhouzhou.authforge.security.ClientAuthenticationToken;
import com.zhouzhou.authforge.service.PkceValidationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 客户端密钥基本认证器实现
 * 
 * 从Authorization请求头中获取Basic认证信息进行客户端认证
 * 参考OAuth 2.0规范 2.3.1节：
 * https://datatracker.ietf.org/doc/html/rfc6749#section-2.3.1
 */
@Component
public class ClientSecretBasicAuthenticator extends AbstractClientAuthenticator {

    private static final String BASIC_AUTH_PREFIX = "Basic ";
    private static final String CODE_PARAM = "code";
    private static final String CODE_VERIFIER_PARAM = "code_verifier";

    private final OAuthClientRepository clientRepository;
    private final PkceValidationService pkceValidationService;

    public ClientSecretBasicAuthenticator(OAuthClientRepository clientRepository, PkceValidationService pkceValidationService) {
        super(clientRepository);
        this.clientRepository = clientRepository;
        this.pkceValidationService = pkceValidationService;
    }

    @Override
    public ClientAuthenticationMethod getAuthenticationMethod() {
        return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
    }

    @Override
    public ClientAuthenticationToken tryExtractCredentials(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith(BASIC_AUTH_PREFIX)) {
            return null;
        }

        String base64Credentials = header.substring(BASIC_AUTH_PREFIX.length());
        String credentials;
        try {
            credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }

        String[] parts = credentials.split(":", 2);
        if (parts.length != 2) {
            return null;
        }

        String clientId = parts[0];
        String clientSecret = parts[1];

        // 验证客户端ID和密钥格式（VSCHAR: %x20-7E）
        if (!isValidCredentials(clientId) || !isValidCredentials(clientSecret)) {
            return null;
        }

        return ClientAuthenticationToken.builder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
    }

    @Override
    public void validateCredentials(ClientAuthenticationToken token, OAuthClient client) {
        // 验证客户端是否支持当前认证方法
        if (!client.getClientAuthenticationMethodSet().contains(getAuthenticationMethod())) {
            throw new OAuth2AuthenticationException(
                "invalid_client",
                "Client does not support " + getAuthenticationMethod() + " authentication method"
            );
        }

        // 验证客户端密钥
        validateClientSecret(token.getClientSecret(), client);
    }

    public boolean isValidCredentials(String credentials) {
        if (!StringUtils.hasText(credentials)) {
            return false;
        }

        // 验证格式（VSCHAR: %x20-7E）
        for (int i = 0; i < credentials.length(); i++) {
            char c = credentials.charAt(i);
            if (c < 32 || c > 126) {
                return false;
            }
        }
        return true;
    }
} 