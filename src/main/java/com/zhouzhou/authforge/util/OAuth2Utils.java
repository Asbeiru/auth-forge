package com.zhouzhou.authforge.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * OAuth 2.0 工具类
 */
public class OAuth2Utils {

    private OAuth2Utils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 解析重定向URI字符串为Set
     * @param redirectUris 空格分隔的重定向URI字符串
     * @return 重定向URI集合
     */
    public static Set<String> parseRedirectUris(String redirectUris) {
        if (redirectUris == null || redirectUris.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(redirectUris.split("\\s+")));
    }

    /**
     * 解析作用域字符串为Set
     * @param scopes 空格分隔的作用域字符串
     * @return 作用域集合
     */
    public static Set<String> parseScopes(String scopes) {
        if (scopes == null || scopes.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(scopes.split("\\s+")));
    }

    /**
     * 验证重定向URI是否合法
     * @param allowedRedirectUris 允许的重定向URI集合
     * @param redirectUri 待验证的重定向URI
     * @return 是否合法
     */
    public static boolean isValidRedirectUri(Set<String> allowedRedirectUris, String redirectUri) {
        return allowedRedirectUris.contains(redirectUri);
    }

    /**
     * 验证作用域是否合法
     * @param allowedScopes 允许的作用域集合
     * @param requestedScopes 请求的作用域集合
     * @return 是否合法
     */
    public static boolean isValidScopes(Set<String> allowedScopes, Set<String> requestedScopes) {
        return allowedScopes.containsAll(requestedScopes);
    }
} 