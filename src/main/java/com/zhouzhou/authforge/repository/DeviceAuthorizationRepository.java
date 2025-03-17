package com.zhouzhou.authforge.repository;

import com.zhouzhou.authforge.model.DeviceAuthorizationEntity;
import com.zhouzhou.authforge.model.DeviceAuthorizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 设备授权数据访问接口。
 *
 * @author zhouzhou
 * @since 1.0.0
 */
@Repository
public interface DeviceAuthorizationRepository extends JpaRepository<DeviceAuthorizationEntity, Long> {
    
    /**
     * 根据设备验证码查找设备授权记录。
     *
     * @param deviceCode 设备验证码
     * @return 设备授权记录
     */
    Optional<DeviceAuthorizationEntity> findByDeviceCode(String deviceCode);

    /**
     * 根据用户验证码查找设备授权记录。
     *
     * @param userCode 用户验证码
     * @return 设备授权记录
     */
    Optional<DeviceAuthorizationEntity> findByUserCode(String userCode);

    /**
     * 查找已过期的设备授权记录。
     *
     * @param now 当前时间
     * @return 已过期的设备授权记录列表
     */
    @Query("SELECT d FROM DeviceAuthorizationEntity d WHERE d.expiresAt < :now AND d.status = 'PENDING'")
    List<DeviceAuthorizationEntity> findExpiredAuthorizations(@Param("now") Instant now);

    /**
     * 根据客户端ID和状态查找设备授权记录。
     *
     * @param clientId 客户端ID
     * @param status 状态
     * @return 设备授权记录列表
     */
    List<DeviceAuthorizationEntity> findByClientIdAndStatus(String clientId, DeviceAuthorizationStatus status);

    /**
     * 检查设备验证码是否存在。
     *
     * @param deviceCode 设备验证码
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByDeviceCode(String deviceCode);

    /**
     * 检查用户验证码是否存在。
     *
     * @param userCode 用户验证码
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByUserCode(String userCode);
} 