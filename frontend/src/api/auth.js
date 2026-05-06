import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// 创建 axios 实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器：添加 Token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器：处理 Token 过期
api.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      // Token 过期，尝试刷新
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const res = await axios.post(`${API_BASE_URL}/auth/refresh`, { refreshToken });
          if (res.data.success) {
            localStorage.setItem('token', res.data.data.token);
            localStorage.setItem('refreshToken', res.data.data.refreshToken);
            // 重试原请求
            error.config.headers.Authorization = `Bearer ${res.data.data.token}`;
            return api.request(error.config);
          }
        } catch (e) {
          // 刷新失败，清除 Token
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
        }
      } else {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// 认证 API
export const authApi = {
  // 注册
  register(data) {
    return api.post('/auth/register', data);
  },
  
  // 登录
  login(data) {
    return api.post('/auth/login', data);
  },
  
  // 刷新 Token
  refresh(refreshToken) {
    return api.post('/auth/refresh', { refreshToken });
  },
  
  // 退出登录
  logout() {
    return api.post('/auth/logout');
  },
  
  // 获取当前用户信息
  getCurrentUser() {
    return api.get('/auth/me');
  }
};

// 用户 API
export const userApi = {
  // 获取当前用户信息
  getMe() {
    return api.get('/users/me');
  },
  
  // 更新用户信息
  updateMe(data) {
    return api.put('/users/me', data);
  },
  
  // 修改密码
  updatePassword(data) {
    return api.put('/users/me/password', data);
  }
};

export default api;
