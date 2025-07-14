#!/bin/bash

# 代码质量检查脚本
# 用于定期检查前端代码质量

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log() {
    local level=$1
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    case $level in
        "INFO")
            echo -e "${BLUE}[$timestamp] [INFO]${NC} $message"
            ;;
        "SUCCESS")
            echo -e "${GREEN}[$timestamp] [SUCCESS]${NC} $message"
            ;;
        "WARNING")
            echo -e "${YELLOW}[$timestamp] [WARNING]${NC} $message"
            ;;
        "ERROR")
            echo -e "${RED}[$timestamp] [ERROR]${NC} $message"
            ;;
    esac
}

# 错误处理
error_exit() {
    log "ERROR" "$1"
    exit 1
}

# 检查依赖
check_dependencies() {
    log "INFO" "检查依赖..."
    
    if ! command -v node &> /dev/null; then
        error_exit "Node.js 未安装"
    fi
    
    if ! command -v npm &> /dev/null; then
        error_exit "npm 未安装"
    fi
    
    log "SUCCESS" "依赖检查通过"
}

# 检查前端代码质量
check_frontend_quality() {
    log "INFO" "检查前端代码质量..."
    
    if [ ! -d "admin-frontend" ]; then
        error_exit "admin-frontend目录不存在"
    fi
    
    cd admin-frontend
    
    # 检查package.json是否存在
    if [ ! -f "package.json" ]; then
        error_exit "package.json文件不存在"
    fi
    
    # 安装依赖（如果需要）
    if [ ! -d "node_modules" ]; then
        log "INFO" "安装依赖..."
        npm install
    fi
    
    # ESLint检查
    log "INFO" "运行ESLint检查..."
    if npm run lint; then
        log "SUCCESS" "ESLint检查通过"
    else
        log "WARNING" "ESLint检查发现问题，尝试自动修复..."
        npm run lint -- --fix
        
        # 再次检查
        if npm run lint; then
            log "SUCCESS" "ESLint问题已自动修复"
        else
            log "ERROR" "ESLint检查失败，需要手动修复"
            return 1
        fi
    fi
    
    # 构建测试
    log "INFO" "运行构建测试..."
    if npm run build; then
        log "SUCCESS" "构建测试通过"
    else
        log "ERROR" "构建测试失败"
        return 1
    fi
    
    cd ..
}

# 检查代码复杂度
check_code_complexity() {
    log "INFO" "检查代码复杂度..."
    
    cd admin-frontend
    
    # 检查大文件
    log "INFO" "检查大文件（>500行）..."
    large_files=$(find src -name "*.vue" -o -name "*.js" | xargs wc -l | awk '$1 > 500 {print $2 " (" $1 " lines)"}' | head -10)
    
    if [ -n "$large_files" ]; then
        log "WARNING" "发现大文件，建议拆分："
        echo "$large_files"
    else
        log "SUCCESS" "没有发现过大的文件"
    fi
    
    # 检查重复代码
    log "INFO" "检查重复代码..."
    duplicate_lines=$(find src -name "*.vue" -o -name "*.js" | xargs cat | sort | uniq -d | wc -l)
    
    if [ "$duplicate_lines" -gt 50 ]; then
        log "WARNING" "发现较多重复代码行数: $duplicate_lines"
    else
        log "SUCCESS" "重复代码检查通过"
    fi
    
    cd ..
}

# 检查依赖安全性
check_security() {
    log "INFO" "检查依赖安全性..."
    
    cd admin-frontend
    
    # npm audit
    log "INFO" "运行npm audit..."
    if npm audit --audit-level=high; then
        log "SUCCESS" "安全检查通过"
    else
        log "WARNING" "发现安全漏洞，建议运行 npm audit fix"
    fi
    
    cd ..
}

# 生成质量报告
generate_report() {
    log "INFO" "生成质量报告..."
    
    local report_file="quality-report-$(date +%Y%m%d-%H%M%S).md"
    
    cat > "$report_file" << EOF
# 代码质量检查报告

**检查时间**: $(date '+%Y-%m-%d %H:%M:%S')

## 检查结果

### ESLint检查
- 状态: ✅ 通过
- 说明: 代码符合ESLint规范

### 构建测试
- 状态: ✅ 通过
- 说明: 项目可以正常构建

### 代码复杂度
- 大文件检查: 已完成
- 重复代码检查: 已完成

### 安全检查
- npm audit: 已完成

## 建议

1. 定期运行此脚本检查代码质量
2. 遵循代码规范和最佳实践
3. 及时修复ESLint警告
4. 保持依赖更新和安全

## 下次检查

建议每周运行一次质量检查。
EOF

    log "SUCCESS" "质量报告已生成: $report_file"
}

# 主函数
main() {
    log "INFO" "开始代码质量检查..."
    
    # 检查依赖
    check_dependencies
    
    # 检查前端代码质量
    if check_frontend_quality; then
        log "SUCCESS" "前端代码质量检查通过"
    else
        log "ERROR" "前端代码质量检查失败"
        exit 1
    fi
    
    # 检查代码复杂度
    check_code_complexity
    
    # 检查安全性
    check_security
    
    # 生成报告
    generate_report
    
    log "SUCCESS" "代码质量检查完成！"
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi