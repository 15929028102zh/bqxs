# 边墙鲜送系统 Docker 快速部署指南

本指南将帮助您快速使用 Docker 部署边墙鲜送系统的完整环境。

## 🚀 快速开始

### 1. 环境准备

确保您的服务器已安装：
- Docker 20.10+
- Docker Compose 1.29+
- Git

### 2. 克隆项目

```bash
git clone <your-repository-url> fresh-delivery
cd fresh-delivery
```

### 3. 配置环境变量

```bash
# 复制环境变量模板
cp docker/.env.example docker/.env

# 编辑配置文件
vim docker/.env
```

**重要配置项**：
```env
# 数据库密码（必须修改）
MYSQL_ROOT_PASSWORD=your_strong_password
MYSQL_PASSWORD=your_db_password

# JWT 密钥（必须修改）
JWT_SECRET=your_jwt_secret_key

# 文件域名（根据实际情况修改）
FILE_DOMAIN=https://yourdomain.com

# 微信小程序配置
WECHAT_APPID=your_wechat_appid
WECHAT_SECRET=your_wechat_secret
```

### 4. 一键部署

#### Linux/macOS
```bash
# 给脚本执行权限
chmod +x deploy.sh

# 启动所有服务
./deploy.sh start
```

#### Windows
```cmd
# 运行部署脚本
deploy.bat start
```

### 5. 验证部署

等待 2-3 分钟后，访问以下地址验证部署：

- **管理后台**: https://localhost
- **API 文档**: http://localhost:8080/swagger-ui/index.html
- **健康检查**: http://localhost:8080/api/health

**默认管理员账号**：
- 用户名: `admin`
- 密码: `admin123`

## 📋 服务架构

```
┌─────────────────┐
│   Nginx (443)   │  ← HTTPS 入口
│   反向代理/SSL   │
└─────────┬───────┘
          │
┌─────────▼───────┐    ┌─────────────────┐
│  Admin Frontend │    │    Backend      │
│     (80)        │    │    (8080)       │
└─────────────────┘    └─────────┬───────┘
                                 │
                       ┌─────────▼───────┐    ┌─────────────────┐
                       │   MySQL (3306)  │    │   Redis (6379)  │
                       │     数据库       │    │     缓存        │
                       └─────────────────┘    └─────────────────┘
```

## 🛠️ 常用命令

### 服务管理
```bash
# 启动服务
./deploy.sh start

# 停止服务
./deploy.sh stop

# 重启服务
./deploy.sh restart

# 查看状态
./deploy.sh status
```

### 日志查看
```bash
# 查看所有服务日志
./deploy.sh logs

# 查看特定服务日志
./deploy.sh logs backend
./deploy.sh logs mysql
./deploy.sh logs redis
```

### 数据管理
```bash
# 备份数据
./deploy.sh backup

# 清理数据（危险操作）
./deploy.sh clean
```

## 🔧 配置说明

### 端口映射

| 服务 | 内部端口 | 外部端口 | 说明 |
|------|----------|----------|------|
| Nginx | 80/443 | 80/443 | Web 服务入口 |
| Backend | 8080 | 8080 | API 服务 |
| MySQL | 3306 | 3306 | 数据库 |
| Redis | 6379 | 6379 | 缓存 |

### 数据持久化

以下数据将持久化存储：
- MySQL 数据: `mysql_data` 卷
- Redis 数据: `redis_data` 卷
- 后端日志: `backend_logs` 卷
- 上传文件: `backend_uploads` 卷
- Nginx 日志: `nginx_logs` 卷

### SSL 证书

系统会自动生成自签名证书用于测试。生产环境建议使用：

1. **Let's Encrypt 免费证书**：
```bash
# 安装 certbot
sudo apt install certbot

# 获取证书
sudo certbot certonly --standalone -d yourdomain.com

# 复制证书
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem docker/nginx/ssl/server.crt
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem docker/nginx/ssl/server.key
```

2. **商业 SSL 证书**：
   将证书文件放置到 `docker/nginx/ssl/` 目录

## 🔍 故障排除

### 常见问题

1. **端口被占用**
```bash
# 检查端口占用
netstat -tulpn | grep :80
netstat -tulpn | grep :443
netstat -tulpn | grep :3306

# 停止占用端口的服务
sudo systemctl stop apache2  # 如果安装了 Apache
sudo systemctl stop nginx    # 如果安装了系统 Nginx
```

2. **内存不足**
```bash
# 检查内存使用
free -h

# 清理 Docker 资源
docker system prune -a

# 调整服务资源限制
vim docker/docker-compose.yml
```

3. **数据库连接失败**
```bash
# 检查数据库状态
docker-compose -f docker/docker-compose.yml logs mysql

# 重置数据库
docker-compose -f docker/docker-compose.yml down
docker volume rm fresh-delivery_mysql_data
./deploy.sh start
```

4. **SSL 证书问题**
```bash
# 重新生成证书
rm -rf docker/nginx/ssl/*
./deploy.sh start
```

### 查看详细日志
```bash
# 实时查看所有日志
docker-compose -f docker/docker-compose.yml logs -f

# 查看特定服务的详细日志
docker-compose -f docker/docker-compose.yml logs -f --tail=100 backend
```

## 📈 性能优化

### 1. 资源配置

根据服务器配置调整 `docker-compose.yml` 中的资源限制：

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2.0'      # CPU 限制
          memory: 2G       # 内存限制
        reservations:
          cpus: '1.0'      # CPU 预留
          memory: 1G       # 内存预留
```

### 2. 数据库优化

编辑 `docker/mysql/conf/my.cnf`：
```ini
# 根据服务器内存调整
innodb_buffer_pool_size = 1G    # 设置为服务器内存的 50-70%
max_connections = 200           # 根据并发需求调整
```

### 3. Redis 优化

编辑 `docker/redis/redis.conf`：
```ini
# 设置最大内存
maxmemory 512mb
maxmemory-policy allkeys-lru
```

## 🔒 安全建议

1. **修改默认密码**
   - 数据库密码
   - Redis 密码（如果启用）
   - 管理员账号密码

2. **防火墙配置**
```bash
# Ubuntu/Debian
sudo ufw allow 80
sudo ufw allow 443
sudo ufw enable

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --reload
```

3. **定期更新**
```bash
# 更新系统包
sudo apt update && sudo apt upgrade

# 更新 Docker 镜像
docker-compose -f docker/docker-compose.yml pull
./deploy.sh restart
```

## 📚 更多文档

- [详细部署指南](docs/docker-deployment-guide.md)
- [API 接口文档](docs/api.md)
- [故障排除指南](docs/troubleshooting.md)

## 🆘 获取帮助

如果遇到问题：

1. 查看 [故障排除](#故障排除) 部分
2. 检查项目 Issues
3. 联系技术支持

## 📝 更新日志

### v1.0.0
- 初始 Docker 部署支持
- 完整的前后端容器化
- 自动化部署脚本
- SSL 证书支持
- 数据持久化

---

**注意**: 首次部署可能需要较长时间下载镜像和构建，请耐心等待。建议在生产环境部署前先在测试环境验证所有功能。