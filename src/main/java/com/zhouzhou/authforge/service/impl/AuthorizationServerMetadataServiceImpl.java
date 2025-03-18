package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.dto.AuthorizationServerMetadata;
import com.zhouzhou.authforge.service.AuthorizationServerMetadataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * OAuth 2.0 授权服务器元数据服务实现类
 */
@Service
public class AuthorizationServerMetadataServiceImpl implements AuthorizationServerMetadataService {

    @Value("${auth.server.issuer}")
    private String issuer;

    @Value("${auth.server.authorization-endpoint}")
    private String authorizationEndpoint;

    @Value("${auth.server.token-endpoint}")
    private String tokenEndpoint;

    @Value("${auth.server.device-authorization-endpoint}")
    private String deviceAuthorizationEndpoint;

    @Value("${auth.server.revocation-endpoint}")
    private String revocationEndpoint;

    @Value("${auth.server.introspection-endpoint}")
    private String introspectionEndpoint;

    @Value("${auth.server.jwks-uri}")
    private String jwksUri;

    @Value("${auth.server.service-documentation:#{null}}")
    private String serviceDocumentation;

    private static final List<String> TOKEN_ENDPOINT_AUTH_METHODS = Arrays.asList(
        "client_secret_basic",
        "client_secret_post",
        "private_key_jwt"
    );

    private static final List<String> TOKEN_ENDPOINT_AUTH_SIGNING_ALGS = Arrays.asList(
        "RS256",
        "ES256"
    );

    private static final List<String> RESPONSE_TYPES = Arrays.asList(
        "code",
        "device_code"
    );

    private static final List<String> GRANT_TYPES = Arrays.asList(
        "authorization_code",
        "refresh_token",
        "client_credentials",
        "urn:ietf:params:oauth:grant-type:device_code"
    );

    private static final List<String> SCOPES = Arrays.asList(
        "read",
        "write",
        "offline_access"
    );

    private static final List<String> UI_LOCALES = Arrays.asList(
        "en-US",
        "zh-CN"
    );

    @Override
    public AuthorizationServerMetadata getMetadata() {
        return AuthorizationServerMetadata.builder()
            .issuer(issuer)
            .authorizationEndpoint(authorizationEndpoint)
            .tokenEndpoint(tokenEndpoint)
            .deviceAuthorizationEndpoint(deviceAuthorizationEndpoint)
            .revocationEndpoint(revocationEndpoint)
            .introspectionEndpoint(introspectionEndpoint)
            .jwksUri(jwksUri)
            .tokenEndpointAuthMethodsSupported(TOKEN_ENDPOINT_AUTH_METHODS)
            .tokenEndpointAuthSigningAlgValuesSupported(TOKEN_ENDPOINT_AUTH_SIGNING_ALGS)
            .responseTypesSupported(RESPONSE_TYPES)
            .grantTypesSupported(GRANT_TYPES)
            .scopesSupported(SCOPES)
            .serviceDocumentation(serviceDocumentation)
            .uiLocalesSupported(UI_LOCALES)
            .build();
    }
} 