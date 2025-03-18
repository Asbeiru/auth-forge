package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.dto.ClientRegistrationRequest;
import com.zhouzhou.authforge.dto.ClientRegistrationResponse;
import com.zhouzhou.authforge.service.ClientRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth 2.0 客户端注册端点控制器
 * 遵循 RFC 6749 和 OIDC 规范
 */
@RestController
@RequestMapping("/connect/register")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(
    origins = "*",
    allowedHeaders = "*",
    methods = {RequestMethod.POST, RequestMethod.OPTIONS}
)
public class ClientRegistrationController {

    private final ClientRegistrationService registrationService;

    /**
     * 处理客户端注册请求
     * 端点: POST /connect/register
     * 
     * @param request 客户端注册请求
     * @param authorization 可选的初始访问令牌
     * @return 客户端注册响应
     */
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClientRegistrationResponse> registerClient(
            @Valid @RequestBody ClientRegistrationRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        log.debug("Received client registration request: {}", request);
        
        String initialAccessToken = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            initialAccessToken = authorization.substring(7);
            log.debug("Initial access token provided");
        }

        try {
            // 1. 验证重定向URI
            if (!registrationService.validateRedirectUris(request.getRedirectUris())) {
                log.warn("Invalid redirect URIs in registration request");
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .body(ClientRegistrationResponse.builder()
                        .error("invalid_redirect_uri")
                        .errorDescription("One or more redirect_uri values are invalid")
                        .build());
            }

            // 2. 注册客户端
            ClientRegistrationResponse response = registrationService.registerClient(request, initialAccessToken);
            log.info("Client registration successful. Client ID: {}", response.getClientId());
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(response);

        } catch (IllegalArgumentException e) {
            // 处理客户端元数据验证错误
            log.warn("Invalid client metadata: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ClientRegistrationResponse.builder()
                    .error("invalid_client_metadata")
                    .errorDescription(e.getMessage())
                    .build());
        } catch (Exception e) {
            // 处理其他错误
            log.error("Error processing client registration request", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ClientRegistrationResponse.builder()
                    .error("invalid_request")
                    .errorDescription("An error occurred while processing the registration request")
                    .build());
        }
    }
} 