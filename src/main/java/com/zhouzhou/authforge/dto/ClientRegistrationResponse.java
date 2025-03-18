package com.zhouzhou.authforge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * OAuth 2.0 客户端注册响应DTO
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientRegistrationResponse {
    
    /**
     * 客户端标识符
     */
    @JsonProperty("client_id")
    private String clientId;
    
    /**
     * 客户端密钥
     */
    @JsonProperty("client_secret")
    private String clientSecret;
    
    /**
     * 客户端密钥过期时间
     */
    @JsonProperty("client_secret_expires_at")
    private Long clientSecretExpiresAt;
    
    /**
     * 注册访问令牌
     */
    @JsonProperty("registration_access_token")
    private String registrationAccessToken;
    
    /**
     * 注册客户端URI
     */
    @JsonProperty("registration_client_uri")
    private String registrationClientUri;
    
    /**
     * 客户端标识符发布时间
     */
    @JsonProperty("client_id_issued_at")
    private Long clientIdIssuedAt;
    
    // 以下字段继承自请求
    
    @JsonProperty("application_type")
    private String applicationType;
    
    @JsonProperty("redirect_uris")
    private List<String> redirectUris;
    
    @JsonProperty("client_name")
    private String clientName;
    
    private Map<String, String> clientNameLocalizations;
    
    @JsonProperty("logo_uri")
    private String logoUri;
    
    @JsonProperty("subject_type")
    private String subjectType;
    
    @JsonProperty("sector_identifier_uri")
    private String sectorIdentifierUri;
    
    @JsonProperty("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;
    
    @JsonProperty("jwks_uri")
    private String jwksUri;
    
    @JsonProperty("userinfo_encrypted_response_alg")
    private String userinfoEncryptedResponseAlg;
    
    @JsonProperty("userinfo_encrypted_response_enc")
    private String userinfoEncryptedResponseEnc;
    
    private List<String> contacts;
    
    @JsonProperty("request_uris")
    private List<String> requestUris;
    
    // 错误响应字段
    
    private String error;
    
    @JsonProperty("error_description")
    private String errorDescription;
} 