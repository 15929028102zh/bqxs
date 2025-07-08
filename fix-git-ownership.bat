@echo off
chcp 65001 >nul
echo ========================================
echo Git 所有权问题修复脚本
echo ========================================
echo.

echo [信息] 检测到 Git "dubious ownership" 错误
echo [信息] 这是 Windows 系统上的常见问题
echo.

echo [步骤 1] 修复 Git 安全目录配置...
git config --global --add safe.directory F:/code
if %errorlevel% neq 0 (
    echo [错误] 配置安全目录失败
    pause
    exit /b 1
)
echo [完成] Git 安全目录配置成功
echo.

echo [步骤 2] 验证 Git 状态...
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
    echo [错误] 设置主分支失败
    pause
    exit /b 1
)
echo [完成] 主分支设置成功
echo.

echo ========================================
echo Git 所有权问题修复完成！
echo ========================================
echo.
echo 现在您可以正常使用 Git 命令了：
echo.
echo   git remote add origin https://github.com/你的用户名/bqxs.git
echo   git push -u origin main
echo.
echo 按任意键退出...
pause >nul