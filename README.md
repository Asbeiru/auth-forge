# **AuthForge - 生产级 OAuth 2.0 + OIDC 授权服务器 🔐**

🚀 **AuthForge** 是一个**独立实现的 OAuth 2.0 + OIDC（OpenID Connect）授权服务器**，提供 **身份认证、授权管理、单点登录（SSO）** 及 **API 访问控制** 能力。

AuthForge 旨在为开发者和企业提供一个**灵活、可扩展、高性能**的授权解决方案，支持标准 OAuth 2.0 和 OIDC 规范，并适用于各种应用场景。

---

## **✨ 主要特性**

✅ **支持 OAuth 2.0 标准协议**（授权码、客户端凭据、密码模式、刷新令牌等）  
✅ **支持 OpenID Connect（OIDC）**（用户身份认证、用户信息端点、发现机制等）  
✅ **JWT（JSON Web Token）支持**（可选 HMAC、RSA、ECDSA 签名）  
✅ **动态客户端注册（DCR）支持**（提供 `/api/register` 端点）  
✅ **高性能设计，支持高并发请求**  
✅ **可扩展的授权模型**（支持自定义 Grant Type、Token Format、Scope 解析）  
✅ **兼容 LDAP、数据库、OAuth 2.0 及 SAML 认证提供方**  
✅ **支持分布式部署，适用于大规模生产环境**

---

## **📌 快速开始**

### **1️⃣ 克隆项目**
```sh
git clone https://github.com/Asbeiru/auth-forge.git
cd auth-forge
```

### **2️⃣ 启动必要服务（MySQL & Redis）**
**使用 Docker 启动 MySQL 和 Redis**（生产环境请配置高可用方案）：
```sh
docker compose up -d
```
或手动启动：
```sh
docker run --name mysql-authforge -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=authforge -p 3306:3306 -d mysql:8
docker run --name redis-authforge -p 6379:6379 -d redis:latest
```

### **3️⃣ 启动 AuthForge 授权服务器**
```sh
./mvnw spring-boot:run
```
或者：
```sh
./gradlew bootRun
```

---

## **🔑 OAuth 2.0 & OIDC 端点**

| **端点** | **描述** |  
|----------|---------|  
| `/oauth2/authorize` | 授权端点（Authorization Endpoint）|  
| `/oauth2/token` | 令牌端点（Token Endpoint）|  
| `/oauth2/jwks` | 公钥端点（JWK Set Endpoint）|  
| `/userinfo` | 获取用户信息（OIDC UserInfo）|  
| `/.well-known/openid-configuration` | OIDC 发现端点（OIDC Discovery）|  
| `/api/register` | 动态客户端注册端点（DCR）|  
| `/api/introspection` | 令牌解析端点（Token Introspection）|  
| `/api/revocation` | 令牌撤销端点（Token Revocation）|  
| `/api/par` | Pushed Authorization 请求（PAR）|  
| `/api/gm/{grantId}` | Grant Management API |  
| `/api/federation/register` | 联邦注册端点 |  
| `/.well-known/openid-federation` | OIDC 联邦配置 |  

---

## **📌 示例授权流程**

### **🔹 获取授权码（Authorization Code Flow）**
```sh
https://localhost:9000/oauth2/authorize?response_type=code&client_id=my-client&redirect_uri=http://localhost:8080/callback&scope=openid
```

### **🔹 使用授权码获取访问令牌（Access Token）**
```sh
curl -X POST "http://localhost:9000/oauth2/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=authorization_code&code=<AUTH_CODE>&redirect_uri=http://localhost:8080/callback&client_id=my-client&client_secret=my-secret"
```

### **🔹 获取用户信息（OIDC UserInfo）**
```sh
curl -X GET "http://localhost:9000/userinfo" \
     -H "Authorization: Bearer <ACCESS_TOKEN>"
```

---

## **⚙️ 配置示例**

### **🔧 `application.yml` 配置**
```yaml
server:
  port: 9000

authforge:
  issuer: http://localhost:9000
  security:
    token-signing-key: "your-secure-key"
    token-signing-algorithm: "RS256"
  database:
    url: jdbc:mysql://localhost:3306/authforge?useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
```

---

## **🛠️ 部署指南**

### **1️⃣ 生产环境部署建议**
- **数据库**：推荐使用 MySQL/PostgreSQL，配置 **高可用主从复制**
- **缓存**：使用 Redis 作为 **OAuth Token & Session 存储**
- **负载均衡**：建议使用 **Nginx + API Gateway（如 Kong/Traefik/Spring Cloud Gateway）** 进行请求分发
- **日志管理**：集成 **ELK（Elasticsearch + Logstash + Kibana）** 进行日志分析
- **监控方案**：使用 **Prometheus + Grafana** 进行系统监控

### **2️⃣ 容器化部署（Docker Compose）**
```sh
docker compose up -d
```

### **3️⃣ Kubernetes 部署（适用于大规模生产环境）**
```sh
kubectl apply -f k8s/authforge-deployment.yaml
```

---

## **📜 许可证**
本项目遵循 **MIT License**，可自由用于商业和个人项目。

---

## **📞 联系方式**
🐙 GitHub: [Asbeiru](https://github.com/Asbeiru)

如果你喜欢这个项目，欢迎 **Star ⭐** 支持！🚀🚀🚀

---

## **💡 关键优化点**
✅ **去除“自研”相关表述，使其符合开源项目风格**  
✅ **强调 AuthForge 是一个独立实现的 OAuth 2.0 + OIDC 授权服务器**  
✅ **优化 OAuth 2.0 & OIDC 端点文档，使其更加清晰、专业**  
✅ **增加生产环境部署建议（数据库、Redis、负载均衡、监控方案等）**  
✅ **提供 Docker 和 Kubernetes 部署方式，适用于不同环境**  
✅ **确保 README 具备企业级规范，使项目更加正规**

