package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.exception.OAuth2AuthenticationException;
import com.zhouzhou.authforge.model.OAuthAuthorization;
import com.zhouzhou.authforge.model.OAuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * PKCE 验证服务
 * 
 * 用于验证授权码流程中的 code_verifier 参数，支持：
 * 1. 公共客户端的必需 PKCE 验证
 * 2. 机密客户端的可选 PKCE 验证
 * 
 * 参考 RFC 7636：
 * https://datatracker.ietf.org/doc/html/rfc7636
 */
@Slf4j
@Service
public class PkceValidationService {

    /**
     * 验证 code_verifier
     * 
     * @param client OAuth 2.0 客户端
     * @param authorization 授权记录
     * @param codeVerifier PKCE code_verifier
     */
    public void validateCodeVerifier(OAuthClient client, OAuthAuthorization authorization, String codeVerifier) {
        // 1. 验证客户端ID
        if (!authorization.getClientId().equals(client.getClientId())) {
            throw new OAuth2AuthenticationException(
                "invalid_grant",
                "Client ID mismatch"
            );
        }

        // 2. 获取 code_challenge
        String codeChallenge = authorization.getCodeChallenge();

        // 3. 根据客户端配置决定是否需要验证 PKCE
        boolean isPublicClient = "none".equals(client.getClientAuthenticationMethods());
        boolean requireProofKey = isPublicClient || Boolean.TRUE.equals(client.getRequireProofKey());

        // 如果存在 code_challenge 或者需要 PKCE，则必须验证 code_verifier
        if (StringUtils.hasText(codeChallenge) || requireProofKey) {
            // 验证 code_verifier 是否存在
            if (!StringUtils.hasText(codeVerifier)) {
                log.debug("Missing code_verifier for client: {}", client.getClientId());
                throw new OAuth2AuthenticationException(
                    "invalid_request",
                    "code_verifier is required"
                );
            }

            // 如果需要 PKCE 但没有 code_challenge，说明授权请求时没有使用 PKCE
            if (!StringUtils.hasText(codeChallenge)) {
                log.debug("Missing code_challenge for client: {}", client.getClientId());
                throw new OAuth2AuthenticationException(
                    "invalid_grant",
                    "code_challenge is required"
                );
            }

            // 验证 code_verifier
            String codeChallengeMethod = authorization.getCodeChallengeMethod();
            if (!isValidCodeVerifier(codeVerifier, codeChallenge, codeChallengeMethod)) {
                log.debug("Invalid code_verifier for client: {}", client.getClientId());
                throw new OAuth2AuthenticationException(
                    "invalid_grant",
                    "Invalid code_verifier"
                );
            }
        } else {
            // 如果提供了 code_verifier 但没有 code_challenge，返回错误
            if (StringUtils.hasText(codeVerifier)) {
                log.debug("Unexpected code_verifier for client: {}", client.getClientId());
                throw new OAuth2AuthenticationException(
                    "invalid_request",
                    "Unexpected code_verifier"
                );
            }
            
            log.trace("Skipping PKCE validation for client: {}", client.getClientId());
        }
    }

    /**
     * 验证 code_verifier 是否有效
     */
    private boolean isValidCodeVerifier(String codeVerifier, String codeChallenge, String codeChallengeMethod) {
        // 验证 code_verifier 格式（[A-Z] / [a-z] / [0-9] / "-" / "." / "_" / "~"，长度43-128字符）
        if (!isValidCodeVerifierFormat(codeVerifier)) {
            return false;
        }

        // 根据 code_challenge_method 计算 code_challenge
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

        return codeChallenge.equals(computedCodeChallenge);
    }

    /**
     * 验证 code_verifier 格式
     */
    private boolean isValidCodeVerifierFormat(String codeVerifier) {
        if (codeVerifier.length() < 43 || codeVerifier.length() > 128) {
            return false;
        }

        return codeVerifier.matches("^[A-Za-z0-9\\-\\._~]+$");
    }

    /**
     * 生成 S256 方式的 code_challenge
     */
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