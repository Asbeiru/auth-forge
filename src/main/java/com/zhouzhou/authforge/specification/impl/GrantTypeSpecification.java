package com.zhouzhou.authforge.specification.impl;

import com.zhouzhou.authforge.dto.TokenRequest;
import com.zhouzhou.authforge.specification.TokenRequestSpecification;
import org.springframework.util.StringUtils;

/**
 * 授权类型规范
 */
public class GrantTypeSpecification implements TokenRequestSpecification<TokenRequest> {

    @Override
    public boolean isSatisfiedBy(TokenRequest request) {
        if (!StringUtils.hasText(request.getGrantType())) {
            return false;
        }
        return request.isAuthorizationCodeGrant() || 
               request.isRefreshTokenGrant() ||
               request.isClientCredentialsGrant();
    }

    @Override
    public String getErrorCode() {
        return "unsupported_grant_type";
    }

    @Override
    public String getErrorDescription() {
        return "Grant type must be one of: authorization_code, refresh_token, client_credentials";
    }
}