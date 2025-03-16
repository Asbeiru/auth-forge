package com.zhouzhou.authforge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth 2.0 令牌响应DTO
 * 
 * 用于封装令牌端点的响应数据，包含：
 * 1. 访问令牌
 * 2. 刷新令牌（如果支持）
 * 3. ID令牌（如果scope包含openid）
 * 4. 令牌类型
 * 5. 过期时间
 * 6. 授权范围
 * 7. 错误码
 * 8. 错误描述
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {
    
    /**
     * 访问令牌
     */
    @JsonProperty("access_token")
    private final String accessToken;
    
    /**
     * 刷新令牌
     */
    @JsonProperty("refresh_token")
    private final String refreshToken;
    
    /**
     * ID令牌
     */
    @JsonProperty("id_token")
    private final String idToken;
    
    /**
     * 令牌类型
     */
    @JsonProperty("token_type")
    private final String tokenType;
    
    /**
     * 过期时间（秒）
     */
    @JsonProperty("expires_in")
    private final Long expiresIn;
    
    /**
     * 授权范围
     */
    @JsonProperty("scope")
    private final String scope;
    
    /**
     * 错误码
     */
    @JsonProperty("error")
    private final String error;
    
    /**
     * 错误描述
     */
    @JsonProperty("error_description")
    private final String errorDescription;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String idToken;
        private String tokenType;
        private Long expiresIn;
        private String scope;
        private String error;
        private String errorDescription;

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder idToken(String idToken) {
            this.idToken = idToken;
            return this;
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder errorDescription(String errorDescription) {
            this.errorDescription = errorDescription;
            return this;
        }

        public TokenResponse build() {
            return new TokenResponse(
                accessToken,
                refreshToken,
                idToken,
                tokenType,
                expiresIn,
                scope,
                error,
                errorDescription
            );
        }
    }
} 