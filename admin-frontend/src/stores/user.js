import { defineStore } from 'pinia';
import { login, logout, getUserInfo } from '@/api/user';
import { removeToken, setToken, getToken } from '@/utils/auth';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: null,
    permissions: [],
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    userName: (state) => state.userInfo?.name || state.userInfo?.username || '管理员',
    userAvatar: (state) => state.userInfo?.avatar || '',
    userRole: (state) => state.userInfo?.role || '',
    hasPermission: (state) => (permission) => {
      return state.permissions.includes(permission);
    },
  },

  actions: {
    // 登录
    async login(loginForm) {
      const response = await login(loginForm);
      const { token } = response.data;

      this.token = token;
      setToken(token);

      return response;
    },

    // 获取用户信息
    async getUserInfo() {
      const response = await getUserInfo();
      const { data } = response;

      this.userInfo = {
        id: data.id,
        username: data.username,
        name: data.name || data.username,
        email: data.email,
        phone: data.phone,
        role: data.role,
        avatar: data.avatar || '',
        lastLoginTime: data.lastLoginTime,
        lastLoginIp: data.lastLoginIp
      };
      this.permissions = data.permissions || [];

      return response;
    },

    // 登出
    async logout() {
      try {
        await logout();
      } catch (error) {
        console.error('登出失败:', error);
      } finally {
        this.token = null;
        this.userInfo = null;
        this.permissions = [];
        removeToken();
      }
    },

    // 重置状态
    resetState() {
      this.token = null;
      this.userInfo = null;
      this.permissions = [];
      removeToken();
    },
  },
});
