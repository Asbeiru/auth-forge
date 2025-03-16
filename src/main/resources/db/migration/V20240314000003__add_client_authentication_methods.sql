ALTER TABLE oauth_clients
    ADD COLUMN client_authentication_methods VARCHAR(255) NOT NULL DEFAULT 'CLIENT_SECRET_BASIC',
    ADD COLUMN default_authentication_method VARCHAR(50) NOT NULL DEFAULT 'CLIENT_SECRET_BASIC';

-- 更新现有记录，为公共客户端设置NONE认证方法
UPDATE oauth_clients 
SET client_authentication_methods = 'NONE',
    default_authentication_method = 'NONE'
WHERE client_type = 'public'; 