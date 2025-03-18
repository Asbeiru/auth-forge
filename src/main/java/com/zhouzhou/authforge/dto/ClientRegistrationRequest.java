package com.zhouzhou.authforge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * OAuth 2.0 客户端注册请求DTO
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientRegistrationRequest {
    
    /**
     * 客户端应用类型
     */
    @JsonProperty("application_type")
    private String applicationType;
    
    /**
     * 重定向URI列表
     */
    @NotEmpty(message = "At least one redirect URI must be provided")
    @JsonProperty("redirect_uris")
    private List<String> redirectUris;
    
    /**
     * 客户端名称
     */
    @JsonProperty("client_name")
    private String clientName;
    
    /**
     * 客户端名称的本地化版本
     * 键格式为 "client_name#语言标签"
     */
    private Map<String, String> clientNameLocalizations;
    
    /**
     * 客户端logo URI
     */
    @JsonProperty("logo_uri")
    private String logoUri;
    
    /**
     * subject标识符类型
     */
    @JsonProperty("subject_type")
    private String subjectType;
    
    /**
     * sector标识符URI
     */
    @JsonProperty("sector_identifier_uri")
    private String sectorIdentifierUri;
    
    /**
     * 令牌端点认证方法
     */
    @JsonProperty("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;
    
    /**
     * JWK Set文档的URL
     */
    @JsonProperty("jwks_uri")
    private String jwksUri;
    
    /**
     * 用户信息加密响应算法
     */
    @JsonProperty("userinfo_encrypted_response_alg")
    private String userinfoEncryptedResponseAlg;
    
    /**
     * 用户信息加密响应编码
     */
    @JsonProperty("userinfo_encrypted_response_enc")
    private String userinfoEncryptedResponseEnc;
    
    /**
     * 联系人邮箱列表
     */
    private List<String> contacts;
    
    /**
     * 请求URI列表
     */
    @JsonProperty("request_uris")
    private List<String> requestUris;
} 