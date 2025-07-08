// pages/order/logistics.js
const app = getApp();

Page({
  data: {
    orderId: '',
    loading: true,
    logistics: null,
    trackingInfo: []
  },

  onLoad(options) {
    if (options.orderId) {
      this.setData({
        orderId: options.orderId
      });
      this.loadLogistics();
    } else {
      wx.showToast({
        title: '订单ID不能为空',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }
  },

  async loadLogistics() {
    try {
      this.setData({ loading: true });
      
      const res = await app.request({
        url: `/order/${this.data.orderId}/logistics`
      });
      
      if (res.data) {
        this.setData({
          logistics: res.data,
          trackingInfo: res.data.trackingInfo || []
        });
      } else {
        wx.showToast({
          title: '暂无物流信息',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('加载物流信息失败:', error);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  // 复制快递单号
  copyTrackingNo() {
    if (this.data.logistics && this.data.logistics.trackingNo) {
      wx.setClipboardData({
        data: this.data.logistics.trackingNo,
        success: () => {
          wx.showToast({
            title: '已复制到剪贴板',
            icon: 'success'
          });
        }
      });
    }
  },

  // 联系客服
  contactService() {
    wx.makePhoneCall({
      phoneNumber: '400-123-4567'
    });
  },

  // 刷新物流信息
  onPullDownRefresh() {
    this.loadLogistics();
    wx.stopPullDownRefresh();
  }
});