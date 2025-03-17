package com.zhouzhou.authforge.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface OAuth2TokenRevocationService {
    
    /**
     * 撤销令牌
     *
     * @param request HTTP请求，用于获取客户端认证信息
     * @param token 要撤销的令牌
     * @param tokenTypeHint 令牌类型提示（可选）
     * @return 响应实体
     */
    ResponseEntity<?> revokeToken(HttpServletRequest request, String token, String tokenTypeHint);
} 