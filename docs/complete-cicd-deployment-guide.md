# 边墙鲜送系统 - 完整CI/CD部署流程指南

## 📋 目录
- [系统架构概览](#系统架构概览)
- [环境准备](#环境准备)
- [CI/CD流水线配置](#cicd流水线配置)
- [部署环境配置](#部署环境配置)
- [自动化部署流程](#自动化部署流程)
- [监控与运维](#监控与运维)
- [故障排查](#故障排查)
- [最佳实践](#最佳实践)

## 🏗️ 系统架构概览

### 技术栈
- **后端**: Spring Boot + MySQL + Redis
- **前端管理**: Vue.js + Element Plus
- **小程序**: 微信小程序原生开发
- **容器化**: Docker + Docker Compose
- **CI/CD**: Jenkins Pipeline
- **反向代理**: Nginx
- **监控**: 日志收集 + 健康检查

### 部署架构
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   开发环境      │    │   测试环境      │    │   生产环境      │
│   (本地)        │    │   (Staging)     │    │   (Production)  │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ • 代码开发      │    │ • 自动部署      │    │ • 手动确认部署  │
│ • 单元测试      │    │ • 集成测试      │    │ • 生产监控      │
│ • 代码提交      │    │ • 功能验证      │    │ • 性能优化      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Jenkins       │
                    │   CI/CD 服务器  │
                    └─────────────────┘
```

## 🛠️ 环境准备

### 1. 服务器环境要求

#### CI/CD服务器 (Jenkins)
- **操作系统**: Ubuntu 20.04 LTS 或 CentOS 8+
- **CPU**: 4核心以上
- **内存**: 8GB以上
- **存储**: 100GB以上 SSD
- **网络**: 稳定的互联网连接

#### 生产服务器
- **操作系统**: Ubuntu 20.04 LTS 或 CentOS 8+
- **CPU**: 8核心以上
- **内存**: 16GB以上
- **存储**: 200GB以上 SSD
- **网络**: 高带宽，低延迟

### 2. 基础软件安装

#### 在所有服务器上安装Docker
```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

#### Jenkins服务器额外安装
```bash
# 安装Jenkins
wget -q -O - https://pkg.jenkins.io/debian/jenkins.io.key | sudo apt-key add -
sudo sh -c 'echo deb http://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'
sudo apt update
sudo apt install jenkins

# 安装Node.js (用于前端构建)
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# 安装Maven (用于后端构建)
sudo apt install maven

# 安装Git
sudo apt install git
```

### 3. Jenkins配置

#### 必需插件安装
```
- Pipeline
- Git
- Docker Pipeline
- SSH Agent
- Email Extension
- SonarQube Scanner
- OWASP Dependency Check
- Publish Test Results
- Code Coverage API
```

#### 全局工具配置
1. **JDK**: OpenJDK 17
2. **Maven**: Apache Maven 3.8+
3. **Node.js**: Node.js 18+
4. **Docker**: Docker latest

#### 凭据配置
```
- docker-registry-credentials: Docker镜像仓库凭据
- deploy-ssh-key: 部署服务器SSH密钥
- dingtalk-webhook: 钉钉通知Webhook
- sonarqube-token: SonarQube访问令牌
```

## 🔄 CI/CD流水线配置

### 1. Jenkinsfile详解

我们的Jenkins流水线包含以下阶段：

#### 阶段1: 代码检出 (Checkout)
```groovy
stage('Checkout') {
    steps {
        git branch: "${GIT_BRANCH}", url: "${GIT_REPO}"
        script {
            env.BUILD_TIME = sh(returnStdout: true, script: 'date +"%Y-%m-%d %H:%M:%S"').trim()
            env.GIT_COMMIT_SHORT = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
        }
    }
}
```

#### 阶段2: 后端构建 (Build Backend)
```groovy
stage('Build Backend') {
    steps {
        dir('backend') {
            sh 'mvn clean compile'
            sh 'mvn test'
            sh 'mvn package -DskipTests'
        }
    }
    post {
        always {
            publishTestResults testResultsPattern: 'backend/target/surefire-reports/*.xml'
            publishCoverage adapters: [jacocoAdapter('backend/target/site/jacoco/jacoco.xml')]
        }
    }
}
```

#### 阶段3: 前端构建 (Build Frontend)
```groovy
stage('Build Frontend') {
    steps {
        dir('admin-frontend') {
            sh 'npm install --registry=https://registry.npmmirror.com'
            sh 'npm run lint'
            sh 'npm run build'
        }
    }
}
```

#### 阶段4: 代码质量分析
```groovy
stage('Code Quality Analysis') {
    parallel {
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    dir('backend') {
                        sh 'mvn sonar:sonar'
                    }
                }
            }
        }
        stage('Security Scan') {
            steps {
                dependencyCheck additionalArguments: '--format XML --format HTML', odcInstallation: 'OWASP-DC'
                dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
            }
        }
    }
}
```

#### 阶段5: Docker镜像构建
```groovy
stage('Build Docker Images') {
    steps {
        script {
            def backendImage = docker.build("${PROJECT_NAME}-backend:${BUILD_NUMBER}", "./backend")
            def frontendImage = docker.build("${PROJECT_NAME}-admin:${BUILD_NUMBER}", "./admin-frontend")
            
            docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-registry-credentials') {
                backendImage.push()
                backendImage.push('latest')
                frontendImage.push()
                frontendImage.push('latest')
            }
        }
    }
}
```

### 2. 环境变量配置

在Jenkins中配置以下环境变量：

```bash
# 项目配置
PROJECT_NAME=biangqiang-fresh-delivery
DOCKER_REGISTRY=your-docker-registry.com

# Git配置
GIT_REPO=https://github.com/your-username/biangqiang-fresh-delivery.git
GIT_BRANCH=main

# 部署配置
DEPLOY_SERVER=your-deploy-server.com
DEPLOY_USER=deploy

# 通知配置
DINGTALK_WEBHOOK=https://oapi.dingtalk.com/robot/send?access_token=xxx
EMAIL_RECIPIENTS=admin@biangqiang.com
```

## 🚀 部署环境配置

### 1. 测试环境 (Staging)

#### 目录结构
```
/opt/fresh-delivery/staging/
├── docker-compose.yml
├── .env
├── nginx/
│   ├── nginx.conf
│   ├── admin.conf
│   └── ssl/
├── mysql/
│   ├── conf/
│   └── init/
└── redis/
    └── redis.conf
```

#### 环境变量文件 (.env)
```bash
# 数据库配置
MYSQL_ROOT_PASSWORD=TestRoot2024!@#
MYSQL_DATABASE=fresh_delivery_test
MYSQL_USERNAME=fresh_test
MYSQL_PASSWORD=FreshTest2024!@#

# Redis配置
REDIS_PASSWORD=RedisTest2024!@#

# 应用配置
SPRING_PROFILES_ACTIVE=staging
JWT_SECRET=fresh-delivery-jwt-secret-test-2024
FILE_DOMAIN=https://staging.biangqiang.com

# 微信配置
WECHAT_APPID=your_test_wechat_appid
WECHAT_SECRET=your_test_wechat_secret

# JVM配置
JAVA_OPTS=-Xmx1g -Xms512m -XX:+UseG1GC
```

### 2. 生产环境 (Production)

#### 目录结构
```
/opt/fresh-delivery/production/
├── docker-compose.prod.yml
├── .env.prod
├── nginx/
│   ├── nginx.conf
│   ├── admin.conf
│   └── ssl/
├── mysql/
│   ├── conf/
│   └── init/
├── redis/
│   └── redis.conf
├── backups/
└── logs/
```

#### 生产环境变量文件 (.env.prod)
```bash
# 数据库配置
MYSQL_ROOT_PASSWORD=FreshDelivery2024!@#
MYSQL_DATABASE=fresh_delivery
MYSQL_USERNAME=fresh_user
MYSQL_PASSWORD=FreshUser2024!@#

# Redis配置
REDIS_PASSWORD=Redis2024!@#

# 应用配置
SPRING_PROFILES_ACTIVE=production
JWT_SECRET=fresh-delivery-jwt-secret-prod-2024
FILE_DOMAIN=https://api.biangqiang.com

# 微信配置
WECHAT_APPID=your_prod_wechat_appid
WECHAT_SECRET=your_prod_wechat_secret

# JVM配置
JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError
```

### 3. Nginx配置优化

#### 生产环境Nginx配置
```nginx
# /opt/fresh-delivery/production/nginx/nginx.conf
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
    use epoll;
    multi_accept on;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    # 日志格式
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';
    
    access_log /var/log/nginx/access.log main;
    
    # 性能优化
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    
    # Gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;
    
    # 上游服务器
    upstream backend {
        server backend:8080;
        keepalive 32;
    }
    
    upstream admin {
        server admin-frontend:80;
        keepalive 32;
    }
    
    # HTTPS重定向
    server {
        listen 80;
        server_name _;
        return 301 https://$server_name$request_uri;
    }
    
    # 主服务器配置
    server {
        listen 443 ssl http2;
        server_name _;
        
        # SSL配置
        ssl_certificate /etc/nginx/ssl/server.crt;
        ssl_certificate_key /etc/nginx/ssl/server.key;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
        ssl_prefer_server_ciphers off;
        
        # 安全头
        add_header X-Frame-Options DENY;
        add_header X-Content-Type-Options nosniff;
        add_header X-XSS-Protection "1; mode=block";
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";
        
        # API代理
        location /api/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # 超时设置
            proxy_connect_timeout 30s;
            proxy_send_timeout 30s;
            proxy_read_timeout 30s;
        }
        
        # 文件上传代理
        location /uploads/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # 大文件上传
            client_max_body_size 100M;
        }
        
        # 管理后台
        location / {
            proxy_pass http://admin;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
        
        # 静态文件缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            proxy_pass http://admin;
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
}
```

## 🔄 自动化部署流程

### 1. 部署脚本增强

创建增强版部署脚本 `deploy-enhanced.sh`：

```bash
#!/bin/bash

# 边墙鲜送系统增强版部署脚本
# 支持多环境、健康检查、回滚等功能

set -e

# 配置
PROJECT_NAME="fresh-delivery"
ENVIRONMENT=${1:-staging}  # staging 或 production
ACTION=${2:-deploy}        # deploy, rollback, status

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 环境配置
if [ "$ENVIRONMENT" = "production" ]; then
    COMPOSE_FILE="docker-compose.prod.yml"
    ENV_FILE=".env.prod"
    DEPLOY_DIR="/opt/fresh-delivery/production"
else
    COMPOSE_FILE="docker-compose.yml"
    ENV_FILE=".env"
    DEPLOY_DIR="/opt/fresh-delivery/staging"
fi

# 健康检查
health_check() {
    local service=$1
    local max_attempts=30
    local attempt=1
    
    log_info "检查 $service 服务健康状态..."
    
    while [ $attempt -le $max_attempts ]; do
        if docker-compose -f "$DEPLOY_DIR/$COMPOSE_FILE" ps | grep -q "$service.*healthy\|$service.*Up"; then
            log_success "$service 服务健康检查通过"
            return 0
        fi
        
        log_info "等待 $service 服务启动... ($attempt/$max_attempts)"
        sleep 10
        ((attempt++))
    done
    
    log_error "$service 服务健康检查失败"
    return 1
}

# 部署函数
deploy() {
    log_info "开始部署到 $ENVIRONMENT 环境..."
    
    # 创建部署目录
    mkdir -p "$DEPLOY_DIR"
    cd "$DEPLOY_DIR"
    
    # 备份当前版本
    if [ -f "$COMPOSE_FILE" ]; then
        log_info "备份当前版本..."
        cp "$COMPOSE_FILE" "$COMPOSE_FILE.backup.$(date +%Y%m%d_%H%M%S)"
    fi
    
    # 拉取最新镜像
    log_info "拉取最新Docker镜像..."
    docker-compose -f "$COMPOSE_FILE" pull
    
    # 停止旧服务
    log_info "停止旧服务..."
    docker-compose -f "$COMPOSE_FILE" down
    
    # 启动新服务
    log_info "启动新服务..."
    docker-compose -f "$COMPOSE_FILE" up -d
    
    # 健康检查
    health_check "mysql" && \
    health_check "redis" && \
    health_check "backend" && \
    health_check "admin-frontend"
    
    if [ $? -eq 0 ]; then
        log_success "$ENVIRONMENT 环境部署成功！"
        
        # 清理旧镜像
        log_info "清理旧镜像..."
        docker system prune -f
        
        # 发送通知
        send_notification "success" "$ENVIRONMENT 环境部署成功"
    else
        log_error "$ENVIRONMENT 环境部署失败！"
        
        # 自动回滚
        log_info "开始自动回滚..."
        rollback
        
        # 发送通知
        send_notification "failure" "$ENVIRONMENT 环境部署失败，已自动回滚"
        exit 1
    fi
}

# 回滚函数
rollback() {
    log_info "开始回滚 $ENVIRONMENT 环境..."
    
    cd "$DEPLOY_DIR"
    
    # 查找最新备份
    BACKUP_FILE=$(ls -t "$COMPOSE_FILE.backup."* 2>/dev/null | head -n1)
    
    if [ -n "$BACKUP_FILE" ]; then
        log_info "使用备份文件: $BACKUP_FILE"
        
        # 停止当前服务
        docker-compose -f "$COMPOSE_FILE" down
        
        # 恢复备份
        cp "$BACKUP_FILE" "$COMPOSE_FILE"
        
        # 启动服务
        docker-compose -f "$COMPOSE_FILE" up -d
        
        # 健康检查
        if health_check "backend"; then
            log_success "回滚成功！"
        else
            log_error "回滚失败！"
            exit 1
        fi
    else
        log_error "未找到备份文件，无法回滚！"
        exit 1
    fi
}

# 状态检查
status() {
    log_info "检查 $ENVIRONMENT 环境状态..."
    
    cd "$DEPLOY_DIR"
    docker-compose -f "$COMPOSE_FILE" ps
    
    # 检查各服务状态
    services=("mysql" "redis" "backend" "admin-frontend")
    
    for service in "${services[@]}"; do
        if docker-compose -f "$COMPOSE_FILE" ps | grep -q "$service.*Up"; then
            log_success "$service 服务运行正常"
        else
            log_error "$service 服务异常"
        fi
    done
}

# 发送通知
send_notification() {
    local status=$1
    local message=$2
    local emoji
    
    if [ "$status" = "success" ]; then
        emoji="✅"
    else
        emoji="❌"
    fi
    
    # 这里可以集成钉钉、企业微信等通知
    log_info "$emoji $message"
}

# 主函数
main() {
    case "$ACTION" in
        "deploy")
            deploy
            ;;
        "rollback")
            rollback
            ;;
        "status")
            status
            ;;
        *)
            echo "使用方法: $0 [staging|production] [deploy|rollback|status]"
            exit 1
            ;;
    esac
}

# 执行主函数
main
```

### 2. 自动化部署触发

#### Git Hooks配置

在Git仓库中配置post-receive hook：

```bash
#!/bin/bash
# post-receive hook

while read oldrev newrev refname; do
    branch=$(git rev-parse --symbolic --abbrev-ref $refname)
    
    if [ "$branch" = "main" ]; then
        echo "触发生产环境部署..."
        curl -X POST "http://jenkins-server:8080/job/fresh-delivery-production/build" \
             --user "jenkins-user:jenkins-token"
    elif [ "$branch" = "develop" ]; then
        echo "触发测试环境部署..."
        curl -X POST "http://jenkins-server:8080/job/fresh-delivery-staging/build" \
             --user "jenkins-user:jenkins-token"
    fi
done
```

#### Webhook配置

在GitHub/GitLab中配置Webhook，指向Jenkins：

```
URL: http://jenkins-server:8080/github-webhook/
Content type: application/json
Events: Push events, Pull request events
```

## 📊 监控与运维

### 1. 日志收集配置

#### Docker日志配置
```yaml
# docker-compose.prod.yml 中的日志配置
logging:
  driver: "json-file"
  options:
    max-size: "50m"
    max-file: "5"
```

#### 集中日志收集
```bash
# 安装ELK Stack (可选)
docker run -d --name elasticsearch \
  -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  elasticsearch:7.14.0

docker run -d --name kibana \
  -p 5601:5601 \
  --link elasticsearch:elasticsearch \
  kibana:7.14.0

docker run -d --name logstash \
  -p 5044:5044 \
  --link elasticsearch:elasticsearch \
  logstash:7.14.0
```

### 2. 健康检查和监控

#### 应用健康检查端点

在Spring Boot应用中添加健康检查：

```java
@RestController
@RequestMapping("/api")
public class HealthController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 检查数据库连接
            dataSource.getConnection().close();
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("database_error", e.getMessage());
        }
        
        try {
            // 检查Redis连接
            redisTemplate.opsForValue().get("health_check");
            health.put("redis", "UP");
        } catch (Exception e) {
            health.put("redis", "DOWN");
            health.put("redis_error", e.getMessage());
        }
        
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
}
```

#### 监控脚本

```bash
#!/bin/bash
# monitor.sh - 系统监控脚本

MONITOR_URL="https://your-domain.com/api/health"
ALERT_EMAIL="admin@biangqiang.com"
LOG_FILE="/var/log/fresh-delivery-monitor.log"

check_service() {
    local response=$(curl -s -o /dev/null -w "%{http_code}" "$MONITOR_URL")
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    if [ "$response" = "200" ]; then
        echo "[$timestamp] 服务正常 - HTTP $response" >> "$LOG_FILE"
        return 0
    else
        echo "[$timestamp] 服务异常 - HTTP $response" >> "$LOG_FILE"
        
        # 发送告警邮件
        echo "边墙鲜送系统服务异常，HTTP状态码: $response" | \
        mail -s "[告警] 边墙鲜送系统服务异常" "$ALERT_EMAIL"
        
        return 1
    fi
}

# 执行检查
check_service
```

#### Crontab配置
```bash
# 每分钟检查一次服务状态
* * * * * /opt/fresh-delivery/scripts/monitor.sh

# 每天凌晨2点备份数据库
0 2 * * * /opt/fresh-delivery/scripts/backup.sh

# 每周日凌晨3点清理旧日志
0 3 * * 0 /opt/fresh-delivery/scripts/cleanup.sh
```

### 3. 性能监控

#### 系统资源监控
```bash
#!/bin/bash
# performance-monitor.sh

# CPU使用率
CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | awk -F'%' '{print $1}')

# 内存使用率
MEM_USAGE=$(free | grep Mem | awk '{printf("%.2f", $3/$2 * 100.0)}')

# 磁盘使用率
DISK_USAGE=$(df -h / | awk 'NR==2{printf "%s", $5}')

# Docker容器状态
CONTAINER_COUNT=$(docker ps | wc -l)

echo "系统性能报告 - $(date)"
echo "CPU使用率: ${CPU_USAGE}%"
echo "内存使用率: ${MEM_USAGE}%"
echo "磁盘使用率: ${DISK_USAGE}"
echo "运行容器数: $((CONTAINER_COUNT-1))"

# 如果资源使用率过高，发送告警
if (( $(echo "$CPU_USAGE > 80" | bc -l) )) || (( $(echo "$MEM_USAGE > 80" | bc -l) )); then
    echo "资源使用率过高，发送告警..." | mail -s "[告警] 系统资源使用率过高" admin@biangqiang.com
fi
```

## 🔧 故障排查

### 1. 常见问题及解决方案

#### 问题1: 容器启动失败
```bash
# 查看容器日志
docker-compose logs [service-name]

# 查看容器状态
docker-compose ps

# 进入容器调试
docker-compose exec [service-name] /bin/bash
```

#### 问题2: 数据库连接失败
```bash
# 检查数据库容器状态
docker-compose exec mysql mysql -u root -p -e "SHOW DATABASES;"

# 检查网络连接
docker network ls
docker network inspect [network-name]
```

#### 问题3: 内存不足
```bash
# 查看内存使用情况
docker stats

# 清理无用镜像和容器
docker system prune -a

# 调整JVM内存参数
export JAVA_OPTS="-Xmx1g -Xms512m"
```

### 2. 故障恢复流程

#### 自动故障恢复脚本
```bash
#!/bin/bash
# auto-recovery.sh

SERVICE_URL="https://your-domain.com/api/health"
MAX_RETRIES=3
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -f "$SERVICE_URL" > /dev/null 2>&1; then
        echo "服务正常"
        exit 0
    else
        echo "服务异常，尝试重启... (第 $((RETRY_COUNT+1)) 次)"
        
        # 重启服务
        cd /opt/fresh-delivery/production
        docker-compose restart backend
        
        # 等待服务启动
        sleep 30
        
        ((RETRY_COUNT++))
    fi
done

echo "自动恢复失败，需要人工介入"
exit 1
```

## 📋 最佳实践

### 1. 代码质量保证

#### Git工作流
```
main (生产分支)
├── release/v1.0 (发布分支)
├── develop (开发分支)
│   ├── feature/user-management
│   ├── feature/order-system
│   └── feature/ai-chat
└── hotfix/critical-bug (紧急修复)
```

#### 代码审查清单
- [ ] 代码符合团队编码规范
- [ ] 单元测试覆盖率 > 80%
- [ ] 没有硬编码的敏感信息
- [ ] 错误处理完善
- [ ] 日志记录适当
- [ ] 性能影响评估
- [ ] 安全漏洞检查

### 2. 部署安全

#### 敏感信息管理
```bash
# 使用环境变量存储敏感信息
export MYSQL_PASSWORD=$(cat /etc/secrets/mysql_password)
export JWT_SECRET=$(cat /etc/secrets/jwt_secret)

# 或使用Docker secrets
docker secret create mysql_password /path/to/mysql_password.txt
```

#### 网络安全
```yaml
# docker-compose.yml 网络配置
networks:
  fresh-delivery-network:
    driver: bridge
    internal: true  # 内部网络，不允许外部访问
  
  public-network:
    driver: bridge
```

### 3. 性能优化

#### 数据库优化
```sql
-- 添加索引
CREATE INDEX idx_user_phone ON users(phone);
CREATE INDEX idx_order_status ON orders(status, created_at);
CREATE INDEX idx_product_category ON products(category_id, status);

-- 分区表（大数据量时）
CREATE TABLE order_logs (
    id BIGINT AUTO_INCREMENT,
    order_id BIGINT,
    action VARCHAR(50),
    created_at TIMESTAMP
) PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026)
);
```

#### 缓存策略
```java
@Service
public class ProductService {
    
    @Cacheable(value = "products", key = "#categoryId")
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    @CacheEvict(value = "products", allEntries = true)
    public void clearProductCache() {
        // 清除所有产品缓存
    }
}
```

### 4. 监控告警

#### 关键指标监控
- **系统指标**: CPU、内存、磁盘、网络
- **应用指标**: 响应时间、错误率、吞吐量
- **业务指标**: 订单量、用户活跃度、支付成功率

#### 告警规则
```yaml
# 告警规则示例
alerts:
  - name: high_cpu_usage
    condition: cpu_usage > 80%
    duration: 5m
    action: send_email
    
  - name: high_error_rate
    condition: error_rate > 5%
    duration: 2m
    action: send_sms
    
  - name: database_connection_failed
    condition: db_connection_failed
    duration: 1m
    action: auto_restart
```

## 📞 联系支持

如果在部署过程中遇到问题，请联系：

- **技术支持**: tech-support@biangqiang.com
- **运维团队**: ops@biangqiang.com
- **紧急联系**: +86-xxx-xxxx-xxxx

---

**文档版本**: v1.0  
**最后更新**: 2024年12月  
**维护团队**: 边墙鲜送技术团队