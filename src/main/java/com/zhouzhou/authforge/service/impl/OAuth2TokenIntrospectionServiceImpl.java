package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.dto.TokenIntrospectionResponse;
import com.zhouzhou.authforge.exception.OAuth2TokenException;
import com.zhouzhou.authforge.model.OAuthAccessToken;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthAccessTokenRepository;
import com.zhouzhou.authforge.security.ClientAuthenticatorChain;
import com.zhouzhou.authforge.service.OAuth2TokenIntrospectionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2TokenIntrospectionServiceImpl implements OAuth2TokenIntrospectionService {

    private final ClientAuthenticatorChain clientAuthenticatorChain;
    private final OAuthAccessTokenRepository accessTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> introspectToken(HttpServletRequest request, String token, String tokenTypeHint) {
        try {
            // 1. 验证客户端身份
            OAuthClient client;
            try {
                client = clientAuthenticatorChain.authenticate(request);
            } catch (OAuth2TokenException e) {
                log.warn("Client authentication failed: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "invalid_client",
                                "error_description", "Invalid client authentication"
                        ));
            }
            
            // 2. 验证token_type_hint的有效性
            if (tokenTypeHint != null && !isValidTokenTypeHint(tokenTypeHint)) {
                log.trace("Invalid token_type_hint: {}, ignoring it", tokenTypeHint);
                tokenTypeHint = null;
            }

            // 3. 根据token_type_hint查找令牌，同时验证client_id
            Optional<OAuthAccessToken> tokenEntity = findTokenByHint(token, tokenTypeHint, client.getClientId());

            // 4. 如果令牌不存在或已失效，返回active=false
            if (tokenEntity.isEmpty() || !isTokenActive(tokenEntity.get())) {
                return ResponseEntity.ok(TokenIntrospectionResponse.builder()
                        .active(false)
                        .build());
            }

            // 5. 构建令牌信息响应
            OAuthAccessToken accessToken = tokenEntity.get();
            return ResponseEntity.ok(buildTokenResponse(accessToken));

        } catch (Exception e) {
            log.error("Unexpected error during token introspection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "server_error",
                            "error_description", "Failed to process token introspection request"
                    ));
        }
    }

    /**
     * 验证token_type_hint是否有效
     */
    private boolean isValidTokenTypeHint(String tokenTypeHint) {
        return "access_token".equals(tokenTypeHint) || "refresh_token".equals(tokenTypeHint);
    }

    /**
     * 根据token_type_hint查找令牌，同时验证client_id
     * 防止令牌扫描攻击，确保token只能被合法的受保护资源检查
     */
    private Optional<OAuthAccessToken> findTokenByHint(String token, String tokenTypeHint, String clientId) {
        if ("refresh_token".equals(tokenTypeHint)) {
            // 先按refresh_token查找，同时验证client_id
            Optional<OAuthAccessToken> tokenEntity = accessTokenRepository
                    .findByRefreshTokenAndClientId(token, clientId);
            if (tokenEntity.isPresent()) {
                return tokenEntity;
            }
            // 如果未找到，扩展搜索范围到access_token，同时验证client_id
            return accessTokenRepository.findByAccessTokenAndClientId(token, clientId);
        } else if ("access_token".equals(tokenTypeHint)) {
            // 先按access_token查找，同时验证client_id
            Optional<OAuthAccessToken> tokenEntity = accessTokenRepository
                    .findByAccessTokenAndClientId(token, clientId);
            if (tokenEntity.isPresent()) {
                return tokenEntity;
            }
            // 如果未找到，扩展搜索范围到refresh_token，同时验证client_id
            return accessTokenRepository.findByRefreshTokenAndClientId(token, clientId);
        } else {
            // token_type_hint为null或无效时，同时搜索两种类型，同时验证client_id
            return accessTokenRepository.findByAccessTokenAndClientId(token, clientId)
                    .or(() -> accessTokenRepository.findByRefreshTokenAndClientId(token, clientId));
        }
    }

    /**
     * 检查令牌是否处于活动状态
     */
    private boolean isTokenActive(OAuthAccessToken token) {
        return token.isActive();
    }

    /**
     * 构建令牌信息响应
     */
    private TokenIntrospectionResponse buildTokenResponse(OAuthAccessToken token) {
        TokenIntrospectionResponse.TokenIntrospectionResponseBuilder builder = TokenIntrospectionResponse.builder()
                .active(true)
                .client_id(token.getClientId())
                .token_type("Bearer") // OAuth 2.0 Bearer Token
                .exp(token.getAccessTokenExpiresAt() != null ? token.getAccessTokenExpiresAt().toEpochSecond(java.time.ZoneOffset.UTC) : null)
                .iat(token.getCreatedAt() != null ? token.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) : null);

        // 设置可选字段
        if (token.getScopes() != null) {
            builder.scope(token.getScopes());
        }
        if (token.getUserId() != null) {
            builder.username(token.getUserId());
            builder.sub(token.getUserId());
        }

        // 设置JWT相关字段
        builder.jti(token.getAccessToken()) // 使用access_token作为jti
               .iss("https://server.example.com/") // 从配置中获取
               .aud("https://protected.example.net/resource"); // 从配置中获取

        return builder.build();
    }
} 