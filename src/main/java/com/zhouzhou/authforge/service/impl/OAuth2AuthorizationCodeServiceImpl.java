package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.constant.OAuth2Constants;
import com.zhouzhou.authforge.exception.OAuth2AuthorizationException;
import com.zhouzhou.authforge.model.OAuthAuthorization;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthAuthorizationRepository;
import com.zhouzhou.authforge.service.OAuth2AuthorizationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * OAuth 2.0 授权码服务实现类
 */
@Service
@RequiredArgsConstructor
public class OAuth2AuthorizationCodeServiceImpl implements OAuth2AuthorizationCodeService {

    private final OAuthAuthorizationRepository authorizationRepository;
    private final StringKeyGenerator codeGenerator = new Base64StringKeyGenerator(32);

    @Override
    @Transactional
    public OAuthAuthorization createAuthorizationCode(
            OAuthClient client,
            Authentication authentication,
            String scope,
            String state,
            String redirectUri,
            Map<String, Object> additionalParameters) {

        // 1. 生成授权码
        String code = codeGenerator.generateKey();

        // 2. 创建授权记录
        OAuthAuthorization authorization = new OAuthAuthorization();
        authorization.setClientId(client.getClientId());
        authorization.setUserId(authentication.getName());
        authorization.setScopes(scope);
        authorization.setAuthorizationCode(code);
        authorization.setState(state);
        authorization.setAuthorizationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        if (additionalParameters != null) {
            authorization.setCodeChallenge((String) additionalParameters.get("code_challenge"));
            authorization.setCodeChallengeMethod((String) additionalParameters.get("code_challenge_method"));
        }

        // 3. 保存授权记录
        return authorizationRepository.save(authorization);
    }

    @Override
    @Transactional
    public void validateAuthorizationCode(
            String code,
            String clientId,
            String redirectUri,
            String codeVerifier) {

        // 1. 查找授权记录
        OAuthAuthorization authorization = authorizationRepository.findByAuthorizationCode(code)
            .orElseThrow(() -> new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Invalid authorization code",
                redirectUri,
                null));

        // 2. 验证客户端ID
        if (!authorization.getClientId().equals(clientId)) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_UNAUTHORIZED_CLIENT,
                "Client ID mismatch",
                redirectUri,
                null);
        }

        // 3. 验证授权码是否过期
        if (authorization.getAuthorizationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Authorization code expired",
                redirectUri,
                null);
        }

        // 4. 验证 PKCE
        validatePkce(authorization, codeVerifier, redirectUri);

        // 5. 删除已使用的授权码
        authorizationRepository.delete(authorization);
    }

    /**
     * 验证 PKCE
     */
    private void validatePkce(OAuthAuthorization authorization, String codeVerifier, String redirectUri) {
        String codeChallenge = authorization.getCodeChallenge();
        String codeChallengeMethod = authorization.getCodeChallengeMethod();

        if (codeChallenge != null) {
            if (codeVerifier == null) {
                throw new OAuth2AuthorizationException(
                    OAuth2Constants.ERROR_INVALID_REQUEST,
                    "code_verifier required",
                    redirectUri,
                    null);
            }

            String computedCodeChallenge;
            if (codeChallengeMethod == null || OAuth2Constants.CODE_CHALLENGE_METHOD_PLAIN.equals(codeChallengeMethod)) {
                computedCodeChallenge = codeVerifier;
            } else {
                computedCodeChallenge = generateS256CodeChallenge(codeVerifier);
            }

            if (!codeChallenge.equals(computedCodeChallenge)) {
                throw new OAuth2AuthorizationException(
                    OAuth2Constants.ERROR_INVALID_REQUEST,
                    "Invalid code_verifier",
                    redirectUri,
                    null);
            }
        }
    }

    /**
     * 生成 S256 方式的 code_challenge
     */
    private String generateS256CodeChallenge(String codeVerifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
} 