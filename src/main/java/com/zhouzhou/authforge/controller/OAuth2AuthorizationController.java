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

/**
 * OAuth 2.0 授权端点控制器
 * 
 * 实现 OAuth 2.0 授权端点 (Authorization Endpoint)，参考 RFC 6749 Section 4.1.1
 * 
 * 主要职责：
 * 1. 接收并处理授权请求，包含以下必需参数：
 *    - response_type：必须为"code"
 *    - client_id：客户端标识
 *    - redirect_uri：重定向URI
 *    - scope：请求的权限范围（可选）
 *    - state：客户端状态（推荐）
 * 
 * 2. PKCE 扩展支持 (RFC 7636)：
 *    - code_challenge：代码挑战
 *    - code_challenge_method：代码挑战方法（plain 或 S256）
 * 
 * 3. 授权流程：
 *    a) 验证请求参数
 *    b) 验证客户端身份
 *    c) 获取资源所有者的授权许可
 *    d) 重定向带授权码或错误到客户端
 * 
 * @see OAuth2AuthorizationService
 * @see RFC 6749 https://tools.ietf.org/html/rfc6749#section-4.1.1
 * @see RFC 7636 https://tools.ietf.org/html/rfc7636
 */
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