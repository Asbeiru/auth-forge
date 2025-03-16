package com.zhouzhou.authforge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 令牌配置属性
 */
@ConfigurationProperties(prefix = "auth.token")
@Getter
@Setter
public class TokenProperties {

    /**
     * 令牌生成器类型
     */
    private TokenType type = TokenType.JWT;

    /**
     * JWT 配置
     */
    private JwtProperties jwt = new JwtProperties();

    /**
     * 令牌生成器类型
     */
    public enum TokenType {
        UUID,
        JWT
    }

    /**
     * JWT 配置属性
     */
    @Getter
    @Setter
    public static class JwtProperties {
        /**
         * JWT 签发者
         */
        private String issuer = "auth-forge";

        /**
         * JWT 密钥
         */
        private String secret;

        /**
         * 访问令牌过期时间（分钟）
         */
        private int accessTokenExpirationMinutes = 30;

        /**
         * 刷新令牌过期时间（分钟）
         */
        private int refreshTokenExpirationMinutes = 1440; // 24小时
    }
} 