package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.service.OAuth2AuthorizationService;
import com.zhouzhou.authforge.dto.AuthorizationResult;
import com.zhouzhou.authforge.service.OAuth2ConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    private final OAuth2ConsentService oAuth2ConsentService;

    @PostMapping("/oauth2/authorize/consent")
    public String handleConsent(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("scope") String scope,
            @RequestParam("trace_id") String traceId,
            @RequestParam("consent") String consent,
            Authentication authentication) {

        AuthorizationResult result = oAuth2ConsentService.handleAuthorizationConsent(
                clientId,
                redirectUri,
                scope,
                traceId,
                consent,
                authentication);

        if (result.getResultType() == AuthorizationResult.ResultType.REDIRECT_WITH_CODE) {
            return "redirect:" + result.getRedirectUri() + "?code=" + result.getCode() +
                    (result.getState() != null ? "&state=" + result.getState() : "");
        } else {
            return "redirect:" + result.getRedirectUri() + "?error=" + result.getError() +
                    "&error_description=" + result.getErrorDescription() +
                    (result.getState() != null ? "&state=" + result.getState() : "");
        }
    }
} 