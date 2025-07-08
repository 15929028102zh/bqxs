// pages/login/login.js
const app = getApp();

Page({
  data: {
    canIUseGetUserProfile: false,
    loading: false,
    showPrivacyNotice: false,
    // 隐私弹窗相关
    showPrivacyModal: false,
    privacyContractName: '《用户隐私保护指引》',
    privacyEventInfo: {},
    // 图片资源
    logoImage: ''
  },

  onLoad() {
    console.log('=== 登录页面加载 ===');
    
    // 设置图片资源
    this.setData({
      logoImage: app.getImageUrl('logo')
    });
    
    // 检查是否支持getUserProfile接口（基础库2.10.4及以上）
    const canUseProfile = wx.canIUse('getUserProfile');
    console.log('wx.canIUse(getUserProfile):', canUseProfile);
    
    if (canUseProfile) {
      this.setData({
        canIUseGetUserProfile: true
      });
      console.log('设备支持getUserProfile接口');
    } else {
      console.log('设备不支持getUserProfile接口，将使用降级方案');
    }
    
    // 检查微信版本信息
    const systemInfo = wx.getSystemInfoSync();
    console.log('系统信息:', {
      platform: systemInfo.platform,
      version: systemInfo.version,
      SDKVersion: systemInfo.SDKVersion
    });
    
    // 主动检查隐私设置
    this.checkPrivacySettingOnLoad();
  },

  // 页面加载时检查隐私设置
  async checkPrivacySettingOnLoad() {
    try {
      const app = getApp();
      const privacySetting = await app.checkPrivacySetting();
      
      if (privacySetting.needAuthorization) {
        console.log('需要隐私授权，准备显示弹窗');
        this.setData({
          privacyContractName: privacySetting.privacyContractName || '《用户隐私保护指引》'
        });
        // 可以选择在页面加载时就显示隐私弹窗，或者等用户操作时再显示
        // this.showPrivacyModal();
      } else {
        console.log('用户已同意隐私协议');
      }
    } catch (error) {
      console.log('检查隐私设置失败或不支持:', error);
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
    
    // 检查是否是从登录流程触发的隐私授权
    const eventInfo = e.detail.eventInfo || {};
    if (eventInfo.referrer === 'wxLogin') {
      console.log('隐私授权完成，继续微信登录流程');
      // 继续执行登录流程
      this.continueWxLoginAfterPrivacy();
    }
  },

  // 隐私授权完成后继续微信登录
  continueWxLoginAfterPrivacy() {
    console.log('=== 继续微信登录流程（隐私授权后）===');
    
    wx.showLoading({
      title: '正在登录...',
      mask: true
    });
    
    try {
      console.log('开始获取用户信息');
      // 必须在用户点击事件的同步执行上下文中调用getUserProfile
      this.getUserProfile().then(async (userInfo) => {
        console.log('获取用户信息成功:', userInfo);
        
        try {
          console.log('准备获取微信登录code');
          const code = await this.getWxLoginCode();
          console.log('获取微信登录code成功:', code);
          
          console.log('准备调用后端登录接口');
          await this.handleLoginSuccess(code, userInfo);
          
          wx.hideLoading();
          console.log('=== 登录流程完成 ===');
        } catch (error) {
          wx.hideLoading();
          console.error('登录过程中发生错误:', error);
          this.handleLoginError(error);
        }
      }).catch((error) => {
        wx.hideLoading();
        console.error('获取用户信息失败:', error);
        this.handleLoginError(error);
      });
    } catch (error) {
      wx.hideLoading();
      console.error('continueWxLoginAfterPrivacy 执行出错:', error);
      this.handleLoginError({
        type: 'system_error',
        message: '登录流程恢复失败: ' + error.message
      });
    }
  },

  // 隐私协议拒绝事件
  onPrivacyDisagree(e) {
    console.log('用户拒绝隐私协议:', e.detail);
    this.setData({
      showPrivacyModal: false
    });
    wx.showToast({
      title: '需要同意隐私协议才能使用相关功能',
      icon: 'none'
    });
  },

  // 微信登录
  async wxLogin() {
    console.log('=== 开始微信登录流程 ===');
    
    // 显示加载提示
    wx.showLoading({
      title: '正在登录...',
      mask: true
    });
    
    try {
      // 先检查隐私设置
      console.log('检查隐私设置...');
      const app = getApp();
      const privacySetting = await app.checkPrivacySetting();
      
      if (privacySetting.needAuthorization) {
        console.log('需要隐私授权，显示隐私弹窗');
        wx.hideLoading();
        
        // 更新隐私协议名称
        this.setData({
          privacyContractName: privacySetting.privacyContractName || '《用户隐私保护指引》'
        });
        
        // 显示隐私弹窗
        this.showPrivacyModal({ referrer: 'wxLogin' });
        
        // 等待用户授权完成后再继续
        return;
      }
      
      console.log('隐私设置检查通过，开始获取用户信息');
      // 必须在用户点击事件的同步执行上下文中调用getUserProfile
      this.getUserProfile().then(async (userInfo) => {
        console.log('获取用户信息成功:', userInfo);
        
        try {
          console.log('准备获取微信登录code');
          const code = await this.getWxLoginCode();
          console.log('获取微信登录code成功:', code);
          
          console.log('准备调用后端登录接口');
          await this.handleLoginSuccess(code, userInfo);
          
          wx.hideLoading();
          console.log('=== 登录流程完成 ===');
        } catch (error) {
          wx.hideLoading();
          console.error('登录过程中发生错误:', error);
          this.handleLoginError(error);
        }
      }).catch((error) => {
        wx.hideLoading();
        console.error('获取用户信息失败:', error);
        this.handleLoginError(error);
      });
    } catch (error) {
      wx.hideLoading();
      console.error('wxLogin 执行出错:', error);
      this.handleLoginError({
        type: 'system_error',
        message: '登录初始化失败: ' + error.message
      });
    }
  },

  // 测试getUserProfile功能
  testGetUserProfile() {
    console.log('=== 测试getUserProfile功能 ===');
    
    if (!this.data.canIUseGetUserProfile) {
      wx.showModal({
        title: '不支持',
        content: '当前微信版本不支持getUserProfile接口',
        showCancel: false
      });
      return;
    }
    
    console.log('开始测试wx.getUserProfile调用...');
    wx.getUserProfile({
      desc: '测试获取用户信息',
      success: (res) => {
        console.log('测试成功，获取到用户信息:', res);
        wx.showModal({
          title: '✅ 授权测试成功',
          content: `获取到用户信息：\n昵称：${res.userInfo.nickName}\n头像：${res.userInfo.avatarUrl ? '已获取' : '未获取'}\n\n现在可以正常使用微信登录功能了！`,
          confirmText: '开始登录',
          cancelText: '知道了',
          success: (modalRes) => {
            if (modalRes.confirm) {
              this.wxLogin();
            }
          }
        });
      },
      fail: (error) => {
        console.error('测试失败:', error);
        let errorMsg = '未知错误';
        let solution = '';
        
        if (error.errMsg === 'getUserProfile:fail cancel') {
          errorMsg = '用户取消了授权';
          solution = '请重新点击按钮并选择"允许"完成授权';
        } else if (error.errno === 112 || (error.errMsg && error.errMsg.includes('privacy agreement'))) {
          errorMsg = '隐私协议配置问题';
          solution = '已更新配置，请稍后重试。如仍有问题，请联系开发者在小程序管理后台配置隐私保护指引。';
        } else if (error.errMsg && error.errMsg.includes('scope')) {
           errorMsg = 'API权限声明问题';
           solution = 'getUserProfile不需要在requiredPrivateInfos中声明，请确保小程序管理后台已正确配置隐私保护指引。';
        } else {
          errorMsg = error.errMsg || '调用失败';
          solution = '请检查微信版本是否支持，或尝试重启小程序。';
        }
        
        wx.showModal({
          title: '❌ 授权测试失败',
          content: `错误信息：${errorMsg}\n\n解决方案：${solution}`,
          confirmText: '重试',
          cancelText: '取消',
          success: (modalRes) => {
            if (modalRes.confirm) {
              this.testGetUserProfile();
            }
          }
        });
      }
    });
  },

  // 处理隐私授权错误
  handlePrivacyError(error) {
    if ((error.errno === 112) || (error.errMsg && error.errMsg.includes('privacy agreement'))) {
      // 显示页面提示
      this.setData({
        showPrivacyNotice: true
      });
      
      wx.showModal({
        title: '隐私协议配置提示',
        content: '小程序隐私保护指引未正确配置。请联系开发者在小程序管理后台配置《小程序用户隐私保护指引》，并在其中声明"用户信息"的处理。\n\n您也可以尝试使用"手机号注册/登录"功能。',
        confirmText: '了解',
        cancelText: '手机号登录',
        success: (res) => {
          if (!res.confirm) {
            // 用户选择手机号登录
            this.phoneLogin();
          }
        }
      });
    } else {
      app.showToast('需要同意隐私协议才能使用登录功能');
    }
  },



  // 处理登录错误
   handleLoginError(error) {
     console.log('=== 开始处理登录错误 ===', error);
     
     // 检查是否是隐私协议错误
     if (error.type === 'privacy_error') {
       console.log('检测到隐私协议错误');
       wx.showModal({
         title: '隐私协议配置问题',
         content: '小程序隐私保护指引未正确配置，无法获取用户信息。\n\n解决方案：\n1. 联系开发者配置隐私保护指引\n2. 使用手机号注册/登录',
         confirmText: '手机号登录',
         cancelText: '稍后再试',
         success: (res) => {
           if (res.confirm) {
             this.phoneLogin();
           }
         }
       });
     } else if (error.type === 'user_cancel') {
       console.log('用户取消了登录');
       app.showToast('登录已取消');
     } else if (error.type === 'network_error') {
       console.log('网络错误');
       wx.showModal({
         title: '网络连接失败',
         content: error.message || '网络连接失败，请检查网络后重试',
         confirmText: '重试',
         cancelText: '取消',
         success: (res) => {
           if (res.confirm) {
             this.wxLogin();
           }
         }
       });
     } else if (error.type === 'system_error') {
       console.log('系统错误');
       wx.showModal({
         title: '系统错误',
         content: error.message || '系统出现错误，请稍后重试',
         confirmText: '重试',
         cancelText: '取消',
         success: (res) => {
           if (res.confirm) {
             this.wxLogin();
           }
         }
       });
     } else {
       console.log('其他类型错误:', error);
       const errorMsg = error.message || error.errMsg || '登录失败，请重试';
       wx.showModal({
         title: '登录失败',
         content: errorMsg,
         confirmText: '重试',
         cancelText: '取消',
         success: (res) => {
           if (res.confirm) {
             this.wxLogin();
           }
         }
       });
     }
   },

  // 处理登录成功逻辑
   async handleLoginSuccess(code, userInfo) {
     console.log('=== 开始handleLoginSuccess ===');
     console.log('微信登录code:', code);
     console.log('用户信息:', userInfo);
    
    try {
      this.setData({ loading: true });
      
      // 发送登录请求到后端
      console.log('发送登录请求到后端');
      const requestData = {
        code: code,
        userInfo: userInfo
      };
      console.log('请求数据:', requestData);
      
      const res = await app.request({
        url: '/user/login',
        method: 'POST',
        data: requestData,
        needToken: false
      });
      console.log('后端登录请求成功:', res);
      
      // 验证响应数据
      if (!res.data || !res.data.token) {
        throw new Error('服务器返回数据格式错误');
      }
      
      // 保存登录信息
      console.log('开始保存登录信息');
      app.globalData.token = res.data.token;
      app.globalData.userInfo = res.data.userInfo;
      wx.setStorageSync('token', res.data.token);
      console.log('登录信息保存成功，token:', res.data.token.substring(0, 10) + '...');
      
      this.setData({ loading: false });
      app.showToast('登录成功', 'success');
      
      // 获取购物车数量
      console.log('获取购物车数量');
      app.getCartCount();
      
      // 返回上一页或跳转到首页
      console.log('准备页面跳转');
      const pages = getCurrentPages();
      console.log('当前页面栈长度:', pages.length);
      
      if (pages.length > 1) {
        console.log('返回上一页');
        wx.navigateBack();
      } else {
        console.log('跳转到首页');
        wx.switchTab({
          url: '/pages/index/index'
        });
      }
      
      console.log('=== handleLoginSuccess 完成 ===');
    } catch (error) {
      this.setData({ loading: false });
      console.error('handleLoginSuccess 失败:', error);
      
      // 根据错误类型提供不同的提示
      let errorMsg = '登录失败，请重试';
      if (error.message && error.message.includes('网络')) {
        errorMsg = '网络连接失败，请检查网络后重试';
      } else if (error.message && error.message.includes('服务器')) {
        errorMsg = '服务器响应异常，请稍后重试';
      } else if (error.errMsg && error.errMsg.includes('request:fail')) {
        // 显示详细的连接失败信息
        wx.showModal({
          title: '连接失败',
          content: '无法连接到后端服务器。\n\n可能原因：\n1. 后端服务未启动\n2. 网络连接问题\n3. 服务器地址配置错误\n\n请检查后端服务状态或联系技术支持。',
          showCancel: false,
          confirmText: '我知道了'
        });
        return; // 不显示toast，已经显示了modal
      }
      
      app.showToast(errorMsg);
      throw error; // 重新抛出错误供上层处理
    }
  },

  // 原来的登录逻辑（备用）
  wxLoginOld() {
    // 必须在用户点击事件的同步部分调用getUserProfile
    this.getUserProfile().then(async (userInfo) => {
      try {
        this.setData({ loading: true });
        
        // 获取微信登录code
        const loginRes = await this.getWxLoginCode();
        
        // 发送登录请求到后端
        const res = await app.request({
          url: '/user/login',
          method: 'POST',
          data: {
            code: loginRes.code,
            userInfo: userInfo
          },
          needToken: false
        });
        
        // 保存登录信息
        app.globalData.token = res.data.token;
        app.globalData.userInfo = res.data.userInfo;
        wx.setStorageSync('token', res.data.token);
        
        this.setData({ loading: false });
        app.showToast('登录成功', 'success');
        
        // 获取购物车数量
        app.getCartCount();
        
        // 返回上一页或跳转到首页
        const pages = getCurrentPages();
        if (pages.length > 1) {
          wx.navigateBack();
        } else {
          wx.switchTab({
            url: '/pages/index/index'
          });
        }
      } catch (error) {
        this.setData({ loading: false });
        console.error('登录失败:', error);
        app.showToast('登录失败，请重试');
      }
    }).catch((error) => {
      console.error('获取用户信息失败:', error);
      app.showToast('需要授权用户信息才能登录');
    });
  },

  // 获取微信登录code
  getWxLoginCode() {
    console.log('=== 开始getWxLoginCode ===');
    return new Promise((resolve, reject) => {
      // 设置超时处理
      const loginTimeout = setTimeout(() => {
        console.error('wx.login超时');
        reject({
          type: 'network_error',
          message: '获取登录凭证超时，请检查网络连接'
        });
      }, 10000);
      
      wx.login({
        success: (res) => {
          clearTimeout(loginTimeout);
          console.log('wx.login成功:', res);
          
          if (res.code) {
            console.log('获取到微信登录code:', res.code);
            resolve(res.code);
          } else {
            console.error('wx.login成功但没有返回code');
            reject({
              type: 'system_error',
              message: '获取登录凭证失败，请重试'
            });
          }
        },
        fail: (error) => {
          clearTimeout(loginTimeout);
          console.error('wx.login失败:', error);
          reject({
            type: 'system_error',
            message: '微信登录失败: ' + (error.errMsg || '未知错误'),
            originalError: error
          });
        }
      });
    });
  },

  // 获取用户信息
  getUserProfile() {
    console.log('=== 开始getUserProfile ===');
    return new Promise((resolve, reject) => {
      console.log('canIUseGetUserProfile:', this.data.canIUseGetUserProfile);
      console.log('当前微信版本支持getUserProfile:', wx.canIUse('getUserProfile'));
      
      if (this.data.canIUseGetUserProfile) {
        try {
          console.log('准备调用wx.getUserProfile，这应该会弹出授权页面');
          
          // 设置超时处理，防止用户长时间不操作
          let profileTimeout = setTimeout(() => {
            console.log('wx.getUserProfile超时，用户可能没有看到授权页面或取消了授权');
            wx.showModal({
              title: '授权提示',
              content: '授权页面可能没有弹出或您取消了授权。\n\n请确保：\n1. 微信版本支持授权功能\n2. 点击"允许"按钮完成授权',
              confirmText: '重新授权',
              cancelText: '使用默认',
              success: (modalRes) => {
                if (modalRes.confirm) {
                  // 用户选择重新授权，递归调用
                  this.getUserProfile().then(resolve).catch(reject);
                } else {
                  // 用户选择使用默认信息
                  console.log('用户选择使用默认信息');
                  resolve({
                    nickName: '微信用户',
                    avatarUrl: '/images/default-avatar.png'
                  });
                }
              }
            });
          }, 15000); // 15秒超时，给用户更多时间
          
          console.log('正在调用wx.getUserProfile API...');
          wx.getUserProfile({
            desc: '用于完善用户资料',
            success: (res) => {
              console.log('wx.getUserProfile成功:', res);
              clearTimeout(profileTimeout);
              resolve(res.userInfo);
            },
            fail: (error) => {
               console.error('wx.getUserProfile失败:', error);
               clearTimeout(profileTimeout);
               
               // 检查是否是用户取消授权
               if (error.errMsg === 'getUserProfile:fail cancel') {
                 console.log('用户取消了授权');
                 wx.showModal({
                   title: '授权被取消',
                   content: '您取消了授权，可以选择重新授权或使用默认信息登录。',
                   confirmText: '重新授权',
                   cancelText: '默认登录',
                   success: (modalRes) => {
                     if (modalRes.confirm) {
                       // 重新调用授权
                       this.getUserProfile().then(resolve).catch(reject);
                     } else {
                       // 使用默认信息
                       resolve({
                         nickName: '微信用户',
                         avatarUrl: '/images/default-avatar.png'
                       });
                     }
                   }
                 });
                 return;
               }
               
               // 检查是否是隐私协议相关错误（错误码112或包含privacy agreement）
               if ((error.errno === 112) || (error.errMsg && error.errMsg.includes('privacy agreement'))) {
                 console.log('检测到隐私协议错误，错误码:', error.errno, '错误信息:', error.errMsg);
                 reject({
                   type: 'privacy_error',
                   message: '隐私保护指引未正确配置',
                   originalError: error
                 });
                 return;
               }
               
               console.log('尝试降级到getUserInfo');
               // 如果getUserProfile失败，尝试使用getUserInfo
               this.fallbackGetUserInfo(resolve, reject);
             }
          });
        } catch (e) {
          console.error('wx.getUserProfile调用异常:', e);
          this.fallbackGetUserInfo(resolve, reject);
        }
      } else {
        console.log('不支持getUserProfile，使用降级方案');
        // 兼容旧版本或不支持getUserProfile的情况
        this.fallbackGetUserInfo(resolve, reject);
      }
    });
  },

  // 降级获取用户信息
  fallbackGetUserInfo(resolve, reject) {
    console.log('=== 开始fallbackGetUserInfo ===');
    wx.getUserInfo({
      success: (res) => {
        console.log('wx.getUserInfo成功:', res);
        resolve(res.userInfo);
      },
      fail: (error) => {
        console.error('wx.getUserInfo也失败了:', error);
        console.log('使用默认用户信息');
        // 如果都失败了，提供一个默认的用户信息
        resolve({
          nickName: '微信用户',
          avatarUrl: '/images/default-avatar.png'
        });
      }
    });
  },

  // 手机号注册/登录
  phoneLogin() {
    wx.showModal({
      title: '手机号注册/登录',
      content: '请输入您的手机号码进行注册或登录',
      editable: true,
      placeholderText: '请输入手机号',
      success: (res) => {
        if (res.confirm && res.content) {
          const phone = res.content.trim();
          if (this.validatePhone(phone)) {
            this.sendSmsCode(phone);
          } else {
            app.showToast('请输入正确的手机号码');
          }
        }
      }
    });
  },

  // 验证手机号格式
  validatePhone(phone) {
    const phoneRegex = /^1[3-9]\d{9}$/;
    return phoneRegex.test(phone);
  },

  // 发送短信验证码
  async sendSmsCode(phone) {
    try {
      this.setData({ loading: true });
      
      // 发送验证码请求
      await app.request({
        url: '/api/user/send-code',
        method: 'POST',
        data: { phone },
        needToken: false
      });
      
      this.setData({ loading: false });
      app.showToast('验证码已发送', 'success');
      
      // 显示验证码输入框
      this.showSmsCodeInput(phone);
    } catch (error) {
      this.setData({ loading: false });
      console.error('发送验证码失败:', error);
      app.showToast('发送验证码失败，请重试');
    }
  },

  // 显示验证码输入框
  showSmsCodeInput(phone) {
    wx.showModal({
      title: '输入验证码',
      content: `验证码已发送至 ${phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')}`,
      editable: true,
      placeholderText: '请输入6位验证码',
      success: (res) => {
        if (res.confirm && res.content) {
          const code = res.content.trim();
          if (code.length === 6 && /^\d{6}$/.test(code)) {
            this.loginWithSmsCode(phone, code);
          } else {
            app.showToast('请输入6位数字验证码');
          }
        }
      }
    });
  },

  // 使用短信验证码登录
  async loginWithSmsCode(phone, smsCode) {
    try {
      this.setData({ loading: true });
      
      // 获取微信登录code
      const loginRes = await this.getWxLoginCode();
      
      // 发送登录请求
      const res = await app.request({
        url: '/api/user/phone-login',
        method: 'POST',
        data: {
          phone,
          smsCode,
          code: loginRes.code
        },
        needToken: false
      });
      
      // 保存登录信息
      app.globalData.token = res.data.token;
      app.globalData.userInfo = res.data.userInfo;
      wx.setStorageSync('token', res.data.token);
      
      this.setData({ loading: false });
      app.showToast('登录成功', 'success');
      
      // 获取购物车数量
      app.getCartCount();
      
      // 返回上一页或跳转到首页
      const pages = getCurrentPages();
      if (pages.length > 1) {
        wx.navigateBack();
      } else {
        wx.switchTab({
          url: '/pages/index/index'
        });
      }
    } catch (error) {
      this.setData({ loading: false });
      console.error('手机号登录失败:', error);
      if (error.message && error.message.includes('验证码')) {
        app.showToast('验证码错误，请重新输入');
        this.showSmsCodeInput(phone);
      } else {
        app.showToast('登录失败，请重试');
      }
    }
  },

  // 获取手机号
  async getPhoneNumber(e) {
    if (e.detail.errMsg === 'getPhoneNumber:ok') {
      try {
        this.setData({ loading: true });
        
        // TODO: 发送手机号到后端解密
        // 暂时注释掉，因为后端还没有实现decrypt-phone接口
        // const res = await app.request({
        //   url: '/api/user/bind-phone',
        //   method: 'POST',
        //   data: {
        //     encryptedData: e.detail.encryptedData,
        //     iv: e.detail.iv
        //   }
        // });
        
        // 模拟成功响应
        const res = { data: { phone: '138****0000' } };
        
        this.setData({ loading: false });
        app.showToast('手机号绑定成功', 'success');
        
        // 更新用户信息
        app.globalData.userInfo.phone = res.data.phone;
      } catch (error) {
        this.setData({ loading: false });
        console.error('获取手机号失败:', error);
        app.showToast('获取手机号失败，请重试');
      }
    } else if (e.detail.errMsg && ((e.detail.errno === 112) || e.detail.errMsg.includes('privacy agreement'))) {
      // 处理隐私协议错误
      wx.showModal({
        title: '隐私协议配置问题',
        content: '小程序隐私保护指引未正确配置，无法获取手机号。\n\n解决方案：\n1. 联系开发者配置隐私保护指引\n2. 使用手机号输入登录',
        confirmText: '手机号登录',
        cancelText: '稍后再试',
        success: (res) => {
          if (res.confirm) {
            this.phoneLogin();
          }
        }
      });
    } else {
      console.log('用户取消授权或其他错误:', e.detail.errMsg);
    }
  },

  // 跳转到首页
  goToHome() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },


});