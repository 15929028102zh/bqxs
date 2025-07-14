# 边墙鲜送系统 - 工具和问题解决指南

## 🚀 快速开始

### 一键管理工具
运行项目管理工具，获得完整的项目管理界面：
```bash
# Windows
project-manager.bat

# 或者双击运行
```

## 🛠️ 工具列表

### 1. 项目管理工具 (`project-manager.bat`)
**功能**: 集成所有项目管理操作的主控制台
- 网络连接诊断和修复
- 代码质量检查和优化
- Docker 部署管理
- Git 操作管理
- 项目构建和测试
- 系统监控和日志
- 数据库管理
- 项目状态概览

### 2. 网络修复工具 (`network-fix.bat`)
**功能**: 解决 GitHub 连接问题
- 网络连接测试
- DNS 解析修复
- Git 代理设置管理
- SSH/HTTPS 协议切换
- GitHub hosts 配置
- SSL 证书问题修复

**使用场景**:
- `Failed to connect to github.com` 错误
- `Permission denied (publickey)` 错误
- Git push/pull 超时
- DNS 解析失败

### 3. 代码质量检查工具 (`quality-check.bat`)
**功能**: 全面的代码质量分析
- 项目结构检查
- 开发工具验证
- 后端代码质量检查（编译、测试、规范）
- 前端代码质量检查（ESLint、测试、构建）
- 安全漏洞扫描
- 代码复杂度分析
- Git 仓库健康检查
- 质量报告生成

### 4. Docker 部署脚本
#### Linux 部署 (`docker-deploy.sh`)
```bash
chmod +x docker-deploy.sh
./docker-deploy.sh
```

#### Windows 部署 (`docker-deploy.bat`)
```bash
docker-deploy.bat
```

**功能**:
- 自动环境检查
- 服务器配置
- SSL 证书生成
- 数据库备份
- 服务部署
- 健康检查

## 🔧 常见问题解决方案

### GitHub 连接问题

#### 问题 1: `Failed to connect to github.com port 443`
**解决方案**:
1. 运行 `network-fix.bat`
2. 或手动执行：
```bash
# 清除代理设置
git config --global --unset http.proxy
git config --global --unset https.proxy

# 切换到 HTTPS
git remote set-url origin https://github.com/15929028102zh/bqxs.git

# 优化 Git 配置
git config --global http.postBuffer 524288000
git config --global http.lowSpeedLimit 0
git config --global http.lowSpeedTime 999999
```

#### 问题 2: `Permission denied (publickey)`
**解决方案**:
1. 切换到 HTTPS 协议：
```bash
git remote set-url origin https://github.com/15929028102zh/bqxs.git
```
2. 或配置 SSH 密钥：
```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
cat ~/.ssh/id_ed25519.pub
# 将公钥添加到 GitHub Settings > SSH Keys
```

#### 问题 3: DNS 解析失败
**解决方案**:
1. 刷新 DNS 缓存：
```bash
ipconfig /flushdns
```
2. 修改 hosts 文件（需管理员权限）：
```
# 添加到 C:\Windows\System32\drivers\etc\hosts
140.82.112.3 github.com
140.82.112.4 api.github.com
185.199.108.153 assets-cdn.github.com
140.82.112.9 codeload.github.com
```

### 代码质量问题

#### 问题 1: 编译失败
**解决方案**:
1. 检查 Java 版本：
```bash
java -version
# 确保使用 Java 11 或更高版本
```
2. 清理并重新编译：
```bash
cd backend
mvn clean compile
```

#### 问题 2: 测试失败
**解决方案**:
1. 运行特定测试：
```bash
mvn test -Dtest=ClassName
```
2. 跳过测试构建：
```bash
mvn clean package -DskipTests
```

#### 问题 3: 前端构建失败
**解决方案**:
1. 清理依赖：
```bash
cd admin-frontend
rm -rf node_modules package-lock.json
npm install
```
2. 检查 Node.js 版本：
```bash
node --version
# 确保使用 Node.js 16+ 版本
```

### Docker 部署问题

#### 问题 1: Docker 服务未启动
**解决方案**:
```bash
# Windows
net start docker

# 或启动 Docker Desktop
```

#### 问题 2: 端口占用
**解决方案**:
```bash
# 查看端口占用
netstat -ano | findstr :8080

# 停止占用进程
taskkill /PID <PID> /F
```

#### 问题 3: 容器启动失败
**解决方案**:
```bash
# 查看容器日志
docker logs <container_name>

# 重新构建镜像
docker-compose build --no-cache

# 重启服务
docker-compose restart
```

## 📊 监控和维护

### 服务健康检查
```bash
# 检查所有容器状态
docker ps -a

# 检查服务响应
curl http://localhost:8080/health
curl http://localhost:3000

# 查看资源使用
docker stats
```

### 日志管理
```bash
# 查看应用日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 查看最近的日志
docker-compose logs --tail=100 backend
```

### 数据库维护
```bash
# 连接数据库
docker exec -it fresh-delivery-mysql mysql -u root -p

# 备份数据库
docker exec fresh-delivery-mysql mysqldump -u root -p123456 fresh_delivery > backup.sql

# 恢复数据库
docker exec -i fresh-delivery-mysql mysql -u root -p123456 fresh_delivery < backup.sql
```

## 🔐 安全最佳实践

### 1. 环境变量管理
- 使用 `.env` 文件存储敏感配置
- 不要将 `.env` 文件提交到版本控制
- 定期更换密码和密钥

### 2. 网络安全
- 使用 HTTPS 协议
- 配置防火墙规则
- 定期更新 SSL 证书

### 3. 代码安全
- 定期运行安全扫描
- 及时更新依赖包
- 使用强密码策略

## 📈 性能优化建议

### 1. 数据库优化
```sql
-- 添加索引
CREATE INDEX idx_user_phone ON users(phone);
CREATE INDEX idx_order_status ON orders(status);

-- 查询优化
EXPLAIN SELECT * FROM orders WHERE user_id = 1;
```

### 2. 缓存策略
```java
// Redis 缓存配置
@Cacheable(value = "products", key = "#id")
public Product getProductById(Long id) {
    return productRepository.findById(id);
}
```

### 3. 前端优化
```javascript
// 代码分割
const ProductList = () => import('@/components/ProductList.vue')

// 图片懒加载
<img v-lazy="product.image" :alt="product.name" />
```

## 🚀 部署流程

### 开发环境
1. 克隆代码：`git clone https://github.com/15929028102zh/bqxs.git`
2. 安装依赖：运行 `quality-check.bat` 检查环境
3. 启动服务：`docker-compose up -d`

### 生产环境
1. 服务器准备：确保 Docker 和 Docker Compose 已安装
2. 代码部署：`git pull origin main`
3. 环境配置：复制并修改 `.env.example` 为 `.env`
4. 服务部署：运行 `docker-deploy.sh` 或 `docker-deploy.bat`
5. 健康检查：访问 `https://8.154.40.188`

## 📞 技术支持

### 问题报告
如果遇到问题，请提供以下信息：
1. 错误信息截图
2. 操作系统版本
3. Docker 版本
4. 执行的命令
5. 相关日志文件

### 联系方式
- 项目仓库：https://github.com/15929028102zh/bqxs
- 服务器地址：https://8.154.40.188
- 文档位置：`f:\code\docs\`

## 📝 更新日志

### v1.0.0 (当前版本)
- ✅ 完整的项目管理工具套件
- ✅ 网络连接问题自动修复
- ✅ 代码质量自动检查
- ✅ Docker 一键部署
- ✅ 生产环境配置
- ✅ 监控和日志管理
- ✅ 数据库管理工具
- ✅ 安全配置和最佳实践

---

**提示**: 建议将此文档加入书签，作为项目开发和维护的快速参考指南！