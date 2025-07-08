#!/bin/bash

# 边墙鲜送系统 Git 初始化脚本
# 适用于 Linux/macOS

set -e

echo "========================================"
echo "边墙鲜送系统 Git 初始化脚本"
echo "========================================"
echo

# 检查是否已安装 Git
if ! command -v git &> /dev/null; then
    echo "[错误] 未检测到 Git，请先安装 Git"
    echo "Ubuntu/Debian: sudo apt-get install git"
    echo "CentOS/RHEL: sudo yum install git"
    echo "macOS: brew install git"
    exit 1
fi

echo "[信息] Git 已安装"
echo

# 检查是否已初始化 Git 仓库
if [ -d ".git" ]; then
    echo "[警告] Git 仓库已存在"
    echo
else
    echo "[步骤 1] 初始化 Git 仓库..."
    git init
    echo "[完成] Git 仓库初始化成功"
    echo
fi

echo "[步骤 2] 配置 Git 用户信息"
read -p "请输入您的 GitHub 用户名: " username
read -p "请输入您的邮箱地址: " email

if [ -z "$username" ]; then
    echo "[错误] 用户名不能为空"
    exit 1
fi

if [ -z "$email" ]; then
    echo "[错误] 邮箱不能为空"
    exit 1
fi

git config --global user.name "$username"
git config --global user.email "$email"
echo "[完成] Git 用户信息配置成功"
echo

echo "[步骤 3] 添加文件到暂存区..."
git add .
echo "[完成] 文件添加成功"
echo

echo "[步骤 4] 创建初始提交..."
git commit -m "初始提交：边墙鲜送系统完整项目"
echo "[完成] 初始提交成功"
echo

echo "[步骤 5] 设置主分支..."
git branch -M main
echo "[完成] 主分支设置成功"
echo

echo "========================================"
echo "Git 初始化完成！"
echo "========================================"
echo
echo "接下来请按照以下步骤操作："
echo
echo "1. 在 GitHub 上创建新仓库 'biangqiang-fresh-delivery'"
echo "2. 复制仓库 URL"
echo "3. 运行以下命令连接远程仓库："
echo
echo "   git remote add origin https://github.com/$username/biangqiang-fresh-delivery.git"
echo "   git push -u origin main"
echo
echo "详细步骤请参考: github-upload-guide.md"
echo
echo "按 Enter 键退出..."
read