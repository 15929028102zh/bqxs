// API配置文件
const config = {
  // 硅基流动API配置
  siliconFlow: {
    baseUrl: 'https://api.siliconflow.cn/v1',
    apiKey: 'sk-rtygpgugwrkztwuyiajvegxfzpzjppyveudljakgjrvzdheh',
    model: 'Qwen/Qwen2.5-7B-Instruct',
    timeout: 30000,
    maxRetries: 3,
    retryDelay: 1000
  },
  
  // 网络配置
  network: {
    timeout: 30000,
    maxRetries: 3,
    retryDelay: 1000
  },
  
  // 错误消息配置
  errorMessages: {
    timeout: '网络请求超时，请检查网络连接后重试',
    networkError: '网络连接失败，请检查网络设置',
    apiError: '服务暂时不可用，请稍后重试',
    unknownError: '未知错误，请稍后重试'
  }
};

module.exports = config;