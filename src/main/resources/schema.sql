-- OAuth2 Client Registration
CREATE TABLE IF NOT EXISTS oauth2_client_registration (
    id VARCHAR(100) PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL UNIQUE,
    client_id_issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_secret VARCHAR(200),
    client_secret_expires_at TIMESTAMP,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000),
    scopes VARCHAR(1000),
    client_settings VARCHAR(2000),
    token_settings VARCHAR(2000)
);

-- OAuth2 Authorization
CREATE TABLE IF NOT EXISTS oauth2_authorization (
    id VARCHAR(100) PRIMARY KEY,
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorization_grant_type VARCHAR(100) NOT NULL,
    authorized_scopes VARCHAR(1000),
    attributes VARCHAR(4000),
    state VARCHAR(500),
    authorization_code_value VARCHAR(4000),
    authorization_code_issued_at TIMESTAMP,
    authorization_code_expires_at TIMESTAMP,
    authorization_code_metadata VARCHAR(2000),
    access_token_value VARCHAR(4000),
    access_token_issued_at TIMESTAMP,
    access_token_expires_at TIMESTAMP,
    access_token_metadata VARCHAR(2000),
    access_token_type VARCHAR(100),
    access_token_scopes VARCHAR(1000),
    refresh_token_value VARCHAR(4000),
    refresh_token_issued_at TIMESTAMP,
    refresh_token_expires_at TIMESTAMP,
    refresh_token_metadata VARCHAR(2000),
    oidc_id_token_value VARCHAR(4000),
    oidc_id_token_issued_at TIMESTAMP,
    oidc_id_token_expires_at TIMESTAMP,
    oidc_id_token_metadata VARCHAR(2000),
    oidc_id_token_claims VARCHAR(2000),
    FOREIGN KEY (registered_client_id) REFERENCES oauth2_client_registration(id)
);

-- OAuth2 Authorization Consent
CREATE TABLE IF NOT EXISTS oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorities VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name),
    FOREIGN KEY (registered_client_id) REFERENCES oauth2_client_registration(id)
);

-- OAuth Clients table
CREATE TABLE IF NOT EXISTS oauth_clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    client_secret VARCHAR(200) NOT NULL,
    client_name VARCHAR(200),
    redirect_uris TEXT NOT NULL,
    scopes TEXT,
    authorized_grant_types VARCHAR(255) NOT NULL,
    access_token_validity_seconds INT DEFAULT 3600,
    refresh_token_validity_seconds INT DEFAULT 86400,
    auto_approve BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_client_id (client_id)
);

-- OAuth Authorizations table
CREATE TABLE IF NOT EXISTS oauth_authorizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    scopes TEXT,
    authorization_code VARCHAR(256),
    code_challenge VARCHAR(256),
    code_challenge_method VARCHAR(32),
    state VARCHAR(256),
    authorization_code_expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES oauth_clients(client_id),
    INDEX idx_auth_code (authorization_code),
    INDEX idx_client_user (client_id, user_id)
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
    user_id VARCHAR(255) NOT NULL,
    scopes TEXT,
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