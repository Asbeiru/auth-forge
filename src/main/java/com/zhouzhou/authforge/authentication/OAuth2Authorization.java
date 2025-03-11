package com.zhouzhou.authforge.authentication;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Getter
@Builder
public class OAuth2Authorization {
    private final String id;
    private final String clientId;
    private final Authentication principal;
    private final String authorizationGrantType;
    private final String authorizationCode;
    private final LocalDateTime authorizationCodeIssuedAt;
    private final LocalDateTime authorizationCodeExpiresAt;
    private final Set<String> authorizedScopes;
    private final String state;
    private final Map<String, Object> attributes;
} 