@echo off
chcp 65001 >nul
echo ========================================
echo      代码质量检查和优化工具
echo ========================================
echo.

set PROJECT_ROOT=f:\code
set REPORT_DIR=%PROJECT_ROOT%\quality-reports
set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%

:: 创建报告目录
if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"

echo [1/8] 检查项目结构...
cd /d "%PROJECT_ROOT%"
if not exist "backend" (
    echo [!] 未找到 backend 目录
    set BACKEND_EXISTS=false
) else (
    echo [✓] 后端项目目录存在
    set BACKEND_EXISTS=true
)

if not exist "admin-frontend" (
    echo [!] 未找到 admin-frontend 目录
    set FRONTEND_EXISTS=false
) else (
    echo [✓] 前端项目目录存在
    set FRONTEND_EXISTS=true
)

echo.
echo [2/8] 检查开发工具...

:: 检查 Java
java -version >nul 2>&1
if %errorLevel% == 0 (
    echo [✓] Java 已安装
    java -version 2>&1 | findstr "version"
) else (
    echo [!] Java 未安装或未配置环境变量
)

:: 检查 Maven
mvn -version >nul 2>&1
if %errorLevel% == 0 (
    echo [✓] Maven 已安装
    set MAVEN_EXISTS=true
) else (
    echo [!] Maven 未安装或未配置环境变量
    set MAVEN_EXISTS=false
)

:: 检查 Node.js
node --version >nul 2>&1
if %errorLevel% == 0 (
    echo [✓] Node.js 已安装
    node --version
    set NODE_EXISTS=true
) else (
    echo [!] Node.js 未安装或未配置环境变量
    set NODE_EXISTS=false
)

:: 检查 npm
npm --version >nul 2>&1
if %errorLevel% == 0 (
    echo [✓] npm 已安装
    set NPM_EXISTS=true
) else (
    echo [!] npm 未安装或未配置环境变量
    set NPM_EXISTS=false
)

echo.
echo [3/8] 后端代码质量检查...
if "%BACKEND_EXISTS%"=="true" if "%MAVEN_EXISTS%"=="true" (
    cd /d "%PROJECT_ROOT%\backend"
    
    echo [i] 编译后端项目...
    mvn clean compile -q
    if %errorLevel% == 0 (
        echo [✓] 后端编译成功
    ) else (
        echo [!] 后端编译失败，请检查代码
    )
    
    echo [i] 运行后端测试...
    mvn test -q > "%REPORT_DIR%\backend-test-%TIMESTAMP%.log" 2>&1
    if %errorLevel% == 0 (
        echo [✓] 后端测试通过
    ) else (
        echo [!] 后端测试失败，详情请查看: %REPORT_DIR%\backend-test-%TIMESTAMP%.log
    )
    
    echo [i] 生成测试覆盖率报告...
    mvn jacoco:report -q
    if exist "target\site\jacoco\index.html" (
        echo [✓] 测试覆盖率报告已生成: backend\target\site\jacoco\index.html
    )
    
    echo [i] 检查代码规范...
    mvn checkstyle:check -q > "%REPORT_DIR%\backend-checkstyle-%TIMESTAMP%.log" 2>&1
    if %errorLevel% == 0 (
        echo [✓] 代码规范检查通过
    ) else (
        echo [!] 代码规范检查发现问题，详情请查看: %REPORT_DIR%\backend-checkstyle-%TIMESTAMP%.log
    )
) else (
    echo [!] 跳过后端检查（缺少必要工具或目录）
)

echo.
echo [4/8] 前端代码质量检查...
if "%FRONTEND_EXISTS%"=="true" if "%NODE_EXISTS%"=="true" if "%NPM_EXISTS%"=="true" (
    cd /d "%PROJECT_ROOT%\admin-frontend"
    
    echo [i] 检查 package.json...
    if exist "package.json" (
        echo [✓] package.json 存在
        
        echo [i] 安装依赖...
        npm install --silent
        if %errorLevel% == 0 (
            echo [✓] 依赖安装成功
        ) else (
            echo [!] 依赖安装失败
        )
        
        echo [i] 运行 ESLint 检查...
        npm run lint > "%REPORT_DIR%\frontend-lint-%TIMESTAMP%.log" 2>&1
        if %errorLevel% == 0 (
            echo [✓] ESLint 检查通过
        ) else (
            echo [!] ESLint 检查发现问题，详情请查看: %REPORT_DIR%\frontend-lint-%TIMESTAMP%.log
        )
        
        echo [i] 运行前端测试...
        npm run test:unit > "%REPORT_DIR%\frontend-test-%TIMESTAMP%.log" 2>&1
        if %errorLevel% == 0 (
            echo [✓] 前端测试通过
        ) else (
            echo [!] 前端测试失败，详情请查看: %REPORT_DIR%\frontend-test-%TIMESTAMP%.log
        )
        
        echo [i] 构建前端项目...
        npm run build > "%REPORT_DIR%\frontend-build-%TIMESTAMP%.log" 2>&1
        if %errorLevel% == 0 (
            echo [✓] 前端构建成功
        ) else (
            echo [!] 前端构建失败，详情请查看: %REPORT_DIR%\frontend-build-%TIMESTAMP%.log
        )
    ) else (
        echo [!] package.json 不存在
    )
) else (
    echo [!] 跳过前端检查（缺少必要工具或目录）
)

echo.
echo [5/8] 安全漏洞扫描...
cd /d "%PROJECT_ROOT%"

echo [i] 检查敏感文件...
set SENSITIVE_FOUND=false
if exist ".env" (
    echo [!] 发现 .env 文件，请确保不包含敏感信息
    set SENSITIVE_FOUND=true
)
if exist "application.properties" (
    findstr /i "password\|secret\|key" backend\src\main\resources\application.properties >nul 2>&1
    if !errorLevel! == 0 (
        echo [!] application.properties 中可能包含敏感信息
        set SENSITIVE_FOUND=true
    )
)
if "%SENSITIVE_FOUND%"=="false" (
    echo [✓] 未发现明显的敏感信息泄露
)

echo [i] 检查依赖漏洞...
if "%MAVEN_EXISTS%"=="true" if "%BACKEND_EXISTS%"=="true" (
    cd /d "%PROJECT_ROOT%\backend"
    mvn dependency:check -q > "%REPORT_DIR%\security-check-%TIMESTAMP%.log" 2>&1
    if %errorLevel% == 0 (
        echo [✓] 后端依赖安全检查通过
    ) else (
        echo [!] 后端依赖存在安全漏洞，详情请查看: %REPORT_DIR%\security-check-%TIMESTAMP%.log
    )
)

if "%NPM_EXISTS%"=="true" if "%FRONTEND_EXISTS%"=="true" (
    cd /d "%PROJECT_ROOT%\admin-frontend"
    npm audit > "%REPORT_DIR%\npm-audit-%TIMESTAMP%.log" 2>&1
    npm audit --audit-level=high >nul 2>&1
    if %errorLevel% == 0 (
        echo [✓] 前端依赖安全检查通过
    ) else (
        echo [!] 前端依赖存在高危漏洞，详情请查看: %REPORT_DIR%\npm-audit-%TIMESTAMP%.log
    )
)

echo.
echo [6/8] 代码复杂度分析...
cd /d "%PROJECT_ROOT%"

echo [i] 统计代码行数...
set JAVA_LINES=0
set JS_LINES=0
set VUE_LINES=0

for /f %%i in ('dir /s /b *.java 2^>nul ^| find /c /v ""') do set JAVA_FILES=%%i
for /f %%i in ('dir /s /b *.js 2^>nul ^| find /c /v ""') do set JS_FILES=%%i
for /f %%i in ('dir /s /b *.vue 2^>nul ^| find /c /v ""') do set VUE_FILES=%%i

echo [i] 项目代码统计:
echo     Java 文件: %JAVA_FILES% 个
echo     JavaScript 文件: %JS_FILES% 个
echo     Vue 文件: %VUE_FILES% 个

echo [i] 检查大文件...
set LARGE_FILES_FOUND=false
for /r %%f in (*.java *.js *.vue) do (
    for %%s in ("%%f") do (
        if %%~zs gtr 10240 (
            echo [!] 发现大文件 (^>10KB): %%f
            set LARGE_FILES_FOUND=true
        )
    )
)
if "%LARGE_FILES_FOUND%"=="false" (
    echo [✓] 未发现过大的代码文件
)

echo.
echo [7/8] Git 仓库健康检查...
if exist ".git" (
    echo [✓] Git 仓库已初始化
    
    echo [i] 检查 Git 状态...
    git status --porcelain > "%REPORT_DIR%\git-status-%TIMESTAMP%.log" 2>&1
    for /f %%i in ('git status --porcelain 2^>nul ^| find /c /v ""') do set UNCOMMITTED=%%i
    if %UNCOMMITTED% gtr 0 (
        echo [!] 有 %UNCOMMITTED% 个文件未提交
    ) else (
        echo [✓] 工作区干净
    )
    
    echo [i] 检查分支状态...
    git branch -r >nul 2>&1
    if %errorLevel% == 0 (
        echo [✓] 远程分支已配置
    ) else (
        echo [!] 未配置远程分支
    )
    
    echo [i] 检查提交历史...
    for /f %%i in ('git rev-list --count HEAD 2^>nul') do set COMMIT_COUNT=%%i
    if defined COMMIT_COUNT (
        echo [i] 总提交数: %COMMIT_COUNT%
    )
) else (
    echo [!] 未初始化 Git 仓库
)

echo.
echo [8/8] 生成质量报告...
set REPORT_FILE=%REPORT_DIR%\quality-report-%TIMESTAMP%.html

echo ^<!DOCTYPE html^> > "%REPORT_FILE%"
echo ^<html^>^<head^>^<meta charset="utf-8"^>^<title^>代码质量报告^</title^>^</head^> >> "%REPORT_FILE%"
echo ^<body^>^<h1^>边墙鲜送系统 - 代码质量报告^</h1^> >> "%REPORT_FILE%"
echo ^<p^>生成时间: %date% %time%^</p^> >> "%REPORT_FILE%"
echo ^<h2^>项目概览^</h2^> >> "%REPORT_FILE%"
echo ^<ul^> >> "%REPORT_FILE%"
echo ^<li^>Java 文件: %JAVA_FILES% 个^</li^> >> "%REPORT_FILE%"
echo ^<li^>JavaScript 文件: %JS_FILES% 个^</li^> >> "%REPORT_FILE%"
echo ^<li^>Vue 文件: %VUE_FILES% 个^</li^> >> "%REPORT_FILE%"
if defined COMMIT_COUNT echo ^<li^>Git 提交数: %COMMIT_COUNT%^</li^> >> "%REPORT_FILE%"
echo ^</ul^> >> "%REPORT_FILE%"
echo ^<h2^>详细日志^</h2^> >> "%REPORT_FILE%"
echo ^<p^>详细的检查日志文件位于: %REPORT_DIR%^</p^> >> "%REPORT_FILE%"
echo ^</body^>^</html^> >> "%REPORT_FILE%"

echo [✓] 质量报告已生成: %REPORT_FILE%

echo.
echo ========================================
echo           检查完成！
echo ========================================
echo.
echo [报告位置] %REPORT_DIR%
echo [主报告] %REPORT_FILE%
echo.
echo [建议操作]:
echo 1. 查看生成的报告文件
echo 2. 修复发现的问题
echo 3. 重新运行检查确认修复
echo 4. 提交代码前再次运行此脚本
echo.
echo [下一步] 运行 network-fix.bat 解决网络问题
echo.
pause