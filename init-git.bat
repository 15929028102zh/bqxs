@echo off
chcp 65001 >nul
echo ========================================
echo 边墙鲜送系统 Git 初始化脚本
echo ========================================
echo.

:: 检查是否已安装 Git
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Git，请先安装 Git
    echo 下载地址: https://git-scm.com/download/windows
    pause
    exit /b 1
)

echo [信息] Git 已安装
echo.

:: 检查是否已初始化 Git 仓库
if exist ".git" (
    echo [警告] Git 仓库已存在
    echo.
    goto :configure
)

echo [步骤 1] 初始化 Git 仓库...
git init
if %errorlevel% neq 0 (
    echo [错误] Git 初始化失败
    pause
    exit /b 1
)
echo [完成] Git 仓库初始化成功
echo.

:configure
echo [步骤 2] 配置 Git 用户信息
echo 请输入您的 GitHub 用户名:
set /p username="用户名: "
echo 请输入您的邮箱地址:
set /p email="邮箱: "

if "%username%"=="" (
    echo [错误] 用户名不能为空
    pause
    exit /b 1
)

if "%email%"=="" (
    echo [错误] 邮箱不能为空
    pause
    exit /b 1
)

git config --global user.name "%username%"
git config --global user.email "%email%"
echo [完成] Git 用户信息配置成功
echo.

echo [步骤 3] 添加文件到暂存区...
git add .
if %errorlevel% neq 0 (
    echo [错误] 添加文件失败
    pause
    exit /b 1
)
echo [完成] 文件添加成功
echo.

echo [步骤 4] 创建初始提交...
git commit -m "初始提交：边墙鲜送系统完整项目"
if %errorlevel% neq 0 (
    echo [错误] 提交失败
    pause
    exit /b 1
)
echo [完成] 初始提交成功
echo.

echo [步骤 5] 设置主分支...
git branch -M main
echo [完成] 主分支设置成功
echo.

echo ========================================
echo Git 初始化完成！
echo ========================================
echo.
echo 接下来请按照以下步骤操作：
echo.
echo 1. 在 GitHub 上创建新仓库 'biangqiang-fresh-delivery'
echo 2. 复制仓库 URL
echo 3. 运行以下命令连接远程仓库：
echo.
echo    git remote add origin https://github.com/%username%/biangqiang-fresh-delivery.git
echo    git push -u origin main
echo.
echo 详细步骤请参考: github-upload-guide.md
echo.
echo 按任意键退出...
pause >nul