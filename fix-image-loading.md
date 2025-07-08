# 微信小程序图片加载问题快速修复指南

## 当前问题

您遇到的错误信息：
```
[渲染层网络层错误] Failed to load local image resource /pages/index/null/api/images/logo.svg
[Component] <wx-image>: 图片链接不再支持 HTTP 协议，请升级到 HTTPS
```

## 立即解决方案（开发环境）

### 步骤 1：关闭微信开发者工具的域名校验

1. 打开微信开发者工具
2. 点击右上角的「详情」按钮
3. 选择「本地设置」标签
4. **勾选**「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」
5. 点击「编译」重新编译小程序

### 步骤 2：验证后端服务状态

确认后端服务正在运行：
- 后端地址：`http://192.168.229.1:8081/api`
- 健康检查：在浏览器访问 `http://192.168.229.1:8081/api/health`

### 步骤 3：清除小程序缓存

1. 在微信开发者工具中点击「清缓存」
2. 选择「清除全部缓存」
3. 重新编译小程序

## 代码修复说明

我已经在代码中添加了以下防护措施：

### 1. imageConfig.js 防护
```javascript
// 防护：确保 baseUrl 不为 null 或 undefined
if (!baseUrl || baseUrl === 'null' || baseUrl === 'undefined') {
  console.error('ImageConfig init: baseUrl is invalid:', baseUrl);
  baseUrl = 'http://192.168.229.1:8081/api'; // 使用默认值
}
```

### 2. app.js 防护
```javascript
// 确保 baseUrl 有效
if (!this.globalData.baseUrl) {
  console.error('App onLaunch: baseUrl is not set, using default');
  this.globalData.baseUrl = 'http://192.168.229.1:8081/api';
}
```

### 3. 后端配置统一
- 所有硬编码的 `localhost` 已替换为配置的域名
- 使用 `@Value("${file.upload.domain}")` 注入配置

## 真机测试解决方案

如果需要在手机上测试：

### 方法 1：开启调试模式
1. 在微信开发者工具中点击「预览」
2. 用手机扫码打开小程序
3. 点击右上角「...」-> 「开发调试」
4. 开启调试模式

### 方法 2：临时修改为 IP 访问
如果调试模式不可用，可以临时修改配置：

在 `app.js` 中：
```javascript
globalData: {
  baseUrl: 'http://192.168.229.1:8081/api', // 确保使用正确的 IP
}
```

## 生产环境解决方案

生产环境必须配置 HTTPS，请参考 `docs/https-setup-guide.md` 文档。

## 验证修复效果

1. **开发者工具验证**：
   - 关闭域名校验后重新编译
   - 检查控制台是否还有错误
   - 验证图片是否正常显示

2. **网络请求验证**：
   - 在开发者工具的 Network 标签中查看请求
   - 确认图片请求返回 200 状态码

3. **真机验证**：
   - 开启调试模式后测试
   - 检查图片是否正常加载

## 常见问题

### Q: 关闭域名校验后仍然报错
**A**: 检查后端服务是否正常运行，确认端口 8081 可访问

### Q: 图片路径中仍然包含 null
**A**: 清除小程序缓存，重新编译，我已添加防护代码避免此问题

### Q: 真机无法访问 192.168.229.1
**A**: 确保手机和电脑在同一网络，或使用电脑的实际 IP 地址

## 下一步计划

1. **短期**：使用开发环境解决方案继续开发
2. **中期**：配置内网穿透或测试域名
3. **长期**：申请正式域名和 SSL 证书，配置生产环境 HTTPS

按照以上步骤操作后，图片加载问题应该得到解决。如果仍有问题，请检查后端服务状态和网络连接。