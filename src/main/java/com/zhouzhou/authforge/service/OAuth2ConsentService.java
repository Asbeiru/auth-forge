package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.dto.AuthorizationResult;
import com.zhouzhou.authforge.exception.OAuth2AuthorizationException;
import com.zhouzhou.authforge.model.OAuthAuthorization;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.model.OAuthConsent;
import com.zhouzhou.authforge.repository.OAuthAuthorizationRepository;
import com.zhouzhou.authforge.repository.OAuthConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2ConsentService {

    private final OAuthConsentRepository consentRepository;
    private final OAuth2ClientService clientService;
    private final OAuthAuthorizationRepository authorizationRepository;

    /**
     * 检查是否需要用户同意
     */
    public boolean isConsentRequired(OAuthClient client, Authentication authentication, String requestedScope) {
        // 如果客户端配置为自动批准,则不需要用户同意
        if (client.getAutoApprove()) {
            return false;
        }

        // 检查用户是否已经同意过所请求的权限范围
        Optional<OAuthConsent> existingConsent = consentRepository.findByClientIdAndUserId(
            client.getClientId(), 
            authentication.getName()
        );

        if (existingConsent.isEmpty()) {
            return true;
        }

        // 检查已同意的权限范围是否包含所有请求的权限范围
        String[] requestedScopes = requestedScope.split(" ");
        String[] approvedScopes = existingConsent.get().getScopes().split(" ");

        for (String scope : requestedScopes) {
            boolean found = false;
            for (String approvedScope : approvedScopes) {
                if (scope.equals(approvedScope)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return true;
            }
        }

        return false;
    }

    /**
     * 查找已有的授权同意记录
     * @param clientId 客户端ID
     * @param userId 用户ID
     * @return 授权同意记录，如果不存在则返回null
     */
    public OAuthConsent findConsent(String clientId, String userId) {
        return consentRepository.findByClientIdAndUserId(clientId, userId)
            .orElse(null);
    }

    /**
     * 保存用户同意记录
     */
    @Transactional
    public OAuthConsent saveConsent(OAuthClient client, Authentication authentication, String scope) {
        // 查找现有的同意记录
        Optional<OAuthConsent> existingConsent = consentRepository.findByClientIdAndUserId(
            client.getClientId(), 
            authentication.getName()
        );

        OAuthConsent consent;
        if (existingConsent.isPresent()) {
            // 更新现有同意记录
            consent = existingConsent.get();
            // 合并新的权限范围
            String[] existingScopes = consent.getScopes().split(" ");
            String[] newScopes = scope.split(" ");
            StringBuilder mergedScopes = new StringBuilder(consent.getScopes());
            
            for (String newScope : newScopes) {
                boolean found = false;
                for (String existingScope : existingScopes) {
                    if (newScope.equals(existingScope)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    mergedScopes.append(" ").append(newScope);
                }
            }
            consent.setScopes(mergedScopes.toString().trim());
        } else {
            // 创建新的同意记录
            consent = new OAuthConsent();
            consent.setClientId(client.getClientId());
            consent.setUserId(authentication.getName());
            consent.setScopes(scope);
        }

        return consentRepository.save(consent);
    }

    /**
     * 撤销用户同意
     */
    @Transactional
    public void revokeConsent(String clientId, String userId) {
        Optional<OAuthConsent> consent = consentRepository.findByClientIdAndUserId(clientId, userId);
        consent.ifPresent(consentRepository::delete);
    }

    /**
     * 处理授权同意请求
     */
    @Transactional
    public AuthorizationResult handleAuthorizationConsent(
            String clientId,
            String redirectUri,
            String scope,
            String traceId,
            String consent,
            Authentication authentication) {

        try {
            // 1. 根据traceId获取原始授权请求
            OAuthAuthorization originalAuthorization = authorizationRepository.findByTraceId(traceId)
                .orElseThrow(() -> new OAuth2AuthorizationException(
                    "invalid_request",
                    "Invalid trace_id parameter",
                    redirectUri,
                    null));

            // 2. 验证客户端ID是否匹配
            if (!originalAuthorization.getClientId().equals(clientId)) {
                throw new OAuth2AuthorizationException(
                    "invalid_request",
                    "Client ID mismatch",
                    redirectUri,
                    originalAuthorization.getState());
            }

            // 3. 验证用户身份是否匹配
            if (!originalAuthorization.getUserId().equals(authentication.getName())) {
                throw new OAuth2AuthorizationException(
                    "access_denied",
                    "User mismatch",
                    redirectUri,
                    originalAuthorization.getState());
            }

            // 4. 获取客户端信息
            OAuthClient client = clientService.findByClientId(clientId)
                .orElseThrow(() -> new OAuth2AuthorizationException(
                    "unauthorized_client",
                    "Client not found",
                    redirectUri,
                    originalAuthorization.getState()));

            // 5. 验证和处理授权范围
            Set<String> requestedScopes = new HashSet<>(Arrays.asList(originalAuthorization.getScopes().split(" ")));
            Set<String> authorizedScopes = new HashSet<>(Arrays.asList(scope.split(" ")));
            
            // 验证请求的scope是否合法
            if (!requestedScopes.containsAll(authorizedScopes)) {
                throw new OAuth2AuthorizationException(
                    "invalid_scope",
                    "Invalid scope",
                    redirectUri,
                    originalAuthorization.getState());
            }

            // 获取已有的授权同意记录
            OAuthConsent currentConsent = findConsent(client.getClientId(), authentication.getName());
            Set<String> currentAuthorizedScopes = currentConsent != null 
                ? new HashSet<>(Arrays.asList(currentConsent.getScopes().split(" "))) 
                : Collections.emptySet();

            // 添加之前已经同意过的scopes
            if (!currentAuthorizedScopes.isEmpty()) {
                for (String requestedScope : requestedScopes) {
                    if (currentAuthorizedScopes.contains(requestedScope)) {
                        authorizedScopes.add(requestedScope);
                    }
                }
            }

            // 自动添加不需要同意的scopes
            if (!authorizedScopes.isEmpty() && requestedScopes.contains("openid")) {
                authorizedScopes.add("openid");
            }

            // 6. 处理用户同意决定
            if (!"approve".equals(consent)) {
                // 如果用户拒绝，删除授权请求
                authorizationRepository.delete(originalAuthorization);
                throw new OAuth2AuthorizationException(
                    "access_denied",
                    "User denied access",
                    redirectUri,
                    originalAuthorization.getState());
            }

            // 7. 保存用户同意记录
            saveConsent(client, authentication, String.join(" ", authorizedScopes));

            // 8. 生成授权码
            String code = UUID.randomUUID().toString();
            OAuthAuthorization authorization = new OAuthAuthorization();
            authorization.setClientId(clientId);
            authorization.setUserId(authentication.getName());
            authorization.setScopes(String.join(" ", authorizedScopes));
            authorization.setState(originalAuthorization.getState());
            authorization.setRedirectUri(redirectUri);
            authorization.setAuthorizationCode(code);
            authorization.setAuthorizationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
            authorization.setResponseType(originalAuthorization.getResponseType());
            authorization.setTraceId(originalAuthorization.getTraceId());
            
            // 如果生成失败，返回服务器错误
            if (code == null) {
                throw new OAuth2AuthorizationException(
                    "server_error",
                    "Failed to generate authorization code",
                    redirectUri,
                    originalAuthorization.getState());
            }

            // 9. 保存新的授权记录，删除原始授权请求
            authorizationRepository.save(authorization);
            authorizationRepository.delete(originalAuthorization);

            return AuthorizationResult.builder()
                .resultType(AuthorizationResult.ResultType.REDIRECT_WITH_CODE)
                .redirectUri(redirectUri)
                .code(code)
                .state(originalAuthorization.getState())
                .build();

        } catch (OAuth2AuthorizationException e) {
            return AuthorizationResult.builder()
                .resultType(AuthorizationResult.ResultType.REDIRECT_WITH_ERROR)
                .redirectUri(redirectUri)
                .error(e.getError())
                .errorDescription(e.getMessage())
                .state(e.getState())
                .build();
        }
    }
} 