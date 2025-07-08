# 管理员登录系统

## 概述

本系统实现了独立的管理员登录功能，与微信小程序用户登录完全分离。管理员通过用户名和密码进行身份验证，获取专用的JWT令牌来访问管理后台接口。

## 系统架构

### 用户类型分离
- **微信小程序用户**: 使用 `/user/*` 接口，通过微信code登录
- **管理员用户**: 使用 `/admin/*` 接口，通过用户名密码登录

### 认证机制
- **用户认证**: JWT令牌包含 `userType: "USER"`
- **管理员认证**: JWT令牌包含 `userType: "ADMIN"`
- **拦截器验证**: 管理员接口只接受管理员类型的令牌

## API接口

### 1. 管理员登录

**接口地址**: `POST /admin/login`

**请求参数**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "系统管理员",
    "email": null,
    "phone": null,
    "role": "SUPER_ADMIN",
    "status": 1,
    "lastLoginTime": "2024-01-01T10:00:00",
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "createTime": "2024-01-01T00:00:00"
  }
}
```

### 2. 获取管理员信息

**接口地址**: `GET /admin/info`

**请求头**:
```
Authorization: Bearer {token}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "系统管理员",
    "email": null,
    "phone": null,
    "role": "SUPER_ADMIN",
    "status": 1,
    "lastLoginTime": "2024-01-01T10:00:00",
    "createTime": "2024-01-01T00:00:00"
  }
}
```

### 3. 管理员退出登录

**接口地址**: `POST /admin/logout`

**请求头**:
```
Authorization: Bearer {token}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

## 数据库设计

### 管理员表 (tb_admin)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 管理员ID（主键） |
| username | varchar(50) | 用户名（唯一） |
| password | varchar(255) | 密码（SHA-256加密） |
| name | varchar(100) | 管理员姓名 |
| email | varchar(100) | 邮箱 |
| phone | varchar(20) | 手机号 |
| role | varchar(20) | 角色（SUPER_ADMIN/ADMIN） |
| status | tinyint(1) | 状态（0:禁用 1:启用） |
| last_login_time | datetime | 最后登录时间 |
| last_login_ip | varchar(50) | 最后登录IP |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

## 安全特性

### 密码加密
- 使用SHA-256算法
- 添加固定盐值：`fresh-delivery-salt-2024`
- Base64编码存储

### 令牌管理
- JWT令牌有效期：7天
- Redis存储令牌状态，支持主动失效
- 令牌包含用户类型标识，防止越权访问

### 访问控制
- 管理员拦截器验证所有 `/admin/*` 接口
- 自动记录登录时间和IP地址
- 支持账户状态控制（启用/禁用）

## 默认账户

系统初始化时会创建默认管理员账户：
- **用户名**: `admin`
- **密码**: `123456`
- **角色**: `SUPER_ADMIN`

> ⚠️ **安全提醒**: 请在首次登录后立即修改默认密码！

## 前端集成

前端管理系统已更新相关API调用：
- 登录接口：`/admin/login`
- 用户信息接口：`/admin/info`
- 退出登录接口：`/admin/logout`

## 部署说明

1. **执行数据库脚本**:
   ```sql
   -- 执行 backend/src/main/resources/sql/admin.sql
   ```

2. **重启后端服务**:
   ```bash
   cd backend
   mvn clean package
   java -jar target/fresh-delivery-backend-1.0.0.jar
   ```

3. **重启前端服务**:
   ```bash
   cd admin-frontend
   npm run dev
   ```

## 测试验证

使用默认账户登录管理后台：
1. 访问前端管理系统
2. 输入用户名：`admin`
3. 输入密码：`123456`
4. 点击登录

登录成功后可以访问管理员专用接口。