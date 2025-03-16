package com.zhouzhou.authforge.specification.impl;

import com.zhouzhou.authforge.dto.TokenRequest;
import com.zhouzhou.authforge.specification.TokenRequestSpecification;
import org.springframework.util.StringUtils;

/**
 * 授权码参数规范
 */
public class AuthorizationCodeParametersSpecification implements TokenRequestSpecification<TokenRequest> {

    @Override
    public boolean isSatisfiedBy(TokenRequest request) {
        if (!request.isAuthorizationCodeGrant()) {
            return true; // 不是授权码模式，不需要验证这些参数
        }
        return StringUtils.hasText(request.getCode()) && 
               StringUtils.hasText(request.getRedirectUri());
    }

    @Override
    public String getErrorCode() {
        return "invalid_request";
    }

    @Override
    public String getErrorDescription() {
        return "code and redirect_uri are required for authorization_code grant type";
    }
}