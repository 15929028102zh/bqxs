@echo off
chcp 65001 >nul
echo ========================================
echo    GitHub 连接问题诊断和修复工具
echo ========================================
echo.

:: 检查管理员权限
net session >nul 2>&1
if %errorLevel% == 0 (
    echo [✓] 已获得管理员权限
) else (
    echo [!] 需要管理员权限，请右键以管理员身份运行
    pause
    exit /b 1
)

echo.
echo [1/6] 检查网络连接状态...
ping -n 1 8.8.8.8 >nul 2>&1
if %errorLevel% == 0 (
    echo [✓] 网络连接正常
) else (
    echo [✗] 网络连接异常，请检查网络设置
    pause
    exit /b 1
)

echo.
echo [2/6] 测试 GitHub 连接...
ping -n 1 github.com >nul 2>&1
if %errorLevel% == 0 (
    echo [✓] GitHub 域名解析正常
) else (
    echo [!] GitHub 域名解析失败，尝试修复 DNS...
    
    :: 刷新 DNS 缓存
    ipconfig /flushdns >nul 2>&1
    echo [✓] DNS 缓存已刷新
    
    :: 添加 GitHub hosts 记录
    echo [!] 添加 GitHub hosts 记录...
    echo 140.82.112.3 github.com >> C:\Windows\System32\drivers\etc\hosts
    echo 140.82.112.4 api.github.com >> C:\Windows\System32\drivers\etc\hosts
    echo 185.199.108.153 assets-cdn.github.com >> C:\Windows\System32\drivers\etc\hosts
    echo 140.82.112.9 codeload.github.com >> C:\Windows\System32\drivers\etc\hosts
    echo [✓] GitHub hosts 记录已添加
)

echo.
echo [3/6] 检查 Git 配置...
git --version >nul 2>&1
if %errorLevel% == 0 (
    echo [✓] Git 已安装
    git --version
) else (
    echo [✗] Git 未安装，请先安装 Git
    pause
    exit /b 1
)

echo.
echo [4/6] 检查 Git 代理设置...
for /f "tokens=*" %%i in ('git config --global --get http.proxy 2^>nul') do set HTTP_PROXY=%%i
for /f "tokens=*" %%i in ('git config --global --get https.proxy 2^>nul') do set HTTPS_PROXY=%%i

if defined HTTP_PROXY (
    echo [!] 检测到 HTTP 代理: %HTTP_PROXY%
    echo [?] 是否清除代理设置? (y/n)
    set /p clear_proxy=
    if /i "!clear_proxy!"=="y" (
        git config --global --unset http.proxy
        git config --global --unset https.proxy
        echo [✓] 代理设置已清除
    )
) else (
    echo [✓] 未检测到代理设置
)

echo.
echo [5/6] 配置 Git 使用 HTTPS...
cd /d f:\code
if exist .git (
    echo [✓] 检测到 Git 仓库
    
    :: 检查当前远程仓库地址
    for /f "tokens=*" %%i in ('git remote get-url origin 2^>nul') do set REMOTE_URL=%%i
    if defined REMOTE_URL (
        echo [i] 当前远程仓库: !REMOTE_URL!
        
        :: 如果是 SSH 地址，转换为 HTTPS
        echo !REMOTE_URL! | findstr "git@github.com" >nul
        if !errorLevel! == 0 (
            echo [!] 检测到 SSH 地址，转换为 HTTPS...
            set NEW_URL=!REMOTE_URL:git@github.com:=https://github.com/!
            git remote set-url origin !NEW_URL!
            echo [✓] 远程仓库地址已更新为: !NEW_URL!
        ) else (
            echo [✓] 已使用 HTTPS 地址
        )
    )
) else (
    echo [!] 未检测到 Git 仓库
)

echo.
echo [6/6] 测试 GitHub 连接...
echo [i] 正在测试连接到 GitHub...
curl -I -s --connect-timeout 10 https://github.com >nul 2>&1
if %errorLevel% == 0 (
    echo [✓] GitHub HTTPS 连接成功！
) else (
    echo [!] GitHub HTTPS 连接失败，尝试其他解决方案...
    
    :: 设置 Git 配置以解决 SSL 问题
    git config --global http.sslVerify false
    git config --global http.postBuffer 524288000
    git config --global http.lowSpeedLimit 0
    git config --global http.lowSpeedTime 999999
    echo [✓] Git HTTP 配置已优化
    
    :: 再次测试
    timeout /t 2 >nul
    curl -I -s --connect-timeout 10 https://github.com >nul 2>&1
    if %errorLevel% == 0 (
        echo [✓] GitHub 连接修复成功！
    ) else (
        echo [!] 仍然无法连接，可能需要检查防火墙或网络策略
    )
)

echo.
echo ========================================
echo           修复完成！
echo ========================================
echo.
echo [建议] 现在可以尝试推送代码:
echo   cd f:\code
echo   git add .
echo   git commit -m "更新代码"
echo   git push origin main
echo.
echo [备选方案] 如果仍然失败，可以尝试:
echo   1. 使用 GitHub Desktop 客户端
echo   2. 使用 GitHub CLI (gh)
echo   3. 配置 SSH 密钥
echo   4. 联系网络管理员检查防火墙设置
echo.
pause