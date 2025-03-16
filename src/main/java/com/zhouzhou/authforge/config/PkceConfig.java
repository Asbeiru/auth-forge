package com.zhouzhou.authforge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * PKCE 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auth-forge.pkce")
public class PkceConfig {

    /**
     * 是否要求所有客户端使用 PKCE
     */
    private boolean requireProofKey = false;

    /**
     * 默认的 code challenge 方法
     */
    private String defaultChallengeMethod = "S256";

    /**
     * PKCE 相关参数名
     */
    public static class PkceParameterNames {
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    }
} 