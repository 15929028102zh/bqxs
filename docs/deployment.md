# 边墙鲜送项目部署文档

## 环境要求

### 服务器配置
- **操作系统**: CentOS 7+ / Ubuntu 18.04+
- **CPU**: 4核心以上
- **内存**: 8GB以上
- **存储**: 100GB以上
- **网络**: 公网IP，带宽10Mbps以上

### 软件依赖
- Docker 20.10+
- Docker Compose 1.29+
- Git 2.0+
- Jenkins 2.400+
- Nginx 1.18+

## 部署架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   用户/小程序    │────│   Nginx代理     │────│   后端服务       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                        │
                       ┌─────────────────┐    ┌─────────────────┐
                       │   前端管理系统   │    │   MySQL数据库   │
                       └─────────────────┘    └─────────────────┘
                                                        │
                                              ┌─────────────────┐
                                              │   Redis缓存     │
                                              └─────────────────┘
```

## 快速部署

### 1. 环境准备

```bash
# 安装Docker
curl -fsSL https://get.docker.com | bash -s docker
sudo systemctl start docker
sudo systemctl enable docker

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

### 2. 克隆项目

```bash
git clone https://github.com/your-username/biangqiang-fresh-delivery.git
cd biangqiang-fresh-delivery
```

### 3. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑环境变量
vim .env
```

环境变量配置示例：
```bash
# 数据库配置
MYSQL_ROOT_PASSWORD=your_mysql_root_password
MYSQL_DATABASE=fresh_delivery
MYSQL_USER=fresh_user
MYSQL_PASSWORD=your_mysql_password

# Redis配置
REDIS_PASSWORD=your_redis_password

# 应用配置
JWT_SECRET=your_jwt_secret_key
WECHAT_APPID=your_wechat_appid
WECHAT_SECRET=your_wechat_secret

# 文件存储配置
FILE_UPLOAD_PATH=/opt/fresh-delivery/uploads
FILE_DOMAIN=https://your-domain.com

# 邮件配置
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_email_password
```

### 4. 启动服务

```bash
# 进入docker目录
cd docker

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 5. 初始化数据库

```bash
# 等待MySQL启动完成
sleep 30

# 执行数据库初始化脚本
docker-compose exec mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} fresh_delivery < /docker-entrypoint-initdb.d/init.sql
```

### 6. 验证部署

```bash
# 检查后端服务
curl http://localhost:8080/api/health

# 检查前端服务
curl http://localhost:80

# 检查数据库连接
docker-compose exec backend java -jar app.jar --spring.profiles.active=docker --spring.datasource.url=jdbc:mysql://mysql:3306/fresh_delivery
```

## 生产环境部署

### 1. 域名和SSL证书配置

```bash
# 申请SSL证书（使用Let's Encrypt）
sudo apt install certbot
sudo certbot certonly --standalone -d your-domain.com -d api.your-domain.com

# 配置Nginx SSL
cp docker/nginx/ssl.conf.example docker/nginx/ssl.conf
vim docker/nginx/ssl.conf
```

### 2. 生产环境配置

```bash
# 使用生产环境配置
cp docker-compose.prod.yml docker-compose.yml

# 配置生产环境变量
cp .env.prod.example .env.prod
vim .env.prod
```

### 3. 启动生产服务

```bash
# 使用生产环境配置启动
docker-compose --env-file .env.prod up -d

# 配置防火墙
sudo ufw allow 80
sudo ufw allow 443
sudo ufw allow 22
sudo ufw enable
```

## Jenkins持续集成部署

### 1. Jenkins安装配置

```bash
# 安装Jenkins
wget -q -O - https://pkg.jenkins.io/debian/jenkins.io.key | sudo apt-key add -
sudo sh -c 'echo deb http://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'
sudo apt update
sudo apt install jenkins

# 启动Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins

# 获取初始密码
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

### 2. Jenkins插件安装

必需插件列表：
- Git Plugin
- Docker Plugin
- Docker Compose Build Step Plugin
- Pipeline Plugin
- Blue Ocean Plugin
- SonarQube Scanner Plugin
- Email Extension Plugin
- DingTalk Plugin

### 3. 创建Jenkins Pipeline

1. 新建Pipeline项目
2. 配置Git仓库地址
3. 指定Jenkinsfile路径：`jenkins/Jenkinsfile`
4. 配置Webhook触发器

### 4. 配置部署密钥

```bash
# 生成SSH密钥
ssh-keygen -t rsa -b 4096 -C "jenkins@your-domain.com"

# 将公钥添加到部署服务器
ssh-copy-id deploy@your-deploy-server.com

# 在Jenkins中添加私钥凭据
```

## 监控和维护

### 1. 日志管理

```bash
# 查看应用日志
docker-compose logs -f backend
docker-compose logs -f admin-frontend

# 日志轮转配置
vim /etc/logrotate.d/fresh-delivery
```

### 2. 数据备份

```bash
#!/bin/bash
# 数据库备份脚本
BACKUP_DIR="/opt/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# 备份数据库
docker-compose exec mysql mysqldump -uroot -p$MYSQL_ROOT_PASSWORD fresh_delivery > $BACKUP_DIR/fresh_delivery_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/fresh_delivery_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
```

### 3. 性能监控

```bash
# 安装监控工具
docker run -d --name=prometheus -p 9090:9090 prom/prometheus
docker run -d --name=grafana -p 3000:3000 grafana/grafana

# 配置应用监控指标
# 在application.yml中添加actuator配置
```

### 4. 健康检查

```bash
#!/bin/bash
# 健康检查脚本
HEALTH_URL="http://localhost:8080/api/health"

if curl -f $HEALTH_URL > /dev/null 2>&1; then
    echo "应用运行正常"
else
    echo "应用异常，尝试重启..."
    docker-compose restart backend
    
    # 发送告警通知
    curl -X POST $DINGTALK_WEBHOOK \
        -H 'Content-Type: application/json' \
        -d '{"msgtype": "text", "text": {"content": "边墙鲜送应用异常，已自动重启"}}'
fi
```

## 故障排查

### 常见问题

1. **服务启动失败**
   ```bash
   # 查看详细错误日志
   docker-compose logs backend
   
   # 检查端口占用
   netstat -tlnp | grep :8080
   ```

2. **数据库连接失败**
   ```bash
   # 检查MySQL服务状态
   docker-compose exec mysql mysql -uroot -p
   
   # 检查网络连接
   docker-compose exec backend ping mysql
   ```

3. **Redis连接失败**
   ```bash
   # 检查Redis服务
   docker-compose exec redis redis-cli ping
   
   # 检查Redis配置
   docker-compose exec redis cat /etc/redis/redis.conf
   ```

4. **文件上传失败**
   ```bash
   # 检查上传目录权限
   ls -la /opt/fresh-delivery/uploads
   
   # 修改目录权限
   sudo chown -R 1000:1000 /opt/fresh-delivery/uploads
   ```

### 性能优化

1. **数据库优化**
   - 配置合适的连接池大小
   - 添加必要的索引
   - 定期清理过期数据

2. **缓存优化**
   - 合理设置Redis过期时间
   - 使用Redis集群提高可用性
   - 监控缓存命中率

3. **应用优化**
   - 配置JVM参数
   - 启用Gzip压缩
   - 使用CDN加速静态资源

## 安全配置

### 1. 网络安全

```bash
# 配置防火墙规则
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 80
sudo ufw allow 443
sudo ufw enable

# 禁用不必要的服务
sudo systemctl disable telnet
sudo systemctl disable ftp
```

### 2. 应用安全

- 定期更新依赖包版本
- 使用HTTPS加密传输
- 实施API访问限流
- 配置SQL注入防护
- 启用CSRF保护

### 3. 数据安全

- 定期备份数据
- 加密敏感数据
- 配置访问权限控制
- 审计日志记录

## 联系方式

如有部署问题，请联系：
- 邮箱：admin@biangqiang.com
- 电话：400-xxx-xxxx
- 技术支持QQ群：xxxxxxxxx