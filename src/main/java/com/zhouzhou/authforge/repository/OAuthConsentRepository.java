package com.zhouzhou.authforge.repository;

import com.zhouzhou.authforge.model.OAuthConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthConsentRepository extends JpaRepository<OAuthConsent, Long> {
    Optional<OAuthConsent> findByClientIdAndUserId(String clientId, String userId);
} 