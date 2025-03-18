package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.dto.ClientRegistrationRequest;
import com.zhouzhou.authforge.dto.ClientRegistrationResponse;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthClientRepository;
import com.zhouzhou.authforge.service.ClientRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.UUID;

/**
 * OAuth 2.0 客户端注册服务实现类
 */
@Service
@Slf4j
public class ClientRegistrationServiceImpl implements ClientRegistrationService {

    private final StringKeyGenerator clientSecretGenerator = new Base64StringKeyGenerator(48);
    private final StringKeyGenerator registrationTokenGenerator = new Base64StringKeyGenerator(32);
    private final OAuthClientRepository clientRepository;

    @Value("${auth.server.issuer}")
    private String issuer;

    @Value("${auth.server.require-initial-access-token:true}")
    private boolean requireInitialAccessToken;

    @Value("${auth.server.client-secret.expires-in:31536000}")
    private long clientSecretExpiresIn;  // 默认1年过期

    @Value("${auth.server.client-secret.never-expires:false}")
    private boolean clientSecretNeverExpires;  // 是否永不过期

    public ClientRegistrationServiceImpl(OAuthClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional
    public ClientRegistrationResponse registerClient(ClientRegistrationRequest request, String initialAccessToken) {
        // 验证初始访问令牌
        validateInitialAccessToken(initialAccessToken);

        // 生成客户端标识符和密钥
        String clientId = generateClientId();
        String clientSecret = generateClientSecret();
        String registrationAccessToken = generateRegistrationAccessToken(clientId);

        // 设置过期时间
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = clientSecretNeverExpires ? 0 : issuedAt + clientSecretExpiresIn;

        log.debug("Setting client_secret_expires_at to {}", expiresAt == 0 ? "never" : Instant.ofEpochSecond(expiresAt));

        // 构建注册客户端URI
        String registrationClientUri = String.format("%s/connect/register?client_id=%s", issuer, clientId);

        // 创建并保存客户端
        OAuthClient client = new OAuthClient();
        client.setClientId(clientId);
        client.setClientSecret(clientSecret);
        client.setClientName(request.getClientName());
        client.setRedirectUris(String.join(" ", request.getRedirectUris()));
        client.setClientType(request.getApplicationType() != null && 
                           request.getApplicationType().equalsIgnoreCase("web") ? 
                           "CONFIDENTIAL" : "PUBLIC");
        client.setClientAuthenticationMethods(request.getTokenEndpointAuthMethod() != null ?
                                           request.getTokenEndpointAuthMethod() :
                                           "client_secret_basic");
        client.setInitialAccessToken(initialAccessToken);
        
        // 设置默认值
        client.setEnabled(true);
        client.setAutoApprove(false);
        client.setRequireAuthConsent(true);
        client.setRequireProofKey(false);
        client.setScopes("read write");
        client.setAuthorizedGrantTypes("authorization_code,refresh_token");
        client.setAccessTokenValiditySeconds(3600);
        client.setRefreshTokenValiditySeconds(86400);
        client.setDescription(request.getClientName());

        clientRepository.save(client);

        // 构建响应
        return ClientRegistrationResponse.builder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .clientSecretExpiresAt(expiresAt)  // 0 表示永不过期
            .registrationAccessToken(registrationAccessToken)
            .registrationClientUri(registrationClientUri)
            .clientIdIssuedAt(issuedAt)
            // 复制请求中的元数据
            .applicationType(request.getApplicationType())
            .redirectUris(request.getRedirectUris())
            .clientName(request.getClientName())
            .clientNameLocalizations(request.getClientNameLocalizations())
            .logoUri(request.getLogoUri())
            .subjectType(request.getSubjectType())
            .sectorIdentifierUri(request.getSectorIdentifierUri())
            .tokenEndpointAuthMethod(request.getTokenEndpointAuthMethod())
            .jwksUri(request.getJwksUri())
            .userinfoEncryptedResponseAlg(request.getUserinfoEncryptedResponseAlg())
            .userinfoEncryptedResponseEnc(request.getUserinfoEncryptedResponseEnc())
            .contacts(request.getContacts())
            .requestUris(request.getRequestUris())
            .build();
    }

    @Override
    public boolean validateRedirectUris(Iterable<String> redirectUris) {
        if (redirectUris == null) {
            return false;
        }

        for (String uri : redirectUris) {
            if (!isValidRedirectUri(uri)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String generateClientId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String generateClientSecret() {
        return clientSecretGenerator.generateKey();
    }

    @Override
    public String generateRegistrationAccessToken(String clientId) {
        return registrationTokenGenerator.generateKey();
    }

    private boolean isValidRedirectUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            return false;
        }

        try {
            URI redirectUri = new URI(uri);
            String scheme = redirectUri.getScheme();
            return "https".equalsIgnoreCase(scheme) || 
                   ("http".equalsIgnoreCase(scheme) && "localhost".equalsIgnoreCase(redirectUri.getHost()));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private void validateInitialAccessToken(String token) {
        if (requireInitialAccessToken) {
            if (token == null) {
                throw new IllegalArgumentException("Initial access token is required");
            }
            if (!isValidInitialAccessToken(token)) {
                throw new IllegalArgumentException("Invalid initial access token");
            }
        }
    }

    private boolean isValidInitialAccessToken(String token) {
        return clientRepository.existsByInitialAccessToken(token);
    }
} 