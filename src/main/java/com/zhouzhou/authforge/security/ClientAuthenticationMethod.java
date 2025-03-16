package com.zhouzhou.authforge.security;

/**
 * 客户端认证方法
 */
public enum ClientAuthenticationMethod {

    /**
     * 无认证（公共客户端）
     */
    NONE("none"),

    /**
     * 客户端密钥基本认证
     */
    CLIENT_SECRET_BASIC("client_secret_basic"),

    /**
     * 客户端密钥POST认证
     */
    CLIENT_SECRET_POST("client_secret_post"),

    /**
     * 私钥JWT认证
     */
    PRIVATE_KEY_JWT("private_key_jwt"),

    /**
     * 客户端密钥JWT认证
     */
    CLIENT_SECRET_JWT("client_secret_jwt");

    private final String value;

    ClientAuthenticationMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ClientAuthenticationMethod fromValue(String value) {
        for (ClientAuthenticationMethod method : values()) {
            if (method.value.equals(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown client authentication method: " + value);
    }
} 