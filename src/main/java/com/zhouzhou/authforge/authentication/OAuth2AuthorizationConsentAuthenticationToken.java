package com.zhouzhou.authforge.authentication;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Set;

@Getter
public class OAuth2AuthorizationConsentAuthenticationToken extends AbstractAuthenticationToken {
    private final String clientId;
    private final Authentication principal;
    private final String redirectUri;
    private final String state;
    private final Set<String> requestedScopes;
    private final Set<String> authorizedScopes;

    public OAuth2AuthorizationConsentAuthenticationToken(
            String clientId,
            Authentication principal,
            String redirectUri,
            String state,
            Set<String> requestedScopes,
            Set<String> authorizedScopes) {
        super(Collections.emptyList());
        this.clientId = clientId;
        this.principal = principal;
        this.redirectUri = redirectUri;
        this.state = state;
        this.requestedScopes = requestedScopes != null ? requestedScopes : Collections.emptySet();
        this.authorizedScopes = authorizedScopes != null ? authorizedScopes : Collections.emptySet();
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
} 