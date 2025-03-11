package com.zhouzhou.authforge.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "oauth_tokens")
public class OAuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "access_token", nullable = false, unique = true, length = 256)
    private String accessToken;

    @Column(name = "refresh_token", unique = true, length = 256)
    private String refreshToken;

    @Column(name = "token_type", length = 50)
    private String tokenType = "Bearer";

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "scopes", columnDefinition = "TEXT")
    private String scopes;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

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