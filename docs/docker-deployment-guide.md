# 边墙鲜送系统 Docker 部署指南

本指南将帮助您使用 Docker 将边墙鲜送系统的前后端部署到 Linux 服务器上。

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Nginx (443)   │    │  Admin Frontend │    │    Backend      │
│   反向代理/SSL   │────│     (80)        │────│    (8080)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                       ┌─────────────────┐    ┌─────────────────┐
                       │   MySQL (3306)  │    │   Redis (6379)  │
                       │     数据库       │    │     缓存        │
                       └─────────────────┘    └─────────────────┘
```

## 服务组件

| 服务 | 端口 | 说明 |
|------|------|------|
| Nginx | 80/443 | 反向代理、SSL 终端、静态文件服务 |
| Admin Frontend | 80 | 管理后台前端 (Vue.js + Vite) |
| Backend | 8080 | 后端 API 服务 (Spring Boot) |
| MySQL | 3306 | 主数据库 |
| Redis | 6379 | 缓存和会话存储 |

## 前置要求

### 服务器要求
- **操作系统**: Linux (推荐 Ubuntu 20.04+ 或 CentOS 7+)
- **内存**: 最低 4GB，推荐 8GB+
- **存储**: 最低 20GB 可用空间
- **网络**: 公网 IP 和域名（可选）

### 软件要求
- Docker 20.10+
- Docker Compose 1.29+
- Git
- OpenSSL（用于生成 SSL 证书）

## 安装步骤

### 1. 安装 Docker 和 Docker Compose

#### Ubuntu/Debian
```bash
# 更新包索引
sudo apt update

# 安装必要的包
sudo apt install -y apt-transport-https ca-certificates curl gnupg lsb-release

# 添加 Docker 官方 GPG 密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 添加 Docker 仓库
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 将当前用户添加到 docker 组
sudo usermod -aG docker $USER

# 启动 Docker 服务
sudo systemctl start docker
sudo systemctl enable docker
```

#### CentOS/RHEL
```bash
# 安装必要的包
sudo yum install -y yum-utils

# 添加 Docker 仓库
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 安装 Docker
sudo yum install -y docker-ce docker-ce-cli containerd.io

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 启动 Docker 服务
sudo systemctl start docker
sudo systemctl enable docker

# 将当前用户添加到 docker 组
sudo usermod -aG docker $USER
```

### 2. 克隆项目代码

```bash
# 克隆项目
git clone <your-repository-url> fresh-delivery
cd fresh-delivery

# 或者上传项目文件到服务器
# 可以使用 scp、rsync 或其他方式
```

### 3. 配置环境

#### 3.1 创建环境变量文件

```bash
# 创建 .env 文件
cp docker/.env.example docker/.env

# 编辑环境变量
vim docker/.env
```

`.env` 文件示例：
```env
# 数据库配置
MYSQL_ROOT_PASSWORD=your_strong_password
MYSQL_DATABASE=fresh_delivery
MYSQL_USERNAME=fresh_user
MYSQL_PASSWORD=your_db_password

# Redis 配置
REDIS_PASSWORD=your_redis_password

# 后端配置
FILE_DOMAIN=https://yourdomain.com
JWT_SECRET=your_jwt_secret_key
WECHAT_APPID=your_wechat_appid
WECHAT_SECRET=your_wechat_secret

# 服务器配置
SERVER_NAME=yourdomain.com
```

#### 3.2 生成 SSL 证书

**选项 1: 自签名证书（仅用于测试）**
```bash
# 创建 SSL 目录
mkdir -p docker/nginx/ssl

# 生成自签名证书
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout docker/nginx/ssl/server.key \
    -out docker/nginx/ssl/server.crt \
    -subj "/C=CN/ST=Beijing/L=Beijing/O=FreshDelivery/OU=IT/CN=localhost"
```

**选项 2: Let's Encrypt 证书（推荐用于生产）**
```bash
# 安装 certbot
sudo apt install -y certbot

# 获取证书
sudo certbot certonly --standalone -d yourdomain.com

# 复制证书到项目目录
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem docker/nginx/ssl/server.crt
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem docker/nginx/ssl/server.key
sudo chown $USER:$USER docker/nginx/ssl/*
```

### 4. 部署应用

#### 4.1 使用部署脚本（推荐）

```bash
# 给脚本执行权限
chmod +x deploy.sh

# 启动所有服务
./deploy.sh start
```

#### 4.2 手动部署

```bash
# 构建镜像
docker build -t fresh-delivery-backend:latest backend/
docker build -t fresh-delivery-admin:latest admin-frontend/

# 启动服务
docker-compose -f docker/docker-compose.yml up -d

# 查看服务状态
docker-compose -f docker/docker-compose.yml ps
```

### 5. 验证部署

```bash
# 检查所有容器状态
docker ps

# 检查服务日志
docker-compose -f docker/docker-compose.yml logs

# 测试服务连通性
curl -k https://localhost/health
curl http://localhost:8080/api/health
```

## 访问应用

部署成功后，您可以通过以下地址访问：

- **管理后台**: https://yourdomain.com 或 https://localhost
- **API 文档**: http://yourdomain.com:8080/swagger-ui/index.html
- **数据库**: yourdomain.com:3306
- **Redis**: yourdomain.com:6379

### 默认账号

**管理员账号**:
- 用户名: `admin`
- 密码: `admin123`

## 常用操作

### 查看日志
```bash
# 查看所有服务日志
./deploy.sh logs

# 查看特定服务日志
./deploy.sh logs backend
./deploy.sh logs mysql
```

### 重启服务
```bash
# 重启所有服务
./deploy.sh restart

# 重启特定服务
docker-compose -f docker/docker-compose.yml restart backend
```

### 更新应用
```bash
# 拉取最新代码
git pull

# 重新构建并启动
./deploy.sh restart
```

### 备份数据
```bash
# 备份数据库和文件
./deploy.sh backup
```

### 数据库操作
```bash
# 连接到数据库
docker-compose -f docker/docker-compose.yml exec mysql mysql -u fresh_user -p fresh_delivery

# 导入数据
docker-compose -f docker/docker-compose.yml exec -T mysql mysql -u fresh_user -pfresh_pass fresh_delivery < backup.sql

# 导出数据
docker-compose -f docker/docker-compose.yml exec mysql mysqldump -u fresh_user -pfresh_pass fresh_delivery > backup.sql
```

## 性能优化

### 1. 资源限制

在 `docker-compose.yml` 中添加资源限制：

```yaml
services:
  backend:
    # ... 其他配置
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
```

### 2. 数据库优化

```bash
# 调整 MySQL 配置
vim docker/mysql/conf/my.cnf

# 重启 MySQL 服务
docker-compose -f docker/docker-compose.yml restart mysql
```

### 3. 缓存优化

```bash
# 调整 Redis 配置
vim docker/redis/redis.conf

# 重启 Redis 服务
docker-compose -f docker/docker-compose.yml restart redis
```

## 监控和维护

### 1. 健康检查

```bash
# 检查服务状态
./deploy.sh status

# 检查磁盘使用
df -h

# 检查内存使用
free -h

# 检查 Docker 资源使用
docker stats
```

### 2. 日志管理

```bash
# 清理旧日志
docker system prune -f

# 设置日志轮转
vim /etc/logrotate.d/docker
```

### 3. 自动备份

创建定时备份任务：

```bash
# 编辑 crontab
crontab -e

# 添加每日备份任务
0 2 * * * /path/to/fresh-delivery/deploy.sh backup
```

## 故障排除

### 常见问题

1. **容器启动失败**
   ```bash
   # 查看详细错误信息
   docker-compose -f docker/docker-compose.yml logs [service_name]
   
   # 检查端口占用
   netstat -tulpn | grep [port]
   ```

2. **数据库连接失败**
   ```bash
   # 检查数据库状态
   docker-compose -f docker/docker-compose.yml exec mysql mysql -u root -p -e "SHOW DATABASES;"
   
   # 重置数据库密码
   docker-compose -f docker/docker-compose.yml exec mysql mysql -u root -p -e "ALTER USER 'fresh_user'@'%' IDENTIFIED BY 'new_password';"
   ```

3. **SSL 证书问题**
   ```bash
   # 检查证书有效性
   openssl x509 -in docker/nginx/ssl/server.crt -text -noout
   
   # 重新生成证书
   rm docker/nginx/ssl/*
   ./deploy.sh start
   ```

4. **内存不足**
   ```bash
   # 清理未使用的镜像和容器
   docker system prune -a
   
   # 调整服务资源限制
   vim docker/docker-compose.yml
   ```

### 紧急恢复

```bash
# 停止所有服务
./deploy.sh stop

# 清理所有数据（危险操作）
./deploy.sh clean

# 从备份恢复
docker-compose -f docker/docker-compose.yml exec -T mysql mysql -u fresh_user -pfresh_pass fresh_delivery < backups/latest/database.sql

# 重新启动
./deploy.sh start
```

## 安全建议

1. **定期更新**
   - 定期更新 Docker 和系统包
   - 更新应用依赖和镜像

2. **访问控制**
   - 使用防火墙限制端口访问
   - 配置 VPN 或跳板机
   - 定期更换密码

3. **数据保护**
   - 定期备份数据
   - 加密敏感数据
   - 监控异常访问

4. **网络安全**
   - 使用 HTTPS
   - 配置 WAF
   - 启用访问日志

## 扩展部署

### 多节点部署

对于高可用部署，可以考虑：

1. **数据库集群**: MySQL 主从复制或 Galera 集群
2. **Redis 集群**: Redis Sentinel 或 Cluster 模式
3. **负载均衡**: 多个后端实例 + Nginx 负载均衡
4. **容器编排**: 使用 Kubernetes 或 Docker Swarm

### 云平台部署

- **阿里云**: 使用 ECS + RDS + Redis + SLB
- **腾讯云**: 使用 CVM + TencentDB + Redis + CLB
- **AWS**: 使用 EC2 + RDS + ElastiCache + ALB

## 联系支持

如果在部署过程中遇到问题，请：

1. 查看本文档的故障排除部分
2. 检查项目的 GitHub Issues
3. 联系技术支持团队

---

**注意**: 本指南假设您具有基本的 Linux 系统管理和 Docker 使用经验。在生产环境中部署前，请确保充分测试所有功能。