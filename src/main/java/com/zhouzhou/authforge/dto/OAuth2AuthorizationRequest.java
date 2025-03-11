package com.zhouzhou.authforge.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class OAuth2AuthorizationRequest {
    private String responseType;
    private String clientId;
    private String redirectUri;
    private String scope;
    private String state;
    private String codeChallenge;
    private String codeChallengeMethod;
    private String prompt;
} 