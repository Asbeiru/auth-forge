package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.config.TokenRevocationConfig;
import com.zhouzhou.authforge.dto.TokenRevocationResponse;
import com.zhouzhou.authforge.exception.OAuth2TokenException;
import com.zhouzhou.authforge.exception.TemporaryServerErrorException;
import com.zhouzhou.authforge.model.OAuthAccessToken;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthAccessTokenRepository;
import com.zhouzhou.authforge.security.ClientAuthenticatorChain;
import com.zhouzhou.authforge.service.OAuth2TokenRevocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2TokenRevocationServiceImpl implements OAuth2TokenRevocationService {

    private final ClientAuthenticatorChain clientAuthenticatorChain;
    private final OAuthAccessTokenRepository accessTokenRepository;
    private final TokenRevocationConfig revocationConfig;

    @Override
    @Transactional
    public ResponseEntity<?> revokeToken(HttpServletRequest request, String token, String tokenTypeHint) {
        try {
            // 1. 验证客户端身份
            OAuthClient client = clientAuthenticatorChain.authenticate(request);
            
            // 2. 验证token_type_hint的有效性
            if (tokenTypeHint != null && !isValidTokenTypeHint(tokenTypeHint)) {
                log.trace("Invalid token_type_hint: {}, ignoring it", tokenTypeHint);
                tokenTypeHint = null;
            }

            // 3. 根据token_type_hint优化查找策略，同时验证client_id
            Optional<OAuthAccessToken> tokenEntity = findTokenByHint(token, tokenTypeHint, client.getClientId());

            // 4. 如果令牌存在，执行撤销操作
            if (tokenEntity.isPresent()) {
                revokeTokenAndRelated(tokenEntity.get());
            }
            // 如果令牌不存在，按照RFC 7009规范，仍然返回200 OK
            return ResponseEntity.ok().build();
            
        } catch (TemporaryServerErrorException e) {
            log.error("Temporary server error during token revocation", e);
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header("Retry-After", String.valueOf(revocationConfig.getRetryAfterSeconds()))
                    .body(TokenRevocationResponse.builder()
                            .error(e.getError())
                            .error_description(e.getErrorDescription())
                            .build());
        } catch (OAuth2TokenException e) {
            log.warn("Token revocation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(TokenRevocationResponse.builder()
                            .error(e.getError())
                            .error_description(e.getErrorDescription())
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error during token revocation", e);
            throw new OAuth2TokenException(
                    "server_error",
                    "Failed to process token revocation request"
            );
        }
    }

    /**
     * 验证token_type_hint是否有效
     */
    private boolean isValidTokenTypeHint(String tokenTypeHint) {
        return "access_token".equals(tokenTypeHint) || "refresh_token".equals(tokenTypeHint);
    }

    /**
     * 根据token_type_hint查找令牌
     */
    private Optional<OAuthAccessToken> findTokenByHint(String token, String tokenTypeHint, String clientId) {
        try {
            if ("refresh_token".equals(tokenTypeHint)) {
                // 先按refresh_token查找
                Optional<OAuthAccessToken> tokenEntity = accessTokenRepository
                        .findByRefreshTokenAndClientId(token, clientId);
                if (tokenEntity.isPresent()) {
                    return tokenEntity;
                }
                // 如果未找到，扩展搜索范围到access_token
                return accessTokenRepository.findByAccessTokenAndClientId(token, clientId);
            } else if ("access_token".equals(tokenTypeHint)) {
                // 先按access_token查找
                Optional<OAuthAccessToken> tokenEntity = accessTokenRepository
                        .findByAccessTokenAndClientId(token, clientId);
                if (tokenEntity.isPresent()) {
                    return tokenEntity;
                }
                // 如果未找到，扩展搜索范围到refresh_token
                return accessTokenRepository.findByRefreshTokenAndClientId(token, clientId);
            } else {
                // token_type_hint为null或无效时，同时搜索两种类型
                return accessTokenRepository.findByAccessTokenAndClientId(token, clientId)
                        .or(() -> accessTokenRepository.findByRefreshTokenAndClientId(token, clientId));
            }
        } catch (DataAccessException e) {
            log.error("Database error during token lookup", e);
            throw new TemporaryServerErrorException("Server temporarily unavailable, please retry later.");
        }
    }

    /**
     * 撤销令牌及相关联的令牌
     */
    private void revokeTokenAndRelated(OAuthAccessToken token) {
        try {
            // 如果是刷新令牌，撤销所有相关的访问令牌
            if (token.getRefreshToken() != null) {
                List<OAuthAccessToken> relatedTokens = accessTokenRepository
                        .findAllByRefreshToken(token.getRefreshToken());
                for (OAuthAccessToken relatedToken : relatedTokens) {
                    relatedToken.markAsInvalidated();
                    accessTokenRepository.save(relatedToken);
                }
            }

            // 如果是访问令牌且配置允许，同时撤销刷新令牌
            if (token.getAccessToken() != null && 
                token.getRefreshToken() != null && 
                revocationConfig.isRevokeRefreshTokenOnAccessTokenRevocation()) {
                Optional<OAuthAccessToken> refreshTokenEntity = accessTokenRepository
                        .findByRefreshToken(token.getRefreshToken());
                refreshTokenEntity.ifPresent(refreshToken -> {
                    refreshToken.markAsInvalidated();
                    accessTokenRepository.save(refreshToken);
                });
            }

            // 撤销当前令牌
            token.markAsInvalidated();
            accessTokenRepository.save(token);
        } catch (DataAccessException e) {
            log.error("Database error during token revocation", e);
            throw new TemporaryServerErrorException("Server temporarily unavailable, please retry later.");
        }
    }
} 