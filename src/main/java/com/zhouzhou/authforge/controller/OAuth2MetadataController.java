package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.dto.AuthorizationServerMetadata;
import com.zhouzhou.authforge.service.AuthorizationServerMetadataService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth 2.0 授权服务器元数据端点控制器
 * 遵循 RFC 8414 规范
 * 
 * @see <a href="https://tools.ietf.org/html/rfc8414">RFC 8414</a>
 */
@RestController
public class OAuth2MetadataController {

    private final AuthorizationServerMetadataService metadataService;
    
    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    public OAuth2MetadataController(AuthorizationServerMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * 获取授权服务器元数据 - 默认端点
     * 端点: /.well-known/oauth-authorization-server
     * 端点: /.well-known/oauth-authorization-server/{issuerPath}
     *
     * @param issuerPath 可选的issuer路径
     * @param request HTTP请求
     * @return 授权服务器元数据
     */
    @GetMapping(
        path = {
            "/.well-known/oauth-authorization-server",
            "/.well-known/oauth-authorization-server/{issuerPath}/**"
        },
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthorizationServerMetadata> getMetadata(
            @PathVariable(required = false) String issuerPath,
            HttpServletRequest request) {
        
        // 检查HTTPS要求
        if (!sslEnabled && !request.getScheme().equals("https")) {
            return ResponseEntity.badRequest()
                .body(AuthorizationServerMetadata.builder()
                    .error("invalid_request")
                    .errorDescription("Metadata endpoint must be accessed via HTTPS")
                    .build());
        }

        return ResponseEntity.ok(metadataService.getMetadata());
    }

    /**
     * OpenID Connect 兼容端点
     * 端点: /.well-known/openid-configuration
     * 端点: /.well-known/openid-configuration/{issuerPath}
     *
     * @param issuerPath 可选的issuer路径
     * @param request HTTP请求
     * @return 授权服务器元数据
     */
    @GetMapping(
        path = {
            "/.well-known/openid-configuration",
            "/.well-known/openid-configuration/{issuerPath}/**"
        },
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthorizationServerMetadata> getOpenIdConfiguration(
            @PathVariable(required = false) String issuerPath,
            HttpServletRequest request) {
        
        // 检查HTTPS要求
        if (!sslEnabled && !request.getScheme().equals("https")) {
            return ResponseEntity.badRequest()
                .body(AuthorizationServerMetadata.builder()
                    .error("invalid_request")
                    .errorDescription("Metadata endpoint must be accessed via HTTPS")
                    .build());
        }

        return ResponseEntity.ok(metadataService.getMetadata());
    }
} 