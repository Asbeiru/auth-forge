package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.dto.TokenResponse;
import com.zhouzhou.authforge.exception.OAuth2TokenException;
import com.zhouzhou.authforge.model.OAuthAccessToken;
import com.zhouzhou.authforge.model.OAuthAuthorization;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthAccessTokenRepository;
import com.zhouzhou.authforge.repository.OAuthClientRepository;
import com.zhouzhou.authforge.security.ClientAuthenticatorChain;
import com.zhouzhou.authforge.service.OAuth2AuthorizationService;
import com.zhouzhou.authforge.service.OAuth2TokenService;
import com.zhouzhou.authforge.service.PkceValidationService;
import com.zhouzhou.authforge.service.TokenGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * OAuth 2.0 令牌服务实现类
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OAuth2TokenServiceImpl implements OAuth2TokenService {

    private final ClientAuthenticatorChain clientAuthenticatorChain;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuthAccessTokenRepository accessTokenRepository;
    private final TokenGenerator tokenGenerator;
    private final PkceValidationService pkceValidationService;
    private final OAuthClientRepository clientRepository;

    @Override
    public TokenResponse handleTokenRequest(
            HttpServletRequest request,
            String grantType,
            String code,
            String redirectUri,
            String codeVerifier,
            String refreshToken) {

        try {
            // 1. 认证客户端
            OAuthClient client = clientAuthenticatorChain.authenticate(request);
            log.debug("Client authenticated: {}", client.getClientId());

            // 2. 根据授权类型处理请求
            return switch (grantType) {
                case "authorization_code" -> handleAuthorizationCodeGrant(
                        client, code, redirectUri, codeVerifier
                );
                case "refresh_token" -> handleRefreshTokenGrant(
                        refreshToken, client
                );
                case "client_credentials" -> handleClientCredentialsGrant(
                        client, request.getParameter("scope")
                );
                default -> throw new OAuth2TokenException(
                        "unsupported_grant_type",
                        "Unsupported grant type: " + grantType
                );
            };
        } catch (Exception e) {
            log.error("Failed to process token request", e);
            throw new OAuth2TokenException(
                    "server_error",
                    "Failed to process token request"
            );
        }
    }

    private TokenResponse handleAuthorizationCodeGrant(
            OAuthClient client,
            String code,
            String redirectUri,
            String codeVerifier) {

        // 1. 验证授权码
        OAuthAuthorization authorization = authorizationService.validateAuthorizationCode(
                code,
                client.getClientId(),
                redirectUri
        );

        // 2. 验证PKCE
        pkceValidationService.validateCodeVerifier(client, authorization, codeVerifier);

        // 3. 生成访问令牌
        OAuthAccessToken accessToken = OAuthAccessToken.createFrom(authorization, client, tokenGenerator);

        // 4. 保存访问令牌
        accessTokenRepository.save(accessToken);

        // 5. 使授权码失效
        authorizationService.invalidateAuthorization(authorization);

        // 6. 构建响应
        TokenResponse.Builder responseBuilder = TokenResponse.builder()
                .accessToken(accessToken.getAccessToken())
                .tokenType("Bearer")
                .expiresIn(client.getAccessTokenValiditySeconds().longValue())
                .scope(accessToken.getScope());

        if (accessToken.getRefreshToken() != null) {
            responseBuilder.refreshToken(accessToken.getRefreshToken());
        }

        return responseBuilder.build();
    }

    private TokenResponse handleClientCredentialsGrant(OAuthClient client, String requestedScope) {
        // 1. 验证和处理作用域
        Set<String> validScopes = validateAndFilterScopes(client, requestedScope);
        String scopeString = String.join(" ", validScopes);

        // 2. 生成访问令牌（不包含refresh_token）
        LocalDateTime accessTokenExpiresAt = LocalDateTime.now()
                .plusSeconds(client.getAccessTokenValiditySeconds());

        OAuthAccessToken accessToken = OAuthAccessToken.builder()
                .clientId(client.getClientId())
                .accessToken(tokenGenerator.generateAccessToken(
                        "service_account",  // 使用service_account作为subject，符合OAuth 2.1规范
                        scopeString,
                        client.getAccessTokenValiditySeconds()
                ))
                .scope(scopeString)
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .build();

        // 3. 保存访问令牌
        accessTokenRepository.save(accessToken);

        // 4. 构建响应（不包含refresh_token）
        return TokenResponse.builder()
                .accessToken(accessToken.getAccessToken())
                .tokenType("Bearer")
                .expiresIn(client.getAccessTokenValiditySeconds().longValue())
                .scope(accessToken.getScope())
                .build();
    }

    private Set<String> validateAndFilterScopes(OAuthClient client, String requestedScope) {
        Set<String> clientScopes = client.getScopeSet();

        // 如果没有请求特定的作用域，使用客户端的所有允许作用域
        if (!StringUtils.hasText(requestedScope)) {
            return new HashSet<>(clientScopes);
        }

        // 验证请求的作用域是否在客户端允许的作用域内
        Set<String> requestedScopes = new HashSet<>();
        for (String scope : requestedScope.split(" ")) {
            if (!clientScopes.contains(scope)) {
                throw new OAuth2TokenException(
                        "invalid_scope",
                        "Requested scope is not allowed: " + scope
                );
            }
            requestedScopes.add(scope);
        }

        return requestedScopes;
    }

    @Override
    @Transactional
    public TokenResponse handleRefreshTokenGrant(String refreshToken, OAuthClient client) {
        // 1. 验证刷新令牌
        OAuthAccessToken existingToken = accessTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new OAuth2TokenException(
                        "invalid_grant",
                        "Invalid refresh token"
                ));

        // 2. 验证刷新令牌是否属于当前客户端
        if (!existingToken.getClientId().equals(client.getClientId())) {
            throw new OAuth2TokenException(
                    "invalid_grant",
                    "Refresh token was not issued to this client"
            );
        }

        // 3. 验证刷新令牌是否已失效
        if (existingToken.isInvalidated()) {
            throw new OAuth2TokenException(
                    "invalid_grant",
                    "Refresh token has been invalidated"
            );
        }

        // 4. 验证刷新令牌是否已过期
        if (existingToken.isRefreshTokenExpired()) {
            throw new OAuth2TokenException(
                    "invalid_grant",
                    "Refresh token has expired"
            );
        }

        // 5. 生成新的访问令牌
        String newAccessToken = tokenGenerator.generateAccessToken(
                existingToken.getUserId(),  // 保持原有的subject
                existingToken.getScope(),   // 保持原有的scope
                client.getAccessTokenValiditySeconds()
        );
        String newRefreshToken = null;

        // 6. 如果客户端配置不允许重用刷新令牌，生成新的刷新令牌
        if (!client.isReuseRefreshTokens()) {
            newRefreshToken = tokenGenerator.generateRefreshToken();
            // 使旧令牌失效
            existingToken.markAsInvalidated();
            accessTokenRepository.save(existingToken);
        }

        // 7. 创建新的访问令牌
        OAuthAccessToken newToken = OAuthAccessToken.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken != null ? newRefreshToken : existingToken.getRefreshToken())
                .clientId(client.getClientId())
                .userId(existingToken.getUserId())
                .scope(existingToken.getScope())
                .accessTokenExpiresAt(LocalDateTime.now().plusSeconds(client.getAccessTokenValiditySeconds()))
                .refreshTokenExpiresAt(newRefreshToken != null ?
                        LocalDateTime.now().plusSeconds(client.getRefreshTokenValiditySeconds()) :
                        existingToken.getRefreshTokenExpiresAt())
                .build();

        accessTokenRepository.save(newToken);

        // 8. 构建响应
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(client.getAccessTokenValiditySeconds().longValue())
                .refreshToken(newToken.getRefreshToken())
                .scope(newToken.getScope())
                .build();
    }


}