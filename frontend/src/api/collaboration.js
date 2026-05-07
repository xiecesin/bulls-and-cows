import api from './index'

export const collaborationApi = {
  // 获取可用房间列表
  getRooms: () => api.get('/collaboration/rooms'),
  
  // 创建房间
  createRoom: (data) => api.post('/collaboration/rooms', data),
  
  // 加入房间
  joinRoom: (data) => api.post('/collaboration/rooms/join', data),
  
  // 离开房间
  leaveRoom: (roomCode) => api.delete(`/collaboration/rooms/${roomCode}/leave`),
  
  // 开始游戏
  startGame: (roomCode) => api.post(`/collaboration/rooms/${roomCode}/start`),
  
  // 提交猜测
  makeGuess: (roomCode, guess) => api.post(`/collaboration/rooms/${roomCode}/guess`, { guess }),
  
  // 重置游戏
  resetGame: (roomCode) => api.post(`/collaboration/rooms/${roomCode}/reset`),
  
  // 获取房间信息
  getRoomInfo: (roomCode) => api.get(`/collaboration/rooms/${roomCode}`)
}

export default collaborationApi
