// pages/image/manage.js
const app = getApp();
const imageUtil = require('../../utils/imageUtil');

Page({
  data: {
    // 图片分类
    imageCategories: [
      { id: 'product', name: '商品图片', icon: '' },
      { id: 'category', name: '分类图片', icon: '' },
      { id: 'banner', name: '轮播图', icon: '' },
      { id: 'avatar', name: '用户头像', icon: '' }
    ],
    // 当前选中的分类
    currentCategory: 'product',
    // 图片列表
    imageList: [],
    // 加载状态
    loading: false,
    // 上传状态
    uploading: false,
    // 预览图片
    previewImages: [],
    // 搜索关键词
    searchKeyword: '',
    // 分页信息
    pagination: {
      page: 1,
      size: 20,
      total: 0
    },
    // 空数据图片
    emptyDataImage: ''
  },

  onLoad() {
    // 设置空数据图片的完整URL
    this.setData({
      emptyDataImage: app.getImageUrl('emptyData'),
      'imageCategories[0].icon': app.getImageUrl('transport'),
      'imageCategories[1].icon': app.getImageUrl('categoryDefault'),
      'imageCategories[2].icon': app.getImageUrl('logo'),
      'imageCategories[3].icon': app.getImageUrl('defaultAvatar')
    });
    
    this.loadImageList();
  },

  onShow() {
    // 刷新图片列表
    this.loadImageList();
  },

  onPullDownRefresh() {
    this.loadImageList().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    this.loadMoreImages();
  },

  /**
   * 切换图片分类
   */
  switchCategory(e) {
    const category = e.currentTarget.dataset.category;
    this.setData({
      currentCategory: category,
      'pagination.page': 1
    });
    this.loadImageList();
  },

  /**
   * 加载图片列表
   */
  async loadImageList() {
    try {
      this.setData({ loading: true });
      
      const category = this.data.currentCategory;
      let imageList = [];
      
      // 根据分类获取不同类型的图片
      switch (category) {
        case 'product':
          imageList = await this.loadProductImages();
          break;
        case 'category':
          imageList = await this.loadCategoryImages();
          break;
        case 'banner':
          imageList = await this.loadBannerImages();
          break;
        case 'avatar':
          imageList = await this.loadAvatarImages();
          break;
      }
      
      this.setData({
        imageList,
        loading: false
      });
    } catch (error) {
      console.error('加载图片列表失败:', error);
      this.setData({ loading: false });
      app.showToast('加载失败');
    }
  },

  /**
   * 加载更多图片
   */
  async loadMoreImages() {
    const { page, size, total } = this.data.pagination;
    if (page * size >= total) {
      return;
    }
    
    try {
      this.setData({
        'pagination.page': page + 1
      });
      
      // 这里可以实现分页加载逻辑
      // 暂时使用模拟数据
    } catch (error) {
      console.error('加载更多图片失败:', error);
    }
  },

  /**
   * 加载商品图片
   */
  async loadProductImages() {
    // 模拟获取商品ID列表
    const productIds = [1, 2, 3, 4, 5];
    const batchImages = await imageUtil.getBatchImages({ productIds });
    
    const imageList = [];
    Object.entries(batchImages.productImages || {}).forEach(([productId, images]) => {
      images.forEach((image, index) => {
        imageList.push({
          id: `product_${productId}_${index}`,
          url: image,
          type: 'product',
          title: `商品${productId}图片${index + 1}`,
          size: '未知',
          uploadTime: new Date().toLocaleString()
        });
      });
    });
    
    return imageList;
  },

  /**
   * 加载分类图片
   */
  async loadCategoryImages() {
    // 模拟获取分类ID列表
    const categoryIds = [1, 2, 3, 4];
    const batchImages = await imageUtil.getBatchImages({ categoryIds });
    
    const imageList = [];
    Object.entries(batchImages.categoryImages || {}).forEach(([categoryId, image]) => {
      imageList.push({
        id: `category_${categoryId}`,
        url: image,
        type: 'category',
        title: `分类${categoryId}图片`,
        size: '未知',
        uploadTime: new Date().toLocaleString()
      });
    });
    
    return imageList;
  },

  /**
   * 加载轮播图
   */
  async loadBannerImages() {
    const banners = await imageUtil.getBannerImages();
    
    return banners.map(banner => ({
      id: `banner_${banner.id}`,
      url: banner.imageUrl,
      type: 'banner',
      title: banner.title || `轮播图${banner.id}`,
      size: '未知',
      uploadTime: new Date().toLocaleString()
    }));
  },

  /**
   * 加载用户头像
   */
  async loadAvatarImages() {
    // 模拟获取用户ID列表
    const userIds = [1, 2, 3];
    const batchImages = await imageUtil.getBatchImages({ userIds });
    
    const imageList = [];
    Object.entries(batchImages.userAvatars || {}).forEach(([userId, avatar]) => {
      imageList.push({
        id: `avatar_${userId}`,
        url: avatar,
        type: 'avatar',
        title: `用户${userId}头像`,
        size: '未知',
        uploadTime: new Date().toLocaleString()
      });
    });
    
    return imageList;
  },

  /**
   * 选择并上传图片
   */
  async chooseAndUploadImage() {
    try {
      this.setData({ uploading: true });
      
      const uploadResult = await imageUtil.chooseAndUploadImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera']
      });
      
      app.showToast('上传成功', 'success');
      
      // 刷新图片列表
      this.loadImageList();
      
    } catch (error) {
      console.error('上传图片失败:', error);
      app.showToast('上传失败');
    } finally {
      this.setData({ uploading: false });
    }
  },

  /**
   * 批量上传图片
   */
  async batchUploadImages() {
    try {
      this.setData({ uploading: true });
      
      const chooseResult = await imageUtil.chooseImage({
        count: 9,
        sizeType: ['compressed'],
        sourceType: ['album']
      });
      
      const uploadPromises = chooseResult.tempFilePaths.map(filePath => 
        imageUtil.uploadImage(filePath)
      );
      
      const results = await Promise.allSettled(uploadPromises);
      
      const successCount = results.filter(result => result.status === 'fulfilled').length;
      const failCount = results.length - successCount;
      
      if (successCount > 0) {
        app.showToast(`成功上传${successCount}张图片${failCount > 0 ? `，失败${failCount}张` : ''}`, 'success');
        this.loadImageList();
      } else {
        app.showToast('上传失败');
      }
      
    } catch (error) {
      console.error('批量上传失败:', error);
      app.showToast('上传失败');
    } finally {
      this.setData({ uploading: false });
    }
  },

  /**
   * 预览图片
   */
  previewImage(e) {
    const current = e.currentTarget.dataset.url;
    const urls = this.data.imageList.map(item => item.url);
    
    wx.previewImage({
      current,
      urls
    });
  },

  /**
   * 复制图片链接
   */
  copyImageUrl(e) {
    const url = e.currentTarget.dataset.url;
    
    wx.setClipboardData({
      data: url,
      success: () => {
        app.showToast('链接已复制', 'success');
      }
    });
  },

  /**
   * 获取图片压缩版本
   */
  async getCompressedImage(e) {
    const url = e.currentTarget.dataset.url;
    
    try {
      app.showLoading('处理中...');
      
      const compressedUrl = await imageUtil.getCompressedImage(url, 300, 300);
      
      wx.setClipboardData({
        data: compressedUrl,
        success: () => {
          app.hideLoading();
          app.showToast('压缩链接已复制', 'success');
        }
      });
    } catch (error) {
      app.hideLoading();
      console.error('获取压缩图片失败:', error);
      app.showToast('处理失败');
    }
  },

  /**
   * 搜索图片
   */
  onSearchInput(e) {
    this.setData({
      searchKeyword: e.detail.value
    });
  },

  /**
   * 执行搜索
   */
  performSearch() {
    const keyword = this.data.searchKeyword.trim();
    if (!keyword) {
      this.loadImageList();
      return;
    }
    
    // 过滤图片列表
    const filteredList = this.data.imageList.filter(item => 
      item.title.includes(keyword) || item.url.includes(keyword)
    );
    
    this.setData({
      imageList: filteredList
    });
  },

  /**
   * 清除搜索
   */
  clearSearch() {
    this.setData({
      searchKeyword: ''
    });
    this.loadImageList();
  },

  /**
   * 清除图片缓存
   */
  clearImageCache() {
    wx.showModal({
      title: '确认清除',
      content: '确定要清除所有图片缓存吗？',
      success: (res) => {
        if (res.confirm) {
          imageUtil.clearCache();
          app.showToast('缓存已清除', 'success');
          this.loadImageList();
        }
      }
    });
  },

  /**
   * 查看缓存信息
   */
  showCacheInfo() {
    const cacheSize = imageUtil.getCacheSize();
    
    wx.showModal({
      title: '缓存信息',
      content: `当前缓存了 ${cacheSize} 个图片资源`,
      showCancel: false
    });
  }
});