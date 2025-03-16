package com.zhouzhou.authforge.security.impl;

import com.zhouzhou.authforge.exception.OAuth2AuthenticationException;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthClientRepository;
import com.zhouzhou.authforge.security.AbstractClientAuthenticator;
import com.zhouzhou.authforge.security.ClientAuthenticationMethod;
import com.zhouzhou.authforge.security.ClientAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * POST参数认证实现
 * 从请求参数中提取客户端ID和密钥
 */
@Component
public class ClientSecretPostAuthenticator extends AbstractClientAuthenticator {

    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String CLIENT_SECRET_PARAM = "client_secret";

    public ClientSecretPostAuthenticator(OAuthClientRepository clientRepository) {
        super(clientRepository);
    }

    @Override
    public ClientAuthenticationMethod getAuthenticationMethod() {
        return ClientAuthenticationMethod.CLIENT_SECRET_POST;
    }

    @Override
    public ClientAuthenticationToken tryExtractCredentials(HttpServletRequest request) {
        String clientId = request.getParameter(CLIENT_ID_PARAM);
        String clientSecret = request.getParameter(CLIENT_SECRET_PARAM);

        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            return null;
        }

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