CREATE TABLE oauth_access_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    access_token VARCHAR(255) NOT NULL UNIQUE,
    refresh_token VARCHAR(255) UNIQUE,
    client_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    scope VARCHAR(1000),
    access_token_expires_at TIMESTAMP NOT NULL,
    refresh_token_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_access_token (access_token),
    INDEX idx_refresh_token (refresh_token),
    INDEX idx_client_user (client_id, user_id)
); 