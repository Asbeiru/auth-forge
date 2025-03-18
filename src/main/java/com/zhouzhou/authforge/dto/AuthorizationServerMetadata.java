package com.zhouzhou.authforge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * OAuth 2.0 授权服务器元数据响应DTO
 * 遵循 RFC 8414 规范
 * 
 * @see <a href="https://tools.ietf.org/html/rfc8414">RFC 8414</a>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizationServerMetadata {
    
    /**
     * 授权服务器的发行者标识符URL
     */
    private String issuer;
    
    /**
     * 授权端点URL
     */
    @JsonProperty("authorization_endpoint")
    private String authorizationEndpoint;
    
    /**
     * 令牌端点URL
     */
    @JsonProperty("token_endpoint")
    private String tokenEndpoint;
    
    /**
     * 令牌端点支持的客户端认证方法
     */
    @JsonProperty("token_endpoint_auth_methods_supported")
    private List<String> tokenEndpointAuthMethodsSupported;
    
    /**
     * 令牌端点支持的签名算法
     */
    @JsonProperty("token_endpoint_auth_signing_alg_values_supported")
    private List<String> tokenEndpointAuthSigningAlgValuesSupported;
    
    /**
     * JWK Set文档的URL
     */
    @JsonProperty("jwks_uri")
    private String jwksUri;
    
    /**
     * 支持的响应类型
     */
    @JsonProperty("response_types_supported")
    private List<String> responseTypesSupported;
    
    /**
     * 支持的授权类型
     */
    @JsonProperty("grant_types_supported")
    private List<String> grantTypesSupported;
    
    /**
     * 支持的作用域
     */
    @JsonProperty("scopes_supported")
    private List<String> scopesSupported;
    
    /**
     * 设备授权端点URL
     */
    @JsonProperty("device_authorization_endpoint")
    private String deviceAuthorizationEndpoint;
    
    /**
     * 服务文档URL
     */
    @JsonProperty("service_documentation")
    private String serviceDocumentation;
    
    /**
     * 支持的UI区域设置
     */
    @JsonProperty("ui_locales_supported")
    private List<String> uiLocalesSupported;
    
    /**
     * 撤销端点URL
     */
    @JsonProperty("revocation_endpoint")
    private String revocationEndpoint;
    
    /**
     * 内省端点URL
     */
    @JsonProperty("introspection_endpoint")
    private String introspectionEndpoint;

    /**
     * 错误码
     */
    private String error;

    /**
     * 错误描述
     */
    @JsonProperty("error_description")
    private String errorDescription;
} 