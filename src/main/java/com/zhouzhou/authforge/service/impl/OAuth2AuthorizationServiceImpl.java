package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.dto.AuthorizationResult;
import com.zhouzhou.authforge.exception.OAuth2AuthorizationException;
import com.zhouzhou.authforge.exception.OAuth2TokenException;
import com.zhouzhou.authforge.model.OAuthAuthorization;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthAuthorizationRepository;
import com.zhouzhou.authforge.service.OAuth2AuthorizationCodeService;
import com.zhouzhou.authforge.service.OAuth2AuthorizationService;
import com.zhouzhou.authforge.service.OAuth2ClientService;
import com.zhouzhou.authforge.service.OAuth2ConsentService;
import com.zhouzhou.authforge.validator.OAuth2AuthorizationRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * OAuth 2.0 授权服务实现类
 */
@Service
@Transactional
@RequiredArgsConstructor
public class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

    private final OAuth2ClientService clientService;
    private final OAuthAuthorizationRepository authorizationRepository;
    private final OAuth2AuthorizationRequestValidator requestValidator;
    private final OAuth2AuthorizationCodeService authorizationCodeService;
    private final OAuth2ConsentService consentService;

    @Override
    @Transactional
    public AuthorizationResult handleAuthorizationRequest(
            String responseType,
            String clientId,
            String redirectUri,
            String scope,
            String state,
            Map<String, Object> additionalParameters,
            Authentication authentication) {

        try {
            // 1. 验证请求参数
            requestValidator.validateAuthorizationRequest(
                    clientId,
                    responseType,
                    redirectUri,
                    scope,
                    state,
                    additionalParameters
            );

            // 2. 获取客户端信息
            OAuthClient client = clientService.findByClientId(clientId)
                    .orElseThrow(() -> new OAuth2AuthorizationException(
                            "unauthorized_client",
                            "Client not found",
                            redirectUri,
                            state));

            // 3. 检查是否需要用户同意
            if (consentService.isConsentRequired(client, authentication, scope)) {
                // 创建并保存授权请求记录，包含traceId和responseType
                OAuthAuthorization pendingAuthorization = new OAuthAuthorization();
                pendingAuthorization.setClientId(clientId);
                pendingAuthorization.setUserId(authentication.getName());
                pendingAuthorization.setScopes(scope);
                pendingAuthorization.setState(state);
                pendingAuthorization.setRedirectUri(redirectUri);
                pendingAuthorization.setResponseType(responseType);  // 保存授权类型
                pendingAuthorization.setTraceId(UUID.randomUUID().toString());
                authorizationRepository.save(pendingAuthorization);

                return AuthorizationResult.builder()
                        .resultType(AuthorizationResult.ResultType.SHOW_CONSENT_PAGE)
                        .client(client)
                        .scopes(new HashSet<>(Arrays.asList(scope.split(" "))))
                        .redirectUri(redirectUri)
                        .state(state)
                        .traceId(pendingAuthorization.getTraceId())  // 传递traceId到同意页面
                        .build();
            }

            // 4. 如果不需要用户同意，直接生成授权码
            OAuthAuthorization authorization = authorizationCodeService.createAuthorizationCode(
                    client,
                    authentication,
                    scope,
                    state,
                    redirectUri,
                    additionalParameters
            );

            return AuthorizationResult.builder()
                    .resultType(AuthorizationResult.ResultType.REDIRECT_WITH_CODE)
                    .redirectUri(redirectUri)
                    .code(authorization.getAuthorizationCode())
                    .state(state)
                    .build();

        } catch (OAuth2AuthorizationException e) {
            return AuthorizationResult.builder()
                    .resultType(AuthorizationResult.ResultType.REDIRECT_WITH_ERROR)
                    .redirectUri(redirectUri)
                    .error(e.getError())
                    .errorDescription(e.getMessage())
                    .state(state)
                    .build();
        }
    }


    @Override
    public Optional<OAuthAuthorization> findByCode(String code) {
        return authorizationRepository.findByAuthorizationCode(code);
    }

    @Override
    public boolean isConsentRequired(String clientId, String userId, Set<String> requestedScopes) {
        OAuthClient client = clientService.findByClientId(clientId)
                .orElseThrow(() -> new OAuth2AuthorizationException(
                        "unauthorized_client",
                        "Client not found",
                        null,
                        null));

        return consentService.isConsentRequired(
                client,
                new Authentication() {
                    @Override
                    public String getName() {
                        return userId;
                    }

                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return Collections.emptyList();
                    }

                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getDetails() {
                        return null;
                    }

                    @Override
                    public Object getPrincipal() {
                        return userId;
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return true;
                    }

                    @Override
                    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
                        throw new UnsupportedOperationException();
                    }
                },
                String.join(" ", requestedScopes)
        );
    }

    @Override
    public void validateAuthorizationCode(String code, String clientId, String redirectUri, String codeVerifier) {
        OAuthAuthorization authorization = findByCode(code)
                .orElseThrow(() -> new OAuth2AuthorizationException(
                        "invalid_grant",
                        "Invalid authorization code",
                        redirectUri,
                        null));

        // 验证授权码是否过期
        if (authorization.getAuthorizationCodeExpiresAt() != null &&
                authorization.getAuthorizationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OAuth2AuthorizationException(
                    "invalid_grant",
                    "Authorization code has expired",
                    redirectUri,
                    null);
        }

        // 验证客户端ID是否匹配
        if (!authorization.getClientId().equals(clientId)) {
            throw new OAuth2AuthorizationException(
                    "invalid_grant",
                    "Client ID mismatch",
                    redirectUri,
                    null);
        }

        // 验证重定向URI是否匹配
        if (!authorization.getRedirectUri().equals(redirectUri)) {
            throw new OAuth2AuthorizationException(
                    "invalid_grant",
                    "Redirect URI mismatch",
                    redirectUri,
                    null);
        }

        // 如果存在code_verifier，验证PKCE
        if (codeVerifier != null && authorization.getCodeChallenge() != null) {
            String computedCodeChallenge;
            if ("S256".equals(authorization.getCodeChallengeMethod())) {
                computedCodeChallenge = generateS256CodeChallenge(codeVerifier);
            } else {
                computedCodeChallenge = codeVerifier;
            }

            if (!computedCodeChallenge.equals(authorization.getCodeChallenge())) {
                throw new OAuth2AuthorizationException(
                        "invalid_grant",
                        "Invalid code verifier",
                        redirectUri,
                        null);
            }
        }
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

    @Override
    public OAuthAuthorization validateAuthorizationCode(String code, String clientId, String redirectUri) {
        // 1. 查找授权记录
        OAuthAuthorization authorization = authorizationRepository.findByAuthorizationCode(code)
            .orElseThrow(() -> new OAuth2TokenException(
                "invalid_grant",
                "Invalid authorization code"
            ));

        // 2. 验证授权码是否有效
        if (!authorization.isValid()) {
            throw new OAuth2TokenException(
                "invalid_grant",
                "Authorization code is invalid or has been used"
            );
        }

        // 3. 验证客户端ID是否匹配
        if (!authorization.getClientId().equals(clientId)) {
            throw new OAuth2TokenException(
                "invalid_grant",
                "Client ID mismatch"
            );
        }

        // 4. 验证重定向URI是否匹配
        if (!authorization.getRedirectUri().equals(redirectUri)) {
            throw new OAuth2TokenException(
                "invalid_grant",
                "Redirect URI mismatch"
            );
        }

        return authorization;
    }

    @Override
    public void invalidateAuthorization(OAuthAuthorization authorization) {
        authorization.markAsInvalidated();
        authorizationRepository.save(authorization);
    }
} 