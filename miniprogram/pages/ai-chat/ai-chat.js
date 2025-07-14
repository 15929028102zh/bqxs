// pages/ai-chat/ai-chat.js
const app = getApp();
const apiConfig = require('../../config/api.js');
const linkGenerator = require('../../utils/linkGenerator');

Page({
  data: {
    messages: [],
    inputText: '',
    isTyping: false,
    scrollTop: 0,
    scrollIntoView: '',
    userAvatar: '',
    aiAvatar: '/images/ai-avatar.svg',
    quickReplies: [
      '商品咨询',
      '订单查询',
      '配送问题',
      '退换货',
      '优惠活动'
    ],
    messageId: 0,
    isConnected: true,
    lastSendTime: 0,
    sendCooldown: 1000 // 发送冷却时间1秒
  },

  onLoad() {
    // 设置用户头像
    const userInfo = app.globalData.userInfo;
    this.setData({
      userAvatar: userInfo?.avatar || app.getImageUrl('defaultAvatar')
    });

    // 发送欢迎消息
    this.addMessage('ai', '您好！我是智能客服小助手，有什么可以帮助您的吗？');
    
    // 监听网络状态变化
    this.setupNetworkListener();
  },

  onShow() {
    // 页面显示时滚动到底部
    this.scrollToBottom();
  },

  onUnload() {
    // 页面卸载时移除网络监听
    if (this.networkListener) {
      wx.offNetworkStatusChange(this.networkListener);
    }
  },

  // 设置网络状态监听
  setupNetworkListener() {
    this.networkListener = (res) => {
      console.log('网络状态变化:', res);
      this.setData({
        isConnected: res.isConnected
      });
      
      if (!res.isConnected) {
        wx.showToast({
          title: '网络连接已断开',
          icon: 'none',
          duration: 2000
        });
      } else {
        wx.showToast({
          title: '网络连接已恢复',
          icon: 'success',
          duration: 1500
        });
      }
    };
    
    wx.onNetworkStatusChange(this.networkListener);
  },

  // 输入框内容变化
  onInputChange(e) {
    this.setData({
      inputText: e.detail.value
    });
  },

  // 发送消息
  async sendMessage() {
    const content = this.data.inputText.trim();
    const currentTime = Date.now();
    
    // 检查发送条件
    if (!content || this.data.isTyping) {
      return;
    }
    
    // 防抖检查
    if (currentTime - this.data.lastSendTime < this.data.sendCooldown) {
      wx.showToast({
        title: '发送太频繁，请稍后再试',
        icon: 'none',
        duration: 1500
      });
      return;
    }
    
    // 检查网络状态
    const networkStatus = await this.checkNetworkStatus();
    if (!networkStatus.connected) {
      wx.showToast({
        title: '网络连接异常，请检查网络设置',
        icon: 'none',
        duration: 2000
      });
      return;
    }

    // 添加用户消息
    this.addMessage('user', content);
    
    // 更新状态
    this.setData({
      inputText: '',
      isTyping: true,
      quickReplies: [], // 隐藏快捷回复
      lastSendTime: currentTime,
      isConnected: networkStatus.connected
    });

    try {
      // 调用硅基流动API
      const aiResponse = await this.callSiliconFlowAPI(content);
      
      // 添加AI回复，传递用户消息用于生成链接
      this.addMessage('ai', aiResponse, content);
    } catch (error) {
      console.error('AI回复失败:', error);
      
      // 根据错误类型显示不同的提示
      let errorMessage = '抱歉，我暂时无法回复您的问题，请稍后再试。';
      if (error.message.includes('网络')) {
        errorMessage = '网络连接异常，请检查网络后重试。';
      } else if (error.message.includes('超时')) {
        errorMessage = '请求超时，请稍后重试。';
      } else if (error.message.includes('频繁')) {
        errorMessage = '请求过于频繁，请稍后再试。';
      }
      
      this.addMessage('ai', errorMessage);
      
      // 显示错误提示
      wx.showToast({
        title: error.message || '发送失败',
        icon: 'none',
        duration: 2000
      });
    } finally {
      this.setData({
        isTyping: false
      });
    }
  },

  // 发送快捷回复
  sendQuickReply(e) {
    const text = e.currentTarget.dataset.text;
    this.setData({
      inputText: text
    });
    this.sendMessage();
  },

  // 添加消息到列表
  addMessage(type, content, userMessage = '') {
    const messageId = this.data.messageId + 1;
    const message = {
      id: messageId,
      type: type,
      content: content,
      time: this.formatTime(new Date())
    };

    // 如果是AI消息，生成相关功能链接
    if (type === 'ai' && userMessage) {
      const enhancedResponse = linkGenerator.enhanceResponse(content, userMessage);
      if (enhancedResponse.hasLinks) {
        message.links = enhancedResponse.links;
        message.linkText = enhancedResponse.linkText;
      }
    }

    this.setData({
      messages: [...this.data.messages, message],
      messageId: messageId
    });

    // 滚动到底部
    this.scrollToBottom();
  },

  // 检查网络状态
  checkNetworkStatus() {
    return new Promise((resolve) => {
      wx.getNetworkType({
        success: (res) => {
          const networkType = res.networkType;
          if (networkType === 'none') {
            resolve({ connected: false, type: 'none' });
          } else {
            resolve({ connected: true, type: networkType });
          }
        },
        fail: () => {
          resolve({ connected: false, type: 'unknown' });
        }
      });
    });
  },

  // 调用硅基流动API
  async callSiliconFlowAPI(message) {
    // 检查网络状态
    const networkStatus = await this.checkNetworkStatus();
    if (!networkStatus.connected) {
      throw new Error(apiConfig.errorMessages.networkError);
    }

    const { siliconFlow } = apiConfig;
    const apiUrl = `${siliconFlow.baseUrl}/chat/completions`;

    // 构建对话历史
    const messages = [
      {
        role: 'system',
        content: '你是边墙鲜送小程序的智能客服助手。边墙鲜送是一个专注于新鲜蔬果配送的小区跑腿服务平台。请用友好、专业的语气回答用户问题，主要帮助用户解决商品咨询、订单查询、配送问题、退换货等相关问题。回答要简洁明了，不超过200字。'
      },
      ...this.data.messages.slice(-5).map(msg => ({
        role: msg.type === 'user' ? 'user' : 'assistant',
        content: msg.content
      })),
      {
        role: 'user',
        content: message
      }
    ];

    // 重试机制
    let retryCount = 0;

    const makeRequest = () => {
      return new Promise((resolve, reject) => {
        wx.request({
          url: apiUrl,
          method: 'POST',
          timeout: siliconFlow.timeout,
          header: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${siliconFlow.apiKey}`
          },
          data: {
            model: siliconFlow.model,
            messages: messages,
            temperature: 0.7,
            max_tokens: 300,
            stream: false
          },
          success: (res) => {
            console.log('API响应:', res);
            if (res.statusCode === 200 && res.data && res.data.choices && res.data.choices.length > 0) {
              const aiResponse = res.data.choices[0].message.content;
              resolve(aiResponse);
            } else if (res.statusCode === 429) {
              reject(new Error('请求过于频繁，请稍后再试'));
            } else if (res.statusCode === 401) {
              reject(new Error('API密钥无效，请联系管理员'));
            } else if (res.statusCode >= 500) {
              reject(new Error('服务器错误，请稍后重试'));
            } else {
              console.error('API响应异常:', res);
              reject(new Error(`API响应格式错误: ${res.statusCode}`));
            }
          },
          fail: (error) => {
            console.error('API请求失败:', error);
            // 详细的错误处理
            if (error.errMsg) {
              if (error.errMsg.includes('timeout')) {
                reject(new Error(apiConfig.errorMessages.timeout));
              } else if (error.errMsg.includes('fail')) {
                reject(new Error(apiConfig.errorMessages.networkError));
              } else {
                reject(new Error(apiConfig.errorMessages.unknownError));
              }
            } else {
              reject(new Error(apiConfig.errorMessages.unknownError));
            }
          }
        });
      });
    };

    // 实现重试逻辑
    while (retryCount < siliconFlow.maxRetries) {
      try {
        const result = await makeRequest();
        return result;
      } catch (error) {
        retryCount++;
        console.log(`API调用失败，第${retryCount}次重试:`, error.message);
        
        if (retryCount >= siliconFlow.maxRetries) {
          throw error;
        }
        
        // 等待一段时间后重试（指数退避）
        const delay = siliconFlow.retryDelay * Math.pow(2, retryCount - 1);
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }
  },

  // 滚动到底部
  scrollToBottom() {
    if (this.data.messages.length > 0) {
      const lastMessageId = `msg-${this.data.messages[this.data.messages.length - 1].id}`;
      this.setData({
        scrollIntoView: lastMessageId
      });
    }
  },

  // 格式化时间
  formatTime(date) {
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  },

  // 页面跳转功能
  navigateToPage(e) {
    const { path, type } = e.currentTarget.dataset;
    
    if (!path) {
      wx.showToast({
        title: '页面路径错误',
        icon: 'none',
        duration: 1500
      });
      return;
    }
    
    // 显示跳转提示
    wx.showToast({
      title: '正在跳转...',
      icon: 'loading',
      duration: 1000
    });
    
    // 根据跳转类型执行不同的跳转方式
    switch (type) {
      case 'navigate':
        wx.navigateTo({
          url: path,
          fail: (error) => {
            console.error('页面跳转失败:', error);
            // 如果navigateTo失败，尝试使用switchTab
            wx.switchTab({
              url: path,
              fail: (tabError) => {
                console.error('Tab跳转也失败:', tabError);
                wx.showToast({
                  title: '页面跳转失败',
                  icon: 'none',
                  duration: 2000
                });
              }
            });
          }
        });
        break;
      case 'switchTab':
        wx.switchTab({
          url: path,
          fail: (error) => {
            console.error('Tab跳转失败:', error);
            wx.showToast({
              title: '页面跳转失败',
              icon: 'none',
              duration: 2000
            });
          }
        });
        break;
      case 'redirectTo':
        wx.redirectTo({
          url: path,
          fail: (error) => {
            console.error('页面重定向失败:', error);
            wx.showToast({
              title: '页面跳转失败',
              icon: 'none',
              duration: 2000
            });
          }
        });
        break;
      default:
        // 默认使用navigateTo
        wx.navigateTo({
          url: path,
          fail: (error) => {
            console.error('默认跳转失败:', error);
            wx.switchTab({
              url: path,
              fail: (tabError) => {
                wx.showToast({
                  title: '页面跳转失败',
                  icon: 'none',
                  duration: 2000
                });
              }
            });
          }
        });
    }
  },

  // 页面分享
  onShareAppMessage() {
    return {
      title: '边墙鲜送 - 智能客服',
      path: '/pages/ai-chat/ai-chat'
    };
  }
});