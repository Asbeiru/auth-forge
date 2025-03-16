-- 插入测试用户 (密码: password)
INSERT INTO users (username, password, authorities, enabled) VALUES
('user', 'password', 'ROLE_USER', true)
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试客户端
DELETE FROM oauth_clients WHERE client_id = 'client';

INSERT INTO oauth_clients (
    client_id,
    client_secret,
    client_name,
    description,
    client_type,
    client_authentication_methods,
    redirect_uris,
    scopes,
    authorized_grant_types,
    require_proof_key,
    require_auth_consent,
    reuse_refresh_tokens,
    access_token_validity_seconds,
    refresh_token_validity_seconds,
    auto_approve,
    enabled
) VALUES (
    'client',
    'password',
    'Test Client',
    'OAuth 2.0 测试客户端',
    'CONFIDENTIAL',
    'client_secret_basic',
    'http://127.0.0.1:8080/callback',
    'read profile',
    'authorization_code refresh_token',
    false,
    true,
    true,
    3600,
    86400,
    false,
    true
); 