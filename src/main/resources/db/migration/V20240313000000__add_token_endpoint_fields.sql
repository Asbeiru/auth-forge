-- Add fields to oauth_authorizations table
ALTER TABLE oauth_authorizations
ADD COLUMN access_token VARCHAR(256) DEFAULT NULL,
ADD COLUMN access_token_expires_at TIMESTAMP DEFAULT NULL,
ADD COLUMN refresh_token VARCHAR(256) DEFAULT NULL,
ADD COLUMN refresh_token_expires_at TIMESTAMP DEFAULT NULL,
ADD COLUMN token_type VARCHAR(50) DEFAULT 'Bearer',
ADD COLUMN id_token VARCHAR(1024) DEFAULT NULL,
ADD COLUMN id_token_expires_at TIMESTAMP DEFAULT NULL,
ADD INDEX idx_access_token (access_token),
ADD INDEX idx_refresh_token (refresh_token);

-- Add fields to oauth_clients table
ALTER TABLE oauth_clients
ADD COLUMN token_endpoint_auth_method VARCHAR(50) DEFAULT 'client_secret_basic',
ADD COLUMN id_token_signed_alg VARCHAR(50) DEFAULT 'RS256',
ADD COLUMN id_token_encrypted_alg VARCHAR(50) DEFAULT NULL,
ADD COLUMN id_token_encrypted_enc VARCHAR(50) DEFAULT NULL; 