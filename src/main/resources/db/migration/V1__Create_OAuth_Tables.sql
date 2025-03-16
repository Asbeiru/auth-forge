-- 客户端表
CREATE TABLE oauth_clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    client_secret VARCHAR(200) NOT NULL,
    client_name VARCHAR(200),
    description TEXT,
    client_type VARCHAR(20) NOT NULL,
    token_endpoint_auth_method VARCHAR(50) NOT NULL,
    require_proof_key BOOLEAN NOT NULL DEFAULT FALSE,
    default_code_challenge_method VARCHAR(20),
    jwks_uri VARCHAR(1000),
    token_endpoint VARCHAR(1000),
    redirect_uris TEXT NOT NULL,
    scopes TEXT,
    authorized_grant_types VARCHAR(255) NOT NULL,
    access_token_validity_seconds INT NOT NULL DEFAULT 3600,
    refresh_token_validity_seconds INT NOT NULL DEFAULT 86400,
    auto_approve BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    client_authentication_methods VARCHAR(255) NOT NULL,
    default_authentication_method VARCHAR(50) NOT NULL,
    public_key TEXT,
    reuse_refresh_tokens BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_client_id (client_id)
);

-- 授权表
CREATE TABLE oauth_authorizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    authorization_code VARCHAR(255),
    authorization_code_expires_at TIMESTAMP,
    code_challenge VARCHAR(255),
    code_challenge_method VARCHAR(20),
    scopes TEXT,
    redirect_uri VARCHAR(1000) NOT NULL,
    state VARCHAR(255),
    trace_id VARCHAR(255),
    response_type VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_authorization_code (authorization_code),
    UNIQUE KEY uk_trace_id (trace_id),
    KEY idx_client_user (client_id, user_id),
    CONSTRAINT fk_auth_client FOREIGN KEY (client_id) REFERENCES oauth_clients (client_id)
);

-- 访问令牌表
CREATE TABLE oauth_access_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100),
    access_token VARCHAR(512) NOT NULL,
    refresh_token VARCHAR(512),
    scope TEXT,
    access_token_expires_at TIMESTAMP NOT NULL,
    refresh_token_expires_at TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_access_token (access_token),
    UNIQUE KEY uk_refresh_token (refresh_token),
    KEY idx_client_user (client_id, user_id),
    CONSTRAINT fk_token_client FOREIGN KEY (client_id) REFERENCES oauth_clients (client_id)
); 