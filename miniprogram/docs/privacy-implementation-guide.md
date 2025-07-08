# 小程序隐私协议实现指南

## 概述

本项目已按照微信官方《小程序隐私协议开发指南》完整实现了隐私授权机制，包括主动检查隐私设置、被动监听隐私接口调用、以及统一的隐私弹窗组件。

## 实现架构

### 1. 全局隐私管理 (app.js)

- `initPrivacyAuthorization()`: 初始化隐私授权监听
- `checkPrivacySetting()`: 主动检查隐私设置
- `triggerPrivacyModal()`: 触发隐私弹窗显示
- `handlePrivacyAgree()`: 处理用户同意
- `handlePrivacyDisagree()`: 处理用户拒绝
- `openPrivacyContract()`: 打开隐私协议页面

### 2. 隐私弹窗组件 (/components/privacy-modal/)

统一的隐私授权弹窗组件，支持：
- 自定义隐私协议名称
- 查看隐私协议详情
- 同意/拒绝操作
- 事件回调机制

### 3. 页面集成示例 (pages/login/)

登录页面完整集成了隐私授权机制：
- 页面加载时主动检查隐私设置
- 集成隐私弹窗组件
- 处理隐私授权事件

## 使用方法

### 在页面中集成隐私弹窗

1. **页面配置 (page.json)**
```json
{
  "usingComponents": {
    "privacy-modal": "/components/privacy-modal/privacy-modal"
  }
}
```

2. **页面数据 (page.js)**
```javascript
Page({
  data: {
    showPrivacyModal: false,
    privacyContractName: '《用户隐私保护指引》',
    privacyEventInfo: {}
  },

  onLoad() {
    // 主动检查隐私设置
    this.checkPrivacySettingOnLoad();
  },

  // 检查隐私设置
  async checkPrivacySettingOnLoad() {
    try {
      const app = getApp();
      const privacySetting = await app.checkPrivacySetting();
      
      if (privacySetting.needAuthorization) {
        this.setData({
          privacyContractName: privacySetting.privacyContractName || '《用户隐私保护指引》'
        });
        // 可选择立即显示或等待用户操作
        // this.showPrivacyModal();
      }
    } catch (error) {
      console.log('检查隐私设置失败或不支持:', error);
    }
  },

  // 显示隐私弹窗（供app.js调用）
  showPrivacyModal(eventInfo = {}) {
    this.setData({
      showPrivacyModal: true,
      privacyEventInfo: eventInfo
    });
  },

  // 隐私协议同意事件
  onPrivacyAgree(e) {
    this.setData({ showPrivacyModal: false });
    // 执行同意后的逻辑
  },

  // 隐私协议拒绝事件
  onPrivacyDisagree(e) {
    this.setData({ showPrivacyModal: false });
    wx.showToast({
      title: '需要同意隐私协议才能使用相关功能',
      icon: 'none'
    });
  }
});
```

3. **页面模板 (page.wxml)**
```xml
<!-- 隐私弹窗组件 -->
<privacy-modal 
  show="{{showPrivacyModal}}"
  privacyContractName="{{privacyContractName}}"
  eventInfo="{{privacyEventInfo}}"
  bind:agree="onPrivacyAgree"
  bind:disagree="onPrivacyDisagree"
></privacy-modal>
```

## 工作流程

### 主动检查流程
1. 页面加载时调用 `app.checkPrivacySetting()`
2. 如果 `needAuthorization` 为 `true`，准备显示隐私弹窗
3. 用户操作时显示弹窗，等待用户选择

### 被动监听流程
1. 用户调用隐私接口（如 `wx.getUserProfile`）
2. 触发 `wx.onNeedPrivacyAuthorization` 事件
3. app.js 自动显示隐私弹窗
4. 用户同意后，原接口继续执行

## 配置要求

### app.json 配置
```json
{
  "__usePrivacyCheck__": true,
  "requiredPrivateInfos": [
    "getLocation",
    "chooseAddress"
  ]
}
```

**注意**: `getUserProfile` 不需要在 `requiredPrivateInfos` 中声明。

### 小程序管理后台配置
1. 登录小程序管理后台
2. 进入「设置」->「基本设置」->「隐私设置」
3. 配置《小程序用户隐私保护指引》
4. 在指引中声明需要处理的用户信息类型

## 兼容性说明

- 基础库版本要求：2.32.3+
- 微信版本要求：支持隐私协议功能的版本
- 对于不支持的版本，会自动降级为传统授权方式

## 调试方法

1. **开发者工具**: 「清除模拟器缓存」->「清除授权数据」
2. **真机调试**: 从微信下拉菜单删除小程序记录
3. **日志查看**: 控制台会输出详细的隐私授权流程日志

## 常见问题

### Q: 隐私弹窗不显示？
A: 检查基础库版本、app.json配置、管理后台隐私设置

### Q: 用户拒绝后如何重新授权？
A: 用户需要删除小程序记录重新进入，或在设置中重新授权

### Q: 如何自定义隐私弹窗样式？
A: 修改 `/components/privacy-modal/privacy-modal.wxss` 文件

## 更新日志

- 2024.01: 完整实现微信官方隐私协议开发指南
- 支持主动检查和被动监听两种模式
- 提供统一的隐私弹窗组件
- 完善的错误处理和兼容性支持