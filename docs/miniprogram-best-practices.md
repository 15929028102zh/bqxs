# 微信小程序开发最佳实践指南

## 📁 项目结构优化

### 推荐的目录结构
```
miniprogram/
├── app.js                 # 小程序入口文件
├── app.json              # 全局配置
├── app.wxss              # 全局样式
├── components/           # 自定义组件
│   ├── common/          # 通用组件
│   ├── business/        # 业务组件
│   └── ui/              # UI组件
├── pages/               # 页面文件
├── utils/               # 工具函数
│   ├── api/            # API接口封装
│   ├── helpers/        # 辅助函数
│   ├── constants/      # 常量定义
│   └── services/       # 业务服务
├── assets/              # 静态资源
│   ├── images/         # 图片资源
│   ├── icons/          # 图标资源
│   └── fonts/          # 字体资源
├── styles/              # 样式文件
│   ├── variables.wxss  # 样式变量
│   ├── mixins.wxss     # 样式混入
│   └── common.wxss     # 通用样式
└── config/              # 配置文件
    ├── env.js          # 环境配置
    └── constants.js    # 全局常量
```

## 🔧 代码组织优化

### 1. API 接口管理

#### 创建统一的 API 管理器
```javascript
// utils/api/apiManager.js
class ApiManager {
  constructor() {
    this.baseURL = getApp().globalData.baseUrl;
    this.defaultTimeout = 10000;
  }
  
  async request(options) {
    const {
      url,
      method = 'GET',
      data = {},
      header = {},
      timeout = this.defaultTimeout
    } = options;
    
    const fullUrl = url.startsWith('http') ? url : `${this.baseURL}${url}`;
    
    try {
      const response = await this.wxRequest({
        url: fullUrl,
        method,
        data,
        header: {
          'Content-Type': 'application/json',
          ...this.getAuthHeader(),
          ...header
        },
        timeout
      });
      
      return this.handleResponse(response);
    } catch (error) {
      throw this.handleError(error);
    }
  }
  
  wxRequest(options) {
    return new Promise((resolve, reject) => {
      wx.request({
        ...options,
        success: resolve,
        fail: reject
      });
    });
  }
  
  getAuthHeader() {
    const token = wx.getStorageSync('token');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  }
  
  handleResponse(response) {
    const { statusCode, data } = response;
    
    if (statusCode >= 200 && statusCode < 300) {
      return data;
    }
    
    throw new Error(`HTTP ${statusCode}: ${data.message || '请求失败'}`);
  }
  
  handleError(error) {
    if (error.errMsg) {
      if (error.errMsg.includes('timeout')) {
        throw new Error('请求超时，请检查网络连接');
      }
      if (error.errMsg.includes('fail')) {
        throw new Error('网络连接失败，请检查网络设置');
      }
    }
    throw error;
  }
}

export default new ApiManager();
```

#### 创建具体的 API 服务
```javascript
// utils/api/authApi.js
import ApiManager from './apiManager.js';

class AuthApi {
  // 微信登录
  async wxLogin(code, userInfo) {
    return await ApiManager.request({
      url: '/auth/wx-login',
      method: 'POST',
      data: { code, userInfo }
    });
  }
  
  // 手机号登录
  async phoneLogin(phone, smsCode) {
    return await ApiManager.request({
      url: '/auth/phone-login',
      method: 'POST',
      data: { phone, smsCode }
    });
  }
  
  // 发送验证码
  async sendSmsCode(phone) {
    return await ApiManager.request({
      url: '/auth/send-sms',
      method: 'POST',
      data: { phone }
    });
  }
}

export default new AuthApi();
```

### 2. 状态管理优化

#### 创建全局状态管理
```javascript
// utils/store/store.js
class Store {
  constructor() {
    this.state = {
      user: null,
      token: null,
      cart: [],
      loading: false
    };
    this.listeners = [];
  }
  
  getState() {
    return { ...this.state };
  }
  
  setState(newState) {
    this.state = { ...this.state, ...newState };
    this.notifyListeners();
  }
  
  subscribe(listener) {
    this.listeners.push(listener);
    return () => {
      const index = this.listeners.indexOf(listener);
      if (index > -1) {
        this.listeners.splice(index, 1);
      }
    };
  }
  
  notifyListeners() {
    this.listeners.forEach(listener => listener(this.state));
  }
  
  // 用户相关操作
  setUser(user) {
    this.setState({ user });
    if (user) {
      wx.setStorageSync('user', user);
    } else {
      wx.removeStorageSync('user');
    }
  }
  
  setToken(token) {
    this.setState({ token });
    if (token) {
      wx.setStorageSync('token', token);
    } else {
      wx.removeStorageSync('token');
    }
  }
  
  // 购物车操作
  addToCart(product) {
    const cart = [...this.state.cart];
    const existingItem = cart.find(item => item.id === product.id);
    
    if (existingItem) {
      existingItem.quantity += product.quantity || 1;
    } else {
      cart.push({ ...product, quantity: product.quantity || 1 });
    }
    
    this.setState({ cart });
    wx.setStorageSync('cart', cart);
  }
  
  removeFromCart(productId) {
    const cart = this.state.cart.filter(item => item.id !== productId);
    this.setState({ cart });
    wx.setStorageSync('cart', cart);
  }
  
  clearCart() {
    this.setState({ cart: [] });
    wx.removeStorageSync('cart');
  }
}

export default new Store();
```

### 3. 页面基类优化

#### 创建页面基类
```javascript
// utils/base/basePage.js
import Store from '../store/store.js';
import Logger from '../helpers/logger.js';

class BasePage {
  constructor(options = {}) {
    this.pageOptions = {
      data: {
        loading: false,
        error: null,
        ...options.data
      },
      
      onLoad(query) {
        Logger.info(`页面加载: ${this.route}`, query);
        this.initStore();
        if (options.onLoad) {
          options.onLoad.call(this, query);
        }
      },
      
      onShow() {
        Logger.info(`页面显示: ${this.route}`);
        if (options.onShow) {
          options.onShow.call(this);
        }
      },
      
      onHide() {
        Logger.info(`页面隐藏: ${this.route}`);
        if (options.onHide) {
          options.onHide.call(this);
        }
      },
      
      onUnload() {
        Logger.info(`页面卸载: ${this.route}`);
        this.cleanupStore();
        if (options.onUnload) {
          options.onUnload.call(this);
        }
      },
      
      // 初始化状态管理
      initStore() {
        this.unsubscribe = Store.subscribe((state) => {
          this.onStoreChange(state);
        });
      },
      
      // 清理状态管理
      cleanupStore() {
        if (this.unsubscribe) {
          this.unsubscribe();
        }
      },
      
      // 状态变化处理
      onStoreChange(state) {
        // 子类可以重写此方法
      },
      
      // 显示加载状态
      showLoading(title = '加载中...') {
        this.setData({ loading: true });
        wx.showLoading({ title, mask: true });
      },
      
      // 隐藏加载状态
      hideLoading() {
        this.setData({ loading: false });
        wx.hideLoading();
      },
      
      // 显示错误信息
      showError(error) {
        const message = error.message || error.errMsg || '操作失败';
        this.setData({ error: message });
        wx.showToast({
          title: message,
          icon: 'none',
          duration: 2000
        });
      },
      
      // 异步操作包装器
      async withLoading(asyncFn, loadingText) {
        try {
          this.showLoading(loadingText);
          const result = await asyncFn();
          this.hideLoading();
          return result;
        } catch (error) {
          this.hideLoading();
          this.showError(error);
          throw error;
        }
      },
      
      ...options
    };
  }
  
  create() {
    return this.pageOptions;
  }
}

export default BasePage;
```

#### 使用页面基类
```javascript
// pages/login/login.js
import BasePage from '../../utils/base/basePage.js';
import AuthApi from '../../utils/api/authApi.js';
import Store from '../../utils/store/store.js';

const loginPage = new BasePage({
  data: {
    canIUseGetUserProfile: false,
    showPrivacyModal: false,
    privacyContractName: '《用户隐私保护指引》'
  },
  
  onLoad() {
    this.checkUserProfileSupport();
    this.checkPrivacySettings();
  },
  
  onStoreChange(state) {
    // 监听全局状态变化
    if (state.user) {
      this.navigateToHome();
    }
  },
  
  checkUserProfileSupport() {
    this.setData({
      canIUseGetUserProfile: wx.canIUse('getUserProfile')
    });
  },
  
  async checkPrivacySettings() {
    try {
      const app = getApp();
      const privacySetting = await app.checkPrivacySetting();
      
      if (privacySetting.needAuthorization) {
        this.setData({
          privacyContractName: privacySetting.privacyContractName || '《用户隐私保护指引》'
        });
      }
    } catch (error) {
      console.log('隐私设置检查失败:', error);
    }
  },
  
  async wxLogin() {
    await this.withLoading(async () => {
      const userInfo = await this.getUserProfile();
      const code = await this.getWxLoginCode();
      const response = await AuthApi.wxLogin(code, userInfo);
      
      Store.setUser(response.user);
      Store.setToken(response.token);
    }, '正在登录...');
  },
  
  getUserProfile() {
    return new Promise((resolve, reject) => {
      wx.getUserProfile({
        desc: '用于完善用户资料',
        success: (res) => resolve(res.userInfo),
        fail: reject
      });
    });
  },
  
  getWxLoginCode() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => resolve(res.code),
        fail: reject
      });
    });
  },
  
  navigateToHome() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  }
});

Page(loginPage.create());
```

## 🎨 样式管理优化

### 1. 样式变量管理
```css
/* styles/variables.wxss */
/* 颜色变量 */
:root {
  --primary-color: #07c160;
  --secondary-color: #576b95;
  --success-color: #07c160;
  --warning-color: #ff976a;
  --error-color: #fa5151;
  --text-primary: #191919;
  --text-secondary: #666666;
  --text-placeholder: #999999;
  --border-color: #e5e5e5;
  --background-color: #f7f7f7;
  --white: #ffffff;
}

/* 尺寸变量 */
:root {
  --border-radius-small: 4rpx;
  --border-radius-medium: 8rpx;
  --border-radius-large: 12rpx;
  --spacing-xs: 8rpx;
  --spacing-sm: 16rpx;
  --spacing-md: 24rpx;
  --spacing-lg: 32rpx;
  --spacing-xl: 48rpx;
}

/* 字体变量 */
:root {
  --font-size-xs: 20rpx;
  --font-size-sm: 24rpx;
  --font-size-md: 28rpx;
  --font-size-lg: 32rpx;
  --font-size-xl: 36rpx;
  --font-weight-normal: 400;
  --font-weight-medium: 500;
  --font-weight-bold: 600;
}
```

### 2. 通用样式类
```css
/* styles/common.wxss */
/* 布局类 */
.flex {
  display: flex;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-1 {
  flex: 1;
}

/* 间距类 */
.m-xs { margin: var(--spacing-xs); }
.m-sm { margin: var(--spacing-sm); }
.m-md { margin: var(--spacing-md); }
.m-lg { margin: var(--spacing-lg); }
.m-xl { margin: var(--spacing-xl); }

.p-xs { padding: var(--spacing-xs); }
.p-sm { padding: var(--spacing-sm); }
.p-md { padding: var(--spacing-md); }
.p-lg { padding: var(--spacing-lg); }
.p-xl { padding: var(--spacing-xl); }

/* 文本类 */
.text-primary { color: var(--text-primary); }
.text-secondary { color: var(--text-secondary); }
.text-placeholder { color: var(--text-placeholder); }

.text-xs { font-size: var(--font-size-xs); }
.text-sm { font-size: var(--font-size-sm); }
.text-md { font-size: var(--font-size-md); }
.text-lg { font-size: var(--font-size-lg); }
.text-xl { font-size: var(--font-size-xl); }

.text-center { text-align: center; }
.text-left { text-align: left; }
.text-right { text-align: right; }

/* 按钮类 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius-medium);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: var(--primary-color);
  color: var(--white);
}

.btn-secondary {
  background-color: var(--secondary-color);
  color: var(--white);
}

.btn-outline {
  background-color: transparent;
  border: 2rpx solid var(--primary-color);
  color: var(--primary-color);
}
```

## 🧪 测试和调试

### 1. 单元测试框架
```javascript
// utils/test/testFramework.js
class TestFramework {
  constructor() {
    this.tests = [];
    this.results = [];
  }
  
  describe(description, testSuite) {
    console.group(`测试套件: ${description}`);
    testSuite();
    console.groupEnd();
  }
  
  it(description, testFn) {
    try {
      testFn();
      console.log(`✅ ${description}`);
      this.results.push({ description, status: 'passed' });
    } catch (error) {
      console.error(`❌ ${description}:`, error.message);
      this.results.push({ description, status: 'failed', error: error.message });
    }
  }
  
  expect(actual) {
    return {
      toBe: (expected) => {
        if (actual !== expected) {
          throw new Error(`期望 ${expected}，实际 ${actual}`);
        }
      },
      toEqual: (expected) => {
        if (JSON.stringify(actual) !== JSON.stringify(expected)) {
          throw new Error(`期望 ${JSON.stringify(expected)}，实际 ${JSON.stringify(actual)}`);
        }
      },
      toBeTruthy: () => {
        if (!actual) {
          throw new Error(`期望真值，实际 ${actual}`);
        }
      },
      toBeFalsy: () => {
        if (actual) {
          throw new Error(`期望假值，实际 ${actual}`);
        }
      }
    };
  }
  
  getResults() {
    return this.results;
  }
}

export default new TestFramework();
```

### 2. 调试工具
```javascript
// utils/debug/debugger.js
class Debugger {
  constructor() {
    this.enabled = false; // 生产环境设为 false
  }
  
  enable() {
    this.enabled = true;
  }
  
  disable() {
    this.enabled = false;
  }
  
  log(message, data) {
    if (this.enabled) {
      console.log(`[DEBUG] ${message}`, data);
    }
  }
  
  time(label) {
    if (this.enabled) {
      console.time(label);
    }
  }
  
  timeEnd(label) {
    if (this.enabled) {
      console.timeEnd(label);
    }
  }
  
  trace(message) {
    if (this.enabled) {
      console.trace(message);
    }
  }
  
  // 性能监控
  measurePerformance(fn, label) {
    if (!this.enabled) {
      return fn();
    }
    
    const start = Date.now();
    const result = fn();
    const end = Date.now();
    
    console.log(`[PERF] ${label}: ${end - start}ms`);
    return result;
  }
}

export default new Debugger();
```

## 📱 用户体验优化

### 1. 加载状态管理
```javascript
// utils/ui/loadingManager.js
class LoadingManager {
  constructor() {
    this.loadingCount = 0;
  }
  
  show(title = '加载中...') {
    this.loadingCount++;
    if (this.loadingCount === 1) {
      wx.showLoading({
        title,
        mask: true
      });
    }
  }
  
  hide() {
    this.loadingCount = Math.max(0, this.loadingCount - 1);
    if (this.loadingCount === 0) {
      wx.hideLoading();
    }
  }
  
  hideAll() {
    this.loadingCount = 0;
    wx.hideLoading();
  }
}

export default new LoadingManager();
```

### 2. 错误边界处理
```javascript
// utils/ui/errorBoundary.js
class ErrorBoundary {
  static handlePageError(error, page) {
    console.error('页面错误:', error);
    
    // 显示友好的错误信息
    wx.showModal({
      title: '出现错误',
      content: '页面遇到了一些问题，请稍后重试',
      confirmText: '重新加载',
      cancelText: '返回',
      success: (res) => {
        if (res.confirm) {
          // 重新加载页面
          page.onLoad();
        } else {
          // 返回上一页
          wx.navigateBack();
        }
      }
    });
  }
  
  static handleGlobalError(error) {
    console.error('全局错误:', error);
    
    // 可以上报错误到监控系统
    this.reportError(error);
  }
  
  static reportError(error) {
    // 错误上报逻辑
    // 可以集成第三方错误监控服务
  }
}

export default ErrorBoundary;
```

## 🔒 安全最佳实践

### 1. 数据验证
```javascript
// utils/validation/validator.js
class Validator {
  static isPhone(phone) {
    return /^1[3-9]\d{9}$/.test(phone);
  }
  
  static isEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }
  
  static isRequired(value) {
    return value !== null && value !== undefined && value !== '';
  }
  
  static minLength(value, min) {
    return value && value.length >= min;
  }
  
  static maxLength(value, max) {
    return value && value.length <= max;
  }
  
  static validate(data, rules) {
    const errors = {};
    
    for (const field in rules) {
      const value = data[field];
      const fieldRules = rules[field];
      
      for (const rule of fieldRules) {
        if (!rule.validator(value)) {
          errors[field] = rule.message;
          break;
        }
      }
    }
    
    return {
      isValid: Object.keys(errors).length === 0,
      errors
    };
  }
}

export default Validator;
```

### 2. 敏感信息处理
```javascript
// utils/security/dataProtection.js
class DataProtection {
  // 脱敏手机号
  static maskPhone(phone) {
    if (!phone || phone.length !== 11) return phone;
    return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
  }
  
  // 脱敏身份证
  static maskIdCard(idCard) {
    if (!idCard) return idCard;
    return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2');
  }
  
  // 清理敏感数据
  static sanitizeUserData(userData) {
    const sanitized = { ...userData };
    
    // 移除敏感字段
    delete sanitized.password;
    delete sanitized.token;
    
    // 脱敏处理
    if (sanitized.phone) {
      sanitized.phone = this.maskPhone(sanitized.phone);
    }
    
    return sanitized;
  }
}

export default DataProtection;
```

## 📊 性能监控

### 1. 性能指标收集
```javascript
// utils/performance/monitor.js
class PerformanceMonitor {
  constructor() {
    this.metrics = {};
  }
  
  // 页面加载时间
  measurePageLoad(pageName) {
    const startTime = Date.now();
    
    return () => {
      const loadTime = Date.now() - startTime;
      this.recordMetric('pageLoad', pageName, loadTime);
    };
  }
  
  // API 请求时间
  measureApiCall(apiName) {
    const startTime = Date.now();
    
    return () => {
      const responseTime = Date.now() - startTime;
      this.recordMetric('apiCall', apiName, responseTime);
    };
  }
  
  recordMetric(type, name, value) {
    if (!this.metrics[type]) {
      this.metrics[type] = {};
    }
    
    if (!this.metrics[type][name]) {
      this.metrics[type][name] = [];
    }
    
    this.metrics[type][name].push({
      value,
      timestamp: Date.now()
    });
    
    // 定期上报数据
    this.reportMetrics();
  }
  
  reportMetrics() {
    // 上报性能数据到监控系统
    // 可以集成第三方性能监控服务
  }
  
  getMetrics() {
    return this.metrics;
  }
}

export default new PerformanceMonitor();
```

通过实施这些最佳实践，可以显著提升微信小程序的代码质量、可维护性、性能和用户体验。建议根据项目实际情况，逐步采用这些实践方案。