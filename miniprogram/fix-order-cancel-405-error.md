# 订单取消 405 错误修复指南

## 问题描述

在微信小程序中取消订单时，出现 HTTP 405 Method Not Allowed 错误：

```
POST http://192.168.229.1:8081/api/order/41/cancel 405(Method Not Allowed)
```

## 问题原因

**HTTP 方法不匹配**：
- 前端小程序使用 `POST` 方法调用取消订单接口
- 后端定义的取消订单接口使用 `PUT` 方法

## 解决方案

### 1. 修复前端代码

修改 `pages/order/list.js` 文件中的 `cancelOrder` 方法：

**修改前：**
```javascript
await app.request({
  url: `/order/${orderId}/cancel`,
  method: 'POST'
});
```

**修改后：**
```javascript
await app.request({
  url: `/order/${orderId}/cancel`,
  method: 'PUT',
  data: {
    reason: '用户取消'
  }
});
```

### 2. 后端接口定义

后端 `OrderController.java` 中的取消订单接口：

```java
@Operation(summary = "取消订单", description = "用户取消订单")
@PutMapping("/{id}/cancel")
public Result<Void> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
    // 实现逻辑
}
```

## 修复验证

1. **确认后端服务运行**：
   - 后端服务已在 8081 端口启动
   - 接口文档：http://localhost:8081/swagger-ui/index.html

2. **测试取消订单功能**：
   - 在小程序订单列表页面
   - 点击"取消订单"按钮
   - 确认不再出现 405 错误

## 相关接口规范

### 订单相关接口 HTTP 方法规范

| 操作 | 接口路径 | HTTP 方法 | 说明 |
|------|----------|-----------|------|
| 创建订单 | `/order/create` | POST | 创建新资源 |
| 获取订单详情 | `/order/{id}` | GET | 查询资源 |
| 取消订单 | `/order/{id}/cancel` | PUT | 更新资源状态 |
| 确认收货 | `/order/{id}/confirm` | PUT | 更新资源状态 |
| 获取订单列表 | `/order/list` | GET | 查询资源列表 |

## 预防措施

1. **API 文档同步**：
   - 确保前后端开发人员都参考最新的 API 文档
   - 使用 Swagger 等工具生成标准化文档

2. **接口测试**：
   - 在开发过程中及时测试接口
   - 使用 Postman 等工具验证接口正确性

3. **代码审查**：
   - 前后端代码变更时进行交叉审查
   - 确保接口定义和调用保持一致

## 常见问题

**Q: 为什么取消订单使用 PUT 而不是 POST？**
A: 取消订单是更新订单状态的操作，符合 RESTful API 设计规范中 PUT 用于更新资源的原则。

**Q: 如何避免类似问题？**
A: 建议使用 TypeScript 或接口定义工具，在编译时检查接口调用的正确性。

## 修复状态

✅ **已修复**：前端 HTTP 方法已从 POST 改为 PUT  
✅ **已验证**：后端服务正常运行  
✅ **已测试**：取消订单功能恢复正常