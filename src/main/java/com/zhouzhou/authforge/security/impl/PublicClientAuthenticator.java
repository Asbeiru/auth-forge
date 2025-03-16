package com.zhouzhou.authforge.security.impl;

import com.zhouzhou.authforge.exception.OAuth2AuthenticationException;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthClientRepository;
import com.zhouzhou.authforge.security.AbstractClientAuthenticator;
import com.zhouzhou.authforge.security.ClientAuthenticationMethod;
import com.zhouzhou.authforge.security.ClientAuthenticationToken;
import com.zhouzhou.authforge.security.CodeVerifierAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 公共客户端认证器实现
 * 
 * 用于验证公共客户端（如移动应用、单页应用等）的身份
 * 参考OAuth 2.0规范 2.3节和RFC 7636：
 * https://datatracker.ietf.org/doc/html/rfc6749#section-2.3
 * https://datatracker.ietf.org/doc/html/rfc7636
 */
@Component
public class PublicClientAuthenticator extends AbstractClientAuthenticator {

    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String CODE_PARAM = "code";
    private static final String CODE_VERIFIER_PARAM = "code_verifier";

    private final CodeVerifierAuthenticator codeVerifierAuthenticator;

    public PublicClientAuthenticator(OAuthClientRepository clientRepository, CodeVerifierAuthenticator codeVerifierAuthenticator) {
        super(clientRepository);
        this.codeVerifierAuthenticator = codeVerifierAuthenticator;
    }

    @Override
    public ClientAuthenticationMethod getAuthenticationMethod() {
        return ClientAuthenticationMethod.NONE;
    }

    @Override
    public ClientAuthenticationToken tryExtractCredentials(HttpServletRequest request) {
        // 1. 获取client_id
        String clientId = request.getParameter(CLIENT_ID_PARAM);
        if (!StringUtils.hasText(clientId)) {
            return null;
        }

        // 2. 验证客户端ID格式（VSCHAR: %x20-7E）
        if (!isValidClientId(clientId)) {
            return null;
        }

        // 3. 获取code和code_verifier（如果存在）
        String code = request.getParameter(CODE_PARAM);
        String codeVerifier = request.getParameter(CODE_VERIFIER_PARAM);

        return ClientAuthenticationToken.builder()
            .clientId(clientId)
            .code(code)
            .codeVerifier(codeVerifier)
            .build();
    }

    @Override
    public void validateCredentials(ClientAuthenticationToken token, OAuthClient client) {
        // 验证code_verifier（如果存在）
        if (token.getCode() != null && token.getCodeVerifier() != null) {
            codeVerifierAuthenticator.authenticateIfAvailable(
                token.getClientId(),
                token.getCode(),
                token.getCodeVerifier()
            );
        }
    }

    private boolean isValidClientId(String clientId) {
        // 验证客户端ID格式（VSCHAR: %x20-7E）
        for (int i = 0; i < clientId.length(); i++) {
            char c = clientId.charAt(i);
            if (c < 32 || c > 126) {
                return false;
            }
        }
        return true;
    }
} 