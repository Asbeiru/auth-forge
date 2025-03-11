如果你的项目命名为 **`AuthForge`**，你需要一个 **清晰、结构化、有吸引力的 README**，让用户（或学生）能快速理解 **OAuth 2.0 + OIDC 授权服务器** 的作用、安装方式以及如何使用它。

下面是一个完整的 **README.md** 模板，你可以直接使用或修改：

---

# **AuthForge - OAuth 2.0 + OIDC 授权服务器 🔐**

🚀 **AuthForge** 是一个基于 **Spring Boot** 构建的 **OAuth 2.0 + OIDC（OpenID Connect）** 授权服务器，旨在提供 **身份认证** 和 **授权管理**，适用于教学和学习目的。

> 🌟 **学习 OAuth 2.0 和 OIDC** 的最佳实践！

---

## **✨ 主要特性**
✅ **OAuth 2.0 & OIDC 支持**（授权码模式、客户端凭据模式等）
✅ **基于 Spring Authorization Server**（轻量级、可扩展）
✅ **支持 JWT 访问令牌**（JSON Web Token）
✅ **提供 OAuth 端点（/authorize, /token, /userinfo 等）**
✅ **内置示例客户端（OAuth Playground）**
✅ **易于部署，快速上手！**

---

## **📌 快速开始**
### **🔧 1. 克隆项目**
```sh
git clone https://github.com/Asbeiru/auth-forge.git
cd auth-forge
```

### **📦 2. 运行 MySQL（Docker）**
使用 Docker 启动 MySQL：
```sh
docker run --name mysql-authforge -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=authforge -p 3306:3306 -d mysql:8
```

### **🚀 3. 启动 AuthForge**
```sh
./mvnw spring-boot:run
```
或者：
```sh
./gradlew bootRun
```

---

## **🔑 认证 & 授权**
### **OAuth 2.0 端点**
| **端点** | **描述** |
|----------|---------|
| `/oauth2/authorize` | 授权端点（Authorization Endpoint） |
| `/oauth2/token` | 令牌端点（Token Endpoint） |
| `/oauth2/jwks` | 公钥端点（JWK Set Endpoint） |
| `/userinfo` | 获取用户信息（OIDC UserInfo） |
|Token Endpoint|	/api/token|
|JWK Set Endpoint|	/api/jwks|
|Discovery Endpoint|	/.well-known/openid-configuration|
|Revocation Endpoint	|/api/revocation|
|Introspection Endpoint|	/api/introspection|
|UserInfo Endpoint|	/api/userinfo|
|Dynamic Client Registration Endpoint|	/api/register|
|Pushed Authorization Request Endpoint|	/api/par|
|Grant Management Endpoint|	/api/gm/{grantId}|
|Federation Configuration Endpoint	|/.well-known/openid-federation|
|Federation Registration Endpoint	|/api/federation/register|
|Credential Issuer Metadata Endpoint|	/.well-known/openid-credential-issuer|
|JWT Issuer Metadata Endpoint|	/.well-known/jwt-issuer|

### **示例授权请求**
**OAuth 2.0 授权码模式**
```sh
https://localhost:9000/oauth2/authorize?response_type=code&client_id=my-client&redirect_uri=http://localhost:8080/callback&scope=openid
```

**获取访问令牌**
```sh
curl -X POST "http://localhost:9000/oauth2/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=authorization_code&code=<AUTH_CODE>&redirect_uri=http://localhost:8080/callback&client_id=my-client&client_secret=my-secret"
```

---

## **📜 教学内容**
AuthForge 适合作为 **OAuth 2.0 + OIDC 教学项目**，涵盖：
- ✅ OAuth 2.0 授权码模式、客户端凭据模式
- ✅ OpenID Connect（OIDC）身份认证
- ✅ JWT 令牌管理与验证
- ✅ Spring Authorization Server 配置与扩展
- ✅ OAuth 2.0 客户端示例

---

## **⚙️ 配置**
### **🔧 `application.yml` 配置**
```yaml
server:
  port: 9000

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/authforge?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
```

---

## **👨‍💻 贡献**
欢迎贡献代码！请遵循以下步骤：
1. Fork 本项目
2. 创建新分支 (`git checkout -b feature-xxx`)
3. 提交更改 (`git commit -m "添加新功能 xxx"`)
4. 推送分支 (`git push origin feature-xxx`)
5. 提交 Pull Request 🎉

---

## **📜 许可证**
本项目遵循 **MIT License**，自由使用、修改和分发。

---

## **📞 联系方式**
📧 Email: `your-email@example.com`
🐙 GitHub: [Asbeiru](https://github.com/Asbeiru)

如果你喜欢这个项目，欢迎 **Star ⭐** 支持！🚀🚀🚀

---

## **💡 总结**
这个 README 具备：
✅ **清晰的介绍**（AuthForge 是什么？）
✅ **快速开始指南**（如何安装 & 运行）
✅ **OAuth 2.0 端点 & 示例请求**
✅ **教学目标**（适合学习 OAuth & OIDC）
✅ **配置说明**（Spring Boot 配置）
✅ **贡献指南 & 许可证**

你可以根据你的 **具体实现** 进行微调，比如 **增加 API 文档** 或 **添加示例 OAuth 客户端**。

💡 **如果你有更多需求，欢迎讨论！🚀**

## 🧪 测试授权端点

### 1. 环境准备

#### 1.1 启动 MySQL
```bash
docker run --name mysql-authforge \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=authforge \
  -p 3306:3306 \
  -d mysql:8
```

#### 1.2 启动应用
```bash
./mvnw spring-boot:run
```

### 2. 测试数据准备

应用启动时会自动创建以下测试数据：

#### 2.1 测试用户
- 用户名：test_user
- 密码：password
- 权限：ROLE_USER

#### 2.2 测试客户端
- 客户端ID：test-client
- 客户端密钥：secret
- 授权类型：authorization_code
- 重定向URI：http://127.0.0.1:8080/callback
- 授权范围：read,write

### 3. 测试步骤

#### 3.1 授权码流程测试

1. **发起授权请求**
   
   在浏览器中访问：
   ```
   http://localhost:9000/oauth2/authorize?response_type=code&client_id=test-client&redirect_uri=http://127.0.0.1:8080/callback&scope=read
   ```

2. **用户登录**
   - 输入用户名：test_user
   - 输入密码：password

3. **授权确认**
   - 在授权确认页面查看请求的权限
   - 点击"Approve"按钮同意授权

4. **获取授权码**
   - 授权成功后会重定向到：
   ```
   http://127.0.0.1:8080/callback?code={authorization_code}
   ```
   - 记录返回的授权码

#### 3.2 PKCE流程测试（可选）

1. **生成 PKCE 参数**
   ```bash
   # 在终端中执行以下Python命令

   # 生成 code_verifier
   python3 -c 'import secrets; print(secrets.token_urlsafe(32))'

   # 生成 code_challenge
   python3 -c 'import base64, hashlib; verifier="你的code_verifier"; m=hashlib.sha256(); m.update(verifier.encode()); challenge=base64.urlsafe_b64encode(m.digest()).decode().replace("=",""); print(challenge)'
   ```

2. **发起带PKCE的授权请求**
   ```
   http://localhost:9000/oauth2/authorize?response_type=code&client_id=test-client&redirect_uri=http://127.0.0.1:8080/callback&scope=read&code_challenge=你的code_challenge&code_challenge_method=S256
   ```

### 4. 验证测试结果

#### 4.1 成功场景检查项
- [ ] 能够访问授权端点
- [ ] 成功跳转到登录页面
- [ ] 登录后显示授权确认页面
- [ ] 同意授权后获得授权码
- [ ] 授权码格式正确（base64编码字符串）

#### 4.2 错误场景测试

1. **无效的客户端ID**
   ```
   http://localhost:9000/oauth2/authorize?response_type=code&client_id=invalid-client&redirect_uri=http://127.0.0.1:8080/callback&scope=read
   ```
   预期：显示错误信息

2. **无效的重定向URI**
   ```
   http://localhost:9000/oauth2/authorize?response_type=code&client_id=test-client&redirect_uri=http://invalid.com&scope=read
   ```
   预期：显示错误信息

3. **无效的授权范围**
   ```
   http://localhost:9000/oauth2/authorize?response_type=code&client_id=test-client&redirect_uri=http://127.0.0.1:8080/callback&scope=invalid
   ```
   预期：显示错误信息

### 5. 问题排查

#### 5.1 查看应用日志
```bash
tail -f logs/auth-forge.log
```

#### 5.2 检查数据库记录
```sql
-- 查看客户端信息
SELECT * FROM oauth_clients;

-- 查看授权记录
SELECT * FROM oauth_authorizations;

-- 查看用户同意记录
SELECT * FROM oauth_consents;
```

#### 5.3 常见问题解决

1. **无法访问授权端点**
   - 检查应用是否正常启动
   - 确认端口9000未被占用
   - 验证SecurityConfig配置是否正确

2. **数据库连接问题**
   - 确保MySQL正在运行
   - 检查数据库连接配置
   - 验证数据库用户权限

3. **授权失败**
   - 检查客户端配置是否正确
   - 验证重定向URI是否匹配
   - 确认请求的scope是否允许

4. **登录失败**
   - 确认用户数据是否正确导入
   - 检查密码编码配置
   - 验证UserDetailsService实现

### 6. 测试工具

为方便测试，我们提供了一个简单的测试客户端页面：
`http://localhost:9000/test-client.html`

这个页面提供：
- 一键发起授权请求
- 显示授权结果
- PKCE参数生成
- 测试结果验证

### 7. 后续步骤

完成授权端点测试后，你可以：
1. 实现令牌端点
2. 添加刷新令牌支持
3. 实现用户信息端点
4. 添加JWT支持
5. 实现客户端管理接口

如果遇到问题或需要帮助，请查看项目文档或提交Issue。
