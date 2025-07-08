# 边墙鲜送系统 GitHub 上传指南

本指南将帮助您将「边墙鲜送系统」项目上传到 GitHub。

## 前置要求

1. **安装 Git**
   - 下载并安装 Git：https://git-scm.com/download/windows
   - 验证安装：`git --version`

2. **GitHub 账号**
   - 注册 GitHub 账号：https://github.com
   - 配置 Git 用户信息：
     ```bash
     git config --global user.name "你的用户名"
     git config --global user.email "你的邮箱@example.com"
     ```

## 步骤一：创建 GitHub 仓库

1. 登录 GitHub
2. 点击右上角的 "+" 按钮，选择 "New repository"
3. 填写仓库信息：
   - **Repository name**: `biangqiang-fresh-delivery`
   - **Description**: `边墙鲜送系统 - 微信小程序生鲜配送平台`
   - **Visibility**: 选择 Public 或 Private
   - **不要勾选** "Add a README file"（因为项目已有 README）
4. 点击 "Create repository"

## 步骤二：初始化本地 Git 仓库

在项目根目录（`f:\code`）打开命令行，执行以下命令：

```bash
# 初始化 Git 仓库
git init

# 添加所有文件到暂存区
git add .

# 创建初始提交
git commit -m "初始提交：边墙鲜送系统完整项目"

# 设置主分支名称
git branch -M main
```

## 步骤三：连接远程仓库并推送

```bash
# 添加远程仓库（替换为你的 GitHub 用户名）
git remote add origin https://github.com/你的用户名/biangqiang-fresh-delivery.git

# 推送到 GitHub
git push -u origin main
```

## 步骤四：验证上传

1. 刷新 GitHub 仓库页面
2. 确认所有文件已成功上传
3. 检查 README.md 是否正确显示

## 项目结构说明

上传后的项目包含以下主要组件：

```
biangqiang-fresh-delivery/
├── backend/              # Spring Boot 后端服务
├── admin-frontend/       # Vue.js 管理后台
├── miniprogram/         # 微信小程序
├── docker/              # Docker 部署配置
├── docs/                # 项目文档
├── jenkins/             # CI/CD 配置
├── deploy.sh            # Linux 部署脚本
├── deploy.bat           # Windows 部署脚本
├── README.md            # 项目说明
├── README-Docker.md     # Docker 部署指南
└── .gitignore           # Git 忽略文件
```

## 后续操作

### 更新代码

```bash
# 添加修改的文件
git add .

# 提交更改
git commit -m "描述你的更改"

# 推送到 GitHub
git push
```

### 创建分支

```bash
# 创建并切换到新分支
git checkout -b feature/新功能名称

# 推送新分支到 GitHub
git push -u origin feature/新功能名称
```

### 克隆项目

其他人可以通过以下命令克隆项目：

```bash
git clone https://github.com/你的用户名/biangqiang-fresh-delivery.git
```

## 注意事项

1. **敏感信息**：确保 `.env` 文件和数据库密码等敏感信息不会被上传
2. **大文件**：Maven 依赖包和构建产物已在 `.gitignore` 中排除
3. **分支管理**：建议使用分支进行功能开发，主分支保持稳定
4. **提交信息**：使用清晰的提交信息，便于项目维护

## 常见问题

### 1. 推送失败

如果遇到推送失败，可能是因为：
- 网络问题：尝试使用 VPN 或更换网络
- 认证问题：确保 GitHub 用户名和密码正确
- 仓库权限：确保有推送权限

### 2. 文件过大

如果有大文件无法推送：
```bash
# 查看大文件
git ls-files --others --ignored --exclude-standard

# 移除大文件并重新提交
git rm --cached 大文件名
git commit -m "移除大文件"
```

### 3. 合并冲突

如果多人协作出现冲突：
```bash
# 拉取最新代码
git pull origin main

# 解决冲突后提交
git add .
git commit -m "解决合并冲突"
git push
```

## 项目特色

- ✅ **完整的微服务架构**：前端、后端、小程序分离
- ✅ **Docker 一键部署**：支持容器化部署
- ✅ **详细的文档**：包含部署、API、开发指南
- ✅ **CI/CD 支持**：Jenkins 自动化部署
- ✅ **生产就绪**：包含监控、日志、安全配置

## 获取帮助

- GitHub Issues：在仓库中创建 Issue
- 项目文档：查看 `docs/` 目录
- 部署指南：参考 `README-Docker.md`

---

**祝您使用愉快！** 🎉