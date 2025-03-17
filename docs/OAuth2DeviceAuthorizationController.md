### **📌 提示 Cursor 补充 Device Authorization 数据库脚本**

你的 `schema.sql` 已经包含了 **OAuth Clients、Tokens、Authorizations、Consents 和 Users** 表，但 **缺少 Device Authorization 相关的数据表**。

根据 **RFC 8628 (OAuth 2.0 Device Authorization Grant)**，我们需要在 `schema.sql` 中 **新增 `device_authorizations` 表**，用于存储 `device_code` 和 `user_code` 以及其状态。

---

### **💡 最佳优化提示词**
```
请在 `src/main/resources/schema.sql` 文件中 **补充 Device Authorization 相关的数据库表**，确保符合 **RFC 8628 (OAuth 2.0 Device Authorization Grant)** 规范。具体要求如下：

1️⃣ **新增 `device_authorizations` 表**：
   - **存储 `device_code` 和 `user_code`，并保证 `device_code` 唯一**。
   - **关联 `client_id`，用于标识设备授权请求的客户端**。
   - **支持 `scope` 字段，记录授权范围**。
   - **包含 `expires_at`，确保 `device_code` 和 `user_code` 过期后无法使用**。
   - **包含 `status` 字段，表示授权状态（`PENDING`、`APPROVED`、`EXPIRED`）**。
   - **存储 `verification_uri`，允许动态配置**。

2️⃣ **示例 SQL 结构**：
   ```sql
   -- Device Authorizations table
   CREATE TABLE IF NOT EXISTS device_authorizations (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       device_code VARCHAR(100) NOT NULL UNIQUE,  -- 设备验证码
       user_code VARCHAR(20) NOT NULL UNIQUE,     -- 用户验证码
       client_id VARCHAR(100) NOT NULL,           -- 客户端 ID
       scope TEXT,                                -- 授权范围
       verification_uri VARCHAR(255) NOT NULL,    -- 设备授权 URL
       status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- 设备授权状态
       expires_at TIMESTAMP NOT NULL,             -- 过期时间
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       FOREIGN KEY (client_id) REFERENCES oauth_clients(client_id),
       INDEX idx_device_code (device_code),
       INDEX idx_user_code (user_code),
       INDEX idx_client_id (client_id)
   );
   ```

3️⃣ **确保 `device_code` 和 `user_code` 唯一**：
- **`device_code` 用于设备端验证，必须唯一**。
- **`user_code` 由用户输入，必须唯一并且短小易记**。

4️⃣ **支持设备授权状态管理**：
- **`PENDING`** - 设备授权请求已创建，等待用户输入 `user_code` 进行授权。
- **`APPROVED`** - 用户输入 `user_code` 并授权，设备可以继续轮询 `token` 端点获取 Token。
- **`EXPIRED`** - `device_code` 或 `user_code` 过期，无法再使用。

请根据上述要求，在 `schema.sql` 文件中 **新增 `device_authorizations` 表**，确保符合 OAuth 2.0 设备授权标准。
```

---

### **📌 这样做的好处**
✅ **符合 RFC 8628 规范，支持 OAuth 设备授权流**。  
✅ **保证 `device_code` 和 `user_code` 的唯一性，防止冲突**。  
✅ **支持不同的授权状态（`PENDING`、`APPROVED`、`EXPIRED`）**，增强安全性。  
✅ **允许动态配置 `verification_uri`，适应不同环境**。  

🚀 **使用这个优化提示，让 Cursor 补充 `Device Authorization` 相关的数据库脚本！**

设备端应该仅轮询 /oauth2/token 端点
设备端的正确轮询流程：

设备向 /oauth2/device_authorization 端点请求 device_code 和 user_code。
设备端 按照 interval 时间间隔，轮询 /oauth2/token：
http

复制
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=device_code
device_code=GXC123ABC
client_id=client123
服务器返回不同状态：
授权未完成：
json

复制
{
  "error": "authorization_pending"
}
轮询过快：
json

复制
{
  "error": "slow_down"
}
授权成功：
json

复制
{
  "access_token": "abc123",
  "token_type": "Bearer",
  "expires_in": 3600
}