# 边墙鲜送 - 小区蔬果跑腿助手

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.0-4FC08D.svg)](https://vuejs.org/)
[![WeChat](https://img.shields.io/badge/WeChat-MiniProgram-07C160.svg)](https://developers.weixin.qq.com/miniprogram/dev/framework/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)

## 项目概述
专为榆林市榆阳区边墙小区居民打造的便捷线上蔬菜水果采购平台，提供高效的买菜服务。

> 🚀 **完整的生鲜配送解决方案**：包含微信小程序、管理后台、后端API和Docker部署方案

## 技术架构

### 后端服务
- **框架**: Spring Boot 2.7.0
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.0
- **持久化**: MyBatis Plus
- **认证**: JWT Token

### 前端管理系统
- **框架**: Vue 3 + ElementUI Plus
- **构建工具**: Vite
- **状态管理**: Pinia

### 微信小程序
- **框架**: 原生微信小程序
- **UI组件**: WeUI

### 持续集成
- **工具**: Jenkins
- **部署**: Docker + Nginx

## 项目结构

```
biangqiang-fresh-delivery/
├── backend/                 # Spring Boot 后端服务
├── admin-frontend/          # Vue 后台管理系统
├── miniprogram/            # 微信小程序
├── docker/                 # Docker 配置
├── jenkins/                # Jenkins 配置
└── docs/                   # 项目文档
```

## 快速开始

### 后端服务启动
```bash
cd backend
mvn spring-boot:run
```

### 前端管理系统启动
```bash
cd admin-frontend
npm install
npm run dev
```

### 微信小程序
使用微信开发者工具导入 miniprogram 目录

## 功能特性

### 用户端功能
- 微信快捷登录
- 商品浏览与搜索
- 购物车管理
- 订单下单与跟踪
- 地址管理
- 订单评价

### 管理端功能
- 用户管理
- 商品管理（上架/下架/编辑）
- 订单管理与状态更新
- 库存管理与预警
- 数据统计与分析
- 进货清单生成

## 接口文档
后端服务启动后访问: http://localhost:8080/swagger-ui.html

## 部署说明

### Docker 一键部署
```bash
# Linux/macOS
./deploy.sh start

# Windows
deploy.bat start
```
详见 [Docker 部署指南](README-Docker.md)

### 传统部署
详见 `docs/deployment.md`

## GitHub 上传指南

如果您想将此项目上传到 GitHub，请参考：
- 📖 [详细上传指南](github-upload-guide.md)
- 🚀 [一键初始化脚本](init-git.bat)（Windows）
- 🚀 [一键初始化脚本](init-git.sh)（Linux/macOS）

### 快速上传步骤

1. **运行初始化脚本**
   ```bash
   # Windows
   init-git.bat
   
   # Linux/macOS
   chmod +x init-git.sh
   ./init-git.sh
   ```

2. **在 GitHub 创建仓库**
   - 仓库名：`biangqiang-fresh-delivery`
   - 描述：`边墙鲜送系统 - 微信小程序生鲜配送平台`

3. **连接并推送**
   ```bash
   git remote add origin https://github.com/你的用户名/biangqiang-fresh-delivery.git
   git push -u origin main
   ```

## 项目特色

- ✅ **完整的微服务架构**：前端、后端、小程序分离
- ✅ **Docker 一键部署**：支持容器化部署
- ✅ **详细的文档**：包含部署、API、开发指南
- ✅ **CI/CD 支持**：Jenkins 自动化部署
- ✅ **生产就绪**：包含监控、日志、安全配置
- ✅ **开源友好**：MIT 许可证，支持二次开发

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系我们

- 📧 邮箱：your-email@example.com
- 🐛 问题反馈：[GitHub Issues](https://github.com/你的用户名/biangqiang-fresh-delivery/issues)
- 📖 项目文档：[Wiki](https://github.com/你的用户名/biangqiang-fresh-delivery/wiki)

---

⭐ 如果这个项目对您有帮助，请给我们一个 Star！#   b q x s  
 