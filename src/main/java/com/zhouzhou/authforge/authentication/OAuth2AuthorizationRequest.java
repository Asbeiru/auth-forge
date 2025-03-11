package com.zhouzhou.authforge.authentication;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Getter
@Builder
public class OAuth2AuthorizationRequest {
    private final String authorizationUri;
    private final String responseType;
    private final String clientId;
    private final String redirectUri;
    private final Set<String> scopes;
    private final String state;
    private final Map<String, Object> additionalParameters;

    public Map<String, Object> getAdditionalParameters() {
        return additionalParameters != null ? additionalParameters : Collections.emptyMap();
    }
} 