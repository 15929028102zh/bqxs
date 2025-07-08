@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 边墙鲜送系统 Docker 部署脚本 (Windows 版本)
REM 使用方法: deploy.bat [start|stop|restart|logs|status]

set PROJECT_NAME=fresh-delivery
set DOCKER_COMPOSE_FILE=docker\docker-compose.yml
set SSL_DIR=docker\nginx\ssl

REM 颜色定义
set RED=[91m
set GREEN=[92m
set YELLOW=[93m
set BLUE=[94m
set NC=[0m

REM 日志函数
:log_info
echo %BLUE%[INFO]%NC% %~1
goto :eof

:log_success
echo %GREEN%[SUCCESS]%NC% %~1
goto :eof

:log_warning
echo %YELLOW%[WARNING]%NC% %~1
goto :eof

:log_error
echo %RED%[ERROR]%NC% %~1
goto :eof

REM 检查 Docker 和 Docker Compose
:check_docker
docker --version >nul 2>&1
if errorlevel 1 (
    call :log_error "Docker 未安装，请先安装 Docker Desktop"
    exit /b 1
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    call :log_error "Docker Compose 未安装，请先安装 Docker Compose"
    exit /b 1
)

call :log_info "Docker 环境检查通过"
goto :eof

REM 创建 SSL 证书目录
:create_ssl_dir
if not exist "%SSL_DIR%" (
    mkdir "%SSL_DIR%"
)

if not exist "%SSL_DIR%\server.crt" (
    call :log_info "请手动创建 SSL 证书或使用自签名证书"
    call :log_info "可以使用以下命令创建自签名证书："
    echo openssl req -x509 -nodes -days 365 -newkey rsa:2048 ^
    echo     -keyout "%SSL_DIR%\server.key" ^
    echo     -out "%SSL_DIR%\server.crt" ^
    echo     -subj "/C=CN/ST=Beijing/L=Beijing/O=FreshDelivery/OU=IT/CN=localhost"
) else (
    call :log_info "SSL 证书已存在"
)
goto :eof

REM 构建镜像
:build_images
call :log_info "开始构建 Docker 镜像..."

REM 构建后端镜像
call :log_info "构建后端镜像..."
docker build -t fresh-delivery-backend:latest backend\
if errorlevel 1 (
    call :log_error "后端镜像构建失败"
    exit /b 1
)

REM 构建前端镜像
call :log_info "构建前端镜像..."
docker build -t fresh-delivery-admin:latest admin-frontend\
if errorlevel 1 (
    call :log_error "前端镜像构建失败"
    exit /b 1
)

call :log_success "Docker 镜像构建完成"
goto :eof

REM 启动服务
:start_services
call :log_info "启动 %PROJECT_NAME% 服务..."

REM 检查环境
call :check_docker
if errorlevel 1 exit /b 1

REM 创建 SSL 证书目录
call :create_ssl_dir

REM 构建镜像
call :build_images
if errorlevel 1 exit /b 1

REM 启动服务
docker-compose -f "%DOCKER_COMPOSE_FILE%" up -d
if errorlevel 1 (
    call :log_error "服务启动失败"
    exit /b 1
)

REM 等待服务启动
call :log_info "等待服务启动..."
timeout /t 10 /nobreak >nul

REM 检查服务状态
call :check_services_status

call :log_success "%PROJECT_NAME% 服务启动完成！"
echo.
echo 访问地址：
echo   - 管理后台: https://localhost (HTTP: http://localhost)
echo   - API 文档: http://localhost:8080/swagger-ui/index.html
echo   - 数据库: localhost:3306 (用户名: fresh_user, 密码: fresh_pass)
echo   - Redis: localhost:6379
echo.
echo 默认管理员账号：
echo   - 用户名: admin
echo   - 密码: admin123
goto :eof

REM 停止服务
:stop_services
call :log_info "停止 %PROJECT_NAME% 服务..."
docker-compose -f "%DOCKER_COMPOSE_FILE%" down
call :log_success "%PROJECT_NAME% 服务已停止"
goto :eof

REM 重启服务
:restart_services
call :log_info "重启 %PROJECT_NAME% 服务..."
call :stop_services
call :start_services
goto :eof

REM 查看日志
:view_logs
if "%~2"=="" (
    docker-compose -f "%DOCKER_COMPOSE_FILE%" logs -f
) else (
    docker-compose -f "%DOCKER_COMPOSE_FILE%" logs -f %~2
)
goto :eof

REM 检查服务状态
:check_services_status
call :log_info "检查服务状态..."
docker-compose -f "%DOCKER_COMPOSE_FILE%" ps

REM 检查各服务健康状态
for %%s in (mysql redis backend admin-frontend) do (
    docker-compose -f "%DOCKER_COMPOSE_FILE%" ps | findstr "%%s" | findstr "Up" >nul
    if !errorlevel! equ 0 (
        call :log_success "%%s 服务运行正常"
    ) else (
        call :log_error "%%s 服务异常"
    )
)
goto :eof

REM 清理数据
:clean_data
call :log_warning "这将删除所有数据，包括数据库数据！"
set /p confirm="确定要继续吗？(y/N): "
if /i "%confirm%"=="y" (
    call :log_info "停止服务并清理数据..."
    docker-compose -f "%DOCKER_COMPOSE_FILE%" down -v
    docker system prune -f
    call :log_success "数据清理完成"
) else (
    call :log_info "操作已取消"
)
goto :eof

REM 备份数据
:backup_data
set BACKUP_DIR=backups\%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set BACKUP_DIR=%BACKUP_DIR: =0%
mkdir "%BACKUP_DIR%" 2>nul

call :log_info "开始备份数据到 %BACKUP_DIR%..."

REM 备份数据库
docker-compose -f "%DOCKER_COMPOSE_FILE%" exec -T mysql mysqldump -u fresh_user -pfresh_pass fresh_delivery > "%BACKUP_DIR%\database.sql"

REM 备份上传文件
docker cp fresh-delivery-backend:/app/uploads "%BACKUP_DIR%\uploads"

call :log_success "数据备份完成: %BACKUP_DIR%"
goto :eof

REM 显示帮助信息
:show_help
echo 边墙鲜送系统 Docker 部署脚本 (Windows 版本)
echo.
echo 使用方法:
echo   %~nx0 [命令] [选项]
echo.
echo 命令:
echo   start     启动所有服务
echo   stop      停止所有服务
echo   restart   重启所有服务
echo   status    查看服务状态
echo   logs      查看服务日志 (可指定服务名)
echo   build     重新构建镜像
echo   backup    备份数据
echo   clean     清理所有数据 (危险操作)
echo   help      显示帮助信息
echo.
echo 示例:
echo   %~nx0 start              # 启动所有服务
echo   %~nx0 logs backend       # 查看后端服务日志
echo   %~nx0 backup             # 备份数据
goto :eof

REM 主函数
:main
if "%~1"=="start" (
    call :start_services
) else if "%~1"=="stop" (
    call :stop_services
) else if "%~1"=="restart" (
    call :restart_services
) else if "%~1"=="status" (
    call :check_services_status
) else if "%~1"=="logs" (
    call :view_logs %*
) else if "%~1"=="build" (
    call :build_images
) else if "%~1"=="backup" (
    call :backup_data
) else if "%~1"=="clean" (
    call :clean_data
) else if "%~1"=="help" (
    call :show_help
) else if "%~1"=="--help" (
    call :show_help
) else if "%~1"=="-h" (
    call :show_help
) else if "%~1"=="" (
    call :log_error "请指定操作命令"
    call :show_help
    exit /b 1
) else (
    call :log_error "未知命令: %~1"
    call :show_help
    exit /b 1
)
goto :eof

REM 执行主函数
call :main %*