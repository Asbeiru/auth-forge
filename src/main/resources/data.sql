-- 插入测试用户 (密码: password)
INSERT INTO users (username, password, authorities, enabled) VALUES
('test_user', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ROLE_USER', true)
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试客户端
INSERT INTO oauth_clients (client_id, client_secret, client_name, redirect_uris, scopes, authorized_grant_types, auto_approve, enabled) VALUES
('test-client', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Test Client', 'http://127.0.0.1:8080/callback', 'read,write', 'authorization_code,refresh_token', false, true)
ON DUPLICATE KEY UPDATE client_id=client_id; 