package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.model.OAuthAuthorization;
import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.model.OAuthConsent;
import com.zhouzhou.authforge.repository.OAuthAuthorizationRepository;
import com.zhouzhou.authforge.repository.OAuthConsentRepository;
import com.zhouzhou.authforge.service.OAuthAuthorizationService;
import com.zhouzhou.authforge.util.OAuth2Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class OAuthAuthorizationServiceImpl implements OAuthAuthorizationService {

    private final OAuthAuthorizationRepository authorizationRepository;
    private final OAuthConsentRepository consentRepository;
    private final StringKeyGenerator codeGenerator = new Base64StringKeyGenerator(32);

    @Override
    public boolean isAuthorizationConsentRequired(OAuthClient client, String userId, Set<String> scopes) {
        // 如果客户端配置了自动授权，则不需要用户确认
        if (Boolean.TRUE.equals(client.getAutoApprove())) {
            return false;
        }

        // 检查用户是否已经授权过这些scope
        Optional<OAuthConsent> existingConsent = consentRepository.findByClientIdAndUserId(
            client.getClientId(), userId);
        
        if (existingConsent.isEmpty()) {
            return true;
        }

        Set<String> approvedScopes = OAuth2Utils.parseScopes(existingConsent.get().getScopes());
        return !approvedScopes.containsAll(scopes);
    }

    @Override
    public String generateAuthorizationCode(OAuthClient client, String userId, Set<String> scopes) {
        String code = codeGenerator.generateKey();
        
        OAuthAuthorization authorization = new OAuthAuthorization();
        authorization.setClientId(client.getClientId());
        authorization.setUserId(userId);
        authorization.setScopes(String.join(" ", scopes));
        authorization.setAuthorizationCode(code);
        authorization.setAuthorizationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        
        authorizationRepository.save(authorization);
        
        return code;
    }

    @Override
    public void saveAuthorizationConsent(String clientId, String userId, Set<String> scopes) {
        OAuthConsent consent = consentRepository.findByClientIdAndUserId(clientId, userId)
            .orElse(new OAuthConsent());
        
        consent.setClientId(clientId);
        consent.setUserId(userId);
        consent.setScopes(String.join(" ", scopes));
        
        consentRepository.save(consent);
    }

    @Override
    public OAuthAuthorization validateAuthorizationCode(String code, String clientId) {
        OAuthAuthorization authorization = authorizationRepository.findByAuthorizationCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Invalid authorization code"));

        if (!authorization.getClientId().equals(clientId)) {
            throw new IllegalArgumentException("Authorization code was not issued to this client");
        }

        if (authorization.getAuthorizationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Authorization code has expired");
        }

        return authorization;
    }
} 