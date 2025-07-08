#!/bin/bash

# 边墙鲜送系统 Docker 部署脚本
# 使用方法: ./deploy.sh [start|stop|restart|logs|status]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目配置
PROJECT_NAME="fresh-delivery"
DOCKER_COMPOSE_FILE="docker/docker-compose.yml"
SSL_DIR="docker/nginx/ssl"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker 和 Docker Compose
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
    
    log_info "Docker 环境检查通过"
}

# 创建 SSL 证书（自签名，仅用于测试）
create_ssl_cert() {
    if [ ! -d "$SSL_DIR" ]; then
        mkdir -p "$SSL_DIR"
    fi
    
    if [ ! -f "$SSL_DIR/server.crt" ] || [ ! -f "$SSL_DIR/server.key" ]; then
        log_info "创建自签名 SSL 证书..."
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout "$SSL_DIR/server.key" \
            -out "$SSL_DIR/server.crt" \
            -subj "/C=CN/ST=Beijing/L=Beijing/O=FreshDelivery/OU=IT/CN=localhost"
        log_success "SSL 证书创建完成"
    else
        log_info "SSL 证书已存在"
    fi
}

# 构建镜像
build_images() {
    log_info "开始构建 Docker 镜像..."
    
    # 构建后端镜像
    log_info "构建后端镜像..."
    docker build -t fresh-delivery-backend:latest backend/
    
    # 构建前端镜像
    log_info "构建前端镜像..."
    docker build -t fresh-delivery-admin:latest admin-frontend/
    
    log_success "Docker 镜像构建完成"
}

# 启动服务
start_services() {
    log_info "启动 ${PROJECT_NAME} 服务..."
    
    # 检查环境
    check_docker
    
    # 创建 SSL 证书
    create_ssl_cert
    
    # 构建镜像
    build_images
    
    # 启动服务
    docker-compose -f "$DOCKER_COMPOSE_FILE" up -d
    
    # 等待服务启动
    log_info "等待服务启动..."
    sleep 10
    
    # 检查服务状态
    check_services_status
    
    log_success "${PROJECT_NAME} 服务启动完成！"
    echo ""
    echo "访问地址："
    echo "  - 管理后台: https://localhost (HTTP: http://localhost)"
    echo "  - API 文档: http://localhost:8080/swagger-ui/index.html"
    echo "  - 数据库: localhost:3306 (用户名: fresh_user, 密码: fresh_pass)"
    echo "  - Redis: localhost:6379"
    echo ""
    echo "默认管理员账号："
    echo "  - 用户名: admin"
    echo "  - 密码: admin123"
}

# 停止服务
stop_services() {
    log_info "停止 ${PROJECT_NAME} 服务..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" down
    log_success "${PROJECT_NAME} 服务已停止"
}

# 重启服务
restart_services() {
    log_info "重启 ${PROJECT_NAME} 服务..."
    stop_services
    start_services
}

# 查看日志
view_logs() {
    if [ -n "$2" ]; then
        docker-compose -f "$DOCKER_COMPOSE_FILE" logs -f "$2"
    else
        docker-compose -f "$DOCKER_COMPOSE_FILE" logs -f
    fi
}

# 检查服务状态
check_services_status() {
    log_info "检查服务状态..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" ps
    
    # 检查各服务健康状态
    services=("mysql" "redis" "backend" "admin-frontend")
    
    for service in "${services[@]}"; do
        if docker-compose -f "$DOCKER_COMPOSE_FILE" ps | grep -q "$service.*Up"; then
            log_success "$service 服务运行正常"
        else
            log_error "$service 服务异常"
        fi
    done
}

# 清理数据
clean_data() {
    log_warning "这将删除所有数据，包括数据库数据！"
    read -p "确定要继续吗？(y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_info "停止服务并清理数据..."
        docker-compose -f "$DOCKER_COMPOSE_FILE" down -v
        docker system prune -f
        log_success "数据清理完成"
    else
        log_info "操作已取消"
    fi
}

# 备份数据
backup_data() {
    BACKUP_DIR="backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR"
    
    log_info "开始备份数据到 $BACKUP_DIR..."
    
    # 备份数据库
    docker-compose -f "$DOCKER_COMPOSE_FILE" exec -T mysql mysqldump -u fresh_user -pfresh_pass fresh_delivery > "$BACKUP_DIR/database.sql"
    
    # 备份上传文件
    docker cp fresh-delivery-backend:/app/uploads "$BACKUP_DIR/uploads"
    
    log_success "数据备份完成: $BACKUP_DIR"
}

# 显示帮助信息
show_help() {
    echo "边墙鲜送系统 Docker 部署脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [命令] [选项]"
    echo ""
    echo "命令:"
    echo "  start     启动所有服务"
    echo "  stop      停止所有服务"
    echo "  restart   重启所有服务"
    echo "  status    查看服务状态"
    echo "  logs      查看服务日志 (可指定服务名)"
    echo "  build     重新构建镜像"
    echo "  backup    备份数据"
    echo "  clean     清理所有数据 (危险操作)"
    echo "  help      显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start              # 启动所有服务"
    echo "  $0 logs backend       # 查看后端服务日志"
    echo "  $0 backup             # 备份数据"
}

# 主函数
main() {
    case "$1" in
        start)
            start_services
            ;;
        stop)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        status)
            check_services_status
            ;;
        logs)
            view_logs "$@"
            ;;
        build)
            build_images
            ;;
        backup)
            backup_data
            ;;
        clean)
            clean_data
            ;;
        help|--help|-h)
            show_help
            ;;
        "")
            log_error "请指定操作命令"
            show_help
            exit 1
            ;;
        *)
            log_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"