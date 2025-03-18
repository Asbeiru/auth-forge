package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.dto.AuthorizationServerMetadata;

/**
 * OAuth 2.0 授权服务器元数据服务接口
 */
public interface AuthorizationServerMetadataService {
    
    /**
     * 获取授权服务器元数据
     *
     * @return 授权服务器元数据
     */
    AuthorizationServerMetadata getMetadata();
} 