package com.zhouzhou.authforge.specification.impl;

import com.zhouzhou.authforge.dto.TokenRequest;
import com.zhouzhou.authforge.specification.TokenRequestSpecification;
import org.springframework.util.StringUtils;

/**
 * 刷新令牌参数规范
 */
public class RefreshTokenParametersSpecification implements TokenRequestSpecification<TokenRequest> {

    @Override
    public boolean isSatisfiedBy(TokenRequest request) {
        if (!request.isRefreshTokenGrant()) {
            return true; // 不是刷新令牌模式，不需要验证这些参数
        }
        return StringUtils.hasText(request.getRefreshToken());
    }

    @Override
    public String getErrorCode() {
        return "invalid_request";
    }

    @Override
    public String getErrorDescription() {
        return "refresh_token is required for refresh_token grant type";
    }
}