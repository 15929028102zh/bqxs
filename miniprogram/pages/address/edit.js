// pages/address/edit.js
const app = getApp();

Page({
  data: {
    addressId: null,
    isEdit: false,
    formData: {
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detail: '',
      isDefault: false
    },
    regions: ['请选择', '请选择', '请选择']
  },

  onLoad(options) {
    if (options.id) {
      this.setData({
        addressId: options.id,
        isEdit: true
      });
      wx.setNavigationBarTitle({
        title: '编辑地址'
      });
      this.loadAddressDetail(options.id);
    } else {
      wx.setNavigationBarTitle({
        title: '添加地址'
      });
    }
  },

  // 加载地址详情
  async loadAddressDetail(addressId) {
    try {
      wx.showLoading({
        title: '加载中...'
      });
      
      const res = await app.request({
        url: `/address/${addressId}`
      });
      
      const address = res.data;
      this.setData({
        formData: {
          receiverName: address.receiverName || '',
          receiverPhone: address.receiverPhone || '',
          province: address.province || '',
          city: address.city || '',
          district: address.district || '',
          detail: address.detail || '',
          isDefault: address.isDefault === 1
        },
        regions: [address.province || '请选择', address.city || '请选择', address.district || '请选择']
      });
    } catch (error) {
      console.error('加载地址详情失败:', error);
      app.showToast('加载失败', 'error');
    } finally {
      wx.hideLoading();
    }
  },

  // 输入框变化
  onInputChange(e) {
    const { field } = e.currentTarget.dataset;
    const { value } = e.detail;
    
    this.setData({
      [`formData.${field}`]: value
    });
  },

  // 地区选择
  onRegionChange(e) {
    const regions = e.detail.value;
    this.setData({
      regions: regions,
      'formData.province': regions[0],
      'formData.city': regions[1],
      'formData.district': regions[2]
    });
  },

  // 默认地址开关
  onDefaultChange(e) {
    this.setData({
      'formData.isDefault': e.detail.value
    });
  },

  // 表单验证
  validateForm() {
    const { formData } = this.data;
    
    if (!formData.receiverName.trim()) {
      app.showToast('请输入收货人姓名', 'error');
      return false;
    }
    
    if (!formData.receiverPhone.trim()) {
      app.showToast('请输入手机号码', 'error');
      return false;
    }
    
    // 手机号格式验证
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(formData.receiverPhone)) {
      app.showToast('请输入正确的手机号码', 'error');
      return false;
    }
    
    if (!formData.province || formData.province === '请选择') {
      app.showToast('请选择所在地区', 'error');
      return false;
    }
    
    if (!formData.detail.trim()) {
      app.showToast('请输入详细地址', 'error');
      return false;
    }
    
    return true;
  },

  // 保存地址
  async saveAddress() {
    if (!this.validateForm()) {
      return;
    }
    
    try {
      wx.showLoading({
        title: '保存中...'
      });
      
      const { formData, addressId, isEdit } = this.data;
      
      const requestData = {
        receiverName: formData.receiverName.trim(),
        receiverPhone: formData.receiverPhone.trim(),
        province: formData.province,
        city: formData.city,
        district: formData.district,
        detailAddress: formData.detail.trim(),
        isDefault: formData.isDefault ? 1 : 0
      };
      
      if (isEdit) {
        // 更新地址
        await app.request({
          url: `/address/${addressId}`,
          method: 'PUT',
          data: requestData
        });
        app.showToast('更新成功', 'success');
      } else {
        // 添加地址
        await app.request({
          url: '/address/add',
          method: 'POST',
          data: requestData
        });
        app.showToast('添加成功', 'success');
      }
      
      // 返回上一页
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      
    } catch (error) {
      console.error('保存地址失败:', error);
      app.showToast('保存失败', 'error');
    } finally {
      wx.hideLoading();
    }
  },

  // 获取微信地址
  async getWechatAddress() {
    try {
      const res = await wx.chooseAddress();
      
      this.setData({
        'formData.receiverName': res.userName,
        'formData.receiverPhone': res.telNumber,
        'formData.province': res.provinceName,
        'formData.city': res.cityName,
        'formData.district': res.countyName,
        'formData.detail': res.detailInfo,
        regions: [res.provinceName, res.cityName, res.countyName]
      });
      
      app.showToast('导入成功', 'success');
    } catch (error) {
      console.error('获取微信地址失败:', error);
      if (error.errMsg !== 'chooseAddress:cancel') {
        app.showToast('获取地址失败', 'error');
      }
    }
  }
});