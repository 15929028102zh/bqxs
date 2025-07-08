// pages/address/list.js
const app = getApp();

Page({
  data: {
    addressList: [],
    isSelectMode: false, // 是否为选择地址模式
    emptyAddressImage: ''
  },

  onLoad(options) {
    // 设置空地址图片的完整URL
    this.setData({
      emptyAddressImage: app.getImageUrl('emptyAddress')
    });
    
    // 检查是否为选择地址模式
    if (options.select === 'true') {
      this.setData({
        isSelectMode: true
      });
      wx.setNavigationBarTitle({
        title: '选择收货地址'
      });
    } else {
      wx.setNavigationBarTitle({
        title: '收货地址管理'
      });
    }
    
    this.loadAddressList();
  },

  onShow() {
    this.loadAddressList();
  },

  // 加载地址列表
  async loadAddressList() {
    try {
      wx.showLoading({
        title: '加载中...'
      });
      
      const res = await app.request({
        url: '/address/list'
      });
      
      this.setData({
        addressList: res.data || []
      });
    } catch (error) {
      console.error('加载地址列表失败:', error);
      
      // 使用模拟数据
      const mockAddressList = [
        {
          id: 1,
          receiverName: '张三',
          receiverPhone: '13800138000',
          province: '北京市',
          city: '北京市',
          district: '朝阳区',
          detail: '三里屯街道工体北路8号院',
          isDefault: 1
        },
        {
          id: 2,
          receiverName: '李四',
          receiverPhone: '13900139000',
          province: '上海市',
          city: '上海市',
          district: '浦东新区',
          detail: '陆家嘴金融中心',
          isDefault: 0
        }
      ];
      
      this.setData({
        addressList: mockAddressList
      });
    } finally {
      wx.hideLoading();
    }
  },

  // 选择地址
  selectAddress(e) {
    if (!this.data.isSelectMode) return;
    
    const index = e.currentTarget.dataset.index;
    const address = this.data.addressList[index];
    
    // 返回到结算页面并传递选中的地址
    const pages = getCurrentPages();
    const prevPage = pages[pages.length - 2];
    
    if (prevPage) {
      prevPage.setData({
        address: address
      });
    }
    
    wx.navigateBack();
  },

  // 添加新地址
  addAddress() {
    wx.navigateTo({
      url: '/pages/address/edit'
    });
  },

  // 编辑地址
  editAddress(e) {
    const index = e.currentTarget.dataset.index;
    const address = this.data.addressList[index];
    
    wx.navigateTo({
      url: `/pages/address/edit?id=${address.id}`
    });
  },

  // 删除地址
  deleteAddress(e) {
    const index = e.currentTarget.dataset.index;
    const address = this.data.addressList[index];
    
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个地址吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await app.request({
              url: `/address/${address.id}`,
              method: 'DELETE'
            });
            
            app.showToast('删除成功', 'success');
            this.loadAddressList();
          } catch (error) {
            console.error('删除地址失败:', error);
            app.showToast('删除失败', 'error');
          }
        }
      }
    });
  },

  // 设置默认地址
  async setDefaultAddress(e) {
    const index = e.currentTarget.dataset.index;
    const address = this.data.addressList[index];
    
    if (address.isDefault) {
      return; // 已经是默认地址
    }
    
    try {
      await app.request({
        url: `/address/${address.id}/default`,
        method: 'PUT'
      });
      
      app.showToast('设置成功', 'success');
      this.loadAddressList();
    } catch (error) {
      console.error('设置默认地址失败:', error);
      app.showToast('设置失败', 'error');
    }
  }
});