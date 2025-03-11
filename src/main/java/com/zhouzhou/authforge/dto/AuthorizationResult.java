package com.zhouzhou.authforge.dto;

import com.zhouzhou.authforge.model.OAuthClient;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

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