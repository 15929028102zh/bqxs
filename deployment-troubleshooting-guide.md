# 边墙鲜送系统部署故障排查指南

## 已修复的问题

### 1. 前端镜像构建失败 - vite: not found

**问题描述：**
```
sh: vite: not found
ERROR: failed to solve: process "/bin/sh -c npm run build" did not complete successfully: exit code 127
```

**根本原因：**
- 前端 Dockerfile 中使用了 `npm ci --only=production`
- 这会跳过 devDependencies，但 vite 构建工具在 devDependencies 中
- 构建阶段需要 vite 来执行 `npm run build`

**解决方案：**
```dockerfile
# 修改前（错误）
RUN npm ci --only=production

# 修改后（正确）
RUN npm ci
```

### 2. 后端镜像构建被跳过

**问题描述：**
```
[WARN] 后端Dockerfile不存在，跳过后端镜像构建
```

**根本原因：**
- 部署脚本检查的路径是 `$PROJECT_ROOT/Dockerfile`
- 实际后端 Dockerfile 位于 `$PROJECT_ROOT/backend/Dockerfile`

**解决方案：**
```bash
# 修改前（错误）
if [ -f "$PROJECT_ROOT/Dockerfile" ]; then
    docker build -t "$backend_image" "$PROJECT_ROOT"

# 修改后（正确）
if [ -f "$PROJECT_ROOT/backend/Dockerfile" ]; then
    docker build -t "$backend_image" "$PROJECT_ROOT/backend"
```

## 完整部署流程

### 1. 环境准备

```bash
# 初始化环境
./deploy-automation.sh init

# 检查配置文件
source deployment-config.env
echo "项目版本: $PROJECT_VERSION"
echo "Docker Registry: $DOCKER_REGISTRY"
```

### 2. 构建镜像

```bash
# 构建所有镜像
./deploy-automation.sh build

# 或分别构建
# 后端镜像
cd backend
docker build -t ghcr.io/your-username/fresh-delivery-backend:1.0.0 .

# 前端镜像
cd admin-frontend
docker build -t ghcr.io/your-username/fresh-delivery-frontend:1.0.0 .
```

### 3. 部署选项

#### Docker Compose 部署（推荐用于开发/测试）

```bash
# 完整部署
./deploy-automation.sh deploy --mode docker

# 手动部署
docker-compose -f docker-compose.prod.yml up -d
```

#### Kubernetes 部署（推荐用于生产）

```bash
# K8s部署
./deploy-automation.sh deploy --mode k8s

# 手动部署
cd k8s
./deploy.sh deploy
```

### 4. 验证部署

```bash
# 检查服务状态
./deploy-automation.sh status

# 查看日志
./deploy-automation.sh logs

# 健康检查
./deploy-automation.sh health
```

## 常见问题排查

### 1. Docker 构建问题

**问题：** 依赖安装失败
```bash
# 清理Docker缓存
docker system prune -f
docker builder prune -f

# 重新构建（不使用缓存）
docker build --no-cache -t image_name .
```

**问题：** 网络连接超时
```bash
# 配置Docker镜像源
echo '{
  "registry-mirrors": ["https://docker.mirrors.ustc.edu.cn"]
}' > /etc/docker/daemon.json
sudo systemctl restart docker
```

### 2. 前端构建问题

**问题：** Node.js 版本不兼容
```dockerfile
# 确保使用正确的Node版本
FROM node:18-alpine as build-stage
```

**问题：** 内存不足
```dockerfile
# 增加Node.js内存限制
RUN NODE_OPTIONS="--max-old-space-size=4096" npm run build
```

### 3. 后端构建问题

**问题：** Maven 依赖下载失败
```dockerfile
# 添加Maven镜像源
RUN mkdir -p /root/.m2 && \
    echo '<settings><mirrors><mirror><id>alimaven</id><name>aliyun maven</name><url>http://maven.aliyun.com/nexus/content/groups/public/</url><mirrorOf>central</mirrorOf></mirror></mirrors></settings>' > /root/.m2/settings.xml
```

**问题：** Java 版本不匹配
```dockerfile
# 确保使用正确的Java版本
FROM openjdk:17-jdk-slim
```

### 4. 部署后问题

**问题：** 服务无法访问
```bash
# 检查端口映射
docker ps
netstat -tlnp | grep :8080

# 检查防火墙
sudo ufw status
sudo firewall-cmd --list-ports
```

**问题：** 数据库连接失败
```bash
# 检查数据库状态
docker logs fresh-delivery-mysql

# 测试数据库连接
docker exec -it fresh-delivery-mysql mysql -u root -p
```

## 性能优化建议

### 1. Docker 镜像优化

```dockerfile
# 使用多阶段构建
FROM node:18-alpine as build-stage
# ... 构建阶段

FROM nginx:alpine as production-stage
# ... 生产阶段

# 减少镜像层数
RUN apt-get update && \
    apt-get install -y curl maven && \
    rm -rf /var/lib/apt/lists/*
```

### 2. 构建缓存优化

```dockerfile
# 先复制依赖文件，利用Docker缓存
COPY package*.json ./
RUN npm ci

# 再复制源代码
COPY . .
RUN npm run build
```

### 3. 资源限制

```yaml
# docker-compose.yml
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
        reservations:
          memory: 512M
          cpus: '0.25'
```

## 监控和日志

### 1. 应用监控

```bash
# 查看资源使用
docker stats

# 查看应用日志
docker logs -f fresh-delivery-backend
docker logs -f fresh-delivery-frontend
```

### 2. 健康检查

```bash
# 后端健康检查
curl http://localhost:8080/actuator/health

# 前端可用性检查
curl http://localhost:3000
```

### 3. 性能测试

```bash
# 使用部署脚本进行性能测试
./deploy-automation.sh test --url http://localhost:8080 --requests 1000

# 手动压力测试
ab -n 1000 -c 10 http://localhost:8080/api/health
```

## 回滚策略

### 1. Docker Compose 回滚

```bash
# 回滚到上一个版本
./deploy-automation.sh rollback

# 手动回滚
docker-compose -f docker-compose.prod.yml down
docker tag backup-image:latest current-image:latest
docker-compose -f docker-compose.prod.yml up -d
```

### 2. Kubernetes 回滚

```bash
# 查看部署历史
kubectl rollout history deployment/fresh-delivery-backend

# 回滚到上一个版本
kubectl rollout undo deployment/fresh-delivery-backend
```

## 安全建议

### 1. 镜像安全

```bash
# 扫描镜像漏洞
docker scan fresh-delivery-backend:latest

# 使用非root用户
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nextjs -u 1001
USER nextjs
```

### 2. 网络安全

```yaml
# 限制网络访问
networks:
  internal:
    driver: bridge
    internal: true
  external:
    driver: bridge
```

### 3. 密钥管理

```bash
# 使用Docker secrets
echo "my_secret" | docker secret create db_password -

# 在Kubernetes中使用secrets
kubectl create secret generic db-secret --from-literal=password=my_secret
```

## 总结

通过修复上述问题，现在可以成功构建和部署边墙鲜送系统：

1. ✅ 前端镜像构建问题已解决
2. ✅ 后端镜像路径问题已修复
3. ✅ 部署脚本功能完整
4. ✅ 提供了完整的故障排查指南

建议按照以下顺序进行部署：
1. 环境初始化
2. 构建镜像
3. 部署服务
4. 验证功能
5. 配置监控

如遇到其他问题，请参考本指南的故障排查部分或查看详细日志进行诊断。