#!/bin/bash

# 边墙鲜送系统部署问题修复脚本
# 版本: 1.0.0
# 作者: AI Assistant
# 描述: 自动修复常见的部署问题

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log() {
    local level=$1
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    case $level in
        "INFO")
            echo -e "${BLUE}[$timestamp] [INFO]${NC} $message"
            ;;
        "WARN")
            echo -e "${YELLOW}[$timestamp] [WARN]${NC} $message"
            ;;
        "ERROR")
            echo -e "${RED}[$timestamp] [ERROR]${NC} $message"
            ;;
        "SUCCESS")
            echo -e "${GREEN}[$timestamp] [SUCCESS]${NC} $message"
            ;;
    esac
}

# 错误处理
error_exit() {
    log "ERROR" "$1"
    exit 1
}

# 检查文件是否存在
check_file() {
    if [ ! -f "$1" ]; then
        error_exit "文件不存在: $1"
    fi
}

# 检查目录是否存在
check_dir() {
    if [ ! -d "$1" ]; then
        error_exit "目录不存在: $1"
    fi
}

# 备份文件
backup_file() {
    local file=$1
    if [ -f "$file" ]; then
        cp "$file" "${file}.backup.$(date +%Y%m%d-%H%M%S)"
        log "INFO" "已备份文件: $file"
    fi
}

# 修复前端Dockerfile
fix_frontend_dockerfile() {
    log "INFO" "修复前端Dockerfile..."
    
    local dockerfile="./admin-frontend/Dockerfile"
    check_file "$dockerfile"
    
    # 备份原文件
    backup_file "$dockerfile"
    
    # 检查是否需要修复
    if grep -q "npm ci --only=production" "$dockerfile"; then
        log "INFO" "发现前端Dockerfile问题，正在修复..."
        
        # 修复npm ci命令
        sed -i 's/npm ci --only=production/npm ci/g' "$dockerfile"
        
        # 验证修复
        if grep -q "npm ci" "$dockerfile" && ! grep -q "npm ci --only=production" "$dockerfile"; then
            log "SUCCESS" "✓ 前端Dockerfile修复成功"
        else
            error_exit "前端Dockerfile修复失败"
        fi
    else
        log "INFO" "前端Dockerfile无需修复"
    fi
}

# 修复部署脚本中的后端路径
fix_deploy_script_backend_path() {
    log "INFO" "修复部署脚本中的后端路径..."
    
    local deploy_script="./deploy-automation.sh"
    check_file "$deploy_script"
    
    # 备份原文件
    backup_file "$deploy_script"
    
    # 检查是否需要修复
    if grep -q '\$PROJECT_ROOT/Dockerfile' "$deploy_script"; then
        log "INFO" "发现部署脚本后端路径问题，正在修复..."
        
        # 修复后端Dockerfile路径
        sed -i 's|\$PROJECT_ROOT/Dockerfile|\$PROJECT_ROOT/backend/Dockerfile|g' "$deploy_script"
        sed -i 's|docker build -t "\$backend_image" "\$PROJECT_ROOT"|docker build -t "\$backend_image" "\$PROJECT_ROOT/backend"|g' "$deploy_script"
        
        # 验证修复
        if grep -q '\$PROJECT_ROOT/backend/Dockerfile' "$deploy_script"; then
            log "SUCCESS" "✓ 部署脚本后端路径修复成功"
        else
            error_exit "部署脚本后端路径修复失败"
        fi
    else
        log "INFO" "部署脚本后端路径无需修复"
    fi
}

# 优化后端Dockerfile（多阶段构建 + 网络优化）
optimize_backend_dockerfile() {
    log "INFO" "优化后端Dockerfile..."
    
    local dockerfile="./backend/Dockerfile"
    check_file "$dockerfile"
    
    # 备份原文件
    backup_file "$dockerfile"
    
    # 检查是否需要优化（查找旧的单阶段构建模式）
    if grep -q "FROM openjdk:17-jdk-slim" "$dockerfile" && ! grep -q "as build-stage" "$dockerfile"; then
        log "INFO" "发现后端Dockerfile需要优化，正在应用多阶段构建..."
        
        # 创建优化后的Dockerfile内容（包含网络优化）
        cat > "$dockerfile" << 'EOF'
# 构建阶段
FROM maven:3.8.6-openjdk-17-slim as build-stage

# 设置工作目录
WORKDIR /app

# 复制 Maven 配置文件
COPY pom.xml .

# 下载依赖（利用Docker缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM openjdk:17-jre-slim as production-stage

# 配置更稳定的镜像源和网络重试
RUN echo "deb http://mirrors.aliyun.com/debian/ bullseye main" > /etc/apt/sources.list && \
    echo "deb http://mirrors.aliyun.com/debian-security/ bullseye-security main" >> /etc/apt/sources.list && \
    echo "deb http://mirrors.aliyun.com/debian/ bullseye-updates main" >> /etc/apt/sources.list

# 安装必要的工具（添加重试机制）
RUN for i in 1 2 3; do \
        apt-get update && \
        apt-get install -y --no-install-recommends curl && \
        rm -rf /var/lib/apt/lists/* && \
        break || sleep 10; \
    done

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=build-stage /app/target/fresh-delivery-backend-1.0.0.jar app.jar

# 创建日志和上传目录
RUN mkdir -p /app/logs /app/uploads

# 暴露端口
EXPOSE 8080

# 设置环境变量
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker

# 启动应用
CMD ["java", "-jar", "app.jar"]
EOF
        
        log "SUCCESS" "✓ 后端Dockerfile优化完成（多阶段构建 + 网络优化）"
    else
        log "INFO" "后端Dockerfile已经是多阶段构建，无需优化"
    fi
}

# 修复配置文件中的引号问题
fix_config_quotes() {
    log "INFO" "修复配置文件中的引号问题..."
    
    local config_file="./deployment-config.env"
    check_file "$config_file"
    
    # 备份原文件
    backup_file "$config_file"
    
    # 需要加引号的配置项
    local configs_to_quote=(
        "DB_POOL_VALIDATION_QUERY=SELECT 1"
        "SWAGGER_TITLE=边墙鲜送API文档"
        "SWAGGER_DESCRIPTION=边墙鲜送系统API接口文档"
        "SWAGGER_CONTACT_NAME=边墙鲜送团队"
        "SMS_SIGN_NAME=边墙鲜送"
        "BACKUP_MYSQL_SCHEDULE=0 2 * * *"
        "BACKUP_FILES_SCHEDULE=0 3 * * *"
    )
    
    local fixed_count=0
    
    for config in "${configs_to_quote[@]}"; do
        local key=$(echo "$config" | cut -d'=' -f1)
        local value=$(echo "$config" | cut -d'=' -f2-)
        
        # 检查是否已经有引号
        if grep -q "^${key}=${value}$" "$config_file"; then
            log "INFO" "修复配置项: $key"
            sed -i "s|^${key}=${value}$|${key}=\"${value}\"|g" "$config_file"
            ((fixed_count++))
        fi
    done
    
    if [ $fixed_count -gt 0 ]; then
        log "SUCCESS" "✓ 修复了 $fixed_count 个配置项的引号问题"
    else
        log "INFO" "配置文件引号无需修复"
    fi
}

# 检查Docker环境
check_docker_environment() {
    log "INFO" "检查Docker环境..."
    
    # 检查Docker是否安装
    if ! command -v docker &> /dev/null; then
        error_exit "Docker未安装，请先安装Docker"
    fi
    
    # 检查Docker是否运行
    if ! docker info &> /dev/null; then
        error_exit "Docker未运行，请启动Docker服务"
    fi
    
    # 检查Docker Compose是否安装
    if ! command -v docker-compose &> /dev/null; then
        log "WARN" "Docker Compose未安装，某些功能可能不可用"
    fi
    
    log "SUCCESS" "✓ Docker环境检查通过"
}

# 清理Docker缓存
clean_docker_cache() {
    log "INFO" "清理Docker缓存..."
    
    # 清理未使用的镜像
    docker image prune -f
    
    # 清理构建缓存
    docker builder prune -f
    
    # 清理未使用的容器
    docker container prune -f
    
    log "SUCCESS" "✓ Docker缓存清理完成"
}

# 验证修复结果
verify_fixes() {
    log "INFO" "验证修复结果..."
    
    local issues_found=0
    
    # 检查前端Dockerfile
    if grep -q "npm ci --only=production" "./admin-frontend/Dockerfile"; then
        log "ERROR" "前端Dockerfile仍有问题"
        ((issues_found++))
    else
        log "SUCCESS" "✓ 前端Dockerfile正常"
    fi
    
    # 检查部署脚本
    if grep -q '\$PROJECT_ROOT/Dockerfile' "./deploy-automation.sh"; then
        log "ERROR" "部署脚本后端路径仍有问题"
        ((issues_found++))
    else
        log "SUCCESS" "✓ 部署脚本后端路径正常"
    fi
    
    # 检查后端Dockerfile优化
    if [ -f "./backend/Dockerfile" ]; then
        if grep -q "as build-stage" "./backend/Dockerfile" && grep -q "as production-stage" "./backend/Dockerfile"; then
            log "SUCCESS" "✓ 后端Dockerfile已优化（多阶段构建）"
        else
            log "WARN" "后端Dockerfile未使用多阶段构建（建议优化）"
        fi
    fi
    
    # 检查配置文件
    if grep -q "DB_POOL_VALIDATION_QUERY=SELECT 1" "./deployment-config.env"; then
        log "ERROR" "配置文件引号仍有问题"
        ((issues_found++))
    else
        log "SUCCESS" "✓ 配置文件引号正常"
    fi
    
    if [ $issues_found -eq 0 ]; then
        log "SUCCESS" "✓ 所有问题已修复"
        return 0
    else
        log "ERROR" "发现 $issues_found 个未修复的问题"
        return 1
    fi
}

# 测试构建
test_build() {
    log "INFO" "测试镜像构建..."
    
    # 测试前端构建
    if [ -f "./admin-frontend/Dockerfile" ]; then
        log "INFO" "测试前端镜像构建..."
        if docker build -t test-frontend:latest ./admin-frontend --no-cache; then
            log "SUCCESS" "✓ 前端镜像构建测试通过"
            docker rmi test-frontend:latest
        else
            log "ERROR" "前端镜像构建测试失败"
            return 1
        fi
    fi
    
    # 测试后端构建
    if [ -f "./backend/Dockerfile" ]; then
        log "INFO" "测试后端镜像构建..."
        if docker build -t test-backend:latest ./backend --no-cache; then
            log "SUCCESS" "✓ 后端镜像构建测试通过"
            docker rmi test-backend:latest
        else
            log "ERROR" "后端镜像构建测试失败"
            return 1
        fi
    fi
    
    log "SUCCESS" "✓ 所有镜像构建测试通过"
}

# 显示帮助信息
show_help() {
    cat << EOF
边墙鲜送系统部署问题修复脚本

用法: $0 [选项] [命令]

命令:
  fix-all          修复所有已知问题（默认）
  fix-frontend     仅修复前端Dockerfile问题
  fix-backend      仅修复后端路径问题
  fix-config       仅修复配置文件引号问题
  optimize-backend 优化后端Dockerfile（多阶段构建）
  check-docker     检查Docker环境
  clean-cache      清理Docker缓存
  verify           验证修复结果
  test-build       测试镜像构建
  help             显示此帮助信息

选项:
  --dry-run        仅显示将要执行的操作，不实际修改文件
  --backup         强制备份所有文件
  --verbose        显示详细输出

示例:
  $0                    # 修复所有问题
  $0 fix-frontend       # 仅修复前端问题
  $0 --dry-run          # 预览修复操作
  $0 test-build         # 测试构建

EOF
}

# 主函数
main() {
    local command="fix-all"
    local dry_run=false
    local force_backup=false
    local verbose=false
    
    # 解析参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            --dry-run)
                dry_run=true
                shift
                ;;
            --backup)
                force_backup=true
                shift
                ;;
            --verbose)
                verbose=true
                shift
                ;;
            fix-all|fix-frontend|fix-backend|fix-config|optimize-backend|check-docker|clean-cache|verify|test-build|help)
                command=$1
                shift
                ;;
            *)
                log "ERROR" "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 显示脚本信息
    log "INFO" "边墙鲜送系统部署问题修复脚本 v1.0.0"
    log "INFO" "执行命令: $command"
    
    if [ "$dry_run" = true ]; then
        log "WARN" "预览模式：不会实际修改文件"
    fi
    
    # 执行命令
    case $command in
        "fix-all")
            if [ "$dry_run" = false ]; then
                check_docker_environment
                fix_frontend_dockerfile
                fix_deploy_script_backend_path
                optimize_backend_dockerfile
                fix_config_quotes
                verify_fixes
                log "SUCCESS" "✓ 所有问题修复完成"
                log "INFO" "建议运行: $0 test-build 来测试构建"
            else
                log "INFO" "将执行以下修复操作："
                log "INFO" "1. 修复前端Dockerfile中的npm ci问题"
                log "INFO" "2. 修复部署脚本中的后端路径问题"
                log "INFO" "3. 优化后端Dockerfile（多阶段构建）"
                log "INFO" "4. 修复配置文件中的引号问题"
            fi
            ;;
        "fix-frontend")
            if [ "$dry_run" = false ]; then
                fix_frontend_dockerfile
            else
                log "INFO" "将修复前端Dockerfile中的npm ci问题"
            fi
            ;;
        "fix-backend")
            if [ "$dry_run" = false ]; then
                fix_deploy_script_backend_path
            else
                log "INFO" "将修复部署脚本中的后端路径问题"
            fi
            ;;
        "fix-config")
            if [ "$dry_run" = false ]; then
                fix_config_quotes
            else
                log "INFO" "将修复配置文件中的引号问题"
            fi
            ;;
        "optimize-backend")
            if [ "$dry_run" = false ]; then
                optimize_backend_dockerfile
            else
                log "INFO" "将优化后端Dockerfile（多阶段构建）"
            fi
            ;;
        "check-docker")
            check_docker_environment
            ;;
        "clean-cache")
            if [ "$dry_run" = false ]; then
                clean_docker_cache
            else
                log "INFO" "将清理Docker缓存"
            fi
            ;;
        "verify")
            verify_fixes
            ;;
        "test-build")
            if [ "$dry_run" = false ]; then
                test_build
            else
                log "INFO" "将测试镜像构建"
            fi
            ;;
        "help")
            show_help
            ;;
        *)
            log "ERROR" "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi