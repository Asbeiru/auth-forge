package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.model.OAuthAuthorization;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthAuthorizationRepository;
import com.zhouzhou.authforge.repository.OAuthClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthClientRepository clientRepository;
    private final OAuthAuthorizationRepository authorizationRepository;
    private final StringKeyGenerator codeGenerator = new Base64StringKeyGenerator(32);

    @GetMapping("/oauth2/authorize")
    public String authorize(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod,
            Authentication authentication,
            Model model) {

        // 1. 验证 response_type
        if (!"code".equals(responseType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid response_type");
        }

        // 2. 验证客户端
        OAuthClient client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid client_id"));

        // 3. 验证重定向URI
        validateRedirectUri(client, redirectUri);

        // 4. 验证授权类型
        if (!client.getAuthorizedGrantTypes().contains("authorization_code")) {
            return redirectWithError(redirectUri, "unauthorized_client", state);
        }

        // 5. 验证作用域
        Set<String> requestedScopes = scope != null ? 
            Arrays.stream(scope.split(" ")).collect(Collectors.toSet()) : 
            Set.of();
        Set<String> allowedScopes = Arrays.stream(client.getScopes().split(" "))
                .collect(Collectors.toSet());
        if (!allowedScopes.containsAll(requestedScopes)) {
            return redirectWithError(redirectUri, "invalid_scope", state);
        }

        // 6. 验证PKCE（如果提供）
        if (codeChallenge != null) {
            if (!"S256".equals(codeChallengeMethod) && !"plain".equals(codeChallengeMethod)) {
                return redirectWithError(redirectUri, "invalid_request", state);
            }
        }

        // 7. 检查用户是否已授权
        List<OAuthAuthorization> existingAuths = authorizationRepository
                .findByClientIdAndUserId(clientId, authentication.getName());
        
        if (existingAuths.isEmpty()) {
            // 未授权，重定向到授权页面
            model.addAttribute("clientName", client.getClientName());
            model.addAttribute("scopes", requestedScopes);
            model.addAttribute("state", state);
            return "consent";
        }

        // 8. 已授权，生成授权码
        String code = codeGenerator.generateKey();
        OAuthAuthorization authorization = new OAuthAuthorization();
        authorization.setClientId(clientId);
        authorization.setUserId(authentication.getName());
        authorization.setScopes(scope);
        authorization.setAuthorizationCode(code);
        authorization.setCodeChallenge(codeChallenge);
        authorization.setCodeChallengeMethod(codeChallengeMethod);
        authorization.setState(state);
        authorization.setAuthorizationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        authorizationRepository.save(authorization);

        // 9. 重定向回客户端
        return "redirect:" + UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", code)
                .queryParamIfPresent("state", Optional.ofNullable(state))
                .build()
                .toUriString();
    }

    private void validateRedirectUri(OAuthClient client, String redirectUri) {
        Set<String> allowedRedirectUris = Arrays.stream(client.getRedirectUris().split(","))
                .collect(Collectors.toSet());
        if (!allowedRedirectUris.contains(redirectUri)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid redirect_uri");
        }
    }

    private String redirectWithError(String redirectUri, String error, String state) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", error);
        if (state != null) {
            builder.queryParam("state", state);
        }
        return "redirect:" + builder.build().toUriString();
    }
} 