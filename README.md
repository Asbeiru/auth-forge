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
