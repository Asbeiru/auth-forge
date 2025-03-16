package com.zhouzhou.authforge.validator;

import com.zhouzhou.authforge.exception.OAuth2TokenException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Token 请求参数验证器
 */
@Component
public class TokenRequestValidator {

    /**
     * 验证令牌请求的基本参数
     * 
     * @param grantType 授权类型
     * @param code 授权码（authorization_code 模式必需）
     * @param redirectUri 重定向URI（authorization_code 模式必需）
     * @param refreshToken 刷新令牌（refresh_token 模式必需）
     * @throws OAuth2TokenException 如果验证失败
     */
    public void validateBasicParameters(
            String grantType,
            String code,
            String redirectUri,
            String refreshToken) {
        
        // 1. 验证授权类型是否支持
        validateGrantType(grantType);

        // 2. 根据授权类型验证必需参数
        switch (grantType) {
            case "authorization_code" -> validateAuthorizationCodeParameters(code, redirectUri);
            case "refresh_token" -> validateRefreshTokenParameters(refreshToken);
            default -> throw new OAuth2TokenException(
                "unsupported_grant_type",
                "Unsupported grant type: " + grantType
            );
        }
    }

    /**
     * 验证授权类型是否支持
     */
    private void validateGrantType(String grantType) {
        if (!StringUtils.hasText(grantType)) {
            throw new OAuth2TokenException(
                "invalid_request",
                "grant_type parameter is required"
            );
        }
    }

    /**
     * 验证授权码模式的必需参数
     */
    private void validateAuthorizationCodeParameters(String code, String redirectUri) {
        if (!StringUtils.hasText(code)) {
            throw new OAuth2TokenException(
                "invalid_request",
                "code parameter is required for authorization_code grant type"
            );
        }
        if (!StringUtils.hasText(redirectUri)) {
            throw new OAuth2TokenException(
                "invalid_request",
                "redirect_uri parameter is required for authorization_code grant type"
            );
        }
    }

    /**
     * 验证刷新令牌模式的必需参数
     */
    private void validateRefreshTokenParameters(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new OAuth2TokenException(
                "invalid_request",
                "refresh_token parameter is required for refresh_token grant type"
            );
        }
    }
} 