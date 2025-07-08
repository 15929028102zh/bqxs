// app.js
const imageConfig = require('./utils/imageConfig');

App({
  globalData: {
    userInfo: null,
    token: null,
    cartCount: 0,
    baseUrl: 'http://192.168.229.1:8081/api',
    // 隐私授权相关
    privacyResolve: null
  },

  onLaunch() {
    // 确保 baseUrl 有效
    if (!this.globalData.baseUrl) {
      console.error('App onLaunch: baseUrl is not set, using default');
      this.globalData.baseUrl = 'http://192.168.229.1:8081/api';
    }
    
    // 初始化图片配置
    imageConfig.init(this.globalData.baseUrl);
    
    // 初始化隐私授权处理
    this.initPrivacyAuthorization();
    // 检查登录状态
    this.checkLoginStatus();
    // 获取购物车数量
    this.getCartCount();
  },

  // 初始化隐私授权处理
  initPrivacyAuthorization() {
    // 监听隐私接口需要用户授权事件
    wx.onNeedPrivacyAuthorization((resolve, eventInfo) => {
      console.log('触发隐私授权的接口：', eventInfo.referrer);
      
      // 存储resolve函数供后续调用
      this.globalData.privacyResolve = resolve;
      
      // 触发全局隐私弹窗事件
      this.triggerPrivacyModal(eventInfo);
    });
  },

  // 主动检查隐私设置
  checkPrivacySetting() {
    return new Promise((resolve, reject) => {
      if (wx.getPrivacySetting) {
        wx.getPrivacySetting({
          success: (res) => {
            console.log('隐私设置检查结果：', res);
            resolve(res);
          },
          fail: (err) => {
            console.error('检查隐私设置失败：', err);
            reject(err);
          }
        });
      } else {
        // 不支持隐私设置接口，认为不需要授权
        resolve({ needAuthorization: false });
      }
    });
  },

  // 触发隐私弹窗
  triggerPrivacyModal(eventInfo = {}) {
    // 通过事件机制通知页面显示隐私弹窗
    const pages = getCurrentPages();
    if (pages.length > 0) {
      const currentPage = pages[pages.length - 1];
      if (currentPage.showPrivacyModal) {
        currentPage.showPrivacyModal(eventInfo);
      } else {
        // 如果当前页面没有隐私弹窗处理，使用默认弹窗
        this.showDefaultPrivacyModal();
      }
    }
  },

  // 默认隐私弹窗（兜底方案）
  showDefaultPrivacyModal() {
    wx.showModal({
      title: '隐私授权提示',
      content: '为了更好地为您提供服务，我们需要获取您的相关信息。请您确认同意《用户隐私保护指引》后继续使用。',
      confirmText: '同意并继续',
      cancelText: '拒绝',
      success: (res) => {
        if (this.globalData.privacyResolve) {
          if (res.confirm) {
            this.globalData.privacyResolve({ event: 'agree' });
          } else {
            this.globalData.privacyResolve({ event: 'disagree' });
          }
          this.globalData.privacyResolve = null;
        }
      }
    });
  },

  // 处理隐私协议同意
  handlePrivacyAgree(buttonId = 'agree-btn') {
    if (this.globalData.privacyResolve) {
      this.globalData.privacyResolve({ 
        event: 'agree', 
        buttonId: buttonId 
      });
      this.globalData.privacyResolve = null;
    }
  },

  // 处理隐私协议拒绝
  handlePrivacyDisagree() {
    if (this.globalData.privacyResolve) {
      this.globalData.privacyResolve({ event: 'disagree' });
      this.globalData.privacyResolve = null;
    }
  },

  // 打开隐私协议页面
  openPrivacyContract() {
    if (wx.openPrivacyContract) {
      wx.openPrivacyContract({
        success: () => {
          console.log('隐私协议页面打开成功');
        },
        fail: (err) => {
          console.error('隐私协议页面打开失败：', err);
          this.showToast('无法打开隐私协议页面');
        }
      });
    } else {
      this.showToast('当前微信版本不支持查看隐私协议');
    }
  },
  onShow() {
    // 小程序切换到前台时执行
  },
  onHide() {
    // 小程序切换到后台时执行
  },

  // 获取图片URL
  getImageUrl(key) {
    return imageConfig.get(key);
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      // 获取用户信息
      this.getUserInfo();
    }
  },
  
  // 检查是否已登录，未登录则跳转到登录页
  checkLogin() {
    const token = wx.getStorageSync('token');
    if (!token || !this.globalData.token) {
      wx.navigateTo({
        url: '/pages/login/login'
      });
      return false;
    }
    return true;
  },
  // 获取用户信息
  getUserInfo() {
    wx.request({
      url: `${this.globalData.baseUrl}/user/info`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${this.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.globalData.userInfo = res.data.data;
        } else {
          // token失效，清除登录状态
          this.clearLoginStatus();
        }
      },
      fail: () => {
        // 请求失败，清除登录状态
        this.clearLoginStatus();
      }
    });
  },
  // 清除登录状态
  clearLoginStatus() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
  },
  // 获取购物车数量
  getCartCount() {
    if (this.globalData.token) {
      wx.request({
        url: `${this.globalData.baseUrl}/cart/count`,
        method: 'GET',
        header: {
          'Authorization': `Bearer ${this.globalData.token}`
        },
        success: (res) => {
          if (res.data.code === 200) {
            this.globalData.cartCount = res.data.data || 0;
          }
        }
      });
    }
  },
  // 显示消息提示框
  showToast(title, icon = 'none') {
    wx.showToast({
      title: title,
      icon: icon,
      duration: 2000
    });
  },
  // 显示加载提示框
  showLoading(title = '加载中') {
    wx.showLoading({
      title: title,
      mask: true
    });
  },
  // 隐藏加载提示框
  hideLoading() {
    wx.hideLoading();
  },
  // 网络请求封装
  request(options) {
    const { url, method = 'GET', data, header = {}, needToken = true } = options;
    
    // 添加token认证
    if (needToken && this.globalData.token) {
      header['Authorization'] = `Bearer ${this.globalData.token}`;
    }
    
    return new Promise((resolve, reject) => {
      wx.request({
        url: url.startsWith('http') ? url : `${this.globalData.baseUrl}${url}`,
        method: method,
        data: data,
        header: {
          'Content-Type': 'application/json',
          ...header
        },
        success: (res) => {
          if (res.statusCode === 200) {
            if (res.data.code === 200) {
              resolve(res.data);
            } else if (res.data.code === 401 || res.data.code === 4001 || res.data.code === 4002) {
              // token失效，清除登录状态并跳转到登录页
              this.clearLoginStatus();
              wx.navigateTo({
                url: '/pages/login/login'
              });
              reject(res.data);
            } else {
              this.showToast(res.data.message);
              reject(res.data);
            }
          } else {
            this.showToast('网络请求失败');
            reject(new Error('网络请求失败'));
          }
        },
        fail: (err) => {
          console.error('网络请求失败:', err);
          // 根据错误类型提供更详细的提示
          if (err.errMsg && err.errMsg.includes('request:fail')) {
            this.showToast('无法连接到服务器，请检查后端服务是否启动');
          } else {
            this.showToast('网络连接失败');
          }
          reject(err);
        }
      });
    });
  }
});