// pages/index/index.js
const app = getApp();
const imageUtil = require('../../utils/imageUtil');

Page({
  data: {
    banners: [],
    categories: [],
    recommendProducts: [],
    hotProducts: []
  },

  onLoad() {
    this.loadData();
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

  // 加载页面数据
  async loadData() {
    try {
      await Promise.all([
        this.loadBanners(),
        this.loadCategories(),
        this.loadRecommendProducts(),
        this.loadHotProducts()
      ]);
    } catch (error) {
      console.error('加载数据失败:', error);
    }
  },

  // 加载轮播图数据
  async loadBanners() {
    try {
      const banners = await imageUtil.getBannerImages();
      this.setData({ banners });
    } catch (error) {
      console.error('加载轮播图失败:', error);
    }
  },

  // 加载分类数据
  async loadCategories() {
    try {
      const res = await app.request({
        url: '/category/list',
        needToken: false
      });
      
      const categories = res.data.slice(0, 8); // 只显示前8个分类
      
      // 批量获取分类图片
      const categoryIds = categories.map(item => item.id);
      const batchImages = await imageUtil.getBatchImages({ categoryIds });
      
      // 为分类添加图片
      const categoriesWithImages = categories.map(category => ({
        ...category,
        image: batchImages.categoryImages?.[category.id] || imageUtil.getDefaultImage('category')
      }));
      
      this.setData({ categories: categoriesWithImages });
    } catch (error) {
      console.error('加载分类失败:', error);
    }
  },

  // 加载推荐商品
  async loadRecommendProducts() {
    try {
      const res = await app.request({
        url: '/product/recommend?limit=6',
        needToken: false
      });
      
      const products = res.data;
      
      // 批量获取商品图片
      const productIds = products.map(item => item.id);
      const batchImages = await imageUtil.getBatchImages({ productIds });
      
      // 为商品添加图片
      const productsWithImages = products.map(product => ({
        ...product,
        images: batchImages.productImages?.[product.id] || [imageUtil.getDefaultImage('product')]
      }));
      
      this.setData({ recommendProducts: productsWithImages });
    } catch (error) {
      console.error('加载推荐商品失败:', error);
    }
  },

  // 加载热销商品
  async loadHotProducts() {
    try {
      const res = await app.request({
        url: '/product/hot?limit=5',
        needToken: false
      });
      
      const products = res.data;
      
      // 批量获取商品图片
      const productIds = products.map(item => item.id);
      const batchImages = await imageUtil.getBatchImages({ productIds });
      
      // 为商品添加图片
      const productsWithImages = products.map(product => ({
        ...product,
        images: batchImages.productImages?.[product.id] || [imageUtil.getDefaultImage('product')]
      }));
      
      this.setData({ hotProducts: productsWithImages });
    } catch (error) {
      console.error('加载热销商品失败:', error);
    }
  },

  // 跳转到搜索页面
  goToSearch() {
    wx.navigateTo({
      url: '/pages/search/search'
    });
  },

  // 轮播图点击事件
  onBannerTap(e) {
    const url = e.currentTarget.dataset.url;
    if (url) {
      // 处理轮播图跳转逻辑
      console.log('轮播图跳转:', url);
    }
  },

  // 跳转到分类页面
  goToCategory(e) {
    const categoryId = e.currentTarget.dataset.id;
    wx.switchTab({
      url: '/pages/category/category'
    });
  },

  // 跳转到商品详情页
  goToProductDetail(e) {
    const productId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/product/detail?id=${productId}`
    });
  },

  // 跳转到商品列表页
  goToProductList(e) {
    const type = e.currentTarget.dataset.type;
    wx.navigateTo({
      url: `/pages/product/list?type=${type}`
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