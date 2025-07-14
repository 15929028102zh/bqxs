#!/bin/bash

# 边墙鲜送系统自动化部署脚本
# 版本: 1.0.0
# 作者: 开发团队
# 描述: 提供完整的CI/CD自动化部署功能

set -e  # 遇到错误立即退出

# ===========================================
# 全局变量定义
# ===========================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"
CONFIG_FILE="$PROJECT_ROOT/deployment-config.env"
LOG_DIR="$PROJECT_ROOT/logs"
LOG_FILE="$LOG_DIR/deploy-$(date +%Y%m%d-%H%M%S).log"
PID_FILE="/tmp/fresh-delivery-deploy.pid"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ===========================================
# 工具函数
# ===========================================

# 日志函数
log() {
    local level=$1
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    case $level in
        "INFO")
            echo -e "${GREEN}[$timestamp] [INFO]${NC} $message" | tee -a "$LOG_FILE"
            ;;
        "WARN")
            echo -e "${YELLOW}[$timestamp] [WARN]${NC} $message" | tee -a "$LOG_FILE"
            ;;
        "ERROR")
            echo -e "${RED}[$timestamp] [ERROR]${NC} $message" | tee -a "$LOG_FILE"
            ;;
        "DEBUG")
            echo -e "${BLUE}[$timestamp] [DEBUG]${NC} $message" | tee -a "$LOG_FILE"
            ;;
        *)
            echo -e "[$timestamp] $message" | tee -a "$LOG_FILE"
            ;;
    esac
}

# 错误处理函数
error_exit() {
    log "ERROR" "$1"
    cleanup
    exit 1
}

# 清理函数
cleanup() {
    log "INFO" "执行清理操作..."
    if [ -f "$PID_FILE" ]; then
        rm -f "$PID_FILE"
    fi
}

# 信号处理
trap cleanup EXIT
trap 'error_exit "脚本被中断"' INT TERM

# 检查命令是否存在
check_command() {
    if ! command -v "$1" &> /dev/null; then
        error_exit "命令 '$1' 未找到，请先安装"
    fi
}

# 检查文件是否存在
check_file() {
    if [ ! -f "$1" ]; then
        error_exit "文件 '$1' 不存在"
    fi
}

# 检查目录是否存在，不存在则创建
ensure_dir() {
    if [ ! -d "$1" ]; then
        mkdir -p "$1"
        log "INFO" "创建目录: $1"
    fi
}

# 加载配置文件
load_config() {
    if [ -f "$CONFIG_FILE" ]; then
        log "INFO" "加载配置文件: $CONFIG_FILE"
        source "$CONFIG_FILE"
    else
        log "WARN" "配置文件不存在: $CONFIG_FILE"
        log "INFO" "使用默认配置"
    fi
}

# 检查必要的工具
check_prerequisites() {
    log "INFO" "检查必要的工具..."
    
    local tools=("docker" "docker-compose" "git" "curl" "jq")
    
    for tool in "${tools[@]}"; do
        check_command "$tool"
        log "INFO" "✓ $tool 已安装"
    done
    
    # 检查Docker是否运行
    if ! docker info &> /dev/null; then
        error_exit "Docker 服务未运行，请启动Docker服务"
    fi
    log "INFO" "✓ Docker 服务正在运行"
    
    # 检查Kubernetes工具（可选）
    if command -v kubectl &> /dev/null; then
        log "INFO" "✓ kubectl 已安装"
        KUBECTL_AVAILABLE=true
    else
        log "WARN" "kubectl 未安装，Kubernetes部署功能不可用"
        KUBECTL_AVAILABLE=false
    fi
    
    if command -v helm &> /dev/null; then
        log "INFO" "✓ helm 已安装"
        HELM_AVAILABLE=true
    else
        log "WARN" "helm 未安装，Helm部署功能不可用"
        HELM_AVAILABLE=false
    fi
}

# 初始化环境
init_environment() {
    log "INFO" "初始化部署环境..."
    
    # 创建必要的目录
    ensure_dir "$LOG_DIR"
    ensure_dir "$PROJECT_ROOT/backups"
    ensure_dir "$PROJECT_ROOT/ssl"
    ensure_dir "$PROJECT_ROOT/uploads"
    
    # 设置权限
    chmod 755 "$PROJECT_ROOT/deploy.sh" 2>/dev/null || true
    chmod 755 "$PROJECT_ROOT/k8s/deploy.sh" 2>/dev/null || true
    
    log "INFO" "环境初始化完成"
}

# 构建Docker镜像
build_images() {
    log "INFO" "开始构建Docker镜像..."
    
    local backend_image="${DOCKER_REGISTRY}/${DOCKER_REGISTRY_USER}/fresh-delivery-backend:${PROJECT_VERSION:-latest}"
    local frontend_image="${DOCKER_REGISTRY}/${DOCKER_REGISTRY_USER}/fresh-delivery-frontend:${PROJECT_VERSION:-latest}"
    
    # 构建后端镜像
    if [ -f "$PROJECT_ROOT/backend/Dockerfile" ]; then
        log "INFO" "构建后端镜像: $backend_image"
        docker build -t "$backend_image" "$PROJECT_ROOT/backend" || error_exit "后端镜像构建失败"
        log "INFO" "✓ 后端镜像构建成功"
    else
        log "WARN" "后端Dockerfile不存在，跳过后端镜像构建"
    fi
    
    # 构建前端镜像
    if [ -f "$PROJECT_ROOT/admin-frontend/Dockerfile" ]; then
        log "INFO" "构建前端镜像: $frontend_image"
        docker build -t "$frontend_image" "$PROJECT_ROOT/admin-frontend" || error_exit "前端镜像构建失败"
        log "INFO" "✓ 前端镜像构建成功"
    else
        log "WARN" "前端Dockerfile不存在，跳过前端镜像构建"
    fi
    
    log "INFO" "Docker镜像构建完成"
}

# 推送Docker镜像
push_images() {
    log "INFO" "开始推送Docker镜像..."
    
    # 登录Docker Registry
    if [ -n "${DOCKER_REGISTRY_PASS}" ]; then
        echo "${DOCKER_REGISTRY_PASS}" | docker login "${DOCKER_REGISTRY}" -u "${DOCKER_REGISTRY_USER}" --password-stdin || error_exit "Docker Registry登录失败"
        log "INFO" "✓ Docker Registry登录成功"
    fi
    
    local backend_image="${DOCKER_REGISTRY}/${DOCKER_REGISTRY_USER}/fresh-delivery-backend:${PROJECT_VERSION:-latest}"
    local frontend_image="${DOCKER_REGISTRY}/${DOCKER_REGISTRY_USER}/fresh-delivery-frontend:${PROJECT_VERSION:-latest}"
    
    # 推送后端镜像
    if docker images | grep -q "fresh-delivery-backend"; then
        log "INFO" "推送后端镜像: $backend_image"
        docker push "$backend_image" || error_exit "后端镜像推送失败"
        log "INFO" "✓ 后端镜像推送成功"
    fi
    
    # 推送前端镜像
    if docker images | grep -q "fresh-delivery-frontend"; then
        log "INFO" "推送前端镜像: $frontend_image"
        docker push "$frontend_image" || error_exit "前端镜像推送失败"
        log "INFO" "✓ 前端镜像推送成功"
    fi
    
    log "INFO" "Docker镜像推送完成"
}

# Docker Compose部署
deploy_docker_compose() {
    log "INFO" "开始Docker Compose部署..."
    
    local compose_file="$PROJECT_ROOT/docker-compose.prod.yml"
    check_file "$compose_file"
    
    # 停止现有服务
    log "INFO" "停止现有服务..."
    docker-compose -f "$compose_file" down || true
    
    # 拉取最新镜像
    log "INFO" "拉取最新镜像..."
    docker-compose -f "$compose_file" pull || error_exit "镜像拉取失败"
    
    # 启动服务
    log "INFO" "启动服务..."
    docker-compose -f "$compose_file" up -d || error_exit "服务启动失败"
    
    # 等待服务启动
    log "INFO" "等待服务启动..."
    sleep 30
    
    # 健康检查
    check_docker_health
    
    log "INFO" "Docker Compose部署完成"
}

# Kubernetes部署
deploy_kubernetes() {
    if [ "$KUBECTL_AVAILABLE" != "true" ]; then
        error_exit "kubectl未安装，无法进行Kubernetes部署"
    fi
    
    log "INFO" "开始Kubernetes部署..."
    
    local k8s_dir="$PROJECT_ROOT/k8s"
    check_file "$k8s_dir/deploy.sh"
    
    # 执行Kubernetes部署脚本
    cd "$k8s_dir"
    bash deploy.sh deploy || error_exit "Kubernetes部署失败"
    cd "$PROJECT_ROOT"
    
    log "INFO" "Kubernetes部署完成"
}

# 健康检查
check_docker_health() {
    log "INFO" "执行健康检查..."
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        log "INFO" "健康检查尝试 $attempt/$max_attempts"
        
        # 检查后端服务
        if curl -f -s "http://localhost:8080/actuator/health" > /dev/null 2>&1; then
            log "INFO" "✓ 后端服务健康检查通过"
            break
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            error_exit "健康检查失败，服务可能未正常启动"
        fi
        
        sleep 10
        ((attempt++))
    done
    
    log "INFO" "健康检查完成"
}

# 数据库迁移
run_database_migration() {
    log "INFO" "执行数据库迁移..."
    
    # 等待数据库启动
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if docker exec fresh-delivery-mysql mysqladmin ping -h localhost -u root -p"${MYSQL_ROOT_PASSWORD}" --silent > /dev/null 2>&1; then
            log "INFO" "✓ 数据库连接成功"
            break
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            error_exit "数据库连接失败"
        fi
        
        log "INFO" "等待数据库启动... ($attempt/$max_attempts)"
        sleep 10
        ((attempt++))
    done
    
    # 执行数据库迁移（如果有Flyway或Liquibase脚本）
    if [ -d "$PROJECT_ROOT/src/main/resources/db/migration" ]; then
        log "INFO" "执行Flyway数据库迁移..."
        docker exec fresh-delivery-backend java -jar app.jar --spring.flyway.migrate=true || error_exit "数据库迁移失败"
    fi
    
    log "INFO" "数据库迁移完成"
}

# 备份数据
backup_data() {
    log "INFO" "开始数据备份..."
    
    local backup_dir="$PROJECT_ROOT/backups/$(date +%Y%m%d-%H%M%S)"
    ensure_dir "$backup_dir"
    
    # 备份MySQL数据
    if docker ps | grep -q "fresh-delivery-mysql"; then
        log "INFO" "备份MySQL数据..."
        docker exec fresh-delivery-mysql mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" --all-databases > "$backup_dir/mysql-backup.sql" || error_exit "MySQL备份失败"
        log "INFO" "✓ MySQL备份完成"
    fi
    
    # 备份Redis数据
    if docker ps | grep -q "fresh-delivery-redis"; then
        log "INFO" "备份Redis数据..."
        docker exec fresh-delivery-redis redis-cli BGSAVE || error_exit "Redis备份失败"
        docker cp fresh-delivery-redis:/data/dump.rdb "$backup_dir/redis-backup.rdb" || error_exit "Redis备份文件复制失败"
        log "INFO" "✓ Redis备份完成"
    fi
    
    # 备份上传文件
    if [ -d "$PROJECT_ROOT/uploads" ]; then
        log "INFO" "备份上传文件..."
        tar -czf "$backup_dir/uploads-backup.tar.gz" -C "$PROJECT_ROOT" uploads || error_exit "上传文件备份失败"
        log "INFO" "✓ 上传文件备份完成"
    fi
    
    log "INFO" "数据备份完成，备份位置: $backup_dir"
}

# 恢复数据
restore_data() {
    local backup_dir="$1"
    
    if [ -z "$backup_dir" ] || [ ! -d "$backup_dir" ]; then
        error_exit "请指定有效的备份目录"
    fi
    
    log "INFO" "开始数据恢复，备份目录: $backup_dir"
    
    # 恢复MySQL数据
    if [ -f "$backup_dir/mysql-backup.sql" ]; then
        log "INFO" "恢复MySQL数据..."
        docker exec -i fresh-delivery-mysql mysql -u root -p"${MYSQL_ROOT_PASSWORD}" < "$backup_dir/mysql-backup.sql" || error_exit "MySQL恢复失败"
        log "INFO" "✓ MySQL恢复完成"
    fi
    
    # 恢复Redis数据
    if [ -f "$backup_dir/redis-backup.rdb" ]; then
        log "INFO" "恢复Redis数据..."
        docker cp "$backup_dir/redis-backup.rdb" fresh-delivery-redis:/data/dump.rdb || error_exit "Redis恢复失败"
        docker restart fresh-delivery-redis || error_exit "Redis重启失败"
        log "INFO" "✓ Redis恢复完成"
    fi
    
    # 恢复上传文件
    if [ -f "$backup_dir/uploads-backup.tar.gz" ]; then
        log "INFO" "恢复上传文件..."
        tar -xzf "$backup_dir/uploads-backup.tar.gz" -C "$PROJECT_ROOT" || error_exit "上传文件恢复失败"
        log "INFO" "✓ 上传文件恢复完成"
    fi
    
    log "INFO" "数据恢复完成"
}

# 滚动更新
rolling_update() {
    log "INFO" "开始滚动更新..."
    
    # 构建新镜像
    build_images
    
    # 推送新镜像
    push_images
    
    # 更新服务
    if [ "$DEPLOYMENT_TYPE" = "kubernetes" ] && [ "$KUBECTL_AVAILABLE" = "true" ]; then
        log "INFO" "执行Kubernetes滚动更新..."
        kubectl set image deployment/fresh-delivery-backend fresh-delivery-backend="${DOCKER_REGISTRY}/${DOCKER_REGISTRY_USER}/fresh-delivery-backend:${PROJECT_VERSION:-latest}" -n "${K8S_NAMESPACE_PROD:-fresh-delivery}" || error_exit "后端滚动更新失败"
        kubectl set image deployment/fresh-delivery-frontend fresh-delivery-frontend="${DOCKER_REGISTRY}/${DOCKER_REGISTRY_USER}/fresh-delivery-frontend:${PROJECT_VERSION:-latest}" -n "${K8S_NAMESPACE_PROD:-fresh-delivery}" || error_exit "前端滚动更新失败"
        
        # 等待滚动更新完成
        kubectl rollout status deployment/fresh-delivery-backend -n "${K8S_NAMESPACE_PROD:-fresh-delivery}" || error_exit "后端滚动更新状态检查失败"
        kubectl rollout status deployment/fresh-delivery-frontend -n "${K8S_NAMESPACE_PROD:-fresh-delivery}" || error_exit "前端滚动更新状态检查失败"
    else
        log "INFO" "执行Docker Compose滚动更新..."
        deploy_docker_compose
    fi
    
    log "INFO" "滚动更新完成"
}

# 回滚部署
rollback_deployment() {
    local revision="${1:-1}"
    
    log "INFO" "开始回滚部署，回滚到版本: $revision"
    
    if [ "$DEPLOYMENT_TYPE" = "kubernetes" ] && [ "$KUBECTL_AVAILABLE" = "true" ]; then
        log "INFO" "执行Kubernetes回滚..."
        kubectl rollout undo deployment/fresh-delivery-backend --to-revision="$revision" -n "${K8S_NAMESPACE_PROD:-fresh-delivery}" || error_exit "后端回滚失败"
        kubectl rollout undo deployment/fresh-delivery-frontend --to-revision="$revision" -n "${K8S_NAMESPACE_PROD:-fresh-delivery}" || error_exit "前端回滚失败"
        
        # 等待回滚完成
        kubectl rollout status deployment/fresh-delivery-backend -n "${K8S_NAMESPACE_PROD:-fresh-delivery}" || error_exit "后端回滚状态检查失败"
        kubectl rollout status deployment/fresh-delivery-frontend -n "${K8S_NAMESPACE_PROD:-fresh-delivery}" || error_exit "前端回滚状态检查失败"
    else
        log "WARN" "Docker Compose模式不支持自动回滚，请手动恢复"
    fi
    
    log "INFO" "回滚部署完成"
}

# 查看服务状态
show_status() {
    log "INFO" "查看服务状态..."
    
    if [ "$DEPLOYMENT_TYPE" = "kubernetes" ] && [ "$KUBECTL_AVAILABLE" = "true" ]; then
        echo -e "\n${CYAN}=== Kubernetes服务状态 ===${NC}"
        kubectl get pods -n "${K8S_NAMESPACE_PROD:-fresh-delivery}" -o wide
        kubectl get services -n "${K8S_NAMESPACE_PROD:-fresh-delivery}"
        kubectl get ingress -n "${K8S_NAMESPACE_PROD:-fresh-delivery}"
    else
        echo -e "\n${CYAN}=== Docker Compose服务状态 ===${NC}"
        docker-compose -f "$PROJECT_ROOT/docker-compose.prod.yml" ps
    fi
    
    echo -e "\n${CYAN}=== 系统资源使用情况 ===${NC}"
    docker stats --no-stream
    
    echo -e "\n${CYAN}=== 磁盘使用情况 ===${NC}"
    df -h
    
    echo -e "\n${CYAN}=== 内存使用情况 ===${NC}"
    free -h
}

# 查看日志
show_logs() {
    local service="$1"
    local lines="${2:-100}"
    
    if [ "$DEPLOYMENT_TYPE" = "kubernetes" ] && [ "$KUBECTL_AVAILABLE" = "true" ]; then
        if [ -n "$service" ]; then
            kubectl logs -f --tail="$lines" -l app="$service" -n "${K8S_NAMESPACE_PROD:-fresh-delivery}"
        else
            kubectl logs -f --tail="$lines" -l app=fresh-delivery-backend -n "${K8S_NAMESPACE_PROD:-fresh-delivery}"
        fi
    else
        if [ -n "$service" ]; then
            docker-compose -f "$PROJECT_ROOT/docker-compose.prod.yml" logs -f --tail="$lines" "$service"
        else
            docker-compose -f "$PROJECT_ROOT/docker-compose.prod.yml" logs -f --tail="$lines"
        fi
    fi
}

# 性能测试
run_performance_test() {
    log "INFO" "开始性能测试..."
    
    local test_url="${1:-http://localhost:8080/actuator/health}"
    local concurrent_users="${2:-10}"
    local test_duration="${3:-60}"
    
    # 检查是否安装了ab（Apache Bench）
    if command -v ab &> /dev/null; then
        log "INFO" "使用Apache Bench进行性能测试..."
        ab -n 1000 -c "$concurrent_users" -t "$test_duration" "$test_url" | tee "$LOG_DIR/performance-test-$(date +%Y%m%d-%H%M%S).log"
    elif command -v wrk &> /dev/null; then
        log "INFO" "使用wrk进行性能测试..."
        wrk -t"$concurrent_users" -c"$concurrent_users" -d"${test_duration}s" "$test_url" | tee "$LOG_DIR/performance-test-$(date +%Y%m%d-%H%M%S).log"
    else
        log "WARN" "未找到性能测试工具（ab或wrk），跳过性能测试"
    fi
    
    log "INFO" "性能测试完成"
}

# 清理资源
clean_resources() {
    log "INFO" "开始清理资源..."
    
    # 清理Docker资源
    log "INFO" "清理Docker资源..."
    docker system prune -f || true
    docker volume prune -f || true
    docker network prune -f || true
    
    # 清理旧的镜像
    log "INFO" "清理旧的Docker镜像..."
    docker images | grep "fresh-delivery" | grep -v "latest" | awk '{print $3}' | head -n -3 | xargs -r docker rmi || true
    
    # 清理旧的日志文件
    log "INFO" "清理旧的日志文件..."
    find "$LOG_DIR" -name "*.log" -mtime +7 -delete || true
    
    # 清理旧的备份文件
    log "INFO" "清理旧的备份文件..."
    find "$PROJECT_ROOT/backups" -type d -mtime +30 -exec rm -rf {} + || true
    
    log "INFO" "资源清理完成"
}

# 发送通知
send_notification() {
    local status="$1"
    local message="$2"
    
    # 钉钉通知
    if [ -n "${DINGTALK_WEBHOOK_TOKEN}" ]; then
        local webhook_url="https://oapi.dingtalk.com/robot/send?access_token=${DINGTALK_WEBHOOK_TOKEN}"
        local payload='{"msgtype":"text","text":{"content":"边墙鲜送系统部署通知\n状态: '"$status"'\n消息: '"$message"'\n时间: '"$(date)"'"}'
        
        curl -H "Content-Type: application/json" -d "$payload" "$webhook_url" > /dev/null 2>&1 || true
    fi
    
    # 邮件通知（如果配置了邮件服务）
    if [ -n "${MAIL_HOST}" ] && command -v mail &> /dev/null; then
        echo "边墙鲜送系统部署通知\n\n状态: $status\n消息: $message\n时间: $(date)" | mail -s "部署通知" "${ALERT_EMAIL_TO}" || true
    fi
}

# 显示帮助信息
show_help() {
    cat << EOF
${CYAN}边墙鲜送系统自动化部署脚本${NC}

${YELLOW}用法:${NC}
    $0 [选项] <命令> [参数]

${YELLOW}命令:${NC}
    ${GREEN}init${NC}                    初始化部署环境
    ${GREEN}build${NC}                   构建Docker镜像
    ${GREEN}push${NC}                    推送Docker镜像到Registry
    ${GREEN}deploy${NC}                  完整部署（构建+推送+部署）
    ${GREEN}deploy-docker${NC}           使用Docker Compose部署
    ${GREEN}deploy-k8s${NC}              使用Kubernetes部署
    ${GREEN}update${NC}                  滚动更新应用
    ${GREEN}rollback${NC} [revision]     回滚到指定版本（默认回滚1个版本）
    ${GREEN}backup${NC}                  备份数据
    ${GREEN}restore${NC} <backup_dir>   从备份恢复数据
    ${GREEN}status${NC}                  查看服务状态
    ${GREEN}logs${NC} [service] [lines] 查看服务日志
    ${GREEN}test${NC}                    运行性能测试
    ${GREEN}clean${NC}                   清理资源
    ${GREEN}health${NC}                  健康检查
    ${GREEN}migrate${NC}                 执行数据库迁移
    ${GREEN}help${NC}                    显示帮助信息

${YELLOW}选项:${NC}
    ${GREEN}-e, --env${NC} <file>        指定配置文件（默认: deployment-config.env）
    ${GREEN}-t, --type${NC} <type>       部署类型（docker|kubernetes，默认: docker）
    ${GREEN}-v, --version${NC} <ver>     指定版本号（默认: latest）
    ${GREEN}-d, --debug${NC}             启用调试模式
    ${GREEN}-q, --quiet${NC}             静默模式
    ${GREEN}-h, --help${NC}              显示帮助信息

${YELLOW}示例:${NC}
    $0 init                          # 初始化环境
    $0 deploy                        # 完整部署
    $0 deploy-k8s                    # Kubernetes部署
    $0 update                        # 滚动更新
    $0 rollback 2                    # 回滚到第2个版本
    $0 logs backend 200              # 查看后端服务最近200行日志
    $0 backup                        # 备份数据
    $0 restore /path/to/backup       # 恢复数据
    $0 -t kubernetes -v 1.2.0 deploy # 使用Kubernetes部署版本1.2.0

${YELLOW}配置文件:${NC}
    配置文件位置: ${CONFIG_FILE}
    请根据实际环境修改配置文件中的参数

${YELLOW}日志文件:${NC}
    日志文件位置: ${LOG_DIR}/
    当前日志文件: ${LOG_FILE}

EOF
}

# ===========================================
# 主函数
# ===========================================
main() {
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -e|--env)
                CONFIG_FILE="$2"
                shift 2
                ;;
            -t|--type)
                DEPLOYMENT_TYPE="$2"
                shift 2
                ;;
            -v|--version)
                PROJECT_VERSION="$2"
                shift 2
                ;;
            -d|--debug)
                set -x
                shift
                ;;
            -q|--quiet)
                exec > /dev/null 2>&1
                shift
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            -*)
                error_exit "未知选项: $1"
                ;;
            *)
                break
                ;;
        esac
    done
    
    local command="$1"
    shift || true
    
    # 检查是否已有实例在运行
    if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
        error_exit "部署脚本已在运行中，PID: $(cat "$PID_FILE")"
    fi
    
    # 记录当前进程PID
    echo $$ > "$PID_FILE"
    
    # 初始化
    ensure_dir "$LOG_DIR"
    load_config
    
    # 设置默认值
    DEPLOYMENT_TYPE="${DEPLOYMENT_TYPE:-docker}"
    PROJECT_VERSION="${PROJECT_VERSION:-latest}"
    
    log "INFO" "开始执行部署脚本"
    log "INFO" "命令: $command"
    log "INFO" "部署类型: $DEPLOYMENT_TYPE"
    log "INFO" "版本: $PROJECT_VERSION"
    log "INFO" "配置文件: $CONFIG_FILE"
    
    # 执行命令
    case "$command" in
        "init")
            check_prerequisites
            init_environment
            log "INFO" "环境初始化完成"
            ;;
        "build")
            check_prerequisites
            build_images
            ;;
        "push")
            check_prerequisites
            push_images
            ;;
        "deploy")
            check_prerequisites
            init_environment
            build_images
            push_images
            if [ "$DEPLOYMENT_TYPE" = "kubernetes" ]; then
                deploy_kubernetes
            else
                deploy_docker_compose
            fi
            run_database_migration
            check_docker_health
            send_notification "SUCCESS" "部署成功完成"
            ;;
        "deploy-docker")
            check_prerequisites
            init_environment
            deploy_docker_compose
            run_database_migration
            check_docker_health
            send_notification "SUCCESS" "Docker Compose部署成功完成"
            ;;
        "deploy-k8s")
            check_prerequisites
            init_environment
            deploy_kubernetes
            send_notification "SUCCESS" "Kubernetes部署成功完成"
            ;;
        "update")
            check_prerequisites
            rolling_update
            check_docker_health
            send_notification "SUCCESS" "滚动更新成功完成"
            ;;
        "rollback")
            check_prerequisites
            rollback_deployment "$1"
            send_notification "SUCCESS" "回滚成功完成"
            ;;
        "backup")
            backup_data
            ;;
        "restore")
            if [ -z "$1" ]; then
                error_exit "请指定备份目录"
            fi
            restore_data "$1"
            ;;
        "status")
            show_status
            ;;
        "logs")
            show_logs "$1" "$2"
            ;;
        "test")
            run_performance_test "$1" "$2" "$3"
            ;;
        "clean")
            clean_resources
            ;;
        "health")
            check_docker_health
            ;;
        "migrate")
            run_database_migration
            ;;
        "help")
            show_help
            ;;
        "")
            log "ERROR" "请指定命令"
            show_help
            exit 1
            ;;
        *)
            error_exit "未知命令: $command"
            ;;
    esac
    
    log "INFO" "脚本执行完成"
}

# 执行主函数
main "$@"