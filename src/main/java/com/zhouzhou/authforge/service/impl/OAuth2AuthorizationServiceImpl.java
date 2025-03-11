package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.authentication.OAuth2Authorization;
import com.zhouzhou.authforge.authentication.OAuth2AuthorizationRequest;
import com.zhouzhou.authforge.constant.OAuth2Constants;
import com.zhouzhou.authforge.dto.AuthorizationResult;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

    private final OAuthClientRepository clientRepository;
    private final OAuthAuthorizationRepository authorizationRepository;
    private final OAuthConsentRepository consentRepository;
    private final StringKeyGenerator codeGenerator = new Base64StringKeyGenerator(32);

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

        // 1. 验证响应类型
        if (!OAuth2Constants.RESPONSE_TYPE_CODE.equals(responseType)) {
            return AuthorizationResult.builder()
                    .resultType(AuthorizationResult.ResultType.REDIRECT_WITH_ERROR)
                    .redirectUri(redirectUri)
                    .error(OAuth2Constants.ERROR_UNSUPPORTED_RESPONSE_TYPE)
                    .errorDescription("Unsupported response type: " + responseType)
                    .state(state)
                    .build();
        }

        // 2. 获取并验证客户端
        OAuthClient client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new OAuth2AuthorizationException(
                        OAuth2Constants.ERROR_UNAUTHORIZED_CLIENT,
                        "Client not found",
                        redirectUri,
                        state));

        // 3. 验证重定向URI
        if (!client.getRedirectUris().contains(redirectUri)) {
            return AuthorizationResult.builder()
                    .resultType(AuthorizationResult.ResultType.REDIRECT_WITH_ERROR)
                    .redirectUri(redirectUri)
                    .error(OAuth2Constants.ERROR_INVALID_REQUEST)
                    .errorDescription("Invalid redirect_uri")
                    .state(state)
                    .build();
        }

        // 4. 验证作用域
        Set<String> requestedScopes = scope != null ?
                new HashSet<>(Arrays.asList(scope.split(" "))) :
                new HashSet<>();
        Set<String> allowedScopes = new HashSet<>(Arrays.asList(client.getScopes().split(" ")));
        if (!allowedScopes.containsAll(requestedScopes)) {
            return AuthorizationResult.builder()
                    .resultType(AuthorizationResult.ResultType.REDIRECT_WITH_ERROR)
                    .redirectUri(redirectUri)
                    .error(OAuth2Constants.ERROR_INVALID_SCOPE)
                    .errorDescription("Invalid scope requested")
                    .state(state)
                    .build();
        }

        // 5. 检查是否需要用户同意
        if (!client.getAutoApprove()) {
            Optional<OAuthConsent> consent = consentRepository.findByClientIdAndUserId(
                    clientId, authentication.getName());
            if (consent.isEmpty()) {
                return AuthorizationResult.builder()
                        .resultType(AuthorizationResult.ResultType.SHOW_CONSENT_PAGE)
                        .client(client)
                        .scopes(requestedScopes)
                        .redirectUri(redirectUri)
                        .state(state)
                        .build();
            }
        }

        // 6. 验证授权类型
        if (!client.getAuthorizedGrantTypes().contains("authorization_code")) {
            return AuthorizationResult.builder()
                    .resultType(AuthorizationResult.ResultType.REDIRECT_WITH_ERROR)
                    .redirectUri(redirectUri)
                    .error(OAuth2Constants.ERROR_UNAUTHORIZED_CLIENT)
                    .errorDescription("Client is not authorized for authorization_code grant type")
                    .state(state)
                    .build();
        }

        // 7. 生成授权码
        return createAuthorizationCode(client, authentication, scope, state, redirectUri, additionalParameters);
    }

    @Override
    @Transactional
    public AuthorizationResult handleAuthorizationConsent(
            String clientId,
            String redirectUri,
            String scope,
            String state,
            String consent,
            Authentication authentication) {

        // 1. 验证客户端
        OAuthClient client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new OAuth2AuthorizationException(
                        OAuth2Constants.ERROR_UNAUTHORIZED_CLIENT,
                        "Client not found",
                        redirectUri,
                        state));

        // 2. 处理用户拒绝授权的情况
        if (!"approve".equals(consent)) {
            return AuthorizationResult.builder()
                    .resultType(AuthorizationResult.ResultType.REDIRECT_WITH_ERROR)
                    .redirectUri(redirectUri)
                    .error(OAuth2Constants.ERROR_ACCESS_DENIED)
                    .errorDescription("User denied access")
                    .state(state)
                    .build();
        }

        // 3. 保存用户同意记录
        OAuthConsent consentRecord = new OAuthConsent();
        consentRecord.setClientId(clientId);
        consentRecord.setUserId(authentication.getName());
        consentRecord.setScopes(scope);
        consentRepository.save(consentRecord);

        // 4. 生成授权码
        return createAuthorizationCode(client, authentication, scope, state, redirectUri, Collections.emptyMap());
    }

    private AuthorizationResult createAuthorizationCode(
            OAuthClient client,
            Authentication authentication,
            String scope,
            String state,
            String redirectUri,
            Map<String, Object> additionalParameters) {

        String code = codeGenerator.generateKey();
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

        authorizationRepository.save(authorization);

        return AuthorizationResult.builder()
                .resultType(AuthorizationResult.ResultType.REDIRECT_WITH_CODE)
                .redirectUri(redirectUri)
                .code(code)
                .state(state)
                .build();
    }

    @Override
    public boolean isConsentRequired(String clientId, String userId, Set<String> requestedScopes) {
        // 获取客户端信息
        OAuthClient client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new OAuth2AuthorizationException(
                        OAuth2Constants.ERROR_UNAUTHORIZED_CLIENT,
                        "Client not found",
                        null,
                        null));

        // 如果客户端配置为自动批准，则不需要用户同意
        if (client.getAutoApprove()) {
            return false;
        }

        // 检查是否已有用户同意记录
        Optional<OAuthConsent> consent = consentRepository.findByClientIdAndUserId(clientId, userId);
        if (consent.isEmpty()) {
            return true;
        }

        // 检查已同意的作用域是否包含所有请求的作用域
        Set<String> authorizedScopes = new HashSet<>(Arrays.asList(consent.get().getScopes().split(" ")));
        return !authorizedScopes.containsAll(requestedScopes);
    }


    @Override
    public Optional<OAuthAuthorization> findByCode(String code) {
        return authorizationRepository.findByAuthorizationCode(code);
    }

    @Override
    @Transactional
    public void validateAuthorizationCode(
            String code,
            String clientId,
            String redirectUri,
            String codeVerifier) {

        OAuthAuthorization authorization = authorizationRepository.findByAuthorizationCode(code)
                .orElseThrow(() -> new OAuth2AuthorizationException(
                        OAuth2Constants.ERROR_INVALID_REQUEST,
                        "Invalid authorization code",
                        null,
                        null));

        // 验证授权码是否过期
        if (authorization.getAuthorizationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OAuth2AuthorizationException(
                    OAuth2Constants.ERROR_INVALID_REQUEST,
                    "Authorization code expired",
                    null,
                    null);
        }

        // 验证客户端ID
        if (!authorization.getClientId().equals(clientId)) {
            throw new OAuth2AuthorizationException(
                    OAuth2Constants.ERROR_INVALID_REQUEST,
                    "Client ID mismatch",
                    null,
                    null);
        }

        // 验证PKCE
        if (authorization.getCodeChallenge() != null) {
            if (codeVerifier == null) {
                throw new OAuth2AuthorizationException(
                        OAuth2Constants.ERROR_INVALID_REQUEST,
                        "Code verifier required",
                        null,
                        null);
            }

            String computedChallenge;
            if (OAuth2Constants.CODE_CHALLENGE_METHOD_S256.equals(authorization.getCodeChallengeMethod())) {
                computedChallenge = generateS256CodeChallenge(codeVerifier);
            } else {
                computedChallenge = codeVerifier;
            }

            if (!authorization.getCodeChallenge().equals(computedChallenge)) {
                throw new OAuth2AuthorizationException(
                        OAuth2Constants.ERROR_INVALID_REQUEST,
                        "Invalid code verifier",
                        null,
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
            throw new RuntimeException(e);
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
            Set<String> scopes,
            Authentication principal,
            OAuthClient client) {

        String code = codeGenerator.generateKey();
        LocalDateTime now = LocalDateTime.now();

        OAuthAuthorization authorization = new OAuthAuthorization();
        authorization.setClientId(client.getClientId());
        authorization.setUserId(principal.getName());
        authorization.setScopes(String.join(" ", scopes));
        authorization.setAuthorizationCode(code);
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
                .authorizedScopes(scopes)
                .state(null)
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

    private void validateBasicParameters(String responseType, String clientId, String redirectUri) {
        if (!OAuth2Constants.RESPONSE_TYPE_CODE.equals(responseType)) {
            throw new OAuth2AuthorizationException(
                    OAuth2Constants.ERROR_UNSUPPORTED_RESPONSE_TYPE,
                    "Response type must be 'code'",
                    redirectUri,
                    null
            );
        }

        OAuthClient client = validateClient(clientId);
        validateRedirectUri(client, redirectUri);
    }

    private void validateRedirectUri(OAuthClient client, String redirectUri) {
        Set<String> allowedRedirectUris = OAuth2Utils.parseRedirectUris(client.getRedirectUris());
        if (!allowedRedirectUris.contains(redirectUri)) {
            throw new OAuth2AuthorizationException(
                    OAuth2Constants.ERROR_INVALID_REQUEST,
                    "Invalid redirect_uri",
                    redirectUri,
                    null
            );
        }
    }
} 