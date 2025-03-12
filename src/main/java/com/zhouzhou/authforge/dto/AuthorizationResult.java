package com.zhouzhou.authforge.dto;

import com.zhouzhou.authforge.model.OAuthClient;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * 授权结果DTO
 * 
 * 用于封装OAuth 2.0授权请求的处理结果:
 * 1. 重定向到同意页面
 * 2. 重定向到客户端(带授权码)
 * 3. 重定向到客户端(带错误信息)
 */
@Data
@Builder
public class AuthorizationResult {

    /**
     * 结果类型
     */
    private ResultType resultType;

    /**
     * 客户端信息(用于同意页面)
     */
    private OAuthClient client;

    /**
     * 请求的权限范围(用于同意页面)
     */
    private Set<String> scopes;

    /**
     * 重定向URI
     */
    private String redirectUri;

    /**
     * 授权码
     */
    private String code;

    /**
     * 错误码
     */
    private String error;

    /**
     * 错误描述
     */
    private String errorDescription;

    /**
     * 状态参数
     */
    private String state;

    /**
     * 跟踪ID
     */
    private String traceId;

    /**
     * 结果类型枚举
     */
    public enum ResultType {
        /**
         * 显示同意页面
         */
        SHOW_CONSENT_PAGE,

        /**
         * 重定向(带授权码)
         */
        REDIRECT_WITH_CODE,

        /**
         * 重定向(带错误信息)
         */
        REDIRECT_WITH_ERROR
    }
} 