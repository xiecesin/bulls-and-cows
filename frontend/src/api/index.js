import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

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
