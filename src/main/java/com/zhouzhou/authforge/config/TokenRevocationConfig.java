package com.zhouzhou.authforge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "auth.token.revocation")
public class TokenRevocationConfig {
    
    /**
     * 是否在撤销访问令牌时同时撤销刷新令牌
     */
    private boolean revokeRefreshTokenOnAccessTokenRevocation = true;
    
    /**
     * 临时错误重试时间（秒）
     */
    private int retryAfterSeconds = 60;
} 