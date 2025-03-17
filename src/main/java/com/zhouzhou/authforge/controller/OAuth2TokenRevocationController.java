package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.service.OAuth2TokenRevocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth 2.0 令牌撤销端点的 Spring 实现，遵循
 * <a href="https://tools.ietf.org/html/rfc7009" target="_blank">RFC 7009</a> 规范。
 *
 * 该端点允许客户端通知授权服务器，之前获取的刷新令牌或访问令牌不再需要。
 * 端点需要客户端认证以防止未授权的令牌撤销。
 *
 * 主要特性：
 * <ul>
 *     <li>支持撤销访问令牌和刷新令牌</li>
 *     <li>要求客户端认证（Basic Auth 或 Bearer Token）</li>
 *     <li>实现相关令牌的级联撤销</li>
 *     <li>防止令牌扫描攻击</li>
 *     <li>完全符合 RFC 7009 规范</li>
 * </ul>
 *
 * 请求示例：
 * <pre>
 * POST /oauth2/revoke HTTP/1.1
 * Host: server.example.com
 * Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
 * Content-Type: application/x-www-form-urlencoded
 *
 * token=45ghiukldjahdnhzdauz&token_type_hint=refresh_token
 * </pre>
 *
 * 响应示例：
 * <pre>
 * HTTP/1.1 200 OK
 * Content-Type: application/json
 *
 * {
 *     "active": false
 * }
 * </pre>
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@RestController
@RequestMapping("/oauth2/revoke")
@RequiredArgsConstructor
@Slf4j
public class OAuth2TokenRevocationController {

    private final OAuth2TokenRevocationService revocationService;

    /**
     * 处理令牌撤销请求。
     *
     * 该端点验证客户端凭据并撤销指定的令牌。即使令牌无效或已被撤销，
     * 根据 RFC 7009 规范，仍然返回 200 OK 响应。如果客户端认证失败，
     * 则返回 401 Unauthorized 响应。
     *
     * @param request 包含客户端认证信息的 HTTP 请求
     * @param token 要撤销的令牌
     * @param tokenTypeHint 令牌类型提示（可选，access_token 或 refresh_token）
     * @return 包含撤销响应或错误详情的 ResponseEntity
     */
    @PostMapping
    public ResponseEntity<?> revokeToken(
            HttpServletRequest request,
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint) {
        
        return revocationService.revokeToken(request, token, tokenTypeHint);
    }
} 