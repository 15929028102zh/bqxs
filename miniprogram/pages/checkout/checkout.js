// pages/checkout/checkout.js
const app = getApp();

Page({
  data: {
    // 收货地址
    address: null,
    
    // 商品信息
    products: [],
    
    // 订单信息
    orderInfo: {
      productAmount: 0,
      deliveryFee: 0,
      discountAmount: 0,
      totalAmount: 0
    },
    
    // 支付方式
    paymentMethods: [
      { id: 'wechat', name: '微信支付', icon: '/images/wechat-pay.svg', selected: true },
      { id: 'cash', name: '现金支付', icon: '/images/cash-pay.svg', selected: false }
    ],
    
    // 配送信息
    deliveryInfo: {
      type: 'standard', // standard: 标准配送, express: 快速配送
      time: '尽快送达',
      fee: 0
    },
    
    // 优惠券
    coupons: [],
    selectedCoupon: null,
    
    // 备注
    remark: '',
    
    // 提交状态
    submitting: false,
    
    // 来源类型
    sourceType: 'cart', // cart: 购物车, direct: 立即购买
    sourceData: null
  },

  onLoad(options) {
    // 检查登录状态
    if (!app.checkLogin()) {
      return;
    }

    // 解析参数
    this.parseOptions(options);
    
    // 加载页面数据
    this.loadPageData();
  },

  // 解析页面参数
  parseOptions(options) {
    if (options.type === 'direct' && options.data) {
      // 立即购买
      this.setData({
        sourceType: 'direct',
        sourceData: JSON.parse(decodeURIComponent(options.data))
      });
    } else if (options.cartItems) {
      // 购物车结算 - 直接传递商品数据
      this.setData({
        sourceType: 'cart',
        sourceData: JSON.parse(decodeURIComponent(options.cartItems))
      });
    } else {
      // 购物车结算 - 传递购物车ID
      this.setData({
        sourceType: 'cart',
        sourceData: options.cartIds ? options.cartIds.split(',') : []
      });
    }
  },

  // 加载页面数据
  async loadPageData() {
    try {
      await Promise.all([
        this.loadAddress(),
        this.loadProducts(),
        this.loadCoupons()
      ]);
      
      this.calculateAmount();
    } catch (error) {
      console.error('加载页面数据失败:', error);
      app.showToast('加载失败，请重试');
    }
  },

  // 加载收货地址
  async loadAddress() {
    try {
      const res = await app.request({
        url: '/address/default'
      });
      
      this.setData({
        address: res.data
      });
    } catch (error) {
      console.error('加载地址失败:', error);
      
      // 如果接口请求失败，使用模拟地址数据
      const mockAddress = {
        id: 1,
        receiverName: '张三',
        receiverPhone: '13800138000',
        province: '北京市',
        city: '北京市',
        district: '朝阳区',
        detailAddress: '三里屯街道工体北路8号院',
        isDefault: 1
      };
      
      this.setData({
        address: mockAddress
      });
      
      app.showToast('使用默认地址，请稍后添加真实地址');
    }
  },

  // 加载商品信息
  async loadProducts() {
    try {
      let res;
      
      if (this.data.sourceType === 'direct') {
        // 立即购买
        const { productId, quantity, specification } = this.data.sourceData;
        res = await app.request({
          url: '/product/detail',
          data: { id: productId }
        });
        
        const product = res.data;
        const price = parseFloat(product.price || 0);
        const qty = parseInt(quantity || 1);
        this.setData({
          products: [{
            id: product.id,
            name: product.name,
            image: product.image,
            price: price.toFixed(2),
            quantity: qty,
            specification: specification || '默认规格',
            subtotal: (price * qty).toFixed(2)
          }]
        });
      } else if (Array.isArray(this.data.sourceData) && this.data.sourceData.length > 0 && this.data.sourceData[0].productId) {
        // 购物车结算 - 直接使用传递的商品数据
        const products = this.data.sourceData.map(item => {
          const price = parseFloat(item.price || 0);
          const quantity = parseInt(item.quantity || 1);
          return {
            id: item.productId,
            cartId: item.id,
            name: item.productName,
            image: item.images,
            price: price.toFixed(2),
            quantity: quantity,
            specification: item.specification || '默认规格',
            subtotal: (price * quantity).toFixed(2)
          };
        });
        
        this.setData({ products });
      } else {
        // 购物车结算 - 通过API获取
        res = await app.request({
          url: '/cart/checkout',
          data: {
            cartIds: this.data.sourceData
          }
        });
        
        this.setData({
          products: res.data.products || []
        });
      }
    } catch (error) {
      console.error('加载商品失败:', error);
      throw error;
    }
  },

  // 加载优惠券
  async loadCoupons() {
    try {
      const res = await app.request({
        url: '/coupon/available'
      });
      
      this.setData({
        coupons: res.data || []
      });
    } catch (error) {
      console.error('加载优惠券失败:', error);
    }
  },

  // 计算金额
  calculateAmount() {
    const products = this.data.products;
    const selectedCoupon = this.data.selectedCoupon;
    const deliveryFee = this.data.deliveryInfo.fee;
    
    // 商品总金额
    const productAmount = products.reduce((sum, item) => sum + parseFloat(item.subtotal || 0), 0);
    
    // 优惠金额
    let discountAmount = 0;
    if (selectedCoupon) {
      if (selectedCoupon.type === 'amount') {
        discountAmount = parseFloat(selectedCoupon.value || 0);
      } else if (selectedCoupon.type === 'percent') {
        discountAmount = productAmount * parseFloat(selectedCoupon.value || 0) / 100;
      }
    }
    
    // 总金额
    const totalAmount = Math.max(0, productAmount + deliveryFee - discountAmount);
    
    this.setData({
      'orderInfo.productAmount': productAmount.toFixed(2),
      'orderInfo.deliveryFee': deliveryFee.toFixed(2),
      'orderInfo.discountAmount': discountAmount.toFixed(2),
      'orderInfo.totalAmount': totalAmount.toFixed(2)
    });
  },

  // 选择收货地址
  selectAddress() {
    wx.navigateTo({
      url: '/pages/address/list?select=true'
    });
  },

  // 选择支付方式
  selectPayment(e) {
    const paymentId = e.currentTarget.dataset.id;
    const paymentMethods = this.data.paymentMethods.map(item => ({
      ...item,
      selected: item.id === paymentId
    }));
    
    this.setData({ paymentMethods });
  },

  // 选择配送方式
  selectDelivery(e) {
    const type = e.currentTarget.dataset.type;
    const deliveryInfo = {
      type: type,
      time: type === 'express' ? '2小时内送达' : '尽快送达',
      fee: type === 'express' ? 10 : 0
    };
    
    this.setData({ deliveryInfo });
    this.calculateAmount();
  },

  // 选择优惠券
  selectCoupon() {
    wx.navigateTo({
      url: '/pages/coupon/list?select=true'
    });
  },

  // 输入备注
  onRemarkInput(e) {
    this.setData({
      remark: e.detail.value
    });
  },

  // 提交订单
  async submitOrder() {
    // 验证必要信息
    if (!this.data.address) {
      app.showToast('请选择收货地址');
      return;
    }
    
    if (this.data.products.length === 0) {
      app.showToast('购物车为空');
      return;
    }
    
    if (this.data.submitting) {
      return;
    }

    try {
      this.setData({ submitting: true });
      
      // 获取选中的支付方式
      const selectedPayment = this.data.paymentMethods.find(item => item.selected);
      
      // 转换支付方式ID为中文名称
      let paymentMethodName;
      if (selectedPayment.id === 'wechat') {
        paymentMethodName = '微信支付';
      } else if (selectedPayment.id === 'cash') {
        paymentMethodName = '现金支付';
      } else {
        paymentMethodName = '货到付款';
      }
      
      // 构建订单数据
      const orderData = {
        addressId: this.data.address.id,
        products: this.data.products.map(item => ({
          id: item.id,
          productId: item.id,
          name: item.name,
          images: item.image,
          price: item.price,
          quantity: item.quantity,
          specification: item.specification
        })),
        deliveryType: this.data.deliveryInfo.type === 'express' ? '快速配送' : '标准配送',
        paymentMethod: paymentMethodName,
        couponId: this.data.selectedCoupon ? this.data.selectedCoupon.id : null,
        remark: this.data.remark,
        sourceType: this.data.sourceType,
        sourceData: this.data.sourceData
      };
      
      // 提交订单
      const res = await app.request({
        url: '/order/create',
        method: 'POST',
        data: orderData
      });
      
      this.setData({ submitting: false });
      
      // 跳转到支付页面
      wx.redirectTo({
        url: `/pages/order/pay?orderId=${res.data.orderId}`
      });
      
    } catch (error) {
      console.error('提交订单失败:', error);
      this.setData({ submitting: false });
      app.showToast('提交失败，请重试');
    }
  },

  // 页面显示时的处理
  onShow() {
    // 检查是否从地址页面返回
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    
    if (currentPage.data.needRefreshAddress) {
      this.loadAddress();
      currentPage.setData({ needRefreshAddress: false });
    }
    
    // 检查是否从优惠券页面返回
    if (currentPage.data.selectedCouponFromList) {
      this.setData({
        selectedCoupon: currentPage.data.selectedCouponFromList
      });
      this.calculateAmount();
      currentPage.setData({ selectedCouponFromList: null });
    }
  }
});