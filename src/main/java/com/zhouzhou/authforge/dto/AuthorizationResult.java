package com.zhouzhou.authforge.dto;

import com.zhouzhou.authforge.model.OAuthClient;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/**
 * OAuth 2.0 授权结果传输对象
 * 
 * 封装 OAuth 2.0 授权端点的处理结果，参考 RFC 6749 Section 4.1.2
 * 
 * 结果类型：
 * 1. REDIRECT_WITH_CODE：授权成功
 *    - code：授权码
 *    - state：客户端状态（如果请求中包含）
 * 
 * 2. REDIRECT_WITH_ERROR：授权失败
 *    - error：错误码（RFC 6749 Section 4.1.2.1）
 *      * invalid_request：请求缺少必需参数
 *      * unauthorized_client：客户端未授权
 *      * access_denied：用户拒绝授权
 *      * invalid_scope：请求的范围无效
 *    - error_description：错误描述
 *    - state：客户端状态（如果请求中包含）
 * 
 * 3. SHOW_CONSENT_PAGE：需要用户确认
 *    - client：客户端信息
 *    - scopes：请求的权限范围
 * 
 * @see RFC 6749 https://tools.ietf.org/html/rfc6749#section-4.1.2
 */
@Getter
@Builder
public class AuthorizationResult {
    private final ResultType resultType;
    private final String redirectUri;
    private final String code;
    private final String state;
    private final String error;
    private final String errorDescription;
    private final OAuthClient client;
    private final Set<String> scopes;

    public enum ResultType {
        REDIRECT_WITH_CODE,      // 重定向返回授权码
        REDIRECT_WITH_ERROR,     // 重定向返回错误
        SHOW_CONSENT_PAGE        // 显示授权确认页面
    }
} 