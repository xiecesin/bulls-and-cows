import api from './index'

// 管理后台相关 API
export const adminApi = {
  // 获取统计数据
  getStats: () => api.get('/admin/stats'),
  
  // 用户管理
  getUsers: (params) => api.get('/admin/users', { params }),
  getUser: (id) => api.get(`/admin/users/${id}`),
  createUser: (data) => api.post('/admin/users', data),
  updateUser: (id, data) => api.put(`/admin/users/${id}`, data),
  deleteUser: (id) => api.delete(`/admin/users/${id}`),
  toggleUserStatus: (id) => api.post(`/admin/users/${id}/toggle-status`),
  
  // 系统设置
  getSettings: () => api.get('/admin/settings'),
  updateSettings: (data) => api.put('/admin/settings', data)
}

// 系统监控 API
export const monitorApi = {
  // 获取系统信息
  getSystemInfo: () => api.get('/admin/monitor/system'),
  
  // 获取 API 统计
  getApiStats: () => api.get('/admin/monitor/api'),
  
  // 获取日志
  getLogs: (level) => api.get('/admin/monitor/logs', { params: { level } }),
  
  // 获取性能指标
  getMetrics: () => api.get('/admin/monitor/metrics'),
  
  // 获取在线用户
  getOnlineUsers: () => api.get('/admin/monitor/online-users')
}

export default { adminApi, monitorApi }
