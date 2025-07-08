// pages/search/search.js
const app = getApp();

Page({
  data: {
    keyword: '',
    searchHistory: [],
    hotKeywords: ['苹果', '香蕉', '西红柿', '土豆', '白菜', '胡萝卜'],
    searchResults: [],
    loading: false,
    hasMore: true,
    page: 1,
    pageSize: 20,
    showResults: false,
    showClearHistory: false,
    emptySearchImage: ''
  },

  onLoad(options) {
    // 设置空搜索图片的完整URL
    this.setData({
      emptySearchImage: app.getImageUrl('emptySearch')
    });
    
    // 如果有传入的关键词，直接搜索
    if (options.keyword) {
      this.setData({
        keyword: options.keyword
      });
      this.searchProducts();
    }
    
    // 加载搜索历史
    this.loadSearchHistory();
  },

  onShow() {
    // 每次显示页面时重新加载搜索历史
    this.loadSearchHistory();
  },

  // 加载搜索历史
  loadSearchHistory() {
    const history = wx.getStorageSync('searchHistory') || [];
    this.setData({
      searchHistory: history,
      showClearHistory: history.length > 0
    });
  },

  // 保存搜索历史
  saveSearchHistory(keyword) {
    if (!keyword.trim()) return;
    
    let history = wx.getStorageSync('searchHistory') || [];
    
    // 移除重复的关键词
    history = history.filter(item => item !== keyword);
    
    // 添加到开头
    history.unshift(keyword);
    
    // 限制历史记录数量
    if (history.length > 10) {
      history = history.slice(0, 10);
    }
    
    wx.setStorageSync('searchHistory', history);
    this.setData({
      searchHistory: history,
      showClearHistory: true
    });
  },

  // 输入框变化
  onInputChange(e) {
    this.setData({
      keyword: e.detail.value
    });
  },

  // 搜索确认
  onSearchConfirm(e) {
    const keyword = e.detail.value.trim();
    if (keyword) {
      this.setData({ keyword });
      this.searchProducts();
    }
  },

  // 点击搜索按钮
  onSearchTap() {
    if (this.data.keyword.trim()) {
      this.searchProducts();
    }
  },

  // 点击热门关键词或历史记录
  onKeywordTap(e) {
    const keyword = e.currentTarget.dataset.keyword;
    this.setData({ keyword });
    this.searchProducts();
  },

  // 搜索商品
  async searchProducts() {
    const keyword = this.data.keyword.trim();
    if (!keyword) {
      app.showToast('请输入搜索关键词');
      return;
    }

    try {
      this.setData({ 
        loading: true,
        page: 1,
        searchResults: [],
        showResults: true
      });

      // 保存搜索历史
      this.saveSearchHistory(keyword);

      const res = await app.request({
        url: '/product/search',
        data: {
          keyword: keyword,
          page: 1,
          pageSize: this.data.pageSize
        }
      });

      // 安全地处理响应数据 - MyBatis Plus分页结构
      const responseData = res.data || {};
      const productList = responseData.records || [];
      const currentPage = responseData.current || 1;
      const totalPages = responseData.pages || 1;
      const hasMoreData = currentPage < totalPages;
      
      this.setData({
        searchResults: productList,
        hasMore: hasMoreData,
        loading: false
      });

      if (productList.length === 0) {
        app.showToast('未找到相关商品');
      }
    } catch (error) {
      console.error('搜索失败:', error);
      this.setData({ loading: false });
      app.showToast('搜索失败，请重试');
    }
  },

  // 加载更多
  async loadMore() {
    if (this.data.loading || !this.data.hasMore) return;

    try {
      this.setData({ loading: true });

      const res = await app.request({
        url: '/product/search',
        data: {
          keyword: this.data.keyword,
          page: this.data.page + 1,
          pageSize: this.data.pageSize
        }
      });

      // 安全地处理响应数据 - MyBatis Plus分页结构
      const responseData = res.data || {};
      const newProductList = responseData.records || [];
      const currentPage = responseData.current || 1;
      const totalPages = responseData.pages || 1;
      const hasMoreData = currentPage < totalPages;
      
      this.setData({
        searchResults: [...this.data.searchResults, ...newProductList],
        hasMore: hasMoreData,
        page: this.data.page + 1,
        loading: false
      });
    } catch (error) {
      console.error('加载更多失败:', error);
      this.setData({ loading: false });
    }
  },

  // 清空搜索历史
  clearSearchHistory() {
    wx.showModal({
      title: '提示',
      content: '确定要清空搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('searchHistory');
          this.setData({
            searchHistory: [],
            showClearHistory: false
          });
          app.showToast('已清空搜索历史');
        }
      }
    });
  },

  // 删除单个搜索历史
  deleteHistoryItem(e) {
    const index = e.currentTarget.dataset.index;
    let history = [...this.data.searchHistory];
    history.splice(index, 1);
    
    wx.setStorageSync('searchHistory', history);
    this.setData({
      searchHistory: history,
      showClearHistory: history.length > 0
    });
  },

  // 跳转到商品详情
  goToProductDetail(e) {
    const productId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/product/detail?id=${productId}`
    });
  },

  // 添加到购物车
  async addToCart(e) {
    const product = e.currentTarget.dataset.product;
    
    if (!app.checkLogin()) {
      return;
    }

    try {
      await app.request({
        url: '/cart/add',
        method: 'POST',
        data: {
          productId: product.id,
          quantity: 1
        }
      });

      app.showToast('已添加到购物车', 'success');
      app.getCartCount();
    } catch (error) {
      console.error('添加购物车失败:', error);
      app.showToast('添加失败，请重试');
    }
  },

  // 清空搜索框
  clearInput() {
    this.setData({
      keyword: '',
      showResults: false
    });
  },

  // 返回上一页
  goBack() {
    wx.navigateBack();
  },

  // 下拉刷新
  onPullDownRefresh() {
    if (this.data.keyword.trim()) {
      this.searchProducts().then(() => {
        wx.stopPullDownRefresh();
      });
    } else {
      wx.stopPullDownRefresh();
    }
  },

  // 上拉加载更多
  onReachBottom() {
    this.loadMore();
  }
});