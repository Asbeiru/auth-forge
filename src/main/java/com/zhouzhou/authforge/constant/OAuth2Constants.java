package com.zhouzhou.authforge.constant;

/**
 * OAuth 2.0 常量定义类
 * 
 * 定义 OAuth 2.0 规范中的常量：
 * 1. 响应类型
 * 2. 授权类型
 * 3. PKCE 方法
 * 4. 错误码
 * 
 * 参考 OAuth 2.0 规范 RFC 6749
 */
public class OAuth2Constants {
    public static final String RESPONSE_TYPE_CODE = "code";
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    public static final String CODE_CHALLENGE_METHOD_PLAIN = "plain";
    public static final String CODE_CHALLENGE_METHOD_S256 = "S256";
    
    // OAuth2 错误码
    public static final String ERROR_INVALID_REQUEST = "invalid_request";
    public static final String ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client";
    public static final String ERROR_ACCESS_DENIED = "access_denied";
    public static final String ERROR_INVALID_SCOPE = "invalid_scope";
    public static final String ERROR_UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";
} 