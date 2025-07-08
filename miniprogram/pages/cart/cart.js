// pages/cart/cart.js
const app = getApp();

Page({
  data: {
    cartItems: [],
    totalAmount: 0,
    selectedItems: [],
    selectAll: false,
    loading: false,
    isLoggedIn: false,
    // 图片资源
    emptyCartImage: ''
  },

  onLoad() {
    // 设置空购物车图片的完整URL
    this.setData({
      emptyCartImage: app.getImageUrl('emptyCart')
    });
    
    this.loadCartData();
  },

  onShow() {
    this.loadCartData();
  },

  onPullDownRefresh() {
    this.loadCartData().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  // 加载购物车数据
  async loadCartData() {
    // 检查登录状态
    if (!app.globalData.token) {
      this.setData({
        cartItems: [],
        totalAmount: 0,
        selectedItems: [],
        selectAll: false,
        isLoggedIn: false
      });
      return;
    }

    try {
      this.setData({ loading: true });
      const res = await app.request({
        url: '/cart/list'
      });
      
      // 处理购物车数据
      const cartItems = res.data.map(item => ({
        ...item,
        selected: false,
        productImages: item.images && item.images.length > 0 ? item.images.split(',') : ['/images/default-product.svg']
      }));
      
      this.setData({
        cartItems: cartItems,
        loading: false,
        isLoggedIn: true
      });
      
      this.calculateTotal();
    } catch (error) {
      console.error('加载购物车失败:', error);
      this.setData({ loading: false });
    }
  },

  // 选择商品
  onItemSelect(e) {
    const index = e.currentTarget.dataset.index;
    const cartItems = this.data.cartItems;
    cartItems[index].selected = !cartItems[index].selected;
    
    this.setData({
      cartItems: cartItems
    });
    
    this.calculateTotal();
    this.checkSelectAll();
  },

  // 全选/取消全选
  onSelectAll() {
    const selectAll = !this.data.selectAll;
    const cartItems = this.data.cartItems.map(item => ({
      ...item,
      selected: selectAll
    }));
    
    this.setData({
      cartItems: cartItems,
      selectAll: selectAll
    });
    
    this.calculateTotal();
  },

  // 检查是否全选
  checkSelectAll() {
    const cartItems = this.data.cartItems;
    const selectAll = cartItems.length > 0 && cartItems.every(item => item.selected);
    this.setData({ selectAll });
  },

  // 计算总价
  calculateTotal() {
    const selectedItems = this.data.cartItems.filter(item => item.selected);
    const totalAmount = selectedItems.reduce((total, item) => {
      return total + (item.price * item.quantity);
    }, 0);
    
    this.setData({
      selectedItems: selectedItems,
      totalAmount: totalAmount.toFixed(2)
    });
  },

  // 修改商品数量
  async updateQuantity(e) {
    const { index, type } = e.currentTarget.dataset;
    const cartItems = this.data.cartItems;
    const item = cartItems[index];
    
    let newQuantity = item.quantity;
    if (type === 'minus' && newQuantity > 1) {
      newQuantity--;
    } else if (type === 'plus') {
      newQuantity++;
    } else {
      return;
    }

    try {
      await app.request({
        url: '/cart/update',
        method: 'PUT',
        data: {
          cartId: item.id,
          quantity: newQuantity
        }
      });
      
      cartItems[index].quantity = newQuantity;
      this.setData({ cartItems });
      this.calculateTotal();
      
      // 更新全局购物车数量
      app.getCartCount();
    } catch (error) {
      console.error('更新数量失败:', error);
    }
  },

  // 删除商品
  async deleteItem(e) {
    const index = e.currentTarget.dataset.index;
    const item = this.data.cartItems[index];
    
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个商品吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await app.request({
              url: '/cart/delete',
              method: 'POST',
              data: {
                productId: item.productId
              }
            });
            
            const cartItems = this.data.cartItems;
            cartItems.splice(index, 1);
            this.setData({ cartItems });
            this.calculateTotal();
            this.checkSelectAll();
            
            // 更新全局购物车数量
            app.getCartCount();
            app.showToast('删除成功', 'success');
          } catch (error) {
            console.error('删除失败:', error);
          }
        }
      }
    });
  },

  // 去结算
  goToCheckout() {
    if (this.data.selectedItems.length === 0) {
      app.showToast('请选择要结算的商品');
      return;
    }
    
    // 将选中的商品信息传递给结算页面
    const selectedItems = this.data.selectedItems;
    wx.navigateTo({
      url: `/pages/checkout/checkout?cartItems=${encodeURIComponent(JSON.stringify(selectedItems))}`
    });
  },

  // 跳转到登录页面
  goToLogin() {
    wx.navigateTo({
      url: '/pages/login/login'
    });
  },

  // 跳转到首页
  goToHome() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  }
});