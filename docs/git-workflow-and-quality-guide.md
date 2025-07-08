# Git 工作流程与代码质量管理指南

## 🚀 快速解决当前 Git 问题

### 问题诊断
您遇到的错误 `Author identity unknown` 是因为 Git 需要知道提交者的身份信息。

### 立即解决方案

#### 方法一：使用自动化脚本（推荐）
```bash
# 运行用户配置脚本
.\git-user-config.bat
```

#### 方法二：手动配置
```bash
# 配置全局用户信息
git config --global user.name "您的姓名"
git config --global user.email "您的邮箱@example.com"

# 验证配置
git config --global user.name
git config --global user.email

# 重新提交
git commit -m "Initial commit"
```

## 📋 Git 工作流程最佳实践

### 1. 分支管理策略

#### Git Flow 模型
```bash
# 主要分支
main/master     # 生产环境代码
develop         # 开发环境代码

# 功能分支
feature/user-management
feature/order-system
feature/payment-integration

# 修复分支
hotfix/critical-bug-fix
bugfix/minor-issue-fix

# 发布分支
release/v1.0.0
```

#### 分支命名规范
```bash
# 功能开发
feature/功能名称
feature/user-authentication
feature/product-search

# Bug 修复
bugfix/问题描述
bugfix/login-error
bugfix/image-loading

# 热修复
hotfix/紧急问题
hotfix/security-patch

# 发布准备
release/版本号
release/v2.1.0
```

### 2. 提交信息规范

#### Conventional Commits 格式
```bash
<类型>[可选范围]: <描述>

[可选正文]

[可选脚注]
```

#### 提交类型
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式化
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变动

#### 示例
```bash
git commit -m "feat(auth): 添加微信登录功能"
git commit -m "fix(order): 修复订单取消时的状态更新问题"
git commit -m "docs(api): 更新用户管理 API 文档"
git commit -m "refactor(payment): 重构支付模块提高可维护性"
```

### 3. 代码审查流程

#### Pull Request 模板
```markdown
## 变更描述
- [ ] 新功能
- [ ] Bug 修复
- [ ] 文档更新
- [ ] 重构

## 测试
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 手动测试完成

## 检查清单
- [ ] 代码符合项目规范
- [ ] 添加了必要的测试
- [ ] 更新了相关文档
- [ ] 没有引入安全漏洞
```

## 🔧 代码质量工具配置

### 1. 后端代码质量工具

#### Maven 插件配置 (pom.xml)
```xml
<build>
    <plugins>
        <!-- Checkstyle 代码规范检查 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>3.1.2</version>
            <configuration>
                <configLocation>checkstyle.xml</configLocation>
                <encoding>UTF-8</encoding>
                <consoleOutput>true</consoleOutput>
                <failsOnError>true</failsOnError>
            </configuration>
        </plugin>
        
        <!-- SpotBugs 静态代码分析 -->
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>4.7.3.0</version>
        </plugin>
        
        <!-- JaCoCo 代码覆盖率 -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.8</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### Checkstyle 配置示例
```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <property name="charset" value="UTF-8"/>
    <property name="severity" value="warning"/>
    
    <module name="TreeWalker">
        <!-- 命名规范 -->
        <module name="ConstantName"/>
        <module name="LocalFinalVariableName"/>
        <module name="LocalVariableName"/>
        <module name="MemberName"/>
        <module name="MethodName"/>
        <module name="PackageName"/>
        <module name="ParameterName"/>
        <module name="StaticVariableName"/>
        <module name="TypeName"/>
        
        <!-- 代码长度 -->
        <module name="LineLength">
            <property name="max" value="120"/>
        </module>
        <module name="MethodLength">
            <property name="max" value="50"/>
        </module>
        
        <!-- 空白符 -->
        <module name="EmptyForIteratorPad"/>
        <module name="GenericWhitespace"/>
        <module name="MethodParamPad"/>
        <module name="NoWhitespaceAfter"/>
        <module name="NoWhitespaceBefore"/>
        <module name="ParenPad"/>
        <module name="TypecastParenPad"/>
        <module name="WhitespaceAfter"/>
        <module name="WhitespaceAround"/>
    </module>
</module>
```

### 2. 前端代码质量工具

#### ESLint 配置 (.eslintrc.js)
```javascript
module.exports = {
  root: true,
  env: {
    node: true,
    browser: true,
    es2021: true
  },
  extends: [
    'plugin:vue/vue3-essential',
    '@vue/standard',
    '@vue/typescript/recommended'
  ],
  parserOptions: {
    ecmaVersion: 2021
  },
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'vue/multi-word-component-names': 'off',
    '@typescript-eslint/no-explicit-any': 'warn',
    'indent': ['error', 2],
    'quotes': ['error', 'single'],
    'semi': ['error', 'never'],
    'comma-dangle': ['error', 'never'],
    'max-len': ['error', { code: 120 }]
  }
}
```

#### Prettier 配置 (.prettierrc)
```json
{
  "semi": false,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "none",
  "printWidth": 120,
  "bracketSpacing": true,
  "arrowParens": "avoid",
  "endOfLine": "lf"
}
```

### 3. Git Hooks 自动化

#### Pre-commit Hook
```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "运行代码质量检查..."

# 后端代码检查
if [ -d "backend" ]; then
    echo "检查后端代码..."
    cd backend
    mvn checkstyle:check
    if [ $? -ne 0 ]; then
        echo "后端代码规范检查失败"
        exit 1
    fi
    cd ..
fi

# 前端代码检查
if [ -d "admin-frontend" ]; then
    echo "检查前端代码..."
    cd admin-frontend
    npm run lint
    if [ $? -ne 0 ]; then
        echo "前端代码规范检查失败"
        exit 1
    fi
    cd ..
fi

echo "代码质量检查通过"
```

## 📊 持续集成配置

### GitHub Actions 工作流
```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run backend tests
      run: |
        cd backend
        mvn clean test
        mvn checkstyle:check
        mvn spotbugs:check
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Backend Tests
        path: backend/target/surefire-reports/*.xml
        reporter: java-junit

  frontend-test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
        cache-dependency-path: admin-frontend/package-lock.json
    
    - name: Install dependencies
      run: |
        cd admin-frontend
        npm ci
    
    - name: Run frontend tests
      run: |
        cd admin-frontend
        npm run lint
        npm run test:unit
        npm run build

  security-scan:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        scan-type: 'fs'
        scan-ref: '.'
        format: 'sarif'
        output: 'trivy-results.sarif'
    
    - name: Upload Trivy scan results
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: 'trivy-results.sarif'
```

## 🔍 代码质量度量

### 关键指标

1. **代码覆盖率**
   - 目标：>80%
   - 工具：JaCoCo (后端), Jest (前端)

2. **代码复杂度**
   - 目标：圈复杂度 <10
   - 工具：SpotBugs, ESLint

3. **代码重复率**
   - 目标：<5%
   - 工具：PMD, SonarQube

4. **技术债务**
   - 目标：<1天
   - 工具：SonarQube

### SonarQube 集成
```yaml
# sonar-project.properties
sonar.projectKey=fresh-delivery-system
sonar.projectName=边墙鲜送系统
sonar.projectVersion=1.0

# 源码路径
sonar.sources=backend/src/main,admin-frontend/src,miniprogram
sonar.tests=backend/src/test,admin-frontend/src/__tests__

# 排除文件
sonar.exclusions=**/node_modules/**,**/target/**,**/*.min.js

# 语言设置
sonar.java.source=17
sonar.javascript.lcov.reportPaths=admin-frontend/coverage/lcov.info
sonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml
```

## 📚 团队协作规范

### 1. 代码审查清单

#### 功能性
- [ ] 功能是否按需求实现
- [ ] 边界条件是否处理
- [ ] 错误处理是否完善
- [ ] 性能是否满足要求

#### 代码质量
- [ ] 代码是否易读易懂
- [ ] 命名是否规范
- [ ] 是否遵循设计模式
- [ ] 是否有代码重复

#### 安全性
- [ ] 输入验证是否充分
- [ ] 是否有SQL注入风险
- [ ] 敏感信息是否加密
- [ ] 权限控制是否正确

#### 测试
- [ ] 单元测试是否充分
- [ ] 集成测试是否通过
- [ ] 测试用例是否覆盖边界

### 2. 发布流程

```bash
# 1. 功能开发完成
git checkout develop
git pull origin develop
git checkout -b feature/new-feature
# 开发...
git add .
git commit -m "feat: 添加新功能"
git push origin feature/new-feature

# 2. 创建 Pull Request
# 在 GitHub 上创建 PR，请求合并到 develop

# 3. 代码审查通过后合并
git checkout develop
git pull origin develop

# 4. 准备发布
git checkout -b release/v1.1.0
# 更新版本号、CHANGELOG 等
git commit -m "chore: 准备 v1.1.0 发布"

# 5. 合并到主分支
git checkout main
git merge release/v1.1.0
git tag v1.1.0
git push origin main --tags

# 6. 回合到开发分支
git checkout develop
git merge release/v1.1.0
git push origin develop
```

## 🛠️ 开发环境配置

### IDE 配置

#### IntelliJ IDEA
```xml
<!-- .idea/codeStyles/Project.xml -->
<component name="ProjectCodeStyleConfiguration">
  <code_scheme name="Project" version="173">
    <option name="LINE_SEPARATOR" value="\n" />
    <option name="RIGHT_MARGIN" value="120" />
    <JavaCodeStyleSettings>
      <option name="IMPORT_LAYOUT_TABLE">
        <value>
          <package name="java" withSubpackages="true" static="false" />
          <package name="javax" withSubpackages="true" static="false" />
          <emptyLine />
          <package name="org" withSubpackages="true" static="false" />
          <package name="com" withSubpackages="true" static="false" />
          <emptyLine />
          <package name="" withSubpackages="true" static="false" />
        </value>
      </option>
    </JavaCodeStyleSettings>
  </code_scheme>
</component>
```

#### VS Code 配置
```json
// .vscode/settings.json
{
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "eslint.validate": [
    "javascript",
    "javascriptreact",
    "typescript",
    "typescriptreact",
    "vue"
  ],
  "vetur.validation.template": false,
  "vetur.validation.script": false,
  "vetur.validation.style": false,
  "typescript.preferences.importModuleSpecifier": "relative",
  "files.eol": "\n",
  "editor.tabSize": 2,
  "editor.insertSpaces": true
}
```

## 📈 质量改进路线图

### 第一阶段：基础设施（1-2周）
- [x] Git 工作流程规范
- [ ] 代码规范工具配置
- [ ] 自动化测试环境
- [ ] CI/CD 流水线

### 第二阶段：代码质量（2-3周）
- [ ] 单元测试覆盖率提升
- [ ] 代码重构和优化
- [ ] 安全漏洞修复
- [ ] 性能优化

### 第三阶段：监控和维护（1-2周）
- [ ] 代码质量监控
- [ ] 自动化部署
- [ ] 文档完善
- [ ] 团队培训

## 🎯 成功指标

- 代码覆盖率 > 80%
- 构建成功率 > 95%
- 代码审查通过率 > 90%
- 平均修复时间 < 2小时
- 技术债务 < 1天
- 安全漏洞数量 = 0

---

**立即行动**：运行 `git-user-config.bat` 解决当前的 Git 配置问题，然后按照本指南逐步提升项目质量！