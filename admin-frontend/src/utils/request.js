import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { getToken } from '@/utils/auth';
import router from '@/router';

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // api的base_url
  timeout: 10000, // 请求超时时间
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 在发送请求之前做些什么
    const token = getToken();
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    // 对请求错误做些什么
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    // 对响应数据做点什么
    const res = response.data;

    // 如果自定义代码不是200，则判断为一个错误
    if (res.code !== 200) {
      ElMessage({
        message: res.message || '请求失败',
        type: 'error',
        duration: 5 * 1000,
      });

      // 401: 未授权
      if (res.code === 401) {
        ElMessageBox.confirm(
          '登录状态已过期，您可以继续留在该页面，或者重新登录',
          '系统提示',
          {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning',
          }
        ).then(() => {
          const userStore = useUserStore();
          userStore.resetState();
          router.push('/login');
        });
      }

      return Promise.reject(new Error(res.message || '请求失败'));
    } else {
      return res;
    }
  },
  (error) => {
    // 对响应错误做点什么
    console.error('响应错误:', error);

    let message = '网络错误';
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求错误';
          break;
        case 401:
          message = '未授权，请登录';
          break;
        case 403:
          message = '拒绝访问';
          break;
        case 404:
          message = '请求地址出错';
          break;
        case 408:
          message = '请求超时';
          break;
        case 500:
          message = '服务器内部错误';
          break;
        case 501:
          message = '服务未实现';
          break;
        case 502:
          message = '网关错误';
          break;
        case 503:
          message = '服务不可用';
          break;
        case 504:
          message = '网关超时';
          break;
        case 505:
          message = 'HTTP版本不受支持';
          break;
        default:
          message = `连接错误${error.response.status}`;
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时';
    } else if (error.message.includes('Network Error')) {
      message = '网络连接异常';
    }

    ElMessage({
      message,
      type: 'error',
      duration: 5 * 1000,
    });

    return Promise.reject(error);
  }
);

export const request = service;
export default service;
