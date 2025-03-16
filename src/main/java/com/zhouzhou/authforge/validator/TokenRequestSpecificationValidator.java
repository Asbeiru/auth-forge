package com.zhouzhou.authforge.validator;

import com.zhouzhou.authforge.dto.TokenRequest;
import com.zhouzhou.authforge.exception.OAuth2TokenException;
import com.zhouzhou.authforge.specification.TokenRequestSpecification;
import com.zhouzhou.authforge.specification.impl.AuthorizationCodeParametersSpecification;
import com.zhouzhou.authforge.specification.impl.ClientCredentialsParametersSpecification;
import com.zhouzhou.authforge.specification.impl.GrantTypeSpecification;
import com.zhouzhou.authforge.specification.impl.RefreshTokenParametersSpecification;
import org.springframework.stereotype.Component;

/**
 * 基于规范模式的令牌请求验证器
 */
@Component
public class TokenRequestSpecificationValidator {

    private final TokenRequestSpecification<TokenRequest> specification;

    public TokenRequestSpecificationValidator() {
        // 组合所有规范
        this.specification = new GrantTypeSpecification()
            .and(new AuthorizationCodeParametersSpecification())
            .and(new RefreshTokenParametersSpecification())
            .and(new ClientCredentialsParametersSpecification());
    }

    /**
     * 验证令牌请求
     *
     * @param request 令牌请求
     * @throws OAuth2TokenException 如果验证失败
     */
    public void validate(TokenRequest request) {
        if (!specification.isSatisfiedBy(request)) {
            throw new OAuth2TokenException(
                specification.getErrorCode(),
                specification.getErrorDescription()
            );
        }
    }
}