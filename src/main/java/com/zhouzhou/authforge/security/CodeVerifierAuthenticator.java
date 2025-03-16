package com.zhouzhou.authforge.security;

import com.zhouzhou.authforge.exception.OAuth2AuthenticationException;
import com.zhouzhou.authforge.model.OAuthAuthorization;
import com.zhouzhou.authforge.repository.OAuthAuthorizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * PKCE 验证器
 * 
 * 用于验证授权码流程中的 code_verifier 参数
 * 参考 RFC 7636：
 * https://datatracker.ietf.org/doc/html/rfc7636
 */
@Component
@RequiredArgsConstructor
public class CodeVerifierAuthenticator {

    private static final String CODE_VERIFIER_PARAM = "code_verifier";
    private static final String CODE_PARAM = "code";

    private final OAuthAuthorizationRepository authorizationRepository;

    /**
     * 验证code_verifier（必需）
     */
    public void authenticateRequired(String clientId, String code, String codeVerifier) {
        // 1. 验证参数
        if (!StringUtils.hasText(code)) {
            throw new OAuth2AuthenticationException(
                "invalid_request",
                "Missing code parameter"
            );
        }

        if (!StringUtils.hasText(codeVerifier)) {
            throw new OAuth2AuthenticationException(
                "invalid_request",
                "Missing code_verifier parameter"
            );
        }

        // 2. 查找授权记录
        OAuthAuthorization authorization = authorizationRepository.findByAuthorizationCode(code)
            .orElseThrow(() -> new OAuth2AuthenticationException(
                "invalid_grant",
                "Invalid authorization code"
            ));

        // 3. 验证客户端ID
        if (!authorization.getClientId().equals(clientId)) {
            throw new OAuth2AuthenticationException(
                "invalid_grant",
                "Client ID mismatch"
            );
        }

        // 4. 验证code_challenge是否存在
        String codeChallenge = authorization.getCodeChallenge();
        if (!StringUtils.hasText(codeChallenge)) {
            throw new OAuth2AuthenticationException(
                "invalid_grant",
                "Code challenge not found"
            );
        }

        // 5. 验证code_verifier
        String codeChallengeMethod = authorization.getCodeChallengeMethod();
        String computedCodeChallenge;

        if ("S256".equals(codeChallengeMethod)) {
            computedCodeChallenge = generateS256CodeChallenge(codeVerifier);
        } else if ("plain".equals(codeChallengeMethod)) {
            computedCodeChallenge = codeVerifier;
        } else {
            throw new OAuth2AuthenticationException(
                "invalid_grant",
                "Unsupported code challenge method: " + codeChallengeMethod
            );
        }

        if (!codeChallenge.equals(computedCodeChallenge)) {
            throw new OAuth2AuthenticationException(
                "invalid_grant",
                "Invalid code verifier"
            );
        }
    }

    /**
     * 验证code_verifier（如果存在）
     */
    public void authenticateIfAvailable(String clientId, String code, String codeVerifier) {
        // 如果没有code_verifier，直接返回
        if (!StringUtils.hasText(codeVerifier)) {
            return;
        }

        // 如果有code_verifier，执行验证
        authenticateRequired(clientId, code, codeVerifier);
    }

    private String generateS256CodeChallenge(String codeVerifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
} 