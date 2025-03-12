package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.dto.AuthorizationResult;
import com.zhouzhou.authforge.model.OAuthAuthorization;
import org.springframework.security.core.Authentication;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * OAuth 2.0 授权服务接口
 * 
 * 定义 OAuth 2.0 授权码授权类型的核心业务逻辑，实现 RFC 6749 Section 4.1
 * 
 * 主要功能：
 * 1. 授权请求处理：
 *    - 验证客户端身份
 *    - 验证重定向URI
 *    - 验证授权范围
 *    - 支持 PKCE 扩展（RFC 7636）
 * 
 * 2. 授权确认处理：
 *    - 记录用户授权决定
 *    - 生成授权码
 *    - 关联授权信息
 * 
 * 3. 授权码规范：
 *    - 必须是随机字符串
 *    - 有效期短（通常10分钟）
 *    - 一次性使用
 *    - 绑定到特定客户端
 * 
 * @see RFC 6749 https://tools.ietf.org/html/rfc6749#section-4.1
 * @see RFC 7636 https://tools.ietf.org/html/rfc7636
 */
public interface OAuth2AuthorizationService {
    
    /**
     * 处理授权请求
     * @param responseType 响应类型
     * @param clientId 客户端ID
     * @param redirectUri 重定向URI
     * @param scope 请求的权限范围
     * @param state 状态参数
     * @param additionalParameters 额外参数
     * @param authentication 用户认证信息
     * @return 授权结果
     */
    AuthorizationResult handleAuthorizationRequest(
            String responseType,
            String clientId,
            String redirectUri,
            String scope,
            String state,
            Map<String, Object> additionalParameters,
            Authentication authentication);



    /**
     * 检查是否需要用户同意
     */
    boolean isConsentRequired(String clientId, String userId, Set<String> requestedScopes);

    /**
     * 根据授权码查找授权信息
     */
    Optional<OAuthAuthorization> findByCode(String code);

    /**
     * 验证授权码
     */
    void validateAuthorizationCode(
            String code,
            String clientId,
            String redirectUri,
            String codeVerifier);
} 