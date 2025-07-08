// pages/privacy-test/privacy-test.js
Page({
  data: {
    showPrivacyModal: false,
    privacyContractName: '《用户隐私保护指引》',
    privacyEventInfo: {},
    testResults: []
  },

  onLoad() {
    console.log('隐私测试页面加载');
    this.checkPrivacySettingOnLoad();
  },

  // 页面加载时检查隐私设置
  async checkPrivacySettingOnLoad() {
    try {
      const app = getApp();
      const privacySetting = await app.checkPrivacySetting();
      
      this.addTestResult('页面加载检查', privacySetting.needAuthorization ? '需要授权' : '已授权', 'info');
      
      if (privacySetting.needAuthorization) {
        this.setData({
          privacyContractName: privacySetting.privacyContractName || '《用户隐私保护指引》'
        });
      }
    } catch (error) {
      this.addTestResult('页面加载检查', '检查失败: ' + error.message, 'error');
    }
  },

  // 显示隐私弹窗（供app.js调用）
  showPrivacyModal(eventInfo = {}) {
    console.log('显示隐私弹窗:', eventInfo);
    this.setData({
      showPrivacyModal: true,
      privacyEventInfo: eventInfo
    });
  },

  // 隐私协议同意事件
  onPrivacyAgree(e) {
    console.log('用户同意隐私协议:', e.detail);
    this.setData({
      showPrivacyModal: false
    });
    this.addTestResult('隐私授权', '用户同意', 'success');
  },

  // 隐私协议拒绝事件
  onPrivacyDisagree(e) {
    console.log('用户拒绝隐私协议:', e.detail);
    this.setData({
      showPrivacyModal: false
    });
    this.addTestResult('隐私授权', '用户拒绝', 'warning');
    wx.showToast({
      title: '需要同意隐私协议才能使用相关功能',
      icon: 'none'
    });
  },

  // 测试主动检查隐私设置
  async testCheckPrivacySetting() {
    try {
      const app = getApp();
      const result = await app.checkPrivacySetting();
      this.addTestResult('主动检查隐私设置', JSON.stringify(result), 'info');
    } catch (error) {
      this.addTestResult('主动检查隐私设置', '失败: ' + error.message, 'error');
    }
  },

  // 测试getUserProfile
  testGetUserProfile() {
    this.addTestResult('测试getUserProfile', '开始调用...', 'info');
    
    wx.getUserProfile({
      desc: '测试获取用户信息',
      success: (res) => {
        this.addTestResult('getUserProfile', '成功获取用户信息', 'success');
        console.log('getUserProfile成功:', res);
      },
      fail: (error) => {
        this.addTestResult('getUserProfile', '失败: ' + error.errMsg, 'error');
        console.error('getUserProfile失败:', error);
      }
    });
  },

  // 测试getLocation
  testGetLocation() {
    this.addTestResult('测试getLocation', '开始调用...', 'info');
    
    wx.getLocation({
      type: 'wgs84',
      success: (res) => {
        this.addTestResult('getLocation', '成功获取位置信息', 'success');
        console.log('getLocation成功:', res);
      },
      fail: (error) => {
        this.addTestResult('getLocation', '失败: ' + error.errMsg, 'error');
        console.error('getLocation失败:', error);
      }
    });
  },

  // 测试chooseMedia
  testChooseMedia() {
    this.addTestResult('测试chooseMedia', '开始调用...', 'info');
    
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        this.addTestResult('chooseMedia', '成功选择媒体文件', 'success');
        console.log('chooseMedia成功:', res);
      },
      fail: (error) => {
        this.addTestResult('chooseMedia', '失败: ' + error.errMsg, 'error');
        console.error('chooseMedia失败:', error);
      }
    });
  },

  // 手动显示隐私弹窗
  showPrivacyModalManually() {
    this.showPrivacyModal({ referrer: 'manual_test' });
  },

  // 打开隐私协议
  openPrivacyContract() {
    const app = getApp();
    app.openPrivacyContract();
  },

  // 清除测试结果
  clearTestResults() {
    this.setData({
      testResults: []
    });
  },

  /**
   * 测试后端连接
   */
  testBackendConnection() {
    const app = getApp();
    const baseUrl = app.globalData.baseUrl;
    
    wx.showLoading({
      title: '测试连接中...'
    });
    
    // 先测试基础连接
    wx.request({
      url: `${baseUrl}/health`,
      method: 'GET',
      timeout: 10000,
      success: (res) => {
        console.log('基础连接测试成功:', res);
        
        if (res.statusCode === 200) {
          // 基础连接成功，继续测试详细信息
          this.testDetailedHealth(baseUrl);
        } else {
          wx.hideLoading();
          wx.showModal({
            title: '连接异常',
            content: `HTTP状态码: ${res.statusCode}\n请检查后端服务状态`,
            showCancel: false
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('后端连接测试失败:', err);
        
        let errorMessage = '连接失败';
        if (err.errMsg) {
          if (err.errMsg.includes('timeout')) {
            errorMessage = '连接超时，请检查网络或后端服务';
          } else if (err.errMsg.includes('fail')) {
            errorMessage = '无法连接到后端服务，请确认服务已启动';
          } else {
            errorMessage = `连接错误: ${err.errMsg}`;
          }
        }
        
        wx.showModal({
          title: '连接失败',
          content: `${errorMessage}\n\n服务地址: ${baseUrl}\n\n请检查：\n1. 后端服务是否已启动\n2. 网络连接是否正常\n3. 服务器地址是否正确`,
          showCancel: false
        });
      }
    });
  },

  /**
   * 测试详细健康信息
   */
  testDetailedHealth(baseUrl) {
    wx.request({
      url: `${baseUrl}/health/detail`,
      method: 'GET',
      timeout: 5000,
      success: (res) => {
        wx.hideLoading();
        console.log('详细健康检查成功:', res);
        
        if (res.statusCode === 200 && res.data.code === 200) {
          const healthData = res.data.data;
          const wechatConfig = healthData.wechat || {};
          
          let content = `✅ 后端服务运行正常\n`;
          content += `🌐 服务地址: ${baseUrl}\n`;
          content += `⚙️ 服务端口: ${healthData.port}\n\n`;
          
          // 微信配置状态
          if (wechatConfig.configured) {
            content += `✅ 微信配置: ${wechatConfig.status}\n`;
            content += `📱 AppID: ${wechatConfig.appId}\n`;
          } else {
            content += `⚠️ 微信配置: ${wechatConfig.status}\n`;
            content += `❗ 这可能是导致登录失败的原因\n`;
          }
          
          wx.showModal({
            title: wechatConfig.configured ? '连接成功' : '连接成功但有配置问题',
            content: content,
            showCancel: false
          });
        } else {
          // 详细检查失败，显示基础成功信息
          wx.showModal({
            title: '连接成功',
            content: `✅ 后端服务运行正常\n🌐 服务地址: ${baseUrl}`,
            showCancel: false
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.warn('详细健康检查失败，但基础连接成功:', err);
        
        // 详细检查失败，但基础连接成功
        wx.showModal({
          title: '连接成功',
          content: `✅ 后端服务运行正常\n🌐 服务地址: ${baseUrl}\n\n注意: 无法获取详细状态信息`,
          showCancel: false
        });
      }
    });
  },

  // 添加测试结果
  addTestResult(action, result, type = 'info') {
    const timestamp = new Date().toLocaleTimeString();
    const newResult = {
      id: Date.now(),
      timestamp,
      action,
      result,
      type
    };
    
    this.setData({
      testResults: [newResult, ...this.data.testResults]
    });
  }
});