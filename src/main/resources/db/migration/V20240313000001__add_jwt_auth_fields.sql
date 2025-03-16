-- Add JWT authentication fields to oauth_clients table
ALTER TABLE oauth_clients
ADD COLUMN token_endpoint_auth_method VARCHAR(50),
ADD COLUMN jwks_uri VARCHAR(255),
ADD COLUMN token_endpoint VARCHAR(255);

-- Create tables for collections
CREATE TABLE oauth_client_redirect_uris (
    oauth_client_id BIGINT NOT NULL,
    redirect_uri VARCHAR(255) NOT NULL,
    PRIMARY KEY (oauth_client_id, redirect_uri),
    FOREIGN KEY (oauth_client_id) REFERENCES oauth_clients(id)
);

CREATE TABLE oauth_client_scopes (
    oauth_client_id BIGINT NOT NULL,
    scope VARCHAR(255) NOT NULL,
    PRIMARY KEY (oauth_client_id, scope),
    FOREIGN KEY (oauth_client_id) REFERENCES oauth_clients(id)
);

CREATE TABLE oauth_client_grant_types (
    oauth_client_id BIGINT NOT NULL,
    grant_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (oauth_client_id, grant_type),
    FOREIGN KEY (oauth_client_id) REFERENCES oauth_clients(id)
);

-- Migrate existing data
INSERT INTO oauth_client_redirect_uris (oauth_client_id, redirect_uri)
SELECT id, redirect_uris FROM oauth_clients WHERE redirect_uris IS NOT NULL;

INSERT INTO oauth_client_scopes (oauth_client_id, scope)
SELECT id, scopes FROM oauth_clients WHERE scopes IS NOT NULL;

INSERT INTO oauth_client_grant_types (oauth_client_id, grant_type)
SELECT id, authorized_grant_types FROM oauth_clients WHERE authorized_grant_types IS NOT NULL;

-- Drop old columns
ALTER TABLE oauth_clients
DROP COLUMN redirect_uris,
DROP COLUMN scopes,
DROP COLUMN authorized_grant_types; 