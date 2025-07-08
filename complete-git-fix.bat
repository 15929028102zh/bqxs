@echo off
chcp 65001 >nul
echo ========================================
echo Git 完整问题修复脚本
echo ========================================
echo.

echo [步骤 1] 修复 Git 所有权问题...
git config --global --add safe.directory F:/code
echo [完成] Git 所有权问题已修复
echo.

echo [步骤 2] 配置 Git 用户信息...
set /p user_name="请输入您的姓名: "
set /p user_email="请输入您的邮箱: "

git config --global user.name "%user_name%"
git config --global user.email "%user_email%"

if %errorlevel% neq 0 (
    echo [错误] 无法配置用户信息
    pause
    exit /b 1
)
echo [完成] Git 用户信息配置完成
echo.

echo [步骤 3] 检查当前 Git 状态...
git status
echo.

echo [步骤 4] 检查是否有提交记录...
git log --oneline -n 1 2>nul
if %errorlevel% neq 0 (
    echo [信息] 没有找到提交记录，需要创建初始提交
    echo [步骤 3.1] 添加所有文件到暂存区...
    git add .
    if %errorlevel% neq 0 (
        echo [错误] 添加文件失败
        pause
        exit /b 1
    )
    echo [完成] 文件添加成功
    
    echo [步骤 3.2] 创建初始提交...
    git commit -m "初始提交：边墙鲜送系统完整项目"
    if %errorlevel% neq 0 (
        echo [错误] 创建提交失败
        pause
        exit /b 1
    )
    echo [完成] 初始提交创建成功
) else (
    echo [信息] 已存在提交记录
)
echo.

echo [步骤 5] 检查当前分支...
for /f "tokens=*" %%i in ('git branch --show-current 2^>nul') do set current_branch=%%i
if "%current_branch%"=="" (
    echo [信息] 当前不在任何分支上，创建并切换到 main 分支
    git checkout -b main
) else (
    echo [信息] 当前分支: %current_branch%
    if not "%current_branch%"=="main" (
        echo [步骤 4.1] 切换到 main 分支...
        git checkout -b main 2>nul
        if %errorlevel% neq 0 (
            git checkout main
        )
    )
)
echo.

echo [步骤 6] 设置主分支...
git branch -M main
echo [完成] 主分支设置完成
echo.

echo [步骤 7] 检查远程仓库配置...
git remote -v
echo.

echo [步骤 8] 检查是否可以推送...
git log --oneline -n 1 >nul 2>&1
if %errorlevel% equ 0 (
    echo [信息] 现在可以推送到远程仓库了
    echo.
    echo 如果还没有添加远程仓库，请运行：
    echo git remote add origin https://github.com/你的用户名/bqxs.git
    echo.
    echo 然后推送：
    echo git push -u origin main
    echo.
) else (
    echo [错误] 仍然没有可推送的提交
)

echo ========================================
echo Git 问题修复完成！
echo ========================================
echo.
echo 修复内容：
echo ✓ Git 所有权问题
echo ✓ 分支创建和切换
echo ✓ 初始提交（如果需要）
echo ✓ 主分支设置
echo.
echo 接下来的操作：
echo 1. 添加远程仓库（如果还没有）：
echo    git remote add origin https://github.com/你的用户名/bqxs.git
echo.
echo 2. 推送到 GitHub：
echo    git push -u origin main
echo.
echo 3. 查看详细指南：github-upload-guide.md
echo.
echo 按任意键退出...
pause >nul