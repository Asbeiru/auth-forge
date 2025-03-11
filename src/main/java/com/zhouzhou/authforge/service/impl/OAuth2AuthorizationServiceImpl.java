package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.authentication.OAuth2Authorization;
import com.zhouzhou.authforge.authentication.OAuth2AuthorizationRequest;
import com.zhouzhou.authforge.constant.OAuth2Constants;
import com.zhouzhou.authforge.exception.OAuth2AuthorizationException;
import com.zhouzhou.authforge.model.OAuthAuthorization;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.model.OAuthConsent;
import com.zhouzhou.authforge.repository.OAuthAuthorizationRepository;
import com.zhouzhou.authforge.repository.OAuthClientRepository;
import com.zhouzhou.authforge.repository.OAuthConsentRepository;
import com.zhouzhou.authforge.service.OAuth2AuthorizationService;
import com.zhouzhou.authforge.util.OAuth2Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

    private final OAuthClientRepository clientRepository;
    private final OAuthAuthorizationRepository authorizationRepository;
    private final OAuthConsentRepository consentRepository;
    private final StringKeyGenerator codeGenerator = new Base64StringKeyGenerator(32);

    @Override
    public OAuth2Authorization authorize(OAuth2AuthorizationRequest request, Authentication principal) {
        // 1. 验证客户端
        OAuthClient client = validateClient(request.getClientId());

        // 2. 验证基本参数
        validateRequest(request, client);

        // 3. 检查用户认证状态
        if (principal == null || !principal.isAuthenticated()) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_ACCESS_DENIED,
                "User not authenticated",
                request.getRedirectUri(),
                request.getState()
            );
        }

        // 4. 检查是否需要用户授权
        if (isConsentRequired(client.getClientId(), principal.getName(), request.getScopes())) {
            return OAuth2Authorization.builder()
                .clientId(client.getClientId())
                .principal(principal)
                .state(request.getState())
                .build();
        }

        // 5. 生成授权码
        return createAuthorization(request, principal, client);
    }

    @Override
    public boolean isConsentRequired(String clientId, String userId, Set<String> scopes) {
        OAuthClient client = clientRepository.findByClientId(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // 如果客户端配置了自动授权，不需要用户确认
        if (Boolean.TRUE.equals(client.getAutoApprove())) {
            return false;
        }

        // 如果只请求了 openid scope，不需要确认
        if (scopes.size() == 1 && scopes.contains("openid")) {
            return false;
        }

        // 检查用户是否已经授权过这些scope
        return consentRepository.findByClientIdAndUserId(clientId, userId)
            .map(consent -> {
                Set<String> approvedScopes = OAuth2Utils.parseScopes(consent.getScopes());
                return !approvedScopes.containsAll(scopes);
            })
            .orElse(true);
    }

    @Override
    public OAuth2Authorization handleConsent(String clientId, String userId, Set<String> approvedScopes) {
        // 保存用户授权记录
        OAuthConsent consent = consentRepository.findByClientIdAndUserId(clientId, userId)
            .orElse(new OAuthConsent());
        
        consent.setClientId(clientId);
        consent.setUserId(userId);
        consent.setScopes(String.join(" ", approvedScopes));
        
        consentRepository.save(consent);

        // 返回授权信息
        return OAuth2Authorization.builder()
            .clientId(clientId)
            .authorizedScopes(approvedScopes)
            .build();
    }

    @Override
    public Optional<OAuth2Authorization> findByCode(String code) {
        return authorizationRepository.findByAuthorizationCode(code)
            .map(this::convertToAuthorization);
    }

    @Override
    public void validateAuthorizationCode(String code, String clientId, String codeVerifier) {
        OAuthAuthorization authorization = authorizationRepository.findByAuthorizationCode(code)
            .orElseThrow(() -> new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Invalid authorization code",
                null,
                null
            ));

        // 验证客户端ID
        if (!authorization.getClientId().equals(clientId)) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Client ID mismatch",
                null,
                null
            );
        }

        // 验证授权码是否过期
        if (authorization.getAuthorizationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Authorization code expired",
                null,
                null
            );
        }

        // 验证PKCE
        if (StringUtils.hasText(authorization.getCodeChallenge())) {
            validatePKCE(authorization, codeVerifier);
        }
    }

    private OAuthClient validateClient(String clientId) {
        return clientRepository.findByClientId(clientId)
            .orElseThrow(() -> new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Invalid client_id",
                null,
                null
            ));
    }

    private void validateRequest(OAuth2AuthorizationRequest request, OAuthClient client) {
        // 验证response_type
        if (!OAuth2Constants.RESPONSE_TYPE_CODE.equals(request.getResponseType())) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_UNSUPPORTED_RESPONSE_TYPE,
                "Response type must be 'code'",
                request.getRedirectUri(),
                request.getState()
            );
        }

        // 验证redirect_uri
        Set<String> allowedRedirectUris = OAuth2Utils.parseRedirectUris(client.getRedirectUris());
        if (!allowedRedirectUris.contains(request.getRedirectUri())) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Invalid redirect_uri",
                request.getRedirectUri(),
                request.getState()
            );
        }

        // 验证授权类型
        if (!client.getAuthorizedGrantTypes().contains(OAuth2Constants.GRANT_TYPE_AUTHORIZATION_CODE)) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_UNAUTHORIZED_CLIENT,
                "Client is not authorized for authorization_code grant",
                request.getRedirectUri(),
                request.getState()
            );
        }

        // 验证scope
        validateScopes(request.getScopes(), client);
    }

    private void validateScopes(Set<String> requestedScopes, OAuthClient client) {
        Set<String> allowedScopes = OAuth2Utils.parseScopes(client.getScopes());
        if (!allowedScopes.containsAll(requestedScopes)) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_SCOPE,
                "Invalid scope requested",
                null,
                null
            );
        }
    }

    private OAuth2Authorization createAuthorization(
            OAuth2AuthorizationRequest request,
            Authentication principal,
            OAuthClient client) {
        
        String code = codeGenerator.generateKey();
        LocalDateTime now = LocalDateTime.now();

        OAuthAuthorization authorization = new OAuthAuthorization();
        authorization.setClientId(client.getClientId());
        authorization.setUserId(principal.getName());
        authorization.setScopes(String.join(" ", request.getScopes()));
        authorization.setAuthorizationCode(code);
        authorization.setCodeChallenge((String) request.getAdditionalParameters().get("code_challenge"));
        authorization.setCodeChallengeMethod((String) request.getAdditionalParameters()
            .get("code_challenge_method"));
        authorization.setState(request.getState());
        authorization.setAuthorizationCodeExpiresAt(now.plusMinutes(10));
        
        authorizationRepository.save(authorization);

        return OAuth2Authorization.builder()
            .id(String.valueOf(authorization.getId()))
            .clientId(client.getClientId())
            .principal(principal)
            .authorizationGrantType(OAuth2Constants.GRANT_TYPE_AUTHORIZATION_CODE)
            .authorizationCode(code)
            .authorizationCodeIssuedAt(now)
            .authorizationCodeExpiresAt(authorization.getAuthorizationCodeExpiresAt())
            .authorizedScopes(request.getScopes())
            .state(request.getState())
            .build();
    }

    private OAuth2Authorization convertToAuthorization(OAuthAuthorization authorization) {
        return OAuth2Authorization.builder()
            .id(String.valueOf(authorization.getId()))
            .clientId(authorization.getClientId())
            .authorizationGrantType(OAuth2Constants.GRANT_TYPE_AUTHORIZATION_CODE)
            .authorizationCode(authorization.getAuthorizationCode())
            .authorizedScopes(OAuth2Utils.parseScopes(authorization.getScopes()))
            .state(authorization.getState())
            .build();
    }

    private void validatePKCE(OAuthAuthorization authorization, String codeVerifier) {
        if (!OAuth2Utils.validateCodeVerifier(codeVerifier, 
            authorization.getCodeChallenge(), 
            authorization.getCodeChallengeMethod())) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Invalid code_verifier",
                null,
                null
            );
        }
    }
} 