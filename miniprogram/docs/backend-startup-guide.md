# 后端服务启动指南

## 问题描述

当小程序登录时出现以下错误：
```
POST http://192.168.229.1:8081/api/user/login 404
网络请求失败
```

这表示后端服务没有启动或无法访问。

## 解决方案

### 方案一：使用Docker启动（推荐）

1. **启动Docker Desktop**
   - 在Windows开始菜单中找到并启动"Docker Desktop"
   - 等待Docker Desktop完全启动（系统托盘图标变为绿色）

2. **启动后端服务**
   ```bash
   cd f:\code\docker
   docker-compose up -d mysql redis
   docker-compose up -d backend
   ```

3. **验证服务状态**
   ```bash
   docker-compose ps
   ```
   确保所有服务状态为"Up"

### 方案二：本地启动

1. **安装依赖**
   - 安装Java 8或更高版本
   - 安装Maven 3.6+
   - 安装MySQL 8.0
   - 安装Redis 6.0+

2. **配置数据库**
   - 启动MySQL服务
   - 创建数据库：`fresh_delivery`
   - 导入初始化SQL脚本

3. **启动Redis**
   ```bash
   redis-server
   ```

4. **启动后端服务**
   ```bash
   cd f:\code\backend
   mvn spring-boot:run
   ```

### 方案三：修改配置使用在线API（临时方案）

如果无法启动本地后端服务，可以临时修改小程序配置：

1. 打开 `f:\code\miniprogram\app.js`
2. 修改 `baseUrl` 为测试服务器地址：
   ```javascript
   globalData: {
     // baseUrl: 'http://192.168.229.1:8081/api',  // 本地服务
     baseUrl: 'https://your-test-server.com/api',  // 测试服务器
   }
   ```

## 验证服务

启动后端服务后，可以通过以下方式验证：

1. **浏览器访问**
   - 打开浏览器访问：`http://192.168.229.1:8081/api/health`
   - 应该返回服务状态信息

2. **查看日志**
   ```bash
   # Docker方式
   docker-compose logs backend
   
   # 本地方式
   tail -f f:\code\backend\logs\fresh-delivery.log
   ```

## 常见问题

### 1. Docker Desktop启动失败
- 确保Windows功能中启用了"Hyper-V"和"容器"
- 以管理员身份运行Docker Desktop
- 重启计算机后再试

### 2. 端口被占用
```bash
# 检查端口占用
netstat -ano | findstr :8081

# 结束占用进程
taskkill /PID <进程ID> /F
```

### 3. 数据库连接失败
- 检查MySQL服务是否启动
- 验证数据库用户名密码
- 确认数据库名称正确

### 4. Redis连接失败
- 检查Redis服务是否启动
- 验证Redis配置
- 确认端口6379未被占用

## 开发建议

1. **使用Docker Compose**：推荐使用Docker方式，可以一键启动所有依赖服务
2. **环境隔离**：开发、测试、生产环境使用不同的配置
3. **健康检查**：定期检查服务状态，及时发现问题
4. **日志监控**：关注应用日志，快速定位问题

## 联系支持

如果按照以上步骤仍无法解决问题，请：
1. 收集错误日志
2. 记录操作步骤
3. 联系技术支持团队