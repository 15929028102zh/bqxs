#!/bin/bash

# 边墙鲜送系统 Docker 部署脚本
# 服务器公网IP: 8.154.40.188
# 使用方法: chmod +x docker-deploy.sh && ./docker-deploy.sh

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
SERVER_IP="8.154.40.188"
PROJECT_NAME="fresh-delivery"
DOCKER_DIR="./docker"
BACKUP_DIR="./backups"
LOG_FILE="./deploy.log"

# 日志函数
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}" | tee -a $LOG_FILE
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}" | tee -a $LOG_FILE
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}" | tee -a $LOG_FILE
    exit 1
}

info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO: $1${NC}" | tee -a $LOG_FILE
}

# 检查系统要求
check_requirements() {
    log "检查系统要求..."
    
    # 检查 Docker
    if ! command -v docker &> /dev/null; then
        error "Docker 未安装，请先安装 Docker"
    fi
    
    # 检查 Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose 未安装，请先安装 Docker Compose"
    fi
    
    # 检查端口占用
    check_port 80 "HTTP"
    check_port 443 "HTTPS"
    check_port 3306 "MySQL"
    check_port 6379 "Redis"
    check_port 8080 "Backend API"
    
    log "系统要求检查完成"
}

# 检查端口占用
check_port() {
    local port=$1
    local service=$2
    
    if netstat -tuln | grep -q ":$port "; then
        warn "端口 $port ($service) 已被占用，可能会导致冲突"
    else
        info "端口 $port ($service) 可用"
    fi
}

# 创建必要的目录
create_directories() {
    log "创建必要的目录..."
    
    mkdir -p $BACKUP_DIR
    mkdir -p $DOCKER_DIR/mysql/init
    mkdir -p $DOCKER_DIR/mysql/conf
    mkdir -p $DOCKER_DIR/nginx/ssl
    mkdir -p $DOCKER_DIR/redis
    mkdir -p logs
    
    log "目录创建完成"
}

# 生成环境变量文件
generate_env_file() {
    log "生成环境变量文件..."
    
    cat > $DOCKER_DIR/.env << EOF
# 边墙鲜送系统生产环境配置
# 生成时间: $(date)
# 服务器IP: $SERVER_IP

# ===========================================
# 服务器配置
# ===========================================
SERVER_IP=$SERVER_IP
SERVER_NAME=$SERVER_IP
FILE_DOMAIN=https://$SERVER_IP

# ===========================================
# 数据库配置
# ===========================================
MYSQL_ROOT_PASSWORD=FreshDelivery2024!@#
MYSQL_DATABASE=fresh_delivery
MYSQL_USERNAME=fresh_user
MYSQL_PASSWORD=FreshUser2024!@#
MYSQL_HOST=mysql
MYSQL_PORT=3306

# ===========================================
# Redis 配置
# ===========================================
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=Redis2024!@#

# ===========================================
# 后端应用配置
# ===========================================
JWT_SECRET=fresh-delivery-jwt-secret-$(openssl rand -hex 16)
SPRING_PROFILES_ACTIVE=docker

# ===========================================
# 微信小程序配置（请替换为实际值）
# ===========================================
WECHAT_APPID=your_wechat_miniprogram_appid
WECHAT_SECRET=your_wechat_miniprogram_secret

# ===========================================
# SSL 证书配置
# ===========================================
SSL_CERTIFICATE=server.crt
SSL_CERTIFICATE_KEY=server.key

# ===========================================
# 性能配置
# ===========================================
JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC
DB_POOL_MIN_SIZE=10
DB_POOL_MAX_SIZE=50
REDIS_POOL_MAX_ACTIVE=16
REDIS_POOL_MAX_IDLE=8
REDIS_POOL_MIN_IDLE=2

# ===========================================
# 安全配置
# ===========================================
HTTPS_REDIRECT=true
CORS_ALLOWED_ORIGINS=https://$SERVER_IP,https://www.$SERVER_IP
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=*
CORS_ALLOW_CREDENTIALS=true

# ===========================================
# 日志配置
# ===========================================
LOG_LEVEL=INFO
LOG_RETENTION_DAYS=30
SQL_LOG_ENABLED=false

# ===========================================
# 监控配置
# ===========================================
MONITORING_ENABLED=true
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
GRAFANA_ADMIN_PASSWORD=Admin2024!@#

# ===========================================
# 备份配置
# ===========================================
BACKUP_INTERVAL=24
BACKUP_RETENTION_DAYS=7
BACKUP_PATH=/app/backups
EOF

    log "环境变量文件生成完成"
}

# 生成自签名SSL证书
generate_ssl_cert() {
    log "生成SSL证书..."
    
    if [ ! -f "$DOCKER_DIR/nginx/ssl/server.crt" ]; then
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout $DOCKER_DIR/nginx/ssl/server.key \
            -out $DOCKER_DIR/nginx/ssl/server.crt \
            -subj "/C=CN/ST=Beijing/L=Beijing/O=FreshDelivery/OU=IT/CN=$SERVER_IP"
        
        chmod 600 $DOCKER_DIR/nginx/ssl/server.key
        chmod 644 $DOCKER_DIR/nginx/ssl/server.crt
        
        log "SSL证书生成完成"
    else
        info "SSL证书已存在，跳过生成"
    fi
}

# 更新Nginx配置
update_nginx_config() {
    log "更新Nginx配置..."
    
    # 替换server_name为实际IP
    sed -i "s/server_name localhost;/server_name $SERVER_IP;/g" $DOCKER_DIR/nginx/nginx.conf
    
    log "Nginx配置更新完成"
}

# 数据库备份
backup_database() {
    log "备份数据库..."
    
    if docker ps | grep -q "fresh-delivery-mysql"; then
        BACKUP_FILE="$BACKUP_DIR/mysql_backup_$(date +%Y%m%d_%H%M%S).sql"
        docker exec fresh-delivery-mysql mysqldump -u root -pFreshDelivery2024!@# fresh_delivery > $BACKUP_FILE
        
        if [ $? -eq 0 ]; then
            log "数据库备份完成: $BACKUP_FILE"
        else
            warn "数据库备份失败"
        fi
    else
        info "MySQL容器未运行，跳过备份"
    fi
}

# 构建和启动服务
deploy_services() {
    log "开始部署服务..."
    
    cd $DOCKER_DIR
    
    # 停止现有服务
    log "停止现有服务..."
    docker-compose down --remove-orphans
    
    # 清理未使用的镜像和容器
    log "清理Docker资源..."
    docker system prune -f
    
    # 构建镜像
    log "构建Docker镜像..."
    docker-compose build --no-cache
    
    # 启动服务
    log "启动服务..."
    docker-compose up -d
    
    cd ..
    
    log "服务部署完成"
}

# 健康检查
health_check() {
    log "执行健康检查..."
    
    # 等待服务启动
    sleep 30
    
    # 检查容器状态
    check_container "fresh-delivery-mysql" "MySQL数据库"
    check_container "fresh-delivery-redis" "Redis缓存"
    check_container "fresh-delivery-backend" "后端服务"
    check_container "fresh-delivery-admin" "前端管理系统"
    check_container "fresh-delivery-nginx" "Nginx代理"
    
    # 检查服务可访问性
    check_service "https://$SERVER_IP" "HTTPS服务"
    check_service "https://$SERVER_IP/api/health" "后端API"
    
    log "健康检查完成"
}

# 检查容器状态
check_container() {
    local container_name=$1
    local service_name=$2
    
    if docker ps | grep -q $container_name; then
        local status=$(docker inspect --format='{{.State.Health.Status}}' $container_name 2>/dev/null || echo "running")
        log "✓ $service_name 容器运行正常 ($status)"
    else
        error "✗ $service_name 容器未运行"
    fi
}

# 检查服务可访问性
check_service() {
    local url=$1
    local service_name=$2
    
    if curl -k -s --connect-timeout 10 $url > /dev/null; then
        log "✓ $service_name 可访问"
    else
        warn "✗ $service_name 暂时无法访问，可能需要更多时间启动"
    fi
}

# 显示部署信息
show_deployment_info() {
    log "部署完成！"
    
    echo -e "\n${GREEN}=== 边墙鲜送系统部署信息 ===${NC}"
    echo -e "${BLUE}服务器IP:${NC} $SERVER_IP"
    echo -e "${BLUE}管理后台:${NC} https://$SERVER_IP"
    echo -e "${BLUE}API接口:${NC} https://$SERVER_IP/api"
    echo -e "${BLUE}数据库:${NC} $SERVER_IP:3306"
    echo -e "${BLUE}Redis:${NC} $SERVER_IP:6379"
    echo -e "\n${YELLOW}默认账号信息:${NC}"
    echo -e "${BLUE}数据库root密码:${NC} FreshDelivery2024!@#"
    echo -e "${BLUE}数据库用户密码:${NC} FreshUser2024!@#"
    echo -e "${BLUE}Redis密码:${NC} Redis2024!@#"
    echo -e "\n${YELLOW}重要提醒:${NC}"
    echo -e "1. 请及时修改默认密码"
    echo -e "2. 配置微信小程序AppID和Secret"
    echo -e "3. 建议配置正式SSL证书"
    echo -e "4. 定期备份数据库"
    echo -e "\n${GREEN}部署日志:${NC} $LOG_FILE"
}

# 主函数
main() {
    log "开始部署边墙鲜送系统..."
    
    check_requirements
    create_directories
    generate_env_file
    generate_ssl_cert
    update_nginx_config
    backup_database
    deploy_services
    health_check
    show_deployment_info
    
    log "部署脚本执行完成！"
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi