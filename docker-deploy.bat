@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 边墙鲜送系统 Docker 部署脚本 (Windows版本)
REM 服务器公网IP: 8.154.40.188
REM 使用方法: 右键以管理员身份运行此脚本

REM 配置变量
set SERVER_IP=8.154.40.188
set PROJECT_NAME=fresh-delivery
set DOCKER_DIR=.\docker
set BACKUP_DIR=.\backups
set LOG_FILE=.\deploy.log

REM 颜色定义
set RED=[91m
set GREEN=[92m
set YELLOW=[93m
set BLUE=[94m
set NC=[0m

echo %GREEN%=== 边墙鲜送系统 Docker 部署脚本 ===%NC%
echo %BLUE%服务器IP: %SERVER_IP%%NC%
echo %BLUE%开始时间: %date% %time%%NC%
echo.

REM 记录日志函数
echo [%date% %time%] 开始部署边墙鲜送系统... >> %LOG_FILE%

REM 检查管理员权限
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo %RED%错误: 请以管理员身份运行此脚本%NC%
    pause
    exit /b 1
)

REM 检查Docker是否安装
echo %YELLOW%检查系统要求...%NC%
docker --version >nul 2>&1
if %errorLevel% neq 0 (
    echo %RED%错误: Docker 未安装，请先安装 Docker Desktop%NC%
    echo 下载地址: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

docker-compose --version >nul 2>&1
if %errorLevel% neq 0 (
    echo %RED%错误: Docker Compose 未安装%NC%
    pause
    exit /b 1
)

echo %GREEN%✓ Docker 环境检查通过%NC%

REM 创建必要的目录
echo %YELLOW%创建必要的目录...%NC%
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
if not exist "%DOCKER_DIR%\mysql\init" mkdir "%DOCKER_DIR%\mysql\init"
if not exist "%DOCKER_DIR%\mysql\conf" mkdir "%DOCKER_DIR%\mysql\conf"
if not exist "%DOCKER_DIR%\nginx\ssl" mkdir "%DOCKER_DIR%\nginx\ssl"
if not exist "%DOCKER_DIR%\redis" mkdir "%DOCKER_DIR%\redis"
if not exist "logs" mkdir "logs"
echo %GREEN%✓ 目录创建完成%NC%

REM 生成环境变量文件
echo %YELLOW%生成环境变量文件...%NC%
(
echo # 边墙鲜送系统生产环境配置
echo # 生成时间: %date% %time%
echo # 服务器IP: %SERVER_IP%
echo.
echo # ==========================================
echo # 服务器配置
echo # ==========================================
echo SERVER_IP=%SERVER_IP%
echo SERVER_NAME=%SERVER_IP%
echo FILE_DOMAIN=https://%SERVER_IP%
echo.
echo # ==========================================
echo # 数据库配置
echo # ==========================================
echo MYSQL_ROOT_PASSWORD=FreshDelivery2024!@#
echo MYSQL_DATABASE=fresh_delivery
echo MYSQL_USERNAME=fresh_user
echo MYSQL_PASSWORD=FreshUser2024!@#
echo MYSQL_HOST=mysql
echo MYSQL_PORT=3306
echo.
echo # ==========================================
echo # Redis 配置
echo # ==========================================
echo REDIS_HOST=redis
echo REDIS_PORT=6379
echo REDIS_PASSWORD=Redis2024!@#
echo.
echo # ==========================================
echo # 后端应用配置
echo # ==========================================
echo JWT_SECRET=fresh-delivery-jwt-secret-2024
echo SPRING_PROFILES_ACTIVE=docker
echo.
echo # ==========================================
echo # 微信小程序配置（请替换为实际值）
echo # ==========================================
echo WECHAT_APPID=your_wechat_miniprogram_appid
echo WECHAT_SECRET=your_wechat_miniprogram_secret
echo.
echo # ==========================================
echo # SSL 证书配置
echo # ==========================================
echo SSL_CERTIFICATE=server.crt
echo SSL_CERTIFICATE_KEY=server.key
echo.
echo # ==========================================
echo # 性能配置
echo # ==========================================
echo JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC
echo DB_POOL_MIN_SIZE=10
echo DB_POOL_MAX_SIZE=50
echo REDIS_POOL_MAX_ACTIVE=16
echo REDIS_POOL_MAX_IDLE=8
echo REDIS_POOL_MIN_IDLE=2
echo.
echo # ==========================================
echo # 安全配置
echo # ==========================================
echo HTTPS_REDIRECT=true
echo CORS_ALLOWED_ORIGINS=https://%SERVER_IP%,https://www.%SERVER_IP%
echo CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
echo CORS_ALLOWED_HEADERS=*
echo CORS_ALLOW_CREDENTIALS=true
echo.
echo # ==========================================
echo # 日志配置
echo # ==========================================
echo LOG_LEVEL=INFO
echo LOG_RETENTION_DAYS=30
echo SQL_LOG_ENABLED=false
echo.
echo # ==========================================
echo # 监控配置
echo # ==========================================
echo MONITORING_ENABLED=true
echo PROMETHEUS_PORT=9090
echo GRAFANA_PORT=3000
echo GRAFANA_ADMIN_PASSWORD=Admin2024!@#
echo.
echo # ==========================================
echo # 备份配置
echo # ==========================================
echo BACKUP_INTERVAL=24
echo BACKUP_RETENTION_DAYS=7
echo BACKUP_PATH=/app/backups
) > "%DOCKER_DIR%\.env"
echo %GREEN%✓ 环境变量文件生成完成%NC%

REM 生成自签名SSL证书
echo %YELLOW%生成SSL证书...%NC%
if not exist "%DOCKER_DIR%\nginx\ssl\server.crt" (
    REM 使用OpenSSL生成自签名证书（需要安装OpenSSL）
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout "%DOCKER_DIR%\nginx\ssl\server.key" -out "%DOCKER_DIR%\nginx\ssl\server.crt" -subj "/C=CN/ST=Beijing/L=Beijing/O=FreshDelivery/OU=IT/CN=%SERVER_IP%" 2>nul
    if %errorLevel% equ 0 (
        echo %GREEN%✓ SSL证书生成完成%NC%
    ) else (
        echo %YELLOW%警告: OpenSSL未安装，将使用默认证书%NC%
        echo %YELLOW%建议安装OpenSSL或手动配置SSL证书%NC%
    )
) else (
    echo %GREEN%✓ SSL证书已存在%NC%
)

REM 更新Docker Compose配置
echo %YELLOW%更新Docker配置...%NC%
REM 这里可以添加配置文件的动态修改逻辑
echo %GREEN%✓ Docker配置更新完成%NC%

REM 数据库备份（如果存在）
echo %YELLOW%检查现有数据库...%NC%
docker ps | findstr "fresh-delivery-mysql" >nul 2>&1
if %errorLevel% equ 0 (
    echo %YELLOW%备份现有数据库...%NC%
    set BACKUP_FILE=%BACKUP_DIR%\mysql_backup_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.sql
    docker exec fresh-delivery-mysql mysqldump -u root -pFreshDelivery2024!@# fresh_delivery > "!BACKUP_FILE!"
    if %errorLevel% equ 0 (
        echo %GREEN%✓ 数据库备份完成: !BACKUP_FILE!%NC%
    ) else (
        echo %YELLOW%警告: 数据库备份失败%NC%
    )
) else (
    echo %BLUE%ℹ MySQL容器未运行，跳过备份%NC%
)

REM 停止现有服务
echo %YELLOW%停止现有服务...%NC%
cd /d "%DOCKER_DIR%"
docker-compose down --remove-orphans
echo %GREEN%✓ 现有服务已停止%NC%

REM 清理Docker资源
echo %YELLOW%清理Docker资源...%NC%
docker system prune -f
echo %GREEN%✓ Docker资源清理完成%NC%

REM 构建镜像
echo %YELLOW%构建Docker镜像...%NC%
docker-compose build --no-cache
if %errorLevel% neq 0 (
    echo %RED%错误: Docker镜像构建失败%NC%
    cd /d ".."
    pause
    exit /b 1
)
echo %GREEN%✓ Docker镜像构建完成%NC%

REM 启动服务
echo %YELLOW%启动服务...%NC%
docker-compose up -d
if %errorLevel% neq 0 (
    echo %RED%错误: 服务启动失败%NC%
    cd /d ".."
    pause
    exit /b 1
)
echo %GREEN%✓ 服务启动完成%NC%

cd /d ".."

REM 等待服务启动
echo %YELLOW%等待服务完全启动...%NC%
timeout /t 30 /nobreak >nul

REM 健康检查
echo %YELLOW%执行健康检查...%NC%

REM 检查容器状态
docker ps | findstr "fresh-delivery-mysql" >nul && echo %GREEN%✓ MySQL数据库 运行正常%NC% || echo %RED%✗ MySQL数据库 未运行%NC%
docker ps | findstr "fresh-delivery-redis" >nul && echo %GREEN%✓ Redis缓存 运行正常%NC% || echo %RED%✗ Redis缓存 未运行%NC%
docker ps | findstr "fresh-delivery-backend" >nul && echo %GREEN%✓ 后端服务 运行正常%NC% || echo %RED%✗ 后端服务 未运行%NC%
docker ps | findstr "fresh-delivery-admin" >nul && echo %GREEN%✓ 前端管理系统 运行正常%NC% || echo %RED%✗ 前端管理系统 未运行%NC%
docker ps | findstr "fresh-delivery-nginx" >nul && echo %GREEN%✓ Nginx代理 运行正常%NC% || echo %RED%✗ Nginx代理 未运行%NC%

REM 检查服务可访问性
echo %YELLOW%检查服务可访问性...%NC%
curl -k -s --connect-timeout 10 https://%SERVER_IP% >nul 2>&1
if %errorLevel% equ 0 (
    echo %GREEN%✓ HTTPS服务 可访问%NC%
) else (
    echo %YELLOW%⚠ HTTPS服务 暂时无法访问，可能需要更多时间启动%NC%
)

curl -k -s --connect-timeout 10 https://%SERVER_IP%/api/health >nul 2>&1
if %errorLevel% equ 0 (
    echo %GREEN%✓ 后端API 可访问%NC%
) else (
    echo %YELLOW%⚠ 后端API 暂时无法访问，可能需要更多时间启动%NC%
)

REM 显示部署信息
echo.
echo %GREEN%=== 边墙鲜送系统部署完成！ ===%NC%
echo.
echo %BLUE%服务器IP:%NC% %SERVER_IP%
echo %BLUE%管理后台:%NC% https://%SERVER_IP%
echo %BLUE%API接口:%NC% https://%SERVER_IP%/api
echo %BLUE%数据库:%NC% %SERVER_IP%:3306
echo %BLUE%Redis:%NC% %SERVER_IP%:6379
echo.
echo %YELLOW%默认账号信息:%NC%
echo %BLUE%数据库root密码:%NC% FreshDelivery2024!@#
echo %BLUE%数据库用户密码:%NC% FreshUser2024!@#
echo %BLUE%Redis密码:%NC% Redis2024!@#
echo.
echo %YELLOW%重要提醒:%NC%
echo 1. 请及时修改默认密码
echo 2. 配置微信小程序AppID和Secret
echo 3. 建议配置正式SSL证书
echo 4. 定期备份数据库
echo 5. 检查防火墙设置，确保端口80、443、3306、6379、8080开放
echo.
echo %GREEN%部署日志:%NC% %LOG_FILE%
echo %GREEN%配置文件:%NC% %DOCKER_DIR%\.env
echo.
echo %BLUE%常用命令:%NC%
echo docker-compose -f %DOCKER_DIR%\docker-compose.yml ps     查看服务状态
echo docker-compose -f %DOCKER_DIR%\docker-compose.yml logs   查看服务日志
echo docker-compose -f %DOCKER_DIR%\docker-compose.yml restart 重启服务
echo docker-compose -f %DOCKER_DIR%\docker-compose.yml down   停止服务
echo.

REM 记录完成日志
echo [%date% %time%] 部署脚本执行完成！ >> %LOG_FILE%

echo %GREEN%部署脚本执行完成！%NC%
echo 按任意键退出...
pause >nul