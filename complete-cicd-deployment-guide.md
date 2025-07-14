# 边墙鲜送系统 - 完整CI/CD部署流程指南

## 目录
1. [系统架构概览](#1-系统架构概览)
2. [环境准备](#2-环境准备)
3. [CI/CD流水线配置](#3-cicd流水线配置)
4. [Docker部署方案](#4-docker部署方案)
5. [Kubernetes部署方案](#5-kubernetes部署方案)
6. [监控与运维](#6-监控与运维)
7. [故障排查](#7-故障排查)
8. [最佳实践](#8-最佳实践)

## 1. 系统架构概览

### 1.1 技术栈
- **后端**: Spring Boot + MySQL + Redis
- **前端**: Vue.js + Element UI
- **小程序**: 微信小程序原生开发
- **容器化**: Docker + Docker Compose
- **编排**: Kubernetes
- **CI/CD**: Jenkins / GitHub Actions
- **监控**: Prometheus + Grafana + AlertManager
- **日志**: ELK Stack
- **反向代理**: Nginx

### 1.2 部署架构
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   开发环境      │    │   测试环境      │    │   生产环境      │
│   (Local)       │    │   (Staging)     │    │   (Production)  │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ Docker Compose  │    │ Kubernetes      │    │ Kubernetes      │
│ 单机部署        │    │ 集群部署        │    │ 高可用集群      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 2. 环境准备

### 2.1 开发环境要求
- **操作系统**: Windows 10/11, macOS, Linux
- **Java**: JDK 17+
- **Node.js**: 18.x+
- **Docker**: 20.x+
- **Docker Compose**: 2.x+
- **Git**: 2.x+

### 2.2 服务器环境要求

#### 测试环境
- **CPU**: 4核心
- **内存**: 8GB
- **存储**: 100GB SSD
- **网络**: 100Mbps

#### 生产环境
- **CPU**: 8核心
- **内存**: 16GB
- **存储**: 500GB SSD
- **网络**: 1Gbps
- **高可用**: 3节点集群

### 2.3 必要工具安装

#### Docker安装
```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# CentOS/RHEL
sudo yum install -y docker-ce docker-ce-cli containerd.io
sudo systemctl start docker
sudo systemctl enable docker
```

#### Kubernetes安装
```bash
# 安装kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# 安装Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

## 3. CI/CD流水线配置

### 3.1 Jenkins流水线

项目已包含完整的Jenkins配置文件 `Jenkinsfile`，包含以下阶段：

1. **代码拉取** - 从Git仓库拉取最新代码
2. **后端构建** - Maven编译、测试、打包
3. **前端构建** - npm安装依赖、构建、优化
4. **代码质量分析** - SonarQube静态代码分析
5. **安全扫描** - OWASP依赖漏洞扫描
6. **Docker镜像构建** - 构建并推送到镜像仓库
7. **部署到测试环境** - 自动部署到Staging环境
8. **集成测试** - 自动化API测试
9. **部署到生产环境** - 手动确认后部署到Production
10. **通知** - 钉钉和邮件通知

#### Jenkins环境变量配置
```bash
# 在Jenkins中配置以下环境变量
DOCKER_REGISTRY=your-registry.com
DOCKER_REGISTRY_CREDENTIALS=docker-registry-credentials
SONARQUBE_SERVER=sonarqube-server
DINGTALK_WEBHOOK=your-dingtalk-webhook-url
EMAIL_RECIPIENTS=admin@biangqiang.com
```

### 3.2 GitHub Actions流水线

项目包含完整的GitHub Actions配置文件 `.github/workflows/ci-cd.yml`：

#### 主要特性
- **多环境支持** - 自动识别分支部署到对应环境
- **并行构建** - 代码检查、测试、安全扫描并行执行
- **Docker缓存** - 使用GitHub Actions缓存加速构建
- **自动化测试** - 单元测试、集成测试、性能测试
- **安全扫描** - Trivy漏洞扫描、依赖检查
- **多通知渠道** - 钉钉、邮件通知

#### 触发条件
```yaml
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
```

#### 环境变量配置
在GitHub仓库的Settings -> Secrets中配置：
```
STAGING_HOST=your-staging-server
STAGING_USER=deploy-user
STAGING_SSH_KEY=your-ssh-private-key
PRODUCTION_HOST=your-production-server
PRODUCTION_USER=deploy-user
PRODUCTION_SSH_KEY=your-ssh-private-key
DINGTALK_WEBHOOK_TOKEN=your-dingtalk-token
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-email-password
```

## 4. Docker部署方案

### 4.1 Docker Compose配置

项目包含三个Docker Compose配置文件：
- `docker/docker-compose.yml` - 开发环境
- `docker/docker-compose.prod.yml` - 生产环境
- `deploy.sh` - 部署脚本

### 4.2 服务组件

#### 4.2.1 MySQL数据库
```yaml
mysql:
  image: mysql:8.0
  container_name: fresh-delivery-mysql
  restart: unless-stopped
  environment:
    MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    MYSQL_DATABASE: fresh_delivery
  volumes:
    - mysql_data:/var/lib/mysql
    - ./mysql/init:/docker-entrypoint-initdb.d
  ports:
    - "3306:3306"
```

#### 4.2.2 Redis缓存
```yaml
redis:
  image: redis:6.2-alpine
  container_name: fresh-delivery-redis
  restart: unless-stopped
  command: redis-server --requirepass ${REDIS_PASSWORD}
  volumes:
    - redis_data:/data
  ports:
    - "6379:6379"
```

#### 4.2.3 后端服务
```yaml
backend:
  build:
    context: ../backend
    dockerfile: Dockerfile
  container_name: fresh-delivery-backend
  restart: unless-stopped
  environment:
    SPRING_PROFILES_ACTIVE: prod
    MYSQL_HOST: mysql
    REDIS_HOST: redis
  depends_on:
    - mysql
    - redis
  ports:
    - "8080:8080"
```

### 4.3 部署命令

#### 4.3.1 开发环境部署
```bash
# 启动所有服务
./deploy.sh start

# 停止所有服务
./deploy.sh stop

# 重启服务
./deploy.sh restart

# 查看日志
./deploy.sh logs

# 查看状态
./deploy.sh status
```

#### 4.3.2 生产环境部署
```bash
# 生产环境部署
docker-compose -f docker-compose.prod.yml up -d

# 更新服务
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d

# 备份数据
./deploy.sh backup

# 清理数据
./deploy.sh clean
```

## 5. Kubernetes部署方案

### 5.1 Kubernetes配置文件

项目已包含完整的Kubernetes部署配置：

- `k8s/namespace.yaml` - 命名空间配置
- `k8s/configmap.yaml` - 应用配置
- `k8s/secret.yaml` - 敏感信息配置
- `k8s/mysql.yaml` - MySQL数据库部署
- `k8s/redis.yaml` - Redis缓存部署
- `k8s/backend.yaml` - 后端服务部署
- `k8s/frontend.yaml` - 前端服务部署
- `k8s/nginx.yaml` - Nginx反向代理
- `k8s/ingress.yaml` - Ingress和SSL配置
- `k8s/monitoring.yaml` - 监控系统部署
- `k8s/deploy.sh` - 自动化部署脚本

### 5.2 部署步骤

#### 5.2.1 环境准备
```bash
# 1. 确保kubectl已配置
kubectl cluster-info

# 2. 设置环境变量
export DOCKER_REGISTRY_USER="your-username"
export DOCKER_REGISTRY_PASS="your-password"
export GRAFANA_ADMIN_PASSWORD="your-grafana-password"

# 3. 进入k8s目录
cd k8s
```

#### 5.2.2 完整部署
```bash
# 一键部署所有组件
./deploy.sh deploy
```

#### 5.2.3 分步部署
```bash
# 仅部署基础设施
./deploy.sh deploy-infra

# 仅部署应用服务
./deploy.sh deploy-app

# 仅部署监控系统
./deploy.sh deploy-monitor
```

### 5.3 运维管理

#### 5.3.1 应用更新
```bash
# 更新生产环境后端
./deploy.sh update prod backend

# 更新测试环境前端
./deploy.sh update staging frontend

# 更新所有组件
./deploy.sh update prod all
```

#### 5.3.2 日志查看
```bash
# 查看生产环境后端日志
./deploy.sh logs prod backend

# 查看测试环境前端日志（最近200行）
./deploy.sh logs staging frontend 200
```

#### 5.3.3 系统状态
```bash
# 检查系统状态
./deploy.sh status

# 数据备份
./deploy.sh backup
```

### 5.4 GitHub Actions + Kubernetes集成

`.github/workflows/ci-cd.yml`文件已配置完整的CI/CD流程：

1. **代码质量检查** - ESLint、单元测试、安全扫描
2. **Docker镜像构建** - 多阶段构建优化
3. **自动部署到测试环境** - 基于develop分支
4. **集成测试** - 自动化API测试
5. **生产环境部署** - 基于main分支，需要手动确认
6. **监控和通知** - 钉钉、邮件通知

### 5.5 监控和告警

监控系统包含：
- **Prometheus** - 指标收集和存储
- **Grafana** - 可视化仪表板
- **AlertManager** - 告警管理
- **各种Exporter** - MySQL、Redis、应用指标

访问地址：
- Grafana: `http://<grafana-service-ip>:3000`
- Prometheus: `http://<prometheus-service-ip>:9090`

## 6. 监控与运维

### 6.1 监控指标

#### 6.1.1 应用指标
- **QPS** - 每秒请求数
- **响应时间** - 平均响应时间、P95、P99
- **错误率** - HTTP 4xx、5xx错误率
- **JVM指标** - 堆内存、GC时间、线程数

#### 6.1.2 基础设施指标
- **CPU使用率** - 节点和Pod CPU使用情况
- **内存使用率** - 内存使用和缓存情况
- **磁盘IO** - 读写IOPS和延迟
- **网络流量** - 入站出站流量

#### 6.1.3 业务指标
- **用户活跃度** - DAU、MAU
- **订单量** - 每日订单数、成功率
- **支付成功率** - 支付转化率
- **库存预警** - 低库存商品数量

### 6.2 告警规则

#### 6.2.1 系统告警
```yaml
# CPU使用率过高
- alert: HighCPUUsage
  expr: cpu_usage_percent > 80
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "CPU使用率过高"
    description: "{{ $labels.instance }} CPU使用率超过80%"

# 内存使用率过高
- alert: HighMemoryUsage
  expr: memory_usage_percent > 85
  for: 5m
  labels:
    severity: critical
  annotations:
    summary: "内存使用率过高"
    description: "{{ $labels.instance }} 内存使用率超过85%"
```

#### 6.2.2 应用告警
```yaml
# 应用响应时间过长
- alert: HighResponseTime
  expr: http_request_duration_seconds{quantile="0.95"} > 2
  for: 2m
  labels:
    severity: warning
  annotations:
    summary: "应用响应时间过长"
    description: "95%请求响应时间超过2秒"

# 错误率过高
- alert: HighErrorRate
  expr: rate(http_requests_total{status=~"5.."}[5m]) / rate(http_requests_total[5m]) > 0.05
  for: 2m
  labels:
    severity: critical
  annotations:
    summary: "应用错误率过高"
    description: "5xx错误率超过5%"
```

### 6.3 日志管理

#### 6.3.1 日志收集
```yaml
# Filebeat配置
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/fresh-delivery/*.log
  fields:
    service: fresh-delivery
    environment: production

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "fresh-delivery-%{+yyyy.MM.dd}"
```

#### 6.3.2 日志分析
```bash
# 查看错误日志
kubectl logs -l app=backend --tail=100 | grep ERROR

# 查看特定时间段日志
kubectl logs -l app=backend --since=1h

# 实时查看日志
kubectl logs -f deployment/backend
```

### 6.4 性能优化

#### 6.4.1 数据库优化
```sql
-- 慢查询分析
SELECT * FROM mysql.slow_log WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 HOUR);

-- 索引优化
EXPLAIN SELECT * FROM orders WHERE user_id = 123 AND status = 'pending';

-- 连接池配置
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

#### 6.4.2 缓存优化
```java
// Redis缓存配置
@Cacheable(value = "products", key = "#id")
public Product getProductById(Long id) {
    return productRepository.findById(id);
}

// 缓存预热
@PostConstruct
public void warmUpCache() {
    List<Product> hotProducts = productService.getHotProducts();
    hotProducts.forEach(product -> 
        redisTemplate.opsForValue().set("product:" + product.getId(), product)
    );
}
```

## 7. 故障排查

### 7.1 常见问题

#### 7.1.1 应用无法启动
```bash
# 检查Pod状态
kubectl describe pod <pod-name> -n <namespace>

# 查看启动日志
kubectl logs <pod-name> -n <namespace> --previous

# 检查配置
kubectl get configmap -n <namespace>
kubectl get secret -n <namespace>
```

#### 7.1.2 服务无法访问
```bash
# 检查服务状态
kubectl get svc -n <namespace>
kubectl get endpoints -n <namespace>

# 检查网络策略
kubectl get networkpolicy -n <namespace>

# 测试服务连通性
kubectl run test-pod --image=busybox --rm -it -- wget -qO- http://service-name:port
```

#### 7.1.3 数据库连接问题
```bash
# 检查数据库状态
kubectl exec -it deployment/mysql -- mysql -u root -p -e "SHOW PROCESSLIST;"

# 检查连接数
kubectl exec -it deployment/mysql -- mysql -u root -p -e "SHOW STATUS LIKE 'Threads_connected';"

# 检查慢查询
kubectl exec -it deployment/mysql -- mysql -u root -p -e "SELECT * FROM mysql.slow_log LIMIT 10;"
```

### 7.2 性能问题排查

#### 7.2.1 CPU和内存
```bash
# 查看资源使用情况
kubectl top nodes
kubectl top pods -n <namespace>

# 查看资源限制
kubectl describe pod <pod-name> -n <namespace> | grep -A 5 "Limits:"

# 查看HPA状态
kubectl get hpa -n <namespace>
kubectl describe hpa <hpa-name> -n <namespace>
```

#### 7.2.2 网络问题
```bash
# 检查Ingress状态
kubectl describe ingress -n <namespace>

# 检查DNS解析
kubectl run test-dns --image=busybox --rm -it -- nslookup service-name

# 检查网络延迟
kubectl run test-ping --image=busybox --rm -it -- ping service-name
```

### 7.3 故障恢复

#### 7.3.1 应用回滚
```bash
# 查看部署历史
kubectl rollout history deployment/backend -n <namespace>

# 回滚到上一个版本
kubectl rollout undo deployment/backend -n <namespace>

# 回滚到指定版本
kubectl rollout undo deployment/backend --to-revision=2 -n <namespace>
```

#### 7.3.2 数据恢复
```bash
# 从备份恢复MySQL
kubectl exec -i deployment/mysql -- mysql -u root -p fresh_delivery < backup.sql

# 从备份恢复Redis
kubectl cp backup.rdb mysql-pod:/tmp/
kubectl exec -it deployment/redis -- redis-cli --rdb /tmp/backup.rdb
```

## 8. 最佳实践

### 8.1 安全最佳实践
- **定期更新依赖和基础镜像**
- **使用非root用户运行容器**
- **启用网络策略限制Pod间通信**
- **定期轮换密钥和证书**
- **实施最小权限原则**
- **使用Secret管理敏感信息**
- **启用RBAC权限控制**
- **定期进行安全扫描**

### 8.2 性能优化
- **合理设置资源请求和限制**
- **使用多阶段构建优化镜像大小**
- **启用应用和数据库连接池**
- **配置适当的缓存策略**
- **使用CDN加速静态资源**
- **配置HPA自动扩缩容**
- **使用PVC持久化存储**
- **优化数据库查询和索引**

### 8.3 监控和告警
- **设置关键指标监控**
- **配置多级告警机制**
- **定期检查和优化告警规则**
- **建立故障响应流程**
- **定期进行灾难恢复演练**
- **监控应用性能指标**
- **设置业务指标告警**
- **建立监控仪表板**

### 8.4 版本管理
- **使用语义化版本号**
- **维护详细的变更日志**
- **实施代码审查流程**
- **使用特性分支开发模式**
- **定期清理旧版本镜像**
- **使用镜像标签管理版本**
- **实施蓝绿部署策略**
- **建立回滚机制**

### 8.5 运维自动化
- **自动化部署流程**
- **自动化测试覆盖**
- **自动化监控告警**
- **自动化备份策略**
- **自动化扩缩容**
- **自动化故障恢复**
- **自动化安全扫描**
- **自动化合规检查**

---

## 总结

通过以上完整的CI/CD部署流程，包括Docker Compose和Kubernetes两种部署方案，可以实现边墙鲜送系统的：

✅ **自动化构建** - 代码提交后自动触发构建流程
✅ **自动化测试** - 单元测试、集成测试、安全扫描
✅ **自动化部署** - 多环境自动部署，支持回滚
✅ **自动化监控** - 全方位监控告警，及时发现问题
✅ **高可用架构** - 支持水平扩展，故障自动恢复
✅ **安全保障** - 多层安全防护，定期安全扫描
✅ **运维便捷** - 一键部署，可视化管理

无论是单机部署还是集群部署，都能满足不同规模的业务需求，大大提高开发效率和系统稳定性。

---

**文档版本**: v1.0.0  
**最后更新**: 2024年1月  
**维护团队**: DevOps Team