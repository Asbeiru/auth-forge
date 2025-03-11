package com.zhouzhou.authforge.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "oauth_authorizations")
public class OAuthAuthorization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "scopes", columnDefinition = "TEXT")
    private String scopes;

    @Column(name = "authorization_code", length = 256)
    private String authorizationCode;

    @Column(name = "code_challenge", length = 256)
    private String codeChallenge;

    @Column(name = "code_challenge_method", length = 32)
    private String codeChallengeMethod;

    @Column(name = "state", length = 256)
    private String state;

    @Column(name = "authorization_code_expires_at")
    private LocalDateTime authorizationCodeExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "client_id", insertable = false, updatable = false)
    private OAuthClient oauthClient;
} 