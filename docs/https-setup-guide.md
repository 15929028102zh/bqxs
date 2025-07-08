# 微信小程序 HTTPS 配置指南

## 问题描述

微信小程序要求所有网络请求必须使用 HTTPS 协议。如果使用 HTTP 协议，会出现以下错误：

- `图片链接不再支持 HTTP 协议，请升级到 HTTPS`
- `Failed to load local image resource` 错误
- `HTTP/1.1 500 Internal Server Error`

## 开发环境解决方案

### 方案一：关闭域名校验（推荐用于开发测试）

1. **微信开发者工具设置**：
   - 打开微信开发者工具
   - 点击右上角「详情」按钮
   - 选择「本地设置」标签
   - 勾选「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」
   - 重新编译小程序

2. **真机调试**：
   - 在微信开发者工具中点击「预览」
   - 用手机扫码打开小程序
   - 在小程序右上角点击「...」
   - 选择「开发调试」
   - 开启调试模式后可以使用 HTTP 协议

### 方案二：使用内网穿透工具

如果需要在真机上测试而不开启调试模式，可以使用内网穿透工具：

1. 安装 ngrok 或类似工具
2. 将本地 HTTP 服务映射为 HTTPS
3. 使用映射后的 HTTPS 地址

## 生产环境解决方案

### 1. 获取 SSL 证书

#### 免费证书（推荐）

**Let's Encrypt**：
```bash
# 安装 certbot
sudo apt-get install certbot

# 获取证书
sudo certbot certonly --standalone -d yourdomain.com
```

**阿里云免费证书**：
1. 登录阿里云控制台
2. 进入「SSL证书服务」
3. 选择「免费证书」
4. 按照指引完成域名验证

#### 商业证书

- 阿里云 SSL 证书
- 腾讯云 SSL 证书
- DigiCert、Symantec 等品牌证书

### 2. 配置 HTTPS 服务器

#### Nginx 配置示例

```nginx
server {
    listen 443 ssl;
    server_name yourdomain.com;
    
    ssl_certificate /path/to/your/certificate.pem;
    ssl_certificate_key /path/to/your/private.key;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
    ssl_prefer_server_ciphers on;
    
    location /api {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

#### Spring Boot 内置 HTTPS 配置

在 `application.yml` 中添加：

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: your-password
    key-store-type: PKCS12
    key-alias: your-alias
```

### 3. 微信小程序后台配置

1. 登录微信公众平台
2. 进入「开发」->「开发设置」
3. 配置「服务器域名」：
   - request合法域名：`https://yourdomain.com`
   - uploadFile合法域名：`https://yourdomain.com`
   - downloadFile合法域名：`https://yourdomain.com`

### 4. 域名要求

- 域名必须经过 ICP 备案
- 不能使用 IP 地址
- 不能使用 localhost
- 证书必须有效且被系统信任
- 支持 TLS 1.2 及以上版本

## 常见问题

### Q1: 证书配置后仍然报错

**解决方案**：
1. 检查证书是否过期
2. 验证证书链是否完整
3. 确认域名与证书匹配
4. 测试 HTTPS 连接：`curl -I https://yourdomain.com`

### Q2: 开发环境无法访问

**解决方案**：
1. 确认已关闭域名校验
2. 检查后端服务是否正常运行
3. 验证网络连接

### Q3: 图片无法加载

**解决方案**：
1. 确认图片 URL 使用 HTTPS
2. 检查图片服务器证书
3. 验证图片路径是否正确

## 测试验证

### 1. 证书验证工具

- SSL Labs: https://www.ssllabs.com/ssltest/
- SSL Checker: https://www.sslshopper.com/ssl-checker.html

### 2. 命令行测试

```bash
# 测试 HTTPS 连接
curl -I https://yourdomain.com/api/health

# 检查证书信息
openssl s_client -connect yourdomain.com:443 -servername yourdomain.com
```

### 3. 小程序测试

1. 在开发者工具中测试网络请求
2. 真机预览测试
3. 体验版测试

## 总结

- **开发阶段**：关闭域名校验，使用 HTTP 协议
- **测试阶段**：配置临时 HTTPS 或使用内网穿透
- **生产阶段**：必须配置正式的 HTTPS 证书和域名

遵循以上步骤，可以有效解决微信小程序的 HTTPS 协议要求问题。