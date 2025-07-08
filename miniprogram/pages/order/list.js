// pages/order/list.js
const app = getApp();

Page({
  data: {
    currentTab: 0,
    tabs: [
      { name: '全部', status: '' },
      { name: '待付款', status: 0 },
      { name: '待发货', status: 1 },
      { name: '待收货', status: 2 },
      { name: '已完成', status: 3 }
    ],
    orders: [],
    loading: false,
    hasMore: true,
    page: 1,
    pageSize: 10,
    emptyOrderImage: ''
  },

  onLoad(options) {
    // 设置空订单图片的完整URL
    this.setData({
      emptyOrderImage: app.getImageUrl('emptyOrder')
    });
    
    // 检查登录状态
    if (!app.checkLogin()) {
      return;
    }

    // 如果有传入状态，切换到对应tab
    if (options.status) {
      const tabIndex = this.data.tabs.findIndex(tab => tab.status === options.status);
      if (tabIndex !== -1) {
        this.setData({ currentTab: tabIndex });
      }
    }

    this.loadOrders();
  },

  onShow() {
    // 每次显示页面时刷新订单列表
    if (app.globalData.token) {
      this.refreshOrders();
    }
  },

  // 切换tab
  switchTab(e) {
    const index = e.currentTarget.dataset.index;
    if (index !== this.data.currentTab) {
      this.setData({ currentTab: index });
      this.refreshOrders();
    }
  },

  // 刷新订单列表
  refreshOrders() {
    this.setData({
      orders: [],
      page: 1,
      hasMore: true
    });
    this.loadOrders();
  },

  // 加载订单列表
  async loadOrders() {
    if (this.data.loading || !this.data.hasMore) return;

    try {
      this.setData({ loading: true });

      const currentTab = this.data.tabs[this.data.currentTab];
      const res = await app.request({
        url: '/order/list',
        data: {
          status: currentTab.status,
          page: this.data.page,
          pageSize: this.data.pageSize
        }
      });

      const newOrders = res.data.list || [];
      this.setData({
        orders: this.data.page === 1 ? newOrders : [...this.data.orders, ...newOrders],
        hasMore: res.data.hasMore || false,
        page: this.data.page + 1,
        loading: false
      });
    } catch (error) {
      console.error('加载订单失败:', error);
      this.setData({ loading: false });
      app.showToast('加载失败，请重试');
    }
  },

  // 跳转到订单详情
  goToOrderDetail(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order/detail?id=${orderId}`
    });
  },

  // 取消订单
  cancelOrder(e) {
    const orderId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '确认取消',
      content: '确定要取消这个订单吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await app.request({
              url: `/order/${orderId}/cancel`,
              method: 'PUT',
              data: {
                reason: '用户取消'
              }
            });
            
            app.showToast('订单已取消', 'success');
            this.refreshOrders();
          } catch (error) {
            console.error('取消订单失败:', error);
            app.showToast('取消失败，请重试');
          }
        }
      }
    });
  },

  // 确认收货
  confirmReceive(e) {
    const orderId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '确认收货',
      content: '确认已收到商品吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await app.request({
              url: `/order/${orderId}/confirm`,
              method: 'PUT'
            });
            
            app.showToast('确认收货成功', 'success');
            this.refreshOrders();
          } catch (error) {
            console.error('确认收货失败:', error);
            app.showToast('操作失败，请重试');
          }
        }
      }
    });
  },

  // 申请退款
  applyRefund(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order/refund?orderId=${orderId}`
    });
  },

  // 再次购买
  buyAgain(e) {
    const order = e.currentTarget.dataset.order;
    
    wx.showModal({
      title: '再次购买',
      content: '将商品重新加入购物车？',
      success: async (res) => {
        if (res.confirm) {
          try {
            // 将订单中的商品添加到购物车
            for (const item of order.items) {
              await app.request({
                url: '/cart/add',
                method: 'POST',
                data: {
                  productId: item.productId,
                  quantity: item.quantity
                }
              });
            }
            
            app.showToast('已添加到购物车', 'success');
            app.getCartCount();
            
            // 跳转到购物车
            wx.switchTab({
              url: '/pages/cart/cart'
            });
          } catch (error) {
            console.error('添加购物车失败:', error);
            app.showToast('操作失败，请重试');
          }
        }
      }
    });
  },

  // 去支付
  goToPay(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order/pay?orderId=${orderId}`
    });
  },

  // 查看物流
  viewLogistics(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order/logistics?orderId=${orderId}`
    });
  },

  // 联系客服
  contactService() {
    wx.makePhoneCall({
      phoneNumber: '400-123-4567'
    });
  },

  // 去逛逛 - 跳转到首页
  goToHome() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.refreshOrders();
    wx.stopPullDownRefresh();
  },

  // 上拉加载更多
  onReachBottom() {
    this.loadOrders();
  },

  // 格式化订单状态
  formatOrderStatus(status) {
    const statusMap = {
      0: '待付款',
      1: '待发货', 
      2: '待收货',
      3: '已完成',
      4: '已取消',
      5: '已退款'
    };
    return statusMap[status] || '未知状态';
  },

  // 获取订单状态颜色
  getStatusColor(status) {
    const colorMap = {
      0: '#ff4757',
      1: '#ffa502',
      2: '#3742fa', 
      3: '#2ed573',
      4: '#747d8c',
      5: '#5f27cd'
    };
    return colorMap[status] || '#333';
  }
});