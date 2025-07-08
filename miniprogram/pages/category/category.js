// pages/category/category.js
const app = getApp();

Page({
  data: {
    categories: [],
    products: [],
    currentCategoryId: 0,
    loading: false
  },

  onLoad() {
    this.loadCategories();
  },

  onShow() {
    // 更新购物车数量
    app.getCartCount();
  },

  onPullDownRefresh() {
    this.loadData().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  // 加载分类数据
  async loadCategories() {
    try {
      const res = await app.request({
        url: '/category/list',
        needToken: false
      });
      const categories = [{ id: 0, name: '全部' }, ...res.data];
      this.setData({
        categories: categories
      });
      // 默认加载全部商品
      this.loadProducts(0);
    } catch (error) {
      console.error('加载分类失败:', error);
    }
  },

  // 加载商品数据
  async loadProducts(categoryId) {
    try {
      this.setData({ loading: true });
      const url = categoryId === 0 ? '/product/list' : `/product/category/${categoryId}`;
      const res = await app.request({
        url: url,
        needToken: false
      });
      // 处理商品图片数据 - 处理分页响应结构
      const productList = res.data.records || res.data || [];
      const products = productList.map(item => ({
        ...item,
        images: item.images && item.images.length > 0 ? item.images : ['/images/default-product.svg']
      }));
      this.setData({
        products: products,
        currentCategoryId: categoryId,
        loading: false
      });
    } catch (error) {
      console.error('加载商品失败:', error);
      this.setData({ loading: false });
    }
  },

  // 切换分类
  onCategoryTap(e) {
    const categoryId = e.currentTarget.dataset.id;
    this.loadProducts(categoryId);
  },

  // 跳转到商品详情页
  goToProductDetail(e) {
    const productId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/product/detail?id=${productId}`
    });
  },

  // 添加到购物车
  async addToCart(e) {
    const productId = e.currentTarget.dataset.id;
    
    // 检查登录状态
    if (!app.globalData.token) {
      wx.navigateTo({
        url: '/pages/login/login'
      });
      return;
    }

    try {
      app.showLoading('添加中...');
      await app.request({
        url: '/cart/add',
        method: 'POST',
        data: {
          productId: productId,
          quantity: 1
        }
      });
      app.hideLoading();
      app.showToast('添加成功', 'success');
      // 更新购物车数量
      app.getCartCount();
    } catch (error) {
      app.hideLoading();
      console.error('添加购物车失败:', error);
    }
  }
});