import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 自动添加 Authorization header
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 - 处理 401 未授权
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 游戏 API
export const gameApi = {
  startGame: (allowDuplicates = true) => api.post('/game/start', { allowDuplicates }),
  makeGuess: (gameId, guess) => api.post('/game/guess', { gameId, guess }),
  getAnswer: (gameId) => api.get(`/game/${gameId}/answer`),
  calculate: (secret, guess) => api.post('/calculate', { secret, guess })
}

// 谜题 API
export const puzzleApi = {
  generate: (allowDuplicates = true) => api.post('/puzzle/generate', { allowDuplicates }),
  verify: (puzzleId, answer) => api.post('/puzzle/verify', { puzzleId, answer })
}

// 算法 API
export const algorithmApi = {
  getBasic: (language = 'java') => api.get('/algorithm/basic', { params: { language } }),
  getAdvanced: (language = 'java') => api.get('/algorithm/advanced', { params: { language } }),
  getAll: () => api.get('/algorithms')
}

// 变形 API
export const variantsApi = {
  getAll: () => api.get('/variants'),
  getOne: (id) => api.get(`/variants/${id}`)
}

export default api
