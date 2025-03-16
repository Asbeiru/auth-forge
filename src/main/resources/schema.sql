-- 创建数据库
CREATE DATABASE IF NOT EXISTS auth_forge;
USE auth_forge;

-- OAuth Clients table
CREATE TABLE IF NOT EXISTS oauth_clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL UNIQUE,
    client_secret VARCHAR(200) NOT NULL,
    client_name VARCHAR(100) NOT NULL,
    client_type VARCHAR(20) NOT NULL DEFAULT 'CONFIDENTIAL',
    client_authentication_methods VARCHAR(200) NOT NULL DEFAULT 'client_secret_basic',
    redirect_uris TEXT NOT NULL,
    scopes TEXT NOT NULL,
    authorized_grant_types VARCHAR(200) NOT NULL,
    access_token_validity_seconds INT NOT NULL DEFAULT 3600,  -- 默认1小时
    refresh_token_validity_seconds INT NOT NULL DEFAULT 86400, -- 默认24小时
    auto_approve BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- OAuth Authorizations table
CREATE TABLE IF NOT EXISTS oauth_authorizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    scopes TEXT,
    authorization_code VARCHAR(256),
    authorization_code_expires_at TIMESTAMP,
    code_challenge VARCHAR(256),
    code_challenge_method VARCHAR(20),
    state VARCHAR(256),
    redirect_uri VARCHAR(1024),
    trace_id VARCHAR(256),
    response_type VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES oauth_clients(client_id),
    INDEX idx_auth_code (authorization_code),
    INDEX idx_client_user (client_id, user_id),
    INDEX idx_state (state),
    INDEX idx_trace_id (trace_id)
);

-- OAuth Tokens table
CREATE TABLE IF NOT EXISTS oauth_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    access_token VARCHAR(256) NOT NULL,
    refresh_token VARCHAR(256),
    token_type VARCHAR(50) DEFAULT 'Bearer',
    client_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    scopes TEXT,
    expires_at TIMESTAMP NOT NULL,
    refresh_token_expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES oauth_clients(client_id),
    UNIQUE KEY uk_access_token (access_token),
    UNIQUE KEY uk_refresh_token (refresh_token),
    INDEX idx_client_user_token (client_id, user_id)
);

-- OAuth Consents table
CREATE TABLE IF NOT EXISTS oauth_consents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    scopes TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES oauth_clients(client_id),
    UNIQUE KEY uk_client_user (client_id, user_id)
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    authorities VARCHAR(200) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
); 