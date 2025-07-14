#!/bin/bash

# 边墙鲜送系统 Kubernetes 部署脚本
# 作者: DevOps Team
# 版本: v1.0.0
# 描述: 用于部署边墙鲜送系统到Kubernetes集群

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
PROJECT_NAME="fresh-delivery"
NAMESPACE_PROD="fresh-delivery"
NAMESPACE_STAGING="fresh-delivery-staging"
NAMESPACE_MONITORING="monitoring"
KUBECTL_CMD="kubectl"
HELM_CMD="helm"

# 检查必要的工具
check_prerequisites() {
    echo -e "${BLUE}检查必要工具...${NC}"
    
    if ! command -v $KUBECTL_CMD &> /dev/null; then
        echo -e "${RED}错误: kubectl 未安装${NC}"
        exit 1
    fi
    
    if ! command -v $HELM_CMD &> /dev/null; then
        echo -e "${YELLOW}警告: helm 未安装，某些功能可能不可用${NC}"
    fi
    
    if ! $KUBECTL_CMD cluster-info &> /dev/null; then
        echo -e "${RED}错误: 无法连接到Kubernetes集群${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ 工具检查完成${NC}"
}

# 创建命名空间
create_namespaces() {
    echo -e "${BLUE}创建命名空间...${NC}"
    
    $KUBECTL_CMD apply -f namespace.yaml
    
    # 创建监控命名空间
    $KUBECTL_CMD create namespace $NAMESPACE_MONITORING --dry-run=client -o yaml | $KUBECTL_CMD apply -f -
    
    echo -e "${GREEN}✓ 命名空间创建完成${NC}"
}

# 创建Secrets
create_secrets() {
    echo -e "${BLUE}创建Secrets...${NC}"
    
    # 检查secrets文件是否存在
    if [ ! -f "secret.yaml" ]; then
        echo -e "${RED}错误: secret.yaml 文件不存在${NC}"
        echo -e "${YELLOW}请先创建secret.yaml文件并配置正确的密钥${NC}"
        exit 1
    fi
    
    $KUBECTL_CMD apply -f secret.yaml
    
    # 创建Docker镜像拉取密钥
    if [ -n "$DOCKER_REGISTRY_USER" ] && [ -n "$DOCKER_REGISTRY_PASS" ]; then
        $KUBECTL_CMD create secret docker-registry ghcr-secret \
            --docker-server=ghcr.io \
            --docker-username=$DOCKER_REGISTRY_USER \
            --docker-password=$DOCKER_REGISTRY_PASS \
            --namespace=$NAMESPACE_PROD \
            --dry-run=client -o yaml | $KUBECTL_CMD apply -f -
            
        $KUBECTL_CMD create secret docker-registry ghcr-secret \
            --docker-server=ghcr.io \
            --docker-username=$DOCKER_REGISTRY_USER \
            --docker-password=$DOCKER_REGISTRY_PASS \
            --namespace=$NAMESPACE_STAGING \
            --dry-run=client -o yaml | $KUBECTL_CMD apply -f -
    fi
    
    echo -e "${GREEN}✓ Secrets创建完成${NC}"
}

# 部署基础设施
deploy_infrastructure() {
    echo -e "${BLUE}部署基础设施...${NC}"
    
    # 部署ConfigMaps
    $KUBECTL_CMD apply -f configmap.yaml
    
    # 部署MySQL
    echo -e "${YELLOW}部署MySQL...${NC}"
    $KUBECTL_CMD apply -f mysql.yaml
    
    # 等待MySQL就绪
    echo -e "${YELLOW}等待MySQL就绪...${NC}"
    $KUBECTL_CMD wait --for=condition=ready pod -l app=mysql -n $NAMESPACE_PROD --timeout=300s
    $KUBECTL_CMD wait --for=condition=ready pod -l app=mysql -n $NAMESPACE_STAGING --timeout=300s
    
    # 部署Redis
    echo -e "${YELLOW}部署Redis...${NC}"
    $KUBECTL_CMD apply -f redis.yaml
    
    # 等待Redis就绪
    echo -e "${YELLOW}等待Redis就绪...${NC}"
    $KUBECTL_CMD wait --for=condition=ready pod -l app=redis -n $NAMESPACE_PROD --timeout=300s
    $KUBECTL_CMD wait --for=condition=ready pod -l app=redis -n $NAMESPACE_STAGING --timeout=300s
    
    echo -e "${GREEN}✓ 基础设施部署完成${NC}"
}

# 部署应用服务
deploy_applications() {
    echo -e "${BLUE}部署应用服务...${NC}"
    
    # 部署后端服务
    echo -e "${YELLOW}部署后端服务...${NC}"
    $KUBECTL_CMD apply -f backend.yaml
    
    # 部署前端服务
    echo -e "${YELLOW}部署前端服务...${NC}"
    $KUBECTL_CMD apply -f frontend.yaml
    
    # 部署Nginx
    echo -e "${YELLOW}部署Nginx...${NC}"
    $KUBECTL_CMD apply -f nginx.yaml
    
    # 等待应用就绪
    echo -e "${YELLOW}等待应用服务就绪...${NC}"
    $KUBECTL_CMD wait --for=condition=ready pod -l app=backend -n $NAMESPACE_PROD --timeout=300s
    $KUBECTL_CMD wait --for=condition=ready pod -l app=admin-frontend -n $NAMESPACE_PROD --timeout=300s
    $KUBECTL_CMD wait --for=condition=ready pod -l app=nginx -n $NAMESPACE_PROD --timeout=300s
    
    echo -e "${GREEN}✓ 应用服务部署完成${NC}"
}

# 部署Ingress
deploy_ingress() {
    echo -e "${BLUE}部署Ingress...${NC}"
    
    # 检查nginx-ingress控制器是否存在
    if ! $KUBECTL_CMD get namespace ingress-nginx &> /dev/null; then
        echo -e "${YELLOW}安装nginx-ingress控制器...${NC}"
        $KUBECTL_CMD apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
        
        # 等待ingress控制器就绪
        $KUBECTL_CMD wait --namespace ingress-nginx \
            --for=condition=ready pod \
            --selector=app.kubernetes.io/component=controller \
            --timeout=300s
    fi
    
    # 安装cert-manager
    if ! $KUBECTL_CMD get namespace cert-manager &> /dev/null; then
        echo -e "${YELLOW}安装cert-manager...${NC}"
        $KUBECTL_CMD apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml
        
        # 等待cert-manager就绪
        $KUBECTL_CMD wait --namespace cert-manager \
            --for=condition=ready pod \
            --selector=app.kubernetes.io/instance=cert-manager \
            --timeout=300s
    fi
    
    # 部署Ingress配置
    $KUBECTL_CMD apply -f ingress.yaml
    
    echo -e "${GREEN}✓ Ingress部署完成${NC}"
}

# 部署监控系统
deploy_monitoring() {
    echo -e "${BLUE}部署监控系统...${NC}"
    
    # 创建Grafana密钥
    if [ -z "$GRAFANA_ADMIN_PASSWORD" ]; then
        GRAFANA_ADMIN_PASSWORD="admin123"
        echo -e "${YELLOW}使用默认Grafana密码: $GRAFANA_ADMIN_PASSWORD${NC}"
    fi
    
    $KUBECTL_CMD create secret generic grafana-secret \
        --from-literal=admin-password=$GRAFANA_ADMIN_PASSWORD \
        --namespace=$NAMESPACE_MONITORING \
        --dry-run=client -o yaml | $KUBECTL_CMD apply -f -
    
    # 部署监控组件
    $KUBECTL_CMD apply -f monitoring.yaml
    
    echo -e "${GREEN}✓ 监控系统部署完成${NC}"
}

# 健康检查
health_check() {
    echo -e "${BLUE}执行健康检查...${NC}"
    
    # 检查Pod状态
    echo -e "${YELLOW}检查Pod状态...${NC}"
    $KUBECTL_CMD get pods -n $NAMESPACE_PROD
    $KUBECTL_CMD get pods -n $NAMESPACE_STAGING
    $KUBECTL_CMD get pods -n $NAMESPACE_MONITORING
    
    # 检查服务状态
    echo -e "${YELLOW}检查服务状态...${NC}"
    $KUBECTL_CMD get services -n $NAMESPACE_PROD
    $KUBECTL_CMD get services -n $NAMESPACE_STAGING
    
    # 检查Ingress状态
    echo -e "${YELLOW}检查Ingress状态...${NC}"
    $KUBECTL_CMD get ingress -n $NAMESPACE_PROD
    $KUBECTL_CMD get ingress -n $NAMESPACE_STAGING
    
    echo -e "${GREEN}✓ 健康检查完成${NC}"
}

# 显示访问信息
show_access_info() {
    echo -e "${BLUE}访问信息:${NC}"
    echo -e "${GREEN}生产环境:${NC}"
    echo -e "  API: https://api.biangqiang.com"
    echo -e "  管理后台: https://admin.biangqiang.com"
    echo -e "${GREEN}测试环境:${NC}"
    echo -e "  测试地址: https://staging.biangqiang.com"
    echo -e "${GREEN}监控系统:${NC}"
    echo -e "  Grafana: http://$(kubectl get svc grafana -n monitoring -o jsonpath='{.status.loadBalancer.ingress[0].ip}'):3000"
    echo -e "  Prometheus: http://$(kubectl get svc prometheus -n monitoring -o jsonpath='{.status.loadBalancer.ingress[0].ip}'):9090"
    echo -e "  默认Grafana用户名: admin"
    echo -e "  默认Grafana密码: $GRAFANA_ADMIN_PASSWORD"
}

# 清理资源
cleanup() {
    echo -e "${YELLOW}清理资源...${NC}"
    
    read -p "确定要删除所有资源吗? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        $KUBECTL_CMD delete namespace $NAMESPACE_PROD
        $KUBECTL_CMD delete namespace $NAMESPACE_STAGING
        $KUBECTL_CMD delete namespace $NAMESPACE_MONITORING
        echo -e "${GREEN}✓ 资源清理完成${NC}"
    else
        echo -e "${YELLOW}取消清理操作${NC}"
    fi
}

# 更新应用
update_app() {
    local environment=$1
    local component=$2
    
    if [ "$environment" = "prod" ]; then
        namespace=$NAMESPACE_PROD
    elif [ "$environment" = "staging" ]; then
        namespace=$NAMESPACE_STAGING
    else
        echo -e "${RED}错误: 无效的环境 $environment${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}更新 $environment 环境的 $component...${NC}"
    
    case $component in
        "backend")
            $KUBECTL_CMD rollout restart deployment/backend -n $namespace
            $KUBECTL_CMD rollout status deployment/backend -n $namespace
            ;;
        "frontend")
            $KUBECTL_CMD rollout restart deployment/admin-frontend -n $namespace
            $KUBECTL_CMD rollout status deployment/admin-frontend -n $namespace
            ;;
        "nginx")
            $KUBECTL_CMD rollout restart deployment/nginx -n $namespace
            $KUBECTL_CMD rollout status deployment/nginx -n $namespace
            ;;
        "all")
            $KUBECTL_CMD rollout restart deployment -n $namespace
            $KUBECTL_CMD rollout status deployment/backend -n $namespace
            $KUBECTL_CMD rollout status deployment/admin-frontend -n $namespace
            $KUBECTL_CMD rollout status deployment/nginx -n $namespace
            ;;
        *)
            echo -e "${RED}错误: 无效的组件 $component${NC}"
            exit 1
            ;;
    esac
    
    echo -e "${GREEN}✓ $component 更新完成${NC}"
}

# 显示日志
show_logs() {
    local environment=$1
    local component=$2
    local lines=${3:-100}
    
    if [ "$environment" = "prod" ]; then
        namespace=$NAMESPACE_PROD
    elif [ "$environment" = "staging" ]; then
        namespace=$NAMESPACE_STAGING
    else
        echo -e "${RED}错误: 无效的环境 $environment${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}显示 $environment 环境 $component 的日志...${NC}"
    
    $KUBECTL_CMD logs -l app=$component -n $namespace --tail=$lines -f
}

# 备份数据
backup_data() {
    echo -e "${BLUE}备份数据...${NC}"
    
    local backup_date=$(date +%Y%m%d_%H%M%S)
    local backup_dir="./backups/$backup_date"
    
    mkdir -p $backup_dir
    
    # 备份MySQL数据
    echo -e "${YELLOW}备份MySQL数据...${NC}"
    $KUBECTL_CMD exec -n $NAMESPACE_PROD deployment/mysql -- mysqldump -u root -pPassword123! fresh_delivery > $backup_dir/mysql_backup.sql
    
    # 备份Redis数据
    echo -e "${YELLOW}备份Redis数据...${NC}"
    $KUBECTL_CMD exec -n $NAMESPACE_PROD deployment/redis -- redis-cli --rdb /tmp/dump.rdb
    $KUBECTL_CMD cp $NAMESPACE_PROD/$(kubectl get pod -l app=redis -n $NAMESPACE_PROD -o jsonpath='{.items[0].metadata.name}'):/tmp/dump.rdb $backup_dir/redis_backup.rdb
    
    # 备份配置文件
    echo -e "${YELLOW}备份配置文件...${NC}"
    $KUBECTL_CMD get configmap -n $NAMESPACE_PROD -o yaml > $backup_dir/configmaps.yaml
    $KUBECTL_CMD get secret -n $NAMESPACE_PROD -o yaml > $backup_dir/secrets.yaml
    
    echo -e "${GREEN}✓ 数据备份完成: $backup_dir${NC}"
}

# 显示帮助信息
show_help() {
    echo "边墙鲜送系统 Kubernetes 部署脚本"
    echo ""
    echo "用法: $0 [命令] [参数]"
    echo ""
    echo "命令:"
    echo "  deploy          完整部署系统"
    echo "  deploy-infra    仅部署基础设施"
    echo "  deploy-app      仅部署应用服务"
    echo "  deploy-monitor  仅部署监控系统"
    echo "  update          更新应用组件"
    echo "  logs            显示日志"
    echo "  status          显示系统状态"
    echo "  backup          备份数据"
    echo "  cleanup         清理所有资源"
    echo "  help            显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 deploy                    # 完整部署"
    echo "  $0 update prod backend       # 更新生产环境后端"
    echo "  $0 logs staging backend 200  # 显示测试环境后端日志"
    echo "  $0 status                    # 显示系统状态"
    echo ""
}

# 主函数
main() {
    case "$1" in
        "deploy")
            check_prerequisites
            create_namespaces
            create_secrets
            deploy_infrastructure
            deploy_applications
            deploy_ingress
            deploy_monitoring
            health_check
            show_access_info
            ;;
        "deploy-infra")
            check_prerequisites
            create_namespaces
            create_secrets
            deploy_infrastructure
            ;;
        "deploy-app")
            check_prerequisites
            deploy_applications
            deploy_ingress
            ;;
        "deploy-monitor")
            check_prerequisites
            deploy_monitoring
            ;;
        "update")
            if [ $# -lt 3 ]; then
                echo -e "${RED}错误: 缺少参数${NC}"
                echo "用法: $0 update <environment> <component>"
                exit 1
            fi
            update_app $2 $3
            ;;
        "logs")
            if [ $# -lt 3 ]; then
                echo -e "${RED}错误: 缺少参数${NC}"
                echo "用法: $0 logs <environment> <component> [lines]"
                exit 1
            fi
            show_logs $2 $3 $4
            ;;
        "status")
            health_check
            ;;
        "backup")
            backup_data
            ;;
        "cleanup")
            cleanup
            ;;
        "help")
            show_help
            ;;
        "")
            show_help
            ;;
        *)
            echo -e "${RED}错误: 未知命令 '$1'${NC}"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"