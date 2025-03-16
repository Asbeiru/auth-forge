package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.dto.TokenRequest;
import com.zhouzhou.authforge.dto.TokenResponse;
import com.zhouzhou.authforge.exception.OAuth2TokenException;
import com.zhouzhou.authforge.service.OAuth2TokenService;
import com.zhouzhou.authforge.validator.TokenRequestSpecificationValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth 2.0 令牌端点控制器
 */
@RestController
@RequestMapping("/oauth2/token")
@RequiredArgsConstructor
@Slf4j
public class OAuth2TokenController {

    private final OAuth2TokenService tokenService;
    private final TokenRequestSpecificationValidator validator;

    @PostMapping
    public ResponseEntity<TokenResponse> token(
            HttpServletRequest request,
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier,
            @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @RequestParam(value = "scope", required = false) String scope) {

        try {
            // 1. 构建令牌请求对象
            TokenRequest tokenRequest = TokenRequest.builder()
                .request(request)
                .grantType(grantType)
                .code(code)
                .redirectUri(redirectUri)
                .codeVerifier(codeVerifier)
                .refreshToken(refreshToken)
                .scope(scope)
                .build();

            // 2. 验证请求
            validator.validate(tokenRequest);

            // 3. 处理令牌请求
            TokenResponse response = tokenService.handleTokenRequest(
                request,
                grantType,
                code,
                redirectUri,
                codeVerifier,
                refreshToken
            );

            return ResponseEntity.ok(response);
        } catch (OAuth2TokenException e) {
            log.warn("Token request failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(TokenResponse.builder()
                    .error(e.getError())
                    .errorDescription(e.getErrorDescription())
                    .build());
        }
    }
} 