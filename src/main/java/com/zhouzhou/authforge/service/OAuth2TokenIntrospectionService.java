package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.dto.TokenIntrospectionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface OAuth2TokenIntrospectionService {
    
    /**
     * 验证令牌并返回令牌信息
     *
     * @param request HTTP请求，用于获取客户端认证信息
     * @param token 要验证的令牌
     * @param tokenTypeHint 令牌类型提示（可选）
     * @return 令牌验证响应
     */
    ResponseEntity<?> introspectToken(HttpServletRequest request, String token, String tokenTypeHint);
} 