package com.zhouzhou.authforge.repository;

import com.zhouzhou.authforge.model.OAuthAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * OAuth 2.0 授权记录仓库
 */
@Repository
public interface OAuthAuthorizationRepository extends JpaRepository<OAuthAuthorization, Long> {


    /**
     * Find authorization by authorization code
     *
     * @param authorizationCode the authorization code
     * @return Optional containing the authorization if found
     */
    Optional<OAuthAuthorization> findByAuthorizationCode(String authorizationCode);


    // 添加根据 state 查询的方法
    Optional<OAuthAuthorization> findByState(String state);

    Optional<OAuthAuthorization> findByTraceId(String traceId);


} 