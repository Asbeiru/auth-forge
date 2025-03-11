package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.service.OAuth2AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Optional;

import java.util.HashMap;
import java.util.Map;
import com.zhouzhou.authforge.dto.AuthorizationResult;

@Controller
@RequiredArgsConstructor
public class OAuth2AuthorizationController {
    
    private final OAuth2AuthorizationService authorizationService;

    @GetMapping("/oauth2/authorize")
    public String authorize(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String code_challenge,
            @RequestParam(required = false) String code_challenge_method,
            Authentication authentication,
            Model model) {
        
        // 收集额外参数
        Map<String, Object> additionalParameters = new HashMap<>();
        if (code_challenge != null) {
            additionalParameters.put("code_challenge", code_challenge);
            additionalParameters.put("code_challenge_method", code_challenge_method);
        }

        // 处理授权请求
        AuthorizationResult result = authorizationService.handleAuthorizationRequest(
            responseType, clientId, redirectUri, scope, state, 
            additionalParameters, authentication);

        // 根据结果类型处理
        switch (result.getResultType()) {
            case REDIRECT_WITH_CODE:
                return "redirect:" + UriComponentsBuilder.fromUriString(result.getRedirectUri())
                    .queryParam("code", result.getCode())
                    .queryParamIfPresent("state", Optional.ofNullable(result.getState()))
                    .build()
                    .toUriString();

            case REDIRECT_WITH_ERROR:
                return "redirect:" + UriComponentsBuilder.fromUriString(result.getRedirectUri())
                    .queryParam("error", result.getError())
                    .queryParam("error_description", result.getErrorDescription())
                    .queryParamIfPresent("state", Optional.ofNullable(result.getState()))
                    .build()
                    .toUriString();

            case SHOW_CONSENT_PAGE:
                model.addAttribute("client", result.getClient());
                model.addAttribute("scopes", result.getScopes());
                model.addAttribute("state", result.getState());
                model.addAttribute("redirectUri", result.getRedirectUri());
                return "consent";

            default:
                throw new IllegalStateException("Unexpected result type: " + result.getResultType());
        }
    }
} 