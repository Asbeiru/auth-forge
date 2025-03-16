package com.zhouzhou.authforge.security;

import com.zhouzhou.authforge.exception.OAuth2AuthenticationException;
import com.zhouzhou.authforge.model.OAuthClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 客户端认证器链
 * 
 * 负责协调多个认证器，按顺序尝试认证，直到成功或全部失败
 */
@Component
@RequiredArgsConstructor
public class ClientAuthenticatorChain {

    private final List<ClientAuthenticator> authenticators;

    /**
     * 执行客户端认证
     * 
     * @param request HTTP请求
     * @return 认证成功的客户端信息
     * @throws OAuth2AuthenticationException 如果认证失败
     */
    public OAuthClient authenticate(HttpServletRequest request) throws OAuth2AuthenticationException {
        // 遍历所有认证器
        for (ClientAuthenticator authenticator : authenticators) {
            OAuthClient client = authenticator.doAuthenticate(request);
            if (client != null) {
                return client;
            }
        }

        // 所有认证器都无法认证
        throw new OAuth2AuthenticationException(
            "invalid_client",
            "Client authentication failed"
        );
    }
} 