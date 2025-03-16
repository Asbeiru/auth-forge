-- 添加 PKCE 相关字段
ALTER TABLE oauth_clients
    ADD COLUMN require_proof_key BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN default_code_challenge_method VARCHAR(10);

-- 更新公共客户端的 PKCE 配置
UPDATE oauth_clients
SET require_proof_key = TRUE,
    default_code_challenge_method = 'S256'
WHERE token_endpoint_auth_method = 'none'; 