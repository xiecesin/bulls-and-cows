import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 游戏 API
export const gameApi = {
  startGame: () => api.post('/game/start'),
  makeGuess: (gameId, guess) => api.post('/game/guess', { gameId, guess }),
  calculate: (secret, guess) => api.post('/calculate', { secret, guess })
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
