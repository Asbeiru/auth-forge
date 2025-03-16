package com.zhouzhou.authforge.config;

import com.zhouzhou.authforge.service.TokenGenerator;
import com.zhouzhou.authforge.service.impl.JwtTokenGenerator;
import com.zhouzhou.authforge.service.impl.UuidTokenGenerator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 令牌生成器配置
 */
@Configuration
@EnableConfigurationProperties(TokenProperties.class)
public class TokenGeneratorConfig {


    /**
     * 根据配置选择令牌生成器
     */
    @Bean
    @Primary
    public TokenGenerator tokenGenerator(TokenProperties properties) {
        return switch (properties.getType()) {
            case UUID -> new UuidTokenGenerator();
            case JWT -> new JwtTokenGenerator(properties.getJwt().getSecret());
        };
    }
} 