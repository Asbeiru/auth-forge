package com.zhouzhou.authforge.security;

import com.zhouzhou.authforge.exception.OAuth2AuthenticationException;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthClientRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 客户端认证器抽象基类
 * 使用模板方法模式实现认证流程
 */
@RequiredArgsConstructor
public abstract class AbstractClientAuthenticator implements ClientAuthenticator {

    protected final OAuthClientRepository clientRepository;

    /**
     * 模板方法：执行客户端认证
     * 定义了标准的认证流程：
     * 1. 提取认证信息
     * 2. 查找客户端
     * 3. 验证认证信息
     */
    @Override
    public final OAuthClient doAuthenticate(HttpServletRequest request) throws OAuth2AuthenticationException {
        // 1. 提取认证信息
        ClientAuthenticationToken token = tryExtractCredentials(request);
        if (token == null) {
            return null;
        }

        // 2. 查找客户端
        OAuthClient client = clientRepository.findByClientId(token.getClientId())
            .orElseThrow(() -> new OAuth2AuthenticationException(
                "invalid_client",
                "Client not found: " + token.getClientId()
            ));

        // 3. 验证认证信息
        validateCredentials(token, client);

        return client;
    }

    /**
     * 验证客户端凭据
     * 由子类实现以提供特定的验证逻辑
     */
    @Override
    public abstract void validateCredentials(ClientAuthenticationToken token, OAuthClient client);

    /**
     * 验证客户端密钥
     * 适用于client_secret_basic和client_secret_post认证方法
     */
    protected void validateClientSecret(String clientSecret, OAuthClient client) {
        if (clientSecret == null) {
            throw new OAuth2AuthenticationException(
                "invalid_client",
                "Missing client_secret"
            );
        }

        if (!client.getClientSecret().equals(clientSecret)) {
            throw new OAuth2AuthenticationException(
                "invalid_client",
                "Invalid client_secret"
            );
        }
    }

    /**
     * 验证字符串是否只包含有效字符（VSCHAR: %x20-7E）
     */
    protected boolean isValidCredentials(String value) {
        if (value == null) {
            return false;
        }
        
        for (char c : value.toCharArray()) {
            if (c < 0x20 || c > 0x7E) {
                return false;
            }
        }
        return true;
    }
} 