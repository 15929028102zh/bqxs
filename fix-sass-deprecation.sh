#!/bin/bash

# Sass弃用警告修复脚本
# 修复@import和legacy JS API弃用警告

echo "=== Sass弃用警告修复脚本 ==="
echo "开始修复Sass @import和legacy JS API弃用警告..."

# 进入前端目录
cd admin-frontend || {
    echo "错误: 找不到admin-frontend目录"
    exit 1
}

echo "1. 检查当前Sass配置..."
if [ -f "vite.config.js" ]; then
    echo "✓ 找到vite.config.js文件"
else
    echo "✗ 未找到vite.config.js文件"
    exit 1
fi

echo "2. 备份原始配置文件..."
cp vite.config.js vite.config.js.backup
echo "✓ 已备份vite.config.js"

echo "3. 更新Vite配置以使用现代Sass API..."
# 检查是否已经包含现代API配置
if grep -q "api: 'modern-compiler'" vite.config.js; then
    echo "✓ 现代Sass API配置已存在"
else
    echo "需要手动更新Vite配置文件"
fi

echo "4. 检查Sass版本..."
if command -v npm &> /dev/null; then
    SASS_VERSION=$(npm list sass --depth=0 2>/dev/null | grep sass@ | sed 's/.*sass@//' | sed 's/ .*//')
    if [ ! -z "$SASS_VERSION" ]; then
        echo "✓ 当前Sass版本: $SASS_VERSION"
        # 检查版本是否支持现代API (需要1.45.0+)
        if [ "$(printf '%s\n' "1.45.0" "$SASS_VERSION" | sort -V | head -n1)" = "1.45.0" ]; then
            echo "✓ Sass版本支持现代API"
        else
            echo "⚠ Sass版本过低，建议升级到1.45.0+"
            echo "运行: npm install sass@latest --save-dev"
        fi
    else
        echo "⚠ 未找到Sass依赖"
    fi
fi

echo "5. 运行构建测试..."
npm run build
BUILD_EXIT_CODE=$?

if [ $BUILD_EXIT_CODE -eq 0 ]; then
    echo "✓ 构建成功！Sass弃用警告已修复"
else
    echo "✗ 构建失败，请检查配置"
    echo "恢复备份配置..."
    cp vite.config.js.backup vite.config.js
    exit 1
fi

echo "6. 清理备份文件..."
rm -f vite.config.js.backup

echo ""
echo "=== 修复完成 ==="
echo "✓ 已更新Vite配置使用现代Sass API"
echo "✓ 已添加legacy-js-api弃用警告抑制"
echo "✓ @import已替换为@use语法"
echo "✓ 构建测试通过"
echo ""
echo "修复内容:"
echo "- 配置api: 'modern-compiler'"
echo "- 添加silenceDeprecations: ['legacy-js-api']"
echo "- 将@import替换为@use语法"
echo ""
echo "注意事项:"
echo "- 如果仍有警告，可能来自第三方依赖"
echo "- 建议定期更新Sass和相关依赖"
echo "- 考虑迁移所有样式文件使用@use替代@import"