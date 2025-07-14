// utils/linkGenerator.js
// 智能链接生成器 - 根据用户意图生成小程序页面跳转链接

// 页面路径映射
var pageMap = {
  // 商品相关
  'product': {
    path: '/pages/product/detail',
    name: '商品详情',
    keywords: ['商品', '产品', '蔬菜', '水果', '详情', '介绍', '价格'],
    type: 'navigate'
  },
  'category': {
    path: '/pages/category/category',
    name: '商品分类',
    keywords: ['分类', '种类', '类别', '浏览', '选择'],
    type: 'switchTab'
  },
  'search': {
    path: '/pages/search/search',
    name: '搜索商品',
    keywords: ['搜索', '查找', '寻找', '找'],
    type: 'navigate'
  },
  
  // 购物相关
  'cart': {
    path: '/pages/cart/cart',
    name: '购物车',
    keywords: ['购物车', '购物', '结算', '清单'],
    type: 'switchTab'
  },
  'checkout': {
    path: '/pages/checkout/checkout',
    name: '结算页面',
    keywords: ['结算', '下单', '支付', '付款'],
    type: 'navigate'
  },
  
  // 订单相关
  'orderList': {
    path: '/pages/order/list',
    name: '订单列表',
    keywords: ['订单', '历史', '记录', '查看订单', '我的订单'],
    type: 'navigate'
  },
  'orderPay': {
    path: '/pages/order/pay',
    name: '订单支付',
    keywords: ['支付', '付款', '缴费'],
    type: 'navigate'
  },
  'logistics': {
    path: '/pages/order/logistics',
    name: '物流信息',
    keywords: ['物流', '配送', '快递', '运输', '送货'],
    type: 'navigate'
  },
  
  // 地址相关
  'addressList': {
    path: '/pages/address/list',
    name: '地址管理',
    keywords: ['地址', '收货地址', '配送地址', '管理地址'],
    type: 'navigate'
  },
  'addressEdit': {
    path: '/pages/address/edit',
    name: '编辑地址',
    keywords: ['编辑地址', '修改地址', '新增地址', '添加地址'],
    type: 'navigate'
  },
  
  // 个人中心
  'profile': {
    path: '/pages/profile/profile',
    name: '个人中心',
    keywords: ['个人', '我的', '账户', '设置', '个人信息'],
    type: 'switchTab'
  },
  'login': {
    path: '/pages/login/login',
    name: '登录页面',
    keywords: ['登录', '注册', '账号'],
    type: 'navigate'
  },
  
  // 首页
  'home': {
    path: '/pages/index/index',
    name: '首页',
    keywords: ['首页', '主页', '回到首页', '返回首页'],
    type: 'switchTab'
  }
};

// 定义常见问题和对应的功能链接
var intentMap = {
  '商品咨询': {
    pages: ['category', 'search', 'product'],
    response: '您可以通过以下方式查看商品信息：'
  },
  '订单查询': {
    pages: ['orderList', 'logistics'],
    response: '您可以在这里查看订单信息：'
  },
  '配送问题': {
    pages: ['logistics', 'addressList'],
    response: '关于配送问题，您可以：'
  },
  '退换货': {
    pages: ['orderList', 'profile'],
    response: '关于退换货，请：'
  },
  '优惠活动': {
    pages: ['home', 'category'],
    response: '查看最新优惠活动：'
  },
  '支付问题': {
    pages: ['orderPay', 'orderList'],
    response: '关于支付问题：'
  },
  '地址管理': {
    pages: ['addressList', 'addressEdit'],
    response: '管理您的收货地址：'
  }
};

/**
 * 检查是否匹配特定意图
 * @param {string} text - 要检查的文本
 * @param {string} intent - 意图名称
 * @returns {boolean}
 */
function matchesIntent(text, intent) {
  var intentKeywords = {
    '商品咨询': ['商品', '产品', '蔬菜', '水果', '咨询', '介绍'],
    '订单查询': ['订单', '查询', '查看', '历史'],
    '配送问题': ['配送', '送货', '物流', '快递', '运输'],
    '退换货': ['退货', '换货', '退换', '退款'],
    '优惠活动': ['优惠', '活动', '促销', '折扣', '特价'],
    '支付问题': ['支付', '付款', '缴费', '付钱'],
    '地址管理': ['地址', '收货', '配送地址']
  };
  
  var keywords = intentKeywords[intent] || [];
  for (var i = 0; i < keywords.length; i++) {
    if (text.indexOf(keywords[i]) !== -1) {
      return true;
    }
  }
  return false;
}

/**
 * 计算文本与关键词的匹配分数
 * @param {string} text - 要检查的文本
 * @param {Array} keywords - 关键词数组
 * @returns {number} 匹配分数
 */
function calculateMatchScore(text, keywords) {
  var score = 0;
  for (var i = 0; i < keywords.length; i++) {
    if (text.indexOf(keywords[i].toLowerCase()) !== -1) {
      score += keywords[i].length; // 关键词越长，权重越高
    }
  }
  return score;
}

/**
 * 根据用户消息生成相关的页面链接
 * @param {string} userMessage - 用户输入的消息
 * @param {string} aiResponse - AI的回复内容
 * @returns {Object} 包含链接信息的对象
 */
function generateLinks(userMessage, aiResponse) {
  aiResponse = aiResponse || '';
  var message = userMessage.toLowerCase();
  var response = aiResponse.toLowerCase();
  var combinedText = message + ' ' + response;
  
  var matchedPages = [];
  var suggestions = [];
  
  // 1. 首先检查是否匹配预定义的意图
  var intentKeys = Object.keys(intentMap);
  for (var i = 0; i < intentKeys.length; i++) {
    var intent = intentKeys[i];
    var config = intentMap[intent];
    if (matchesIntent(combinedText, intent)) {
      for (var j = 0; j < config.pages.length; j++) {
        var pageKey = config.pages[j];
        if (pageMap[pageKey]) {
          var found = false;
          for (var k = 0; k < matchedPages.length; k++) {
            if (matchedPages[k].key === pageKey) {
              found = true;
              break;
            }
          }
          if (!found) {
            var page = pageMap[pageKey];
            matchedPages.push({
              key: pageKey,
              path: page.path,
              name: page.name,
              keywords: page.keywords,
              type: page.type,
              priority: 10 // 意图匹配优先级最高
            });
          }
        }
      }
      break;
    }
  }
  
  // 2. 如果没有匹配到预定义意图，则进行关键词匹配
  if (matchedPages.length === 0) {
    var pageKeys = Object.keys(pageMap);
    for (var i = 0; i < pageKeys.length; i++) {
      var pageKey = pageKeys[i];
      var pageInfo = pageMap[pageKey];
      var score = calculateMatchScore(combinedText, pageInfo.keywords);
      if (score > 0) {
        matchedPages.push({
          key: pageKey,
          path: pageInfo.path,
          name: pageInfo.name,
          keywords: pageInfo.keywords,
          type: pageInfo.type,
          priority: score
        });
      }
    }
  }
  
  // 3. 按优先级排序，取前3个
  matchedPages.sort(function(a, b) {
    return b.priority - a.priority;
  });
  var sortedPages = matchedPages.slice(0, 3);
  
  // 4. 生成建议链接
  for (var i = 0; i < sortedPages.length; i++) {
    var page = sortedPages[i];
    suggestions.push({
      text: '📱 ' + page.name,
      path: page.path,
      type: page.type || 'navigate'
    });
  }
  
  return {
    hasLinks: suggestions.length > 0,
    suggestions: suggestions,
    matchedPages: sortedPages
  };
}

/**
 * 生成带链接的AI回复
 * @param {string} originalResponse - 原始AI回复
 * @param {string} userMessage - 用户消息
 * @returns {Object} 包含回复内容和链接的对象
 */
function enhanceResponse(originalResponse, userMessage) {
  var linkInfo = generateLinks(userMessage, originalResponse);
  
  if (linkInfo.hasLinks) {
    return {
      content: originalResponse,
      hasLinks: true,
      links: linkInfo.suggestions,
      linkText: '\n\n💡 相关功能推荐：'
    };
  }
  
  return {
    content: originalResponse,
    hasLinks: false,
    links: [],
    linkText: ''
  };
}

// 导出模块
module.exports = {
  pageMap: pageMap,
  intentMap: intentMap,
  generateLinks: generateLinks,
  enhanceResponse: enhanceResponse,
  matchesIntent: matchesIntent,
  calculateMatchScore: calculateMatchScore
};