package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.service.OAuth2AuthorizationService;
import com.zhouzhou.authforge.dto.AuthorizationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OAuth 2.0 授权确认控制器
 * 
 * 处理 /oauth2/authorize/consent 端点的用户授权确认请求
 * 负责：
 * 1. 处理用户对授权请求的同意或拒绝
 * 2. 记录用户的授权决定
 * 3. 根据用户的选择生成授权码或返回错误
 * 
 * @see OAuth2AuthorizationService
 */
@Controller
@RequiredArgsConstructor
public class ConsentController {

    private final OAuth2AuthorizationService authorizationService;

    @PostMapping("/oauth2/authorize/consent")
    public String handleConsent(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String state,
            @RequestParam("consent") String consent,
            Authentication authentication) {

        AuthorizationResult result = authorizationService.handleAuthorizationConsent(
            clientId, redirectUri, scope, state, consent, authentication);

        // 构建重定向URL
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(result.getRedirectUri());
        
        if (result.getResultType() == AuthorizationResult.ResultType.REDIRECT_WITH_CODE) {
            builder.queryParam("code", result.getCode());
            if (result.getState() != null) {
                builder.queryParam("state", result.getState());
            }
        } else {
            builder.queryParam("error", result.getError())
                   .queryParam("error_description", result.getErrorDescription());
            if (result.getState() != null) {
                builder.queryParam("state", result.getState());
            }
        }

        return "redirect:" + builder.build().toUriString();
    }
} 