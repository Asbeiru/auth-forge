package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.service.OAuth2TokenIntrospectionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth 2.0 令牌自省端点的 Spring 实现，遵循
 * <a href="https://tools.ietf.org/html/rfc7662" target="_blank">RFC 7662</a> 规范。
 *
 * 该端点允许授权客户端查询 OAuth 2.0 令牌的状态和元数据。端点需要客户端认证
 * 以防止令牌扫描攻击。
 *
 * 主要特性：
 * <ul>
 *     <li>支持访问令牌和刷新令牌的自省</li>
 *     <li>要求客户端认证（Basic Auth 或 Bearer Token）</li>
 *     <li>以 JSON 格式返回令牌元数据</li>
 *     <li>实现令牌扫描攻击防护</li>
 *     <li>完全符合 RFC 7662 规范</li>
 * </ul>
 *
 * 请求示例：
 * <pre>
 * POST /oauth2/introspect HTTP/1.1
 * Host: server.example.com
 * Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
 * Content-Type: application/x-www-form-urlencoded
 *
 * token=2YotnFZFEjr1zCsicMWpAA&token_type_hint=access_token
 * </pre>
 *
 * 响应示例：
 * <pre>
 * {
 *     "active": true,
 *     "client_id": "l238j323ds-23ij4",
 *     "username": "jdoe",
 *     "scope": "read write dolphin",
 *     "sub": "Z5O3upPC88QrAjx00dis",
 *     "aud": "https://protected.example.net/resource",
 *     "iss": "https://server.example.com/",
 *     "exp": 1419356238,
 *     "iat": 1419350238
 * }
 * </pre>
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@RestController
@RequestMapping("/oauth2/introspect")
@RequiredArgsConstructor
@Slf4j
public class OAuth2TokenIntrospectionController {

    private final OAuth2TokenIntrospectionService introspectionService;

    /**
     * 处理令牌自省请求。
     *
     * 该端点验证客户端凭据并返回令牌相关信息。如果令牌无效或已过期，
     * 则返回 "active": false 的响应。如果客户端认证失败，则返回
     * 401 Unauthorized 响应。
     *
     * @param request 包含客户端认证信息的 HTTP 请求
     * @param token 要自省的令牌
     * @param tokenTypeHint 令牌类型提示（可选，access_token 或 refresh_token）
     * @return 包含自省响应或错误详情的 ResponseEntity
     */
    @PostMapping
    public ResponseEntity<?> introspectToken(
            HttpServletRequest request,
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint) {
        
        return introspectionService.introspectToken(request, token, tokenTypeHint);
    }
} 