package com.zhouzhou.authforge.service.impl;

import com.zhouzhou.authforge.model.OAuthClient;
import com.zhouzhou.authforge.repository.OAuthClientRepository;
import com.zhouzhou.authforge.service.OAuth2ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OAuth2ClientServiceImpl implements OAuth2ClientService {

    private final OAuthClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<OAuthClient> findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId);
    }

    @Override
    public boolean validateClientSecret(String clientId, String clientSecret) {
        return findByClientId(clientId)
            .map(client -> passwordEncoder.matches(clientSecret, client.getClientSecret()))
            .orElse(false);
    }

    @Override
    public boolean validateRedirectUri(String clientId, String redirectUri) {
        return findByClientId(clientId)
            .map(client -> {
                String[] allowedRedirectUris = client.getRedirectUris().split(",");
                for (String allowedUri : allowedRedirectUris) {
                    if (allowedUri.trim().equals(redirectUri)) {
                        return true;
                    }
                }
                return false;
            })
            .orElse(false);
    }
} 