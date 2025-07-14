@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo     边墙鲜送系统 - 项目管理工具
echo ========================================
echo.
echo 请选择要执行的操作:
echo.
echo [1] 网络连接诊断和修复
echo [2] 代码质量检查和优化
echo [3] Docker 部署 (Linux)
echo [4] Docker 部署 (Windows)
echo [5] Git 操作管理
echo [6] 项目构建和测试
echo [7] 系统监控和日志
echo [8] 数据库管理
echo [9] 查看项目状态
echo [0] 退出
echo.
set /p choice=请输入选项 (0-9): 

if "%choice%"=="1" goto network_fix
if "%choice%"=="2" goto quality_check
if "%choice%"=="3" goto docker_deploy_linux
if "%choice%"=="4" goto docker_deploy_windows
if "%choice%"=="5" goto git_management
if "%choice%"=="6" goto build_test
if "%choice%"=="7" goto monitoring
if "%choice%"=="8" goto database_management
if "%choice%"=="9" goto project_status
if "%choice%"=="0" goto exit

echo 无效选项，请重新选择
pause
goto menu

:network_fix
echo.
echo ========================================
echo        网络连接诊断和修复
echo ========================================
if exist "network-fix.bat" (
    call network-fix.bat
) else (
    echo [!] network-fix.bat 文件不存在
    echo [i] 正在创建网络修复脚本...
    echo 请重新运行此工具
)
pause
goto menu

:quality_check
echo.
echo ========================================
echo        代码质量检查和优化
echo ========================================
if exist "quality-check.bat" (
    call quality-check.bat
) else (
    echo [!] quality-check.bat 文件不存在
    echo [i] 请确保所有工具脚本都在同一目录
)
pause
goto menu

:docker_deploy_linux
echo.
echo ========================================
echo         Docker 部署 (Linux)
echo ========================================
if exist "docker-deploy.sh" (
    echo [i] 请在 Linux 服务器上运行以下命令:
    echo     chmod +x docker-deploy.sh
    echo     ./docker-deploy.sh
    echo.
    echo [i] 或者使用 WSL 运行:
    wsl bash docker-deploy.sh
) else (
    echo [!] docker-deploy.sh 文件不存在
)
pause
goto menu

:docker_deploy_windows
echo.
echo ========================================
echo        Docker 部署 (Windows)
echo ========================================
if exist "docker-deploy.bat" (
    call docker-deploy.bat
) else (
    echo [!] docker-deploy.bat 文件不存在
)
pause
goto menu

:git_management
echo.
echo ========================================
echo           Git 操作管理
echo ========================================
echo.
echo 请选择 Git 操作:
echo [1] 查看状态
echo [2] 添加所有文件
echo [3] 提交更改
echo [4] 推送到远程仓库
echo [5] 拉取最新代码
echo [6] 查看提交历史
echo [7] 创建新分支
echo [8] 切换分支
echo [9] 返回主菜单
echo.
set /p git_choice=请选择操作: 

cd /d f:\code

if "%git_choice%"=="1" (
    echo.
    echo [Git 状态]
    git status
) else if "%git_choice%"=="2" (
    echo.
    echo [添加文件]
    git add .
    echo [✓] 所有文件已添加到暂存区
) else if "%git_choice%"=="3" (
    echo.
    set /p commit_msg=请输入提交信息: 
    git commit -m "!commit_msg!"
) else if "%git_choice%"=="4" (
    echo.
    echo [推送代码]
    git push origin main
) else if "%git_choice%"=="5" (
    echo.
    echo [拉取代码]
    git pull origin main
) else if "%git_choice%"=="6" (
    echo.
    echo [提交历史]
    git log --oneline -10
) else if "%git_choice%"=="7" (
    echo.
    set /p branch_name=请输入新分支名称: 
    git checkout -b !branch_name!
) else if "%git_choice%"=="8" (
    echo.
    echo [当前分支]
    git branch
    echo.
    set /p target_branch=请输入要切换的分支名称: 
    git checkout !target_branch!
) else if "%git_choice%"=="9" (
    goto menu
)

pause
goto git_management

:build_test
echo.
echo ========================================
echo         项目构建和测试
echo ========================================
echo.
echo 请选择构建操作:
echo [1] 构建后端项目
echo [2] 构建前端项目
echo [3] 运行后端测试
echo [4] 运行前端测试
echo [5] 完整构建和测试
echo [6] 返回主菜单
echo.
set /p build_choice=请选择操作: 

if "%build_choice%"=="1" (
    echo.
    echo [构建后端]
    cd /d f:\code\backend
    mvn clean package -DskipTests
) else if "%build_choice%"=="2" (
    echo.
    echo [构建前端]
    cd /d f:\code\admin-frontend
    npm run build
) else if "%build_choice%"=="3" (
    echo.
    echo [后端测试]
    cd /d f:\code\backend
    mvn test
) else if "%build_choice%"=="4" (
    echo.
    echo [前端测试]
    cd /d f:\code\admin-frontend
    npm run test:unit
) else if "%build_choice%"=="5" (
    echo.
    echo [完整构建]
    echo [1/4] 后端编译...
    cd /d f:\code\backend
    mvn clean compile
    
    echo [2/4] 后端测试...
    mvn test
    
    echo [3/4] 前端构建...
    cd /d f:\code\admin-frontend
    npm run build
    
    echo [4/4] 前端测试...
    npm run test:unit
    
    echo [✓] 完整构建完成
) else if "%build_choice%"=="6" (
    goto menu
)

pause
goto build_test

:monitoring
echo.
echo ========================================
echo         系统监控和日志
echo ========================================
echo.
echo 请选择监控操作:
echo [1] 查看 Docker 容器状态
echo [2] 查看应用日志
echo [3] 查看系统资源使用
echo [4] 查看网络连接
echo [5] 健康检查
echo [6] 返回主菜单
echo.
set /p monitor_choice=请选择操作: 

if "%monitor_choice%"=="1" (
    echo.
    echo [Docker 容器状态]
    docker ps -a
    echo.
    echo [Docker 镜像]
    docker images
) else if "%monitor_choice%"=="2" (
    echo.
    echo [应用日志]
    echo 请选择要查看的服务日志:
    echo [1] 后端服务
    echo [2] 前端服务
    echo [3] 数据库
    echo [4] Redis
    echo [5] Nginx
    set /p log_choice=请选择: 
    
    if "!log_choice!"=="1" docker logs fresh-delivery-backend
    if "!log_choice!"=="2" docker logs fresh-delivery-frontend
    if "!log_choice!"=="3" docker logs fresh-delivery-mysql
    if "!log_choice!"=="4" docker logs fresh-delivery-redis
    if "!log_choice!"=="5" docker logs fresh-delivery-nginx
) else if "%monitor_choice%"=="3" (
    echo.
    echo [系统资源使用]
    docker stats --no-stream
) else if "%monitor_choice%"=="4" (
    echo.
    echo [网络连接]
    netstat -an | findstr :8080
    netstat -an | findstr :3000
    netstat -an | findstr :80
    netstat -an | findstr :443
) else if "%monitor_choice%"=="5" (
    echo.
    echo [健康检查]
    echo 检查服务可用性...
    
    curl -s http://localhost:8080/health >nul 2>&1
    if !errorLevel! == 0 (
        echo [✓] 后端服务正常
    ) else (
        echo [✗] 后端服务异常
    )
    
    curl -s http://localhost:3000 >nul 2>&1
    if !errorLevel! == 0 (
        echo [✓] 前端服务正常
    ) else (
        echo [✗] 前端服务异常
    )
    
    ping -n 1 localhost >nul 2>&1
    if !errorLevel! == 0 (
        echo [✓] 本地网络正常
    ) else (
        echo [✗] 本地网络异常
    )
) else if "%monitor_choice%"=="6" (
    goto menu
)

pause
goto monitoring

:database_management
echo.
echo ========================================
echo           数据库管理
echo ========================================
echo.
echo 请选择数据库操作:
echo [1] 连接数据库
echo [2] 备份数据库
echo [3] 恢复数据库
echo [4] 查看数据库状态
echo [5] 执行 SQL 脚本
echo [6] 返回主菜单
echo.
set /p db_choice=请选择操作: 

if "%db_choice%"=="1" (
    echo.
    echo [连接数据库]
    echo 使用以下命令连接数据库:
    echo docker exec -it fresh-delivery-mysql mysql -u root -p fresh_delivery
) else if "%db_choice%"=="2" (
    echo.
    echo [备份数据库]
    set backup_file=backup_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.sql
    set backup_file=!backup_file: =0!
    docker exec fresh-delivery-mysql mysqldump -u root -p123456 fresh_delivery > !backup_file!
    echo [✓] 数据库已备份到: !backup_file!
) else if "%db_choice%"=="3" (
    echo.
    echo [恢复数据库]
    set /p restore_file=请输入备份文件路径: 
    if exist "!restore_file!" (
        docker exec -i fresh-delivery-mysql mysql -u root -p123456 fresh_delivery < !restore_file!
        echo [✓] 数据库恢复完成
    ) else (
        echo [!] 备份文件不存在
    )
) else if "%db_choice%"=="4" (
    echo.
    echo [数据库状态]
    docker exec fresh-delivery-mysql mysql -u root -p123456 -e "SHOW DATABASES;"
    echo.
    docker exec fresh-delivery-mysql mysql -u root -p123456 -e "SHOW PROCESSLIST;"
) else if "%db_choice%"=="5" (
    echo.
    echo [执行 SQL 脚本]
    set /p sql_file=请输入 SQL 文件路径: 
    if exist "!sql_file!" (
        docker exec -i fresh-delivery-mysql mysql -u root -p123456 fresh_delivery < !sql_file!
        echo [✓] SQL 脚本执行完成
    ) else (
        echo [!] SQL 文件不存在
    )
) else if "%db_choice%"=="6" (
    goto menu
)

pause
goto database_management

:project_status
echo.
echo ========================================
echo           项目状态概览
echo ========================================
echo.

cd /d f:\code

echo [项目信息]
echo 项目路径: %cd%
echo 服务器 IP: 8.154.40.188
echo.

echo [Git 状态]
if exist ".git" (
    git branch --show-current 2>nul
    if !errorLevel! == 0 (
        for /f %%i in ('git branch --show-current') do echo 当前分支: %%i
    )
    
    for /f %%i in ('git status --porcelain 2^>nul ^| find /c /v ""') do set uncommitted=%%i
    if !uncommitted! gtr 0 (
        echo 未提交文件: !uncommitted! 个
    ) else (
        echo 工作区状态: 干净
    )
    
    for /f %%i in ('git rev-list --count HEAD 2^>nul') do echo 总提交数: %%i
) else (
    echo Git 状态: 未初始化
)
echo.

echo [项目结构]
if exist "backend" echo [✓] 后端项目
if exist "admin-frontend" echo [✓] 前端管理系统
if exist "miniprogram" echo [✓] 微信小程序
if exist "docker" echo [✓] Docker 配置
if exist "docs" echo [✓] 项目文档
echo.

echo [Docker 服务状态]
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>nul
if !errorLevel! neq 0 (
    echo Docker 未运行或未安装
)
echo.

echo [网络连接测试]
ping -n 1 8.154.40.188 >nul 2>&1
if !errorLevel! == 0 (
    echo [✓] 服务器连接正常
) else (
    echo [✗] 服务器连接失败
)

ping -n 1 github.com >nul 2>&1
if !errorLevel! == 0 (
    echo [✓] GitHub 连接正常
) else (
    echo [✗] GitHub 连接失败
)
echo.

echo [快速操作]
echo 1. 推送代码: git add . && git commit -m "更新" && git push
echo 2. 部署应用: docker-compose up -d
echo 3. 查看日志: docker-compose logs -f
echo 4. 重启服务: docker-compose restart
echo.

pause
goto menu

:menu
cls
echo ========================================
echo     边墙鲜送系统 - 项目管理工具
echo ========================================
echo.
echo 请选择要执行的操作:
echo.
echo [1] 网络连接诊断和修复
echo [2] 代码质量检查和优化
echo [3] Docker 部署 (Linux)
echo [4] Docker 部署 (Windows)
echo [5] Git 操作管理
echo [6] 项目构建和测试
echo [7] 系统监控和日志
echo [8] 数据库管理
echo [9] 查看项目状态
echo [0] 退出
echo.
set /p choice=请输入选项 (0-9): 

if "%choice%"=="1" goto network_fix
if "%choice%"=="2" goto quality_check
if "%choice%"=="3" goto docker_deploy_linux
if "%choice%"=="4" goto docker_deploy_windows
if "%choice%"=="5" goto git_management
if "%choice%"=="6" goto build_test
if "%choice%"=="7" goto monitoring
if "%choice%"=="8" goto database_management
if "%choice%"=="9" goto project_status
if "%choice%"=="0" goto exit

echo 无效选项，请重新选择
pause
goto menu

:exit
echo.
echo 感谢使用边墙鲜送系统项目管理工具！
echo.
pause
exit /b 0