我来帮您设计一个完整的测试步骤，包括授权端点（/oauth2/authorize）和确认授权端点（/oauth2/consent）的测试。

1. 首先，让我们准备测试数据：

```sql
-- 插入测试客户端
INSERT INTO oauth_clients (
    client_id, 
    client_secret, 
    client_name, 
    redirect_uris, 
    scopes, 
    authorized_grant_types,
    auto_approve
) VALUES (
    'test-client',
    '$2a$10$8jb.YP7Zc8CKpXDKRPIXHOsYqC7.aSiJXCGtB.ftQzBQfqj3QyKXK',  -- 密码: test-secret
    'Test Client',
    'http://localhost:9999/callback',
    'read write profile email',
    'authorization_code',
    false
);

-- 插入测试用户
INSERT INTO users (
    username,
    password,
    authorities,
    enabled
) VALUES (
    'testuser',
    '$2a$10$8jb.YP7Zc8CKpXDKRPIXHOsYqC7.aSiJXCGtB.ftQzBQfqj3QyKXK',  -- 密码: password
    'ROLE_USER',
    true
);
```

2. 测试步骤：

A. 测试授权端点（/oauth2/authorize）：

```bash
# 1. 未登录状态下访问授权端点
curl -v "
http://localhost:9999/oauth2/authorize?response_type=code&client_id=client&redirect_uri=http://127.0.0.1:8080/callback&scope=read%20profile&state=xyz123
"

# 应该重定向到登录页面

# 2. 登录后访问授权端点
# 先登录（通过浏览器或Postman完成）
curl -v -X POST "http://localhost:9999/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=testuser&password=password"

# 然后再次访问授权端点
curl -v "http://localhost:9999/oauth2/authorize?response_type=code&client_id=test-client&redirect_uri=http://localhost:9999/callback&scope=read%20profile&state=xyz123" \
  -H "Cookie: JSESSIONID=<your-session-id>"

# 应该重定向到同意页面
```

B. 测试确认授权端点（/oauth2/consent）：

```bash
# 3. 提交同意决定
curl -v -X POST "http://localhost:9999/oauth2/consent" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Cookie: JSESSIONID=<your-session-id>" \
  -d "client_id=test-client&scope=read%20profile&state=xyz123&consent=approve"

# 应该重定向到客户端的回调地址，并带有授权码
```

3. 使用 Postman 进行测试（更方便的方式）：

创建以下请求集合：

A. 授权请求：
- Method: GET
- URL: `http://localhost:9999/oauth2/authorize`
- Query Parameters:
    - response_type: code
    - client_id: test-client
    - redirect_uri: http://localhost:9999/callback
    - scope: read profile
    - state: xyz123

B. 登录请求：
- Method: POST
- URL: `http://localhost:9999/login`
- Body (x-www-form-urlencoded):
    - username: testuser
    - password: password

C. 确认授权请求：
- Method: POST
- URL: `http://localhost:9999/oauth2/consent`
- Body (x-www-form-urlencoded):
    - client_id: test-client
    - scope: read profile
    - state: xyz123
    - consent: approve

4. 预期结果验证：

A. 数据库验证：
```sql
-- 检查授权记录
SELECT * FROM oauth_authorizations WHERE client_id = 'test-client' AND user_id = 'testuser';

-- 检查同意记录
SELECT * FROM oauth_consents WHERE client_id = 'test-client' AND user_id = 'testuser';
```

B. 日志验证：
- 检查应用程序日志中的授权流程记录
- 验证state参数在整个流程中的一致性
- 确认授权码的生成和过期时间设置

5. 错误场景测试：

```bash
# 1. 测试无效的client_id
curl -v "http://localhost:9999/oauth2/authorize?response_type=code&client_id=invalid-client&redirect_uri=http://localhost:9999/callback&scope=read&state=xyz123"

# 2. 测试无效的redirect_uri
curl -v "http://localhost:9999/oauth2/authorize?response_type=code&client_id=test-client&redirect_uri=http://invalid.com&scope=read&state=xyz123"

# 3. 测试无效的scope
curl -v "http://localhost:9999/oauth2/authorize?response_type=code&client_id=test-client&redirect_uri=http://localhost:9999/callback&scope=invalid&state=xyz123"

# 4. 测试CSRF攻击（修改state）
curl -v -X POST "http://localhost:9999/oauth2/consent" \
  -H "Cookie: JSESSIONID=<your-session-id>" \
  -d "client_id=test-client&scope=read&state=modified-state&consent=approve"
```

6. 安全测试：

A. 检查XSS防护：
```bash
# 在state参数中注入脚本
curl -v "http://localhost:9999/oauth2/authorize?response_type=code&client_id=test-client&redirect_uri=http://localhost:9999/callback&scope=read&state=<script>alert(1)</script>"
```

B. 检查CSRF防护：
```bash
# 不带CSRF token提交同意请求
curl -v -X POST "http://localhost:9999/oauth2/consent" \
  -d "client_id=test-client&scope=read&state=xyz123&consent=approve"
```

7. 性能测试：

```bash
# 使用Apache Bench进行并发测试
ab -n 1000 -c 10 "http://localhost:9999/oauth2/authorize?response_type=code&client_id=test-client&redirect_uri=http://localhost:9999/callback&scope=read&state=xyz123"
```

测试完成后，建议检查：
1. 授权码是否正确生成并存储
2. 授权码是否在10分钟后过期
3. state参数是否在整个流程中保持一致
4. 用户同意记录是否正确保存
5. 错误响应是否符合OAuth2.0规范
6. 安全headers是否正确设置

需要我为您创建一个测试脚本来自动化这些测试步骤吗？
```sql
INSERT INTO ruqi_collect_data.oauth_clients (id, client_id, client_secret, client_name, redirect_uris, scopes, authorized_grant_types, access_token_validity_seconds, refresh_token_validity_seconds, auto_approve, enabled, created_at, updated_at) VALUES (1, 'test-client', '$2a$10$8jb.YP7Zc8CKpXDKRPIXHOsYqC7.aSiJXCGtB.ftQzBQfqj3QyKXK', 'Test Client', 'http://localhost:9999/callback', 'read profile email', 'authorization_code', 3600, 86400, 0, 1, '2025-03-12 14:23:38', '2025-03-12 14:48:32');
INSERT INTO ruqi_collect_data.users (id, username, password, authorities, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) VALUES (1, 'testuser', 'password', 'ROLE_USER', 1, 1, 1, 1, '2025-03-12 14:23:39', '2025-03-12 14:43:57');


删除
-- 删除授权表
DROP TABLE IF EXISTS oauth_authorizations;

-- 删除同意表
DROP TABLE IF EXISTS oauth_consents;

-- 删除客户端表
DROP TABLE IF EXISTS oauth_clients;

-- 删除用户表
DROP TABLE IF EXISTS oauth_tokens;

-- 删除角色表
DROP TABLE IF EXISTS users;

```