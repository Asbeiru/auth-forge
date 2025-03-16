-- 插入测试用户 (密码: password)
INSERT INTO users (username, password, authorities, enabled) VALUES
('user', 'password', 'ROLE_USER', true)
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试客户端
INSERT INTO oauth_clients (
    client_id,
    client_secret,
    client_name,
    client_type,
    client_authentication_methods,
    redirect_uris,
    scopes,
    authorized_grant_types,
    access_token_validity_seconds,
    refresh_token_validity_seconds,
    auto_approve,
    enabled
) VALUES (
    'client',
    'password',
    'Test Client',
    'CONFIDENTIAL',
    'client_secret_basic',
    'http://127.0.0.1:8080/callback',
    'read,write',
    'authorization_code,refresh_token',
    3600,   -- 1小时
    86400,  -- 24小时
    false,
    true
) ON DUPLICATE KEY UPDATE client_id=client_id; 