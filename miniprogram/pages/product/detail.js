// pages/product/detail.js
const app = getApp();
const imageUtil = require('../../utils/imageUtil');

Page({
  data: {
    productId: null,
    product: null,
    currentImageIndex: 0,
    quantity: 1,
    loading: true,
    showQuantityModal: false
  },

  onLoad(options) {
    const productId = options.id;
    if (productId) {
      this.setData({ productId });
      this.loadProductDetail(productId);
    } else {
      app.showToast('商品ID不能为空');
      wx.navigateBack();
    }
  },

  onShow() {
    // 更新购物车数量
    app.getCartCount();
  },

  onShareAppMessage() {
    const product = this.data.product;
    return {
      title: product ? product.name : '边墙鲜送',
      path: `/pages/product/detail?id=${this.data.productId}`,
      imageUrl: product && product.images ? product.images[0] : ''
    };
  },

  // 加载商品详情
  async loadProductDetail(productId) {
    try {
      this.setData({ loading: true });
      
      // 并行加载商品详情和图片
      const [productRes, productImages] = await Promise.all([
        app.request({
          url: `/product/${productId}`,
          needToken: false
        }),
        imageUtil.getProductImages(productId)
      ]);
      
      // 处理商品数据
      const product = {
        ...productRes.data,
        images: productImages && productImages.length > 0 ? productImages : [imageUtil.getDefaultImage('product')]
      };
      
      this.setData({
        product: product,
        loading: false
      });
      
      // 设置页面标题
      wx.setNavigationBarTitle({
        title: product.name
      });
      
      // 预加载商品图片
      if (product.images.length > 0) {
        imageUtil.preloadImages(product.images).catch(error => {
          console.warn('预加载商品图片失败:', error);
        });
      }
    } catch (error) {
      console.error('加载商品详情失败:', error);
      this.setData({ loading: false });
      app.showToast('加载失败');
    }
  },

  // 轮播图切换
  onSwiperChange(e) {
    this.setData({
      currentImageIndex: e.detail.current
    });
  },

  // 预览图片
  previewImage(e) {
    const current = e.currentTarget.dataset.src;
    const urls = this.data.product.images;
    wx.previewImage({
      current: current,
      urls: urls
    });
  },

  // 数量减少
  decreaseQuantity() {
    const quantity = this.data.quantity;
    if (quantity > 1) {
      this.setData({
        quantity: quantity - 1
      });
    }
  },

  // 数量增加
  increaseQuantity() {
    const quantity = this.data.quantity;
    const stock = this.data.product.stock;
    if (quantity < stock) {
      this.setData({
        quantity: quantity + 1
      });
    } else {
      app.showToast('库存不足');
    }
  },

  // 显示数量选择弹窗
  showQuantityModal() {
    this.setData({
      showQuantityModal: true
    });
  },

  // 隐藏数量选择弹窗
  hideQuantityModal() {
    this.setData({
      showQuantityModal: false
    });
  },

  // 添加到购物车
  async addToCart() {
    // 检查登录状态
    if (!app.globalData.token) {
      wx.navigateTo({
        url: '/pages/login/login'
      });
      return;
    }

    // 检查库存
    if (this.data.product.stock <= 0) {
      app.showToast('商品已售罄');
      return;
    }

    try {
      app.showLoading('添加中...');
      await app.request({
        url: '/cart/add',
        method: 'POST',
        data: {
          productId: this.data.productId,
          quantity: this.data.quantity
        }
      });
      app.hideLoading();
      app.showToast('添加成功', 'success');
      
      // 更新购物车数量
      app.getCartCount();
      
      // 隐藏弹窗
      this.hideQuantityModal();
    } catch (error) {
      app.hideLoading();
      console.error('添加购物车失败:', error);
    }
  },

  // 立即购买
  buyNow() {
    // 检查登录状态
    if (!app.globalData.token) {
      wx.navigateTo({
        url: '/pages/login/login'
      });
      return;
    }

    // 检查库存
    if (this.data.product.stock <= 0) {
      app.showToast('商品已售罄');
      return;
    }

    // 构造订单商品数据
    const orderItem = {
      productId: this.data.productId,
      productName: this.data.product.name,
      productImage: this.data.product.images[0],
      productPrice: this.data.product.price,
      quantity: this.data.quantity,
      totalPrice: (this.data.product.price * this.data.quantity).toFixed(2)
    };

    // 跳转到确认订单页面
    wx.navigateTo({
      url: `/pages/order/confirm?buyNow=true&orderItem=${encodeURIComponent(JSON.stringify(orderItem))}`
    });
    
    // 隐藏弹窗
    this.hideQuantityModal();
  },

  // 跳转到购物车
  goToCart() {
    wx.switchTab({
      url: '/pages/cart/cart'
    });
  },

  // 联系客服
  contactService() {
    wx.navigateTo({
      url: '/pages/ai-chat/ai-chat'
    });
  }
});