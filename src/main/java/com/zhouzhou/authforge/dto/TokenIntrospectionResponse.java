package com.zhouzhou.authforge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenIntrospectionResponse {
    /**
     * REQUIRED. Boolean indicator of whether or not the presented token is currently active.
     */
    private boolean active;

    /**
     * OPTIONAL. A JSON string containing a space-separated list of scopes associated with this token.
     */
    private String scope;

    /**
     * OPTIONAL. Client identifier for the OAuth 2.0 client that requested this token.
     */
    private String client_id;

    /**
     * OPTIONAL. Human-readable identifier for the resource owner who authorized this token.
     */
    private String username;

    /**
     * OPTIONAL. Type of the token.
     */
    private String token_type;

    /**
     * OPTIONAL. Integer timestamp indicating when this token will expire.
     */
    private Long exp;

    /**
     * OPTIONAL. Integer timestamp indicating when this token was originally issued.
     */
    private Long iat;

    /**
     * OPTIONAL. Integer timestamp indicating when this token is not to be used before.
     */
    private Long nbf;

    /**
     * OPTIONAL. Subject of the token.
     */
    private String sub;

    /**
     * OPTIONAL. Service-specific string identifier or list of string identifiers representing the intended audience.
     */
    private String aud;

    /**
     * OPTIONAL. String representing the issuer of this token.
     */
    private String iss;

    /**
     * OPTIONAL. String identifier for the token.
     */
    private String jti;
} 