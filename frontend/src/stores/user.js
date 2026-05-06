import { defineStore } from 'pinia';
import { authApi } from '@/api/auth';

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    refreshToken: localStorage.getItem('refreshToken') || null,
    isLoggedIn: false
  }),
  
  getters: {
    isAdmin: (state) => state.user?.role === 'ADMIN',
    username: (state) => state.user?.username || '',
    nickname: (state) => state.user?.nickname || state.user?.username || '',
    avatarUrl: (state) => state.user?.avatarUrl || ''
  },
  
  actions: {
    // 初始化用户状态
    initUser() {
      const token = localStorage.getItem('token');
      if (token) {
        this.token = token;
        this.refreshToken = localStorage.getItem('refreshToken');
        this.isLoggedIn = true;
        this.fetchUserInfo();
      }
    },
    
    // 获取用户信息
    async fetchUserInfo() {
      try {
        const res = await authApi.getCurrentUser();
        if (res.data) {
          this.user = res.data;
          this.isLoggedIn = true;
        }
      } catch (error) {
        console.error('获取用户信息失败:', error);
        this.logout();
      }
    },
    
    // 登录
    async login(username, password) {
      try {
        const res = await authApi.login({ username, password });
        if (res.data.success) {
          const { token, refreshToken, user } = res.data.data;
          this.token = token;
          this.refreshToken = refreshToken;
          this.user = user;
          this.isLoggedIn = true;
          localStorage.setItem('token', token);
          localStorage.setItem('refreshToken', refreshToken);
          return { success: true };
        }
        return { success: false, message: res.data.message };
      } catch (error) {
        return { 
          success: false, 
          message: error.response?.data?.message || '登录失败' 
        };
      }
    },
    
    // 注册
    async register(data) {
      try {
        const res = await authApi.register(data);
        if (res.data.success) {
          const { token, refreshToken, user } = res.data.data;
          this.token = token;
          this.refreshToken = refreshToken;
          this.user = user;
          this.isLoggedIn = true;
          localStorage.setItem('token', token);
          localStorage.setItem('refreshToken', refreshToken);
          return { success: true };
        }
        return { success: false, message: res.data.message };
      } catch (error) {
        return { 
          success: false, 
          message: error.response?.data?.message || '注册失败' 
        };
      }
    },
    
    // 退出登录
    async logout() {
      try {
        await authApi.logout();
      } catch (error) {
        console.error('退出登录失败:', error);
      } finally {
        this.token = null;
        this.refreshToken = null;
        this.user = null;
        this.isLoggedIn = false;
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
      }
    }
  }
});
