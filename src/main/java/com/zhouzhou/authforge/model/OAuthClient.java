package com.zhouzhou.authforge.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "oauth_clients")
public class OAuthClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, unique = true, length = 100)
    private String clientId;

    @Column(name = "client_secret", nullable = false, length = 200)
    private String clientSecret;

    @Column(name = "client_name", length = 200)
    private String clientName;

    @Column(name = "redirect_uris", nullable = false, columnDefinition = "TEXT")
    private String redirectUris;

    @Column(name = "scopes", columnDefinition = "TEXT")
    private String scopes;

    @Column(name = "authorized_grant_types", nullable = false)
    private String authorizedGrantTypes;

    @Column(name = "access_token_validity_seconds")
    private Integer accessTokenValiditySeconds = 3600;

    @Column(name = "refresh_token_validity_seconds")
    private Integer refreshTokenValiditySeconds = 86400;

    @Column(name = "auto_approve")
    private Boolean autoApprove = false;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 