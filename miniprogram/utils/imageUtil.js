/**
 * 图片资源管理工具类
 * 统一管理小程序中的图片资源获取、缓存和显示
 */

const app = getApp();

class ImageUtil {
  constructor() {
    // 图片缓存
    this.imageCache = new Map();
    // 图片加载队列
    this.loadingQueue = new Set();
  }

  /**
   * 获取商品图片列表
   * @param {number} productId 商品ID
   * @param {boolean} useCache 是否使用缓存
   * @returns {Promise<Array>} 图片URL数组
   */
  async getProductImages(productId, useCache = true) {
    const cacheKey = `product_${productId}`;
    
    // 检查缓存
    if (useCache && this.imageCache.has(cacheKey)) {
      return this.imageCache.get(cacheKey);
    }

    try {
      const res = await app.request({
        url: `/image/product/${productId}`,
        method: 'GET'
      });

      const images = res.data.images || [this.getDefaultImage('product')];
      
      // 缓存结果
      if (useCache) {
        this.imageCache.set(cacheKey, images);
      }

      return images;
    } catch (error) {
      console.error('获取商品图片失败:', error);
      return [this.getDefaultImage('product')];
    }
  }

  /**
   * 获取分类图片
   * @param {number} categoryId 分类ID
   * @param {boolean} useCache 是否使用缓存
   * @returns {Promise<string>} 图片URL
   */
  async getCategoryImage(categoryId, useCache = true) {
    const cacheKey = `category_${categoryId}`;
    
    // 检查缓存
    if (useCache && this.imageCache.has(cacheKey)) {
      return this.imageCache.get(cacheKey);
    }

    try {
      const res = await app.request({
        url: `/image/category/${categoryId}`,
        method: 'GET'
      });

      const image = res.data.image || this.getDefaultImage('category');
      
      // 缓存结果
      if (useCache) {
        this.imageCache.set(cacheKey, image);
      }

      return image;
    } catch (error) {
      console.error('获取分类图片失败:', error);
      return this.getDefaultImage('category');
    }
  }

  /**
   * 获取用户头像
   * @param {number} userId 用户ID
   * @param {boolean} useCache 是否使用缓存
   * @returns {Promise<string>} 头像URL
   */
  async getUserAvatar(userId, useCache = true) {
    const cacheKey = `avatar_${userId}`;
    
    // 检查缓存
    if (useCache && this.imageCache.has(cacheKey)) {
      return this.imageCache.get(cacheKey);
    }

    try {
      const res = await app.request({
        url: `/image/avatar/${userId}`,
        method: 'GET'
      });

      const avatar = res.data.avatar || this.getDefaultImage('avatar');
      
      // 缓存结果
      if (useCache) {
        this.imageCache.set(cacheKey, avatar);
      }

      return avatar;
    } catch (error) {
      console.error('获取用户头像失败:', error);
      return this.getDefaultImage('avatar');
    }
  }

  /**
   * 获取轮播图列表
   * @param {boolean} useCache 是否使用缓存
   * @returns {Promise<Array>} 轮播图数组
   */
  async getBannerImages(useCache = true) {
    const cacheKey = 'banner_images';
    
    // 检查缓存
    if (useCache && this.imageCache.has(cacheKey)) {
      return this.imageCache.get(cacheKey);
    }

    try {
      const res = await app.request({
        url: '/image/banner',
        method: 'GET'
      });

      const banners = res.data || [];
      
      // 缓存结果
      if (useCache) {
        this.imageCache.set(cacheKey, banners);
      }

      return banners;
    } catch (error) {
      console.error('获取轮播图失败:', error);
      return [];
    }
  }

  /**
   * 批量获取图片资源
   * @param {Object} options 批量获取选项
   * @param {Array} options.productIds 商品ID数组
   * @param {Array} options.categoryIds 分类ID数组
   * @param {Array} options.userIds 用户ID数组
   * @param {boolean} options.includeBanner 是否包含轮播图
   * @returns {Promise<Object>} 批量图片数据
   */
  async getBatchImages(options = {}) {
    try {
      const res = await app.request({
        url: '/image/batch',
        method: 'POST',
        data: options
      });

      const result = res.data || {};
      
      // 缓存批量结果
      if (result.productImages) {
        Object.entries(result.productImages).forEach(([productId, images]) => {
          this.imageCache.set(`product_${productId}`, images);
        });
      }
      
      if (result.categoryImages) {
        Object.entries(result.categoryImages).forEach(([categoryId, image]) => {
          this.imageCache.set(`category_${categoryId}`, image);
        });
      }
      
      if (result.userAvatars) {
        Object.entries(result.userAvatars).forEach(([userId, avatar]) => {
          this.imageCache.set(`avatar_${userId}`, avatar);
        });
      }
      
      if (result.bannerImages) {
        this.imageCache.set('banner_images', result.bannerImages);
      }

      return result;
    } catch (error) {
      console.error('批量获取图片失败:', error);
      return {};
    }
  }

  /**
   * 获取压缩图片URL
   * @param {string} originalUrl 原始图片URL
   * @param {number} width 目标宽度
   * @param {number} height 目标高度
   * @returns {Promise<string>} 压缩图片URL
   */
  async getCompressedImage(originalUrl, width = 300, height = 300) {
    try {
      const res = await app.request({
        url: '/image/compress',
        method: 'GET',
        data: {
          url: originalUrl,
          width,
          height
        }
      });

      return res.data.compressedUrl || originalUrl;
    } catch (error) {
      console.error('获取压缩图片失败:', error);
      return originalUrl;
    }
  }

  /**
   * 预加载图片
   * @param {string|Array} urls 图片URL或URL数组
   * @returns {Promise<Array>} 预加载结果
   */
  async preloadImages(urls) {
    const urlArray = Array.isArray(urls) ? urls : [urls];
    const promises = urlArray.map(url => this.preloadSingleImage(url));
    
    try {
      const results = await Promise.allSettled(promises);
      return results.map((result, index) => ({
        url: urlArray[index],
        success: result.status === 'fulfilled',
        error: result.status === 'rejected' ? result.reason : null
      }));
    } catch (error) {
      console.error('预加载图片失败:', error);
      return [];
    }
  }

  /**
   * 预加载单个图片
   * @param {string} url 图片URL
   * @returns {Promise}
   */
  preloadSingleImage(url) {
    return new Promise((resolve, reject) => {
      if (this.loadingQueue.has(url)) {
        resolve(url);
        return;
      }

      this.loadingQueue.add(url);
      
      wx.getImageInfo({
        src: url,
        success: () => {
          this.loadingQueue.delete(url);
          resolve(url);
        },
        fail: (error) => {
          this.loadingQueue.delete(url);
          reject(error);
        }
      });
    });
  }

  /**
   * 获取默认图片
   * @param {string} type 图片类型
   * @returns {string} 默认图片URL
   */
  getDefaultImage(type) {
    // 动态获取默认图片URL
    const typeMap = {
      product: 'defaultProduct',
      category: 'categoryDefault',
      avatar: 'defaultAvatar',
      banner: 'logo'
    };
    
    const imageKey = typeMap[type] || 'defaultProduct';
    return app.getImageUrl(imageKey);
  }

  /**
   * 清除图片缓存
   * @param {string} key 缓存键，不传则清除所有缓存
   */
  clearCache(key) {
    if (key) {
      this.imageCache.delete(key);
    } else {
      this.imageCache.clear();
    }
  }

  /**
   * 获取缓存大小
   * @returns {number} 缓存条目数量
   */
  getCacheSize() {
    return this.imageCache.size;
  }

  /**
   * 上传图片
   * @param {string} filePath 本地文件路径
   * @param {Object} options 上传选项
   * @returns {Promise<Object>} 上传结果
   */
  async uploadImage(filePath, options = {}) {
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: app.globalData.baseUrl + '/upload/image',
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': wx.getStorageSync('token') || ''
        },
        formData: options.formData || {},
        success: (res) => {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200) {
              resolve(data.data);
            } else {
              reject(new Error(data.message || '上传失败'));
            }
          } catch (error) {
            reject(new Error('解析响应失败'));
          }
        },
        fail: reject
      });
    });
  }

  /**
   * 选择并上传图片
   * @param {Object} options 选择选项
   * @returns {Promise<Object>} 上传结果
   */
  async chooseAndUploadImage(options = {}) {
    try {
      const chooseResult = await this.chooseImage(options);
      const uploadResult = await this.uploadImage(chooseResult.tempFilePaths[0]);
      return uploadResult;
    } catch (error) {
      console.error('选择并上传图片失败:', error);
      throw error;
    }
  }

  /**
   * 选择图片
   * @param {Object} options 选择选项
   * @returns {Promise<Object>} 选择结果
   */
  chooseImage(options = {}) {
    return new Promise((resolve, reject) => {
      wx.chooseImage({
        count: options.count || 1,
        sizeType: options.sizeType || ['original', 'compressed'],
        sourceType: options.sourceType || ['album', 'camera'],
        success: resolve,
        fail: reject
      });
    });
  }
}

// 创建单例实例
const imageUtil = new ImageUtil();

module.exports = imageUtil;