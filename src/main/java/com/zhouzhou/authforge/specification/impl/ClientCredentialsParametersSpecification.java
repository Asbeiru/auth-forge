package com.zhouzhou.authforge.specification.impl;

import com.zhouzhou.authforge.dto.TokenRequest;
import com.zhouzhou.authforge.specification.TokenRequestSpecification;

/**
 * 客户端凭证模式参数规范
 */
public class ClientCredentialsParametersSpecification implements TokenRequestSpecification<TokenRequest> {

    @Override
    public boolean isSatisfiedBy(TokenRequest request) {
        if (!request.isClientCredentialsGrant()) {
            return true; // 不是客户端凭证模式，不需要验证
        }
        // 客户端凭证模式不需要额外参数，只需要验证grant_type
        return true;
    }

    @Override
    public String getErrorCode() {
        return "invalid_request";
    }

    @Override
    public String getErrorDescription() {
        return "Invalid parameters for client_credentials grant type";
    }
} 