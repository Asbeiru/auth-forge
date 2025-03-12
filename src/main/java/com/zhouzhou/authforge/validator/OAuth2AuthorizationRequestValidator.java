package com.zhouzhou.authforge.validator;

import com.zhouzhou.authforge.constant.OAuth2Constants;
import com.zhouzhou.authforge.exception.OAuth2AuthorizationException;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * OAuth 2.0 授权请求参数验证器
 * 
 * 验证授权请求的必需参数和可选参数:
 * 1. 必需参数:
 *    - response_type
 *    - client_id
 *    - redirect_uri
 * 
 * 2. 可选参数:
 *    - scope
 *    - state
 *    - code_challenge
 *    - code_challenge_method
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthorizationRequestValidator {

    private final OAuthClientRepository clientRepository;

    /**
     * 验证授权请求参数
     */
    public void validateAuthorizationRequest(
            String clientId,
            String responseType,
            String redirectUri,
            String scope,
            String state,
            Map<String, Object> additionalParameters) {

        // 1. 验证必需参数
        validateRequiredParameters(clientId, responseType, redirectUri);

        // 2. 获取并验证客户端
        OAuthClient client = validateClient(clientId);

        // 3. 验证响应类型
        validateResponseType(responseType, redirectUri, state);

        // 4. 验证重定向URI
        validateRedirectUri(client, redirectUri, state);

        // 5. 验证授权类型
        validateGrantType(client, redirectUri, state);

        // 6. 验证作用域
        if (StringUtils.hasText(scope)) {
            validateScopes(client, scope, redirectUri, state);
        }

        // 7. 验证PKCE参数
        validatePkceParameters(additionalParameters, redirectUri, state);

        // 8. 验证state参数长度
        if (StringUtils.hasText(state) && state.length() > 256) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "State parameter exceeds 256 characters",
                redirectUri,
                state);
        }
    }

    private void validateRequiredParameters(String clientId, String responseType, String redirectUri) {
        if (!StringUtils.hasText(clientId)) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Missing client_id parameter",
                null,
                null);
        }

        if (!StringUtils.hasText(responseType)) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Missing response_type parameter",
                redirectUri,
                null);
        }

        if (!StringUtils.hasText(redirectUri)) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Missing redirect_uri parameter",
                null,
                null);
        }
    }

    private OAuthClient validateClient(String clientId) {
        return clientRepository.findByClientId(clientId)
            .orElseThrow(() -> new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_UNAUTHORIZED_CLIENT,
                "Client not found",
                null,
                null));
    }

    private void validateResponseType(String responseType, String redirectUri, String state) {
        if (!OAuth2Constants.RESPONSE_TYPE_CODE.equals(responseType)) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_UNSUPPORTED_RESPONSE_TYPE,
                "Response type must be 'code'",
                redirectUri,
                state);
        }
    }

    private void validateRedirectUri(OAuthClient client, String redirectUri, String state) {
        Set<String> allowedRedirectUris = new HashSet<>(Arrays.asList(client.getRedirectUris().split(" ")));
        if (!allowedRedirectUris.contains(redirectUri)) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_REQUEST,
                "Invalid redirect_uri",
                redirectUri,
                state);
        }
    }

    private void validateGrantType(OAuthClient client, String redirectUri, String state) {
        if (!client.getAuthorizedGrantTypes().contains(OAuth2Constants.GRANT_TYPE_AUTHORIZATION_CODE)) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_UNAUTHORIZED_CLIENT,
                "Client is not authorized for authorization_code grant",
                redirectUri,
                state);
        }

    }

    private void validateScopes(OAuthClient client, String scope, String redirectUri, String state) {
        Set<String> requestedScopes = new HashSet<>(Arrays.asList(scope.split(" ")));
        Set<String> allowedScopes = new HashSet<>(Arrays.asList(client.getScopes().split(" ")));

        if (!allowedScopes.containsAll(requestedScopes)) {
            throw new OAuth2AuthorizationException(
                OAuth2Constants.ERROR_INVALID_SCOPE,
                "Invalid scope requested",
                redirectUri,
                state);
        }
    }

    private void validatePkceParameters(Map<String, Object> additionalParameters, String redirectUri, String state) {
        String codeChallenge = (String) additionalParameters.get("code_challenge");
        String codeChallengeMethod = (String) additionalParameters.get("code_challenge_method");

        if (codeChallenge != null) {
            // 验证code_challenge长度(最小43,最大128)
            if (codeChallenge.length() < 43 || codeChallenge.length() > 128) {
                throw new OAuth2AuthorizationException(
                    OAuth2Constants.ERROR_INVALID_REQUEST,
                    "Invalid code_challenge length",
                    redirectUri,
                    state);
            }

            // 验证code_challenge字符集(Base64URL-encoded)
            if (!codeChallenge.matches("^[A-Za-z0-9\\-._~]+$")) {
                throw new OAuth2AuthorizationException(
                    OAuth2Constants.ERROR_INVALID_REQUEST,
                    "Invalid code_challenge format",
                    redirectUri,
                    state);
            }

            // 验证code_challenge_method
            if (codeChallengeMethod != null &&
                !OAuth2Constants.CODE_CHALLENGE_METHOD_PLAIN.equals(codeChallengeMethod) &&
                !OAuth2Constants.CODE_CHALLENGE_METHOD_S256.equals(codeChallengeMethod)) {
                throw new OAuth2AuthorizationException(
                    OAuth2Constants.ERROR_INVALID_REQUEST,
                    "Invalid code_challenge_method",
                    redirectUri,
                    state);
            }
        }
    }
} 