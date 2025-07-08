// pages/order/pay.js
const app = getApp();

Page({
  data: {
    orderId: null,
    order: null,
    loading: true,
    paying: false
  },

  onLoad(options) {
    const orderId = options.orderId;
    if (orderId) {
      this.setData({ orderId });
      this.loadOrderDetail();
    } else {
      wx.showToast({
        title: '订单信息错误',
        icon: 'error'
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }
  },

  // 加载订单详情
  async loadOrderDetail() {
    try {
      this.setData({ loading: true });
      
      const res = await app.request({
        url: `/order/${this.data.orderId}`
      });
      
      console.log('订单详情数据:', res.data);
      console.log('支付方式字段:', {
        paymentMethod: res.data.paymentMethod,
        payType: res.data.payType,
        paymentMethodType: typeof res.data.paymentMethod,
        payTypeType: typeof res.data.payType
      });
      
      // 确保支付方式字段正确设置
      if (res.data && (res.data.paymentMethod === undefined || res.data.paymentMethod === null)) {
        // 如果 paymentMethod 为空，使用 payType 作为备用
        res.data.paymentMethod = res.data.payType;
        console.log('使用 payType 作为支付方式:', res.data.paymentMethod);
      }
      
      this.setData({
        order: res.data,
        loading: false
      });
    } catch (error) {
      console.error('加载订单详情失败:', error);
      this.setData({ loading: false });
      wx.showToast({
        title: '加载失败',
        icon: 'error'
      });
    }
  },

  // 发起支付
  async startPay() {
    if (this.data.paying) {
      return;
    }

    try {
      this.setData({ paying: true });
      
      const order = this.data.order;
      
      // 根据支付方式处理
      let paymentMethod = order.paymentMethod || order.payType;
      
      // 如果仍然为空，尝试从其他字段获取或设置默认值
      if (paymentMethod === undefined || paymentMethod === null) {
        console.warn('支付方式字段为空，使用默认微信支付');
        paymentMethod = 1; // 默认微信支付
      }
      
      // 确保为数字类型
      paymentMethod = parseInt(paymentMethod) || paymentMethod;
      console.log('最终支付方式:', paymentMethod, '类型:', typeof paymentMethod);
      console.log('原始订单数据:', {
        paymentMethod: order.paymentMethod,
        payType: order.payType
      });
      
      if (paymentMethod === 1 || paymentMethod === '1') {
        // 微信支付
        await this.handleWechatPay();
      } else if (paymentMethod === 2 || paymentMethod === '2') {
        // 现金支付
        await this.handleCashPay();
      } else if (paymentMethod === 3 || paymentMethod === '3') {
        // 货到付款
        await this.handleCashOnDelivery();
      } else {
        console.error('未知的支付方式:', {
          original: order.paymentMethod,
          payType: order.payType,
          processed: paymentMethod
        });
        wx.showModal({
          title: '支付方式错误',
          content: `支付方式异常，请重新进入订单页面或联系客服处理`,
          showCancel: true,
          cancelText: '返回',
          confirmText: '重新加载',
          success: (res) => {
            if (res.confirm) {
              // 重新加载订单详情
              this.loadOrderDetail();
            } else {
              wx.navigateBack();
            }
          }
        });
      }
    } catch (error) {
      console.error('支付失败:', error);
      wx.showToast({
        title: '支付失败',
        icon: 'error'
      });
    } finally {
      this.setData({ paying: false });
    }
  },

  // 处理微信支付
  async handleWechatPay() {
    const res = await app.request({
      url: `/order/${this.data.orderId}/pay`,
      method: 'POST',
      data: {
        payType: 1 // 微信支付
      }
    });
    
    // 检查是否返回了微信支付参数
    if (res.data && res.data.payInfo) {
      // 调用微信支付
      await this.callWechatPay(res.data.payInfo);
    } else {
      // 如果后端没有返回支付参数，显示提示
      wx.showModal({
        title: '支付提示',
        content: '支付功能正在开发中，订单已创建成功！',
        showCancel: false,
        success: () => {
          wx.redirectTo({
            url: '/pages/order/list'
          });
        }
      });
    }
  },

  // 处理现金支付
  async handleCashPay() {
    const res = await app.request({
      url: `/order/${this.data.orderId}/pay`,
      method: 'POST',
      data: {
        payType: 2 // 现金支付
      }
    });
    
    if (res.data && res.data.status === 'success') {
      wx.showModal({
        title: '现金支付',
        content: res.data.message || '现金支付记账成功！请准备现金，配送员会在送达时收取。',
        showCancel: false,
        success: () => {
          wx.redirectTo({
            url: '/pages/order/list'
          });
        }
      });
    } else {
      throw new Error('现金支付记账失败');
    }
  },

  // 处理货到付款
  async handleCashOnDelivery() {
    const res = await app.request({
      url: `/order/${this.data.orderId}/pay`,
      method: 'POST',
      data: {
        payType: 3 // 货到付款
      }
    });
    
    if (res.data && res.data.status === 'success') {
      wx.showModal({
        title: '货到付款',
        content: res.data.message || '货到付款订单已确认，配送员将在送达时收取现金。',
        showCancel: false,
        success: () => {
          wx.redirectTo({
            url: '/pages/order/list'
          });
        }
      });
    } else {
      throw new Error('货到付款记账失败');
    }
  },

  // 调用微信支付
  async callWechatPay(payInfo) {
    return new Promise((resolve, reject) => {
      wx.requestPayment({
        timeStamp: payInfo.timeStamp,
        nonceStr: payInfo.nonceStr,
        package: payInfo.package,
        signType: payInfo.signType || 'MD5',
        paySign: payInfo.paySign,
        success: (res) => {
          console.log('支付成功:', res);
          wx.showToast({
            title: '支付成功',
            icon: 'success'
          });
          
          // 跳转到订单列表
          setTimeout(() => {
            wx.redirectTo({
              url: '/pages/order/list'
            });
          }, 1500);
          
          resolve(res);
        },
        fail: (err) => {
          console.error('支付失败:', err);
          if (err.errMsg === 'requestPayment:fail cancel') {
            wx.showToast({
              title: '支付已取消',
              icon: 'none'
            });
          } else {
            wx.showToast({
              title: '支付失败',
              icon: 'error'
            });
          }
          reject(err);
        }
      });
    });
  },

  // 取消支付，返回订单列表
  cancelPay() {
    wx.redirectTo({
      url: '/pages/order/list'
    });
  }
});