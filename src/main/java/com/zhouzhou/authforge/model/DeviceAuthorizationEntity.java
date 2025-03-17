package com.zhouzhou.authforge.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * 设备授权实体类，用于存储设备授权流程中的验证码信息。
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "device_authorizations")
public class DeviceAuthorizationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_code", nullable = false, unique = true)
    private String deviceCode;

    @Column(name = "user_code", nullable = false, unique = true)
    private String userCode;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "scope", columnDefinition = "TEXT")
    private String scope;

    @Column(name = "verification_uri", nullable = false)
    private String verificationUri;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceAuthorizationStatus status = DeviceAuthorizationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_polled_at")
    private Instant lastPolledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 检查设备授权是否已过期。
     *
     * @return 如果已过期返回 true，否则返回 false
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * 检查设备授权是否处于待处理状态。
     *
     * @return 如果是待处理状态返回 true，否则返回 false
     */
    public boolean isPending() {
        return status == DeviceAuthorizationStatus.PENDING;
    }

    /**
     * 检查是否可以进行轮询。
     * 根据上次轮询时间和轮询间隔判断是否允许新的轮询请求。
     *
     * @param interval 轮询间隔（秒）
     * @return 如果可以轮询返回 true，否则返回 false
     */
    public boolean canPoll(int interval) {
        if (lastPolledAt == null) {
            return true;
        }
        return Instant.now().isAfter(lastPolledAt.plusSeconds(interval));
    }

    /**
     * 更新最后轮询时间。
     */
    public void updateLastPolledAt() {
        this.lastPolledAt = Instant.now();
    }
} 