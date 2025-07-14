// pages/profile/profile.js
const app = getApp();

Page({
  data: {
    userInfo: null,
    orderStats: {
      pending: 0,
      shipped: 0,
      completed: 0,
      refund: 0
    },
    menuItems: [
      {
        icon: '/images/profile/address.svg',
        title: '收货地址',
        url: '/pages/address/list'
      },
      {
        icon: '/images/profile/service.svg',
        title: '客服中心',
        url: '/pages/ai-chat/ai-chat'
      },
      {
        icon: '/images/profile/about.svg',
        title: '关于我们',
        url: ''
      },
      {
        icon: '/images/profile/settings.svg',
        title: '设置',
        url: ''
      }
    ],
    defaultAvatarImage: ''
  },

  onLoad() {
    // 设置默认头像图片的完整URL
    this.setData({
      defaultAvatarImage: app.getImageUrl('defaultAvatar')
    });
    
    this.loadUserData();
  },

  onShow() {
    this.loadUserData();
  },

  onPullDownRefresh() {
    this.loadUserData().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  // 加载用户数据
  async loadUserData() {
    if (!app.globalData.token) {
      this.setData({
        userInfo: null,
        orderStats: {
          pending: 0,
          shipped: 0,
          completed: 0,
          refund: 0
        }
      });
      return;
    }

    try {
      // 获取用户信息
      const userRes = await app.request({
        url: '/user/info'
      });
      
      // 获取订单统计
      const statsRes = await app.request({
        url: '/order/stats'
      });
      
      this.setData({
        userInfo: userRes.data,
        orderStats: statsRes.data
      });
    } catch (error) {
      console.error('加载用户数据失败:', error);
    }
  },

  // 跳转到登录页面
  goToLogin() {
    wx.navigateTo({
      url: '/pages/login/login'
    });
  },

  // 跳转到订单列表
  goToOrderList(e) {
    const status = e.currentTarget.dataset.status;
    let url = '/pages/order/list';
    if (status !== undefined) {
      url += `?status=${status}`;
    }
    wx.navigateTo({
      url: url
    });
  },

  // 跳转到菜单页面
  goToMenu(e) {
    const url = e.currentTarget.dataset.url;
    if (url) {
      wx.navigateTo({
        url: url
      });
    } else {
      app.showToast('功能开发中');
    }
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.clearLoginStatus();
          this.setData({
            userInfo: null,
            orderStats: {
              pending: 0,
              shipped: 0,
              completed: 0,
              refund: 0
            }
          });
          app.showToast('已退出登录', 'success');
        }
      }
    });
  },

  // 联系客服
  contactService() {
    wx.navigateTo({
      url: '/pages/ai-chat/ai-chat'
    });
  },

  // 获取用户头像
  onChooseAvatar(e) {
    const { avatarUrl } = e.detail;
    // 这里可以上传头像到服务器
    console.log('选择头像:', avatarUrl);
    app.showToast('头像更新功能开发中');
  },

  // 更新用户昵称
  onNicknameChange(e) {
    const nickname = e.detail.value;
    if (nickname.trim()) {
      // 这里可以更新昵称到服务器
      console.log('更新昵称:', nickname);
      app.showToast('昵称更新功能开发中');
    }
  }
});