@echo off
echo ========================================
echo Git 用户配置脚本
echo ========================================
echo.
echo 当前遇到的错误是因为 Git 不知道您的身份信息。
echo 请按照提示输入您的姓名和邮箱地址。
echo.

echo 配置 Git 用户信息...
set /p user_name="请输入您的姓名（例如：张三）: "
set /p user_email="请输入您的邮箱（例如：zhangsan@example.com）: "

echo.
echo 正在配置 Git 用户信息...
git config --global user.name "%user_name%"
git config --global user.email "%user_email%"

if %errorlevel% neq 0 (
    echo 错误: 无法配置用户信息
    pause
    exit /b 1
)

echo ✓ Git 用户信息配置完成
echo.
echo 配置结果:
echo 姓名: %user_name%
echo 邮箱: %user_email%
echo.
echo 现在您可以重新运行 git commit 命令了:
echo git commit -m "Initial commit"
echo.
echo 按任意键退出...
pause >nul