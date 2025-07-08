/**
 * 图片资源配置
 * 统一管理小程序中的静态图片资源路径
 */

const app = getApp();

class ImageConfig {
  constructor() {
    this.baseUrl = '';
    this.images = {};
  }

  // 初始化图片配置
  init(baseUrl) {
    // 防护：确保 baseUrl 不为 null 或 undefined
    if (!baseUrl || baseUrl === 'null' || baseUrl === 'undefined') {
      console.error('ImageConfig init: baseUrl is invalid:', baseUrl);
      baseUrl = 'http://192.168.229.1:8081/api'; // 使用默认值
    }
    
    this.baseUrl = baseUrl;
    this.images = {
      // Logo相关
      logo: `${baseUrl}/images/logo.svg`,
      
      // 默认图片
      defaultProduct: `${baseUrl}/images/default-product.svg`,
      defaultAvatar: `${baseUrl}/images/default-avatar.svg`,
      
      // 空状态图片
      emptyCart: `${baseUrl}/images/empty-cart.svg`,
      emptyOrder: `${baseUrl}/images/empty-order.svg`,
      emptySearch: `${baseUrl}/images/empty-search.svg`,
      emptyAddress: `${baseUrl}/images/empty-address.svg`,
      emptyData: `${baseUrl}/images/empty-data.png`,
      
      // 分类图标
      categoryDefault: `${baseUrl}/images/tab/category.png`,
      transport: `${baseUrl}/images/tab/transport.png`
    };
  }

  // 获取图片URL
  get(key) {
    // 防护：确保返回有效的URL
    const url = this.images[key] || this.images.defaultProduct;
    if (!url || url.includes('null') || url.includes('undefined')) {
      console.error('ImageConfig get: invalid URL for key:', key, 'url:', url);
      return `http://192.168.229.1:8081/api/images/default-product.svg`;
    }
    return url;
  }

  // 获取所有图片配置
  getAll() {
    return this.images;
  }

  // 动态添加图片配置
  add(key, url) {
    this.images[key] = url;
  }
}

// 创建单例实例
const imageConfig = new ImageConfig();

module.exports = imageConfig;