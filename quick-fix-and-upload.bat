@echo off
chcp 65001 >nul
echo ========================================
echo 边墙鲜送系统 - 快速修复并上传到GitHub
echo ========================================
echo.

echo [步骤 1] 修复 Git 所有权问题...
git config --global --add safe.directory F:/code
if %errorlevel% neq 0 (
    echo [错误] 修复Git所有权失败
    pause
    exit /b 1
)
echo [完成] Git 所有权问题已修复
echo.

echo [步骤 2] 检查 Git 状态...
git status
if %errorlevel% neq 0 (
    echo [错误] Git 状态检查失败
    pause
    exit /b 1
)
echo [完成] Git 状态正常
echo.

echo [步骤 3] 设置主分支...
git branch -M main
if %errorlevel% neq 0 (
    echo [警告] 主分支可能已存在，继续执行...
fi
echo [完成] 主分支设置完成
echo.

echo [步骤 4] 检查远程仓库配置...
git remote -v
echo.

echo [信息] 如果还没有添加远程仓库，请运行：
echo git remote add origin https://github.com/你的用户名/bqxs.git
echo.
echo [信息] 然后推送到GitHub：
echo git push -u origin main
echo.

echo ========================================
echo 修复完成！现在可以正常使用Git了
echo ========================================
echo.
echo 接下来的操作：
echo 1. 如果还没有添加远程仓库，运行上面显示的命令
echo 2. 如果已经添加了远程仓库，直接运行: git push -u origin main
echo 3. 查看详细指南: github-upload-guide.md
echo.
echo 按任意键退出...
pause >nul