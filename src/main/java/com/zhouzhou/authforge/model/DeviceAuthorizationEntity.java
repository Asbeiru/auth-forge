package com.zhouzhou.authforge.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * OAuth 2.0 设备授权实体类，遵循
 * <a href="https://tools.ietf.org/html/rfc8628" target="_blank">RFC 8628</a> 规范。
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@Entity
@Table(name = "device_authorizations")
@Getter
@Setter
public class DeviceAuthorizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 设备验证码
     */
    @Column(name = "device_code", nullable = false, unique = true)
    private String deviceCode;

    /**
     * 用户验证码
     */
    @Column(name = "user_code", nullable = false, unique = true)
    private String userCode;

    /**
     * 客户端ID
     */
    @Column(name = "client_id", nullable = false)
    private String clientId;

    /**
     * 授权范围
     */
    @Column(name = "scope")
    private String scope;

    /**
     * 验证URI
     */
    @Column(name = "verification_uri", nullable = false)
    private String verificationUri;

    /**
     * 授权状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceAuthorizationStatus status;

    /**
     * 过期时间
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * 最后轮询时间
     */
    @Column(name = "last_polled_at")
    private Instant lastPolledAt;

    /**
     * 轮询间隔（秒）
     */
    @Column(name = "interval", nullable = false)
    private Integer interval;

    /**
     * 访问令牌
     */
    @Column(name = "access_token")
    private String accessToken;

    /**
     * 访问令牌过期时间
     */
    @Column(name = "access_token_expires_at")
    private Instant accessTokenExpiresAt;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 检查设备授权是否已过期
     *
     * @return 如果已过期返回 true，否则返回 false
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * 检查设备授权是否处于待处理状态
     *
     * @return 如果是待处理状态返回 true，否则返回 false
     */
    public boolean isPending() {
        return DeviceAuthorizationStatus.PENDING.equals(status);
    }

    /**
     * 检查设备授权是否已被拒绝
     *
     * @return 如果已被拒绝返回 true，否则返回 false
     */
    public boolean isDenied() {
        return DeviceAuthorizationStatus.DENIED.equals(status);
    }

    /**
     * 检查是否可以轮询
     *
     * @param requestedInterval 轮询间隔（秒）
     * @return 如果可以轮询返回 true，否则返回 false
     */
    public boolean canPoll(int requestedInterval) {
        if (lastPolledAt == null) {
            return true;
        }
        Instant now = Instant.now();
        return now.isAfter(lastPolledAt.plusSeconds(requestedInterval));
    }

    /**
     * 更新最后轮询时间
     */
    public void updateLastPolledAt() {
        this.lastPolledAt = Instant.now();
    }

    /**
     * 增加轮询间隔
     */
    public void increasePollingInterval() {
        this.interval += 5; // RFC 8628 要求增加 5 秒
    }

    /**
     * 检查设备授权是否已使用
     */
    public boolean isUsed() {
        return status == DeviceAuthorizationStatus.COMPLETED || 
               status == DeviceAuthorizationStatus.DENIED;
    }
} 