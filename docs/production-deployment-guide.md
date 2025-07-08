# 边墙鲜送系统生产环境部署指南

## 🚀 服务器信息
- **公网IP**: 8.154.40.188
- **操作系统**: Linux (推荐 Ubuntu 20.04+ 或 CentOS 8+)
- **架构**: Docker 容器化部署
- **域名**: 可选配置

## 📋 部署前准备

### 1. 服务器要求
- **CPU**: 4核心以上
- **内存**: 8GB以上
- **存储**: 100GB以上可用空间
- **网络**: 稳定的互联网连接

### 2. 软件依赖
```bash
# 安装 Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

### 3. 防火墙配置
```bash
# Ubuntu/Debian
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 3306/tcp  # MySQL (可选，仅内网)
sudo ufw allow 6379/tcp  # Redis (可选，仅内网)
sudo ufw enable

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=22/tcp
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --permanent --add-port=3306/tcp
sudo firewall-cmd --permanent --add-port=6379/tcp
sudo firewall-cmd --reload
```

## 🛠️ 部署步骤

### 方法一：自动化部署（推荐）

#### Linux 系统
```bash
# 1. 克隆项目
git clone https://github.com/15929028102zh/bqxs.git
cd bqxs

# 2. 给脚本执行权限
chmod +x docker-deploy.sh

# 3. 运行部署脚本
./docker-deploy.sh
```

#### Windows 系统
```cmd
REM 1. 克隆项目
git clone https://github.com/15929028102zh/bqxs.git
cd bqxs

REM 2. 以管理员身份运行部署脚本
docker-deploy.bat
```

### 方法二：手动部署

#### 1. 环境配置
```bash
# 创建环境变量文件
cp docker/.env.example docker/.env

# 编辑配置文件
nano docker/.env
```

#### 2. 关键配置项
```bash
# 服务器配置
SERVER_IP=8.154.40.188
SERVER_NAME=8.154.40.188
FILE_DOMAIN=https://8.154.40.188

# 数据库配置（请修改默认密码）
MYSQL_ROOT_PASSWORD=your_strong_root_password
MYSQL_PASSWORD=your_strong_db_password

# Redis配置
REDIS_PASSWORD=your_redis_password

# JWT密钥
JWT_SECRET=your_jwt_secret_key

# 微信小程序配置
WECHAT_APPID=your_wechat_appid
WECHAT_SECRET=your_wechat_secret
```

#### 3. SSL证书配置
```bash
# 生成自签名证书（测试用）
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout docker/nginx/ssl/server.key \
    -out docker/nginx/ssl/server.crt \
    -subj "/C=CN/ST=Beijing/L=Beijing/O=FreshDelivery/OU=IT/CN=8.154.40.188"

# 或者使用 Let's Encrypt（推荐）
# 安装 certbot
sudo apt-get install certbot

# 获取证书
sudo certbot certonly --standalone -d 8.154.40.188

# 复制证书到项目目录
sudo cp /etc/letsencrypt/live/8.154.40.188/fullchain.pem docker/nginx/ssl/server.crt
sudo cp /etc/letsencrypt/live/8.154.40.188/privkey.pem docker/nginx/ssl/server.key
```

#### 4. 启动服务
```bash
# 进入docker目录
cd docker

# 使用生产环境配置启动
docker-compose -f docker-compose.prod.yml up -d

# 或使用标准配置
docker-compose up -d
```

## 📊 服务验证

### 1. 检查容器状态
```bash
# 查看所有容器状态
docker-compose ps

# 查看容器日志
docker-compose logs -f backend
docker-compose logs -f mysql
docker-compose logs -f redis
docker-compose logs -f nginx
```

### 2. 健康检查
```bash
# 检查服务可访问性
curl -k https://8.154.40.188/health
curl -k https://8.154.40.188/api/health

# 检查数据库连接
docker exec fresh-delivery-mysql mysql -u root -p -e "SHOW DATABASES;"

# 检查Redis连接
docker exec fresh-delivery-redis redis-cli ping
```

### 3. 访问地址
- **管理后台**: https://8.154.40.188
- **API接口**: https://8.154.40.188/api
- **API文档**: https://8.154.40.188/api/doc.html
- **监控面板**: https://8.154.40.188:3001 (如果启用)

## 🔧 常用管理命令

### 服务管理
```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f [service_name]

# 进入容器
docker exec -it fresh-delivery-backend bash
```

### 数据库管理
```bash
# 备份数据库
docker exec fresh-delivery-mysql mysqldump -u root -p fresh_delivery > backup_$(date +%Y%m%d).sql

# 恢复数据库
docker exec -i fresh-delivery-mysql mysql -u root -p fresh_delivery < backup.sql

# 连接数据库
docker exec -it fresh-delivery-mysql mysql -u root -p
```

### 更新部署
```bash
# 拉取最新代码
git pull origin main

# 重新构建镜像
docker-compose build --no-cache

# 重启服务
docker-compose up -d
```

## 🛡️ 安全配置

### 1. 修改默认密码
```bash
# 修改数据库密码
docker exec -it fresh-delivery-mysql mysql -u root -p
ALTER USER 'root'@'%' IDENTIFIED BY 'new_strong_password';
ALTER USER 'fresh_user'@'%' IDENTIFIED BY 'new_user_password';
FLUSH PRIVILEGES;

# 修改Redis密码
# 编辑 docker/redis/redis.conf
requirepass new_redis_password
```

### 2. 配置SSL证书
```bash
# 使用 Let's Encrypt 自动续期
echo "0 12 * * * /usr/bin/certbot renew --quiet" | sudo crontab -
```

### 3. 设置备份策略
```bash
# 创建备份脚本
cat > /opt/backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
docker exec fresh-delivery-mysql mysqldump -u root -p$MYSQL_ROOT_PASSWORD fresh_delivery > $BACKUP_DIR/mysql_$DATE.sql

# 备份上传文件
tar -czf $BACKUP_DIR/uploads_$DATE.tar.gz -C /var/lib/docker/volumes/docker_backend_uploads/_data .

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete
EOF

# 设置执行权限
chmod +x /opt/backup.sh

# 添加到定时任务（每天凌晨2点执行）
echo "0 2 * * * /opt/backup.sh" | sudo crontab -
```

## 🔍 故障排除

### 常见问题

#### 1. 容器启动失败
```bash
# 查看详细错误信息
docker-compose logs [service_name]

# 检查端口占用
netstat -tulpn | grep :80
netstat -tulpn | grep :443
netstat -tulpn | grep :3306

# 检查磁盘空间
df -h
```

#### 2. 数据库连接失败
```bash
# 检查MySQL容器状态
docker exec fresh-delivery-mysql mysqladmin -u root -p ping

# 检查网络连接
docker network ls
docker network inspect docker_fresh-delivery-network
```

#### 3. 前端无法访问
```bash
# 检查Nginx配置
docker exec fresh-delivery-nginx nginx -t

# 重新加载Nginx配置
docker exec fresh-delivery-nginx nginx -s reload
```

#### 4. SSL证书问题
```bash
# 检查证书有效性
openssl x509 -in docker/nginx/ssl/server.crt -text -noout

# 检查证书和私钥匹配
openssl x509 -noout -modulus -in docker/nginx/ssl/server.crt | openssl md5
openssl rsa -noout -modulus -in docker/nginx/ssl/server.key | openssl md5
```

### 性能优化

#### 1. 数据库优化
```sql
-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- 分析表
ANALYZE TABLE table_name;

-- 优化表
OPTIMIZE TABLE table_name;
```

#### 2. Redis优化
```bash
# 查看Redis信息
docker exec fresh-delivery-redis redis-cli info memory
docker exec fresh-delivery-redis redis-cli info stats

# 清理过期键
docker exec fresh-delivery-redis redis-cli --scan --pattern "*" | xargs docker exec fresh-delivery-redis redis-cli del
```

#### 3. Nginx优化
```bash
# 查看Nginx状态
curl https://8.154.40.188/nginx_status

# 分析访问日志
tail -f /var/log/nginx/access.log | grep -E "(404|500|502|503|504)"
```

## 📈 监控和日志

### 1. 启用监控服务
```bash
# 启动监控服务
docker-compose --profile monitoring up -d

# 访问监控面板
# Prometheus: http://8.154.40.188:9090
# Grafana: http://8.154.40.188:3001
```

### 2. 日志管理
```bash
# 查看应用日志
docker-compose logs -f --tail=100 backend

# 查看系统日志
sudo journalctl -u docker -f

# 清理Docker日志
docker system prune -a
```

### 3. 性能监控
```bash
# 系统资源监控
top
htop
iotop

# Docker资源监控
docker stats
```

## 📞 技术支持

如果在部署过程中遇到问题，请：

1. 查看部署日志：`tail -f deploy.log`
2. 检查容器状态：`docker-compose ps`
3. 查看服务日志：`docker-compose logs [service_name]`
4. 联系技术支持团队

## 🔄 版本更新

### 更新流程
```bash
# 1. 备份数据
./backup.sh

# 2. 拉取最新代码
git pull origin main

# 3. 更新服务
docker-compose down
docker-compose build --no-cache
docker-compose up -d

# 4. 验证服务
curl -k https://8.154.40.188/health
```

### 回滚操作
```bash
# 如果更新失败，可以回滚到之前版本
git checkout [previous_commit_hash]
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

---

**部署完成后，您的边墙鲜送系统将在 https://8.154.40.188 上运行！**