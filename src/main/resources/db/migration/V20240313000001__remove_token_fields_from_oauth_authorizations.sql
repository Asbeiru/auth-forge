-- Remove token-related fields from oauth_authorizations table
ALTER TABLE oauth_authorizations
DROP COLUMN access_token,
DROP COLUMN access_token_expires_at,
DROP COLUMN refresh_token,
DROP COLUMN refresh_token_expires_at,
DROP COLUMN token_type,
DROP COLUMN id_token,
DROP COLUMN id_token_expires_at,
DROP INDEX idx_access_token,
DROP INDEX idx_refresh_token; 