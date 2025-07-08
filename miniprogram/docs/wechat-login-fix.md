# 微信登录配置修复指南

## 问题描述

用户在尝试微信登录时遇到 `RuntimeException: 微信登录失败，请重试` 错误。

## 问题分析

通过分析后端代码和日志，发现问题的根本原因是**微信小程序配置不匹配**：

### 1. 配置键不匹配
- **WeChatUtil.java** 中使用的配置键：`wechat.appid` 和 `wechat.secret`
- **application.yml** 中定义的配置键：`wechat.miniapp.app-id` 和 `wechat.miniapp.app-secret`
- 由于配置键不匹配，导致使用了默认值 `your-app-id` 和 `your-app-secret`

### 2. 微信API调用失败
- 使用无效的AppID和AppSecret调用微信API
- 微信服务器返回错误响应
- `WeChatUtil.getWeChatUserInfo()` 方法返回 `null`
- 触发 `UserServiceImpl.java:45` 的异常

## 修复步骤

### 步骤1：修复配置键不匹配问题

已修复 `WeChatUtil.java` 中的配置注解：

```java
// 修复前
@Value("${wechat.appid:your-app-id}")
private String appId;

@Value("${wechat.secret:your-app-secret}")
private String appSecret;

// 修复后
@Value("${wechat.miniapp.app-id:your-app-id}")
private String appId;

@Value("${wechat.miniapp.app-secret:your-app-secret}")
private String appSecret;
```

### 步骤2：配置真实的微信小程序信息

需要在 `application.yml` 中配置真实的微信小程序AppID和AppSecret：

```yaml
wechat:
  miniapp:
    app-id: wx1234567890abcdef  # 替换为真实的AppID
    app-secret: abcdef1234567890abcdef1234567890  # 替换为真实的AppSecret
```

## 获取微信小程序配置信息

### 1. 注册微信小程序
1. 访问 [微信公众平台](https://mp.weixin.qq.com)
2. 注册小程序账号
3. 完成小程序信息填写和认证

### 2. 获取AppID和AppSecret
1. 登录微信公众平台
2. 进入「开发」→「开发管理」→「开发设置」
3. 复制 **AppID(小程序ID)**
4. 生成并复制 **AppSecret(小程序密钥)**

### 3. 配置服务器域名
在微信公众平台配置服务器域名：
- **request合法域名**：添加后端服务器域名
- **socket合法域名**：如需要WebSocket功能
- **uploadFile合法域名**：如需要文件上传功能
- **downloadFile合法域名**：如需要文件下载功能

## 测试验证

### 1. 重启后端服务
配置修改后需要重启后端服务：

```bash
# 如果使用Docker
cd f:\code\docker
docker-compose restart backend

# 如果使用本地运行
# 停止当前服务，重新启动
```

### 2. 使用健康检查接口验证配置

#### 方法1：小程序内测试（推荐）
1. 打开小程序
2. 进入「隐私测试」页面
3. 点击「测试后端连接」按钮
4. 查看连接状态和微信配置信息

#### 方法2：浏览器直接访问
```
# 基础健康检查
http://localhost:8081/health

# 详细健康检查（包含微信配置状态）
http://localhost:8081/health/detail

# 专门的微信配置检查
http://localhost:8081/health/wechat
```

#### 方法3：使用单元测试
```bash
# 在backend目录下运行
mvn test -Dtest=WeChatUtilTest
```

### 3. 检查配置是否生效
查看后端启动日志，确认微信配置已正确加载：

```
# 正确的日志应该显示：
微信小程序配置: appId=wx1234567890abcdef

# 错误的日志会显示：
微信小程序AppID使用默认值，请在application.yml中配置真实的AppID
```

### 4. 测试微信登录
1. 在小程序中尝试登录
2. 查看后端日志中的微信API响应
3. 确认能够成功获取到openid

## 常见问题排查

### 1. 仍然显示 "invalid appid" 错误
- 检查AppID是否正确复制（注意去除空格）
- 确认AppID对应的是小程序而不是公众号
- 验证AppSecret是否正确且未过期

### 2. "invalid code" 错误
- 检查前端传递的code是否有效
- 确认code没有被重复使用（微信code只能使用一次）
- 验证小程序的AppID与后端配置是否一致

### 3. 网络连接问题
- 确认服务器能够访问微信API（api.weixin.qq.com）
- 检查防火墙设置
- 验证DNS解析是否正常

## 开发建议

### 1. 环境配置管理
建议为不同环境配置不同的微信小程序：

```yaml
# application-dev.yml (开发环境)
wechat:
  miniapp:
    app-id: wx_dev_appid
    app-secret: dev_app_secret

# application-prod.yml (生产环境)
wechat:
  miniapp:
    app-id: wx_prod_appid
    app-secret: prod_app_secret
```

### 2. 错误处理优化
可以在 `WeChatUtil` 中添加更详细的错误信息：

```java
if (jsonNode.has("errcode")) {
    int errcode = jsonNode.get("errcode").asInt();
    if (errcode != 0) {
        String errmsg = jsonNode.get("errmsg").asText();
        log.error("微信登录失败: errcode={}, errmsg={}, appId={}", errcode, errmsg, appId);
        throw new RuntimeException(String.format("微信登录失败: %s (错误码: %d)", errmsg, errcode));
    }
}
```

### 3. 配置验证
可以添加配置验证逻辑：

```java
@PostConstruct
public void validateConfig() {
    if ("your-app-id".equals(appId) || "your-app-secret".equals(appSecret)) {
        log.warn("微信小程序配置使用默认值，请在application.yml中配置真实的AppID和AppSecret");
    }
}
```

## 总结

微信登录失败的主要原因是配置问题：
1. **配置键不匹配** - 已修复
2. **使用默认配置值** - 需要配置真实的微信小程序信息

完成配置后，微信登录功能应该能够正常工作。如果仍有问题，请检查微信公众平台的配置和网络连接。