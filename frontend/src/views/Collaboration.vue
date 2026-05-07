<template>
  <div class="page-card">
    <h2 class="page-title">👥 多人协作</h2>
    
    <el-alert
      title="协作功能"
      type="info"
      :closable="false"
      show-icon
      class="info-alert"
    >
      <template #default>
        <p>多人协作模式允许多名玩家同时参与猜数字游戏，共享推理信息，比拼谁先猜出答案！</p>
      </template>
    </el-alert>
    
    <div v-if="!isInRoom">
      <!-- 创建/加入房间 -->
      <el-card shadow="hover" class="action-card">
        <template #header>
          <span>加入游戏</span>
        </template>
        
        <el-tabs v-model="activeTab">
          <el-tab-pane label="创建房间" name="create">
            <div class="create-form">
              <el-form :model="createForm" label-width="100px">
                <el-form-item label="房间名称">
                  <el-input v-model="createForm.roomName" placeholder="给房间起个名字" />
                </el-form-item>
                <el-form-item label="玩家昵称">
                  <el-input v-model="createForm.playerName" placeholder="你的昵称" />
                </el-form-item>
                <el-form-item label="允许重复">
                  <el-switch v-model="createForm.allowDuplicates" />
                </el-form-item>
              </el-form>
              <el-button type="primary" size="large" @click="createRoom" :loading="loading">
                创建房间
              </el-button>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="加入房间" name="join">
            <div class="join-form">
              <el-form :model="joinForm" label-width="100px">
                <el-form-item label="房间号">
                  <el-input v-model="joinForm.roomCode" placeholder="输入房间号" />
                </el-form-item>
                <el-form-item label="玩家昵称">
                  <el-input v-model="joinForm.playerName" placeholder="你的昵称" />
                </el-form-item>
              </el-form>
              <el-button type="success" size="large" @click="joinRoom" :loading="loading">
                加入房间
              </el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
      
      <!-- 当前房间列表 -->
      <el-card shadow="hover" class="rooms-card">
        <template #header>
          <span>当前开放的房间</span>
        </template>
        <el-table :data="availableRooms" v-loading="loadingRooms">
          <el-table-column prop="roomCode" label="房间号" width="120" />
          <el-table-column prop="roomName" label="房间名" />
          <el-table-column prop="playerCount" label="玩家数" width="100" align="center" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'WAITING' ? 'success' : 'warning'" size="small">
                {{ row.status === 'WAITING' ? '等待中' : '游戏中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="quickJoin(row)">
                加入
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="refresh-btn">
          <el-button @click="loadRooms" size="small">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </el-card>
    </div>
    
    <div v-else class="game-room">
      <!-- 房间信息 -->
      <el-card shadow="hover" class="room-info-card">
        <div class="room-header">
          <div class="room-info">
            <h3>房间号：{{ currentRoom?.roomCode }}</h3>
            <p>{{ currentRoom?.roomName }}</p>
          </div>
          <div class="room-actions">
            <el-tag type="info">当前回合：第 {{ currentTurn }} 回合</el-tag>
            <el-button type="danger" @click="leaveRoom">离开房间</el-button>
          </div>
        </div>
      </el-card>
      
      <!-- 玩家列表 -->
      <el-card shadow="hover" class="players-card">
        <template #header>
          <span>玩家列表</span>
        </template>
        <div class="players-grid">
          <div 
            v-for="player in players" 
            :key="player.id"
            class="player-item"
            :class="{ 'active': player.id === currentPlayerId, 'winner': player.hasWon }"
          >
            <el-avatar :size="48">{{ player.name?.charAt(0) || 'P' }}</el-avatar>
            <div class="player-info">
              <span class="player-name">{{ player.name }}</span>
              <span class="player-score">{{ player.guessCount }} 次猜测</span>
            </div>
            <el-tag v-if="player.id === currentPlayerId" type="primary" size="small">你</el-tag>
            <el-tag v-if="player.hasWon" type="success" size="small">🏆 胜利</el-tag>
          </div>
        </div>
      </el-card>
      
      <!-- 游戏区域 -->
      <el-card shadow="hover" class="game-card">
        <template #header>
          <span>秘密数字：{{ gameStatus === 'ENDED' ? secretNumber : '????' }}</span>
        </template>
        
        <div v-if="gameStatus === 'WAITING'" class="waiting-section">
          <el-empty description="等待其他玩家加入...">
            <el-button type="primary" @click="startGame" v-if="isHost">
              开始游戏
            </el-button>
          </el-empty>
        </div>
        
        <div v-else-if="gameStatus === 'PLAYING'" class="playing-section">
          <div class="guess-input-area">
            <p class="turn-info">{{ isMyTurn ? '轮到你了！' : '等待其他玩家...' }}</p>
            <div class="input-row">
              <el-input
                v-model="myGuess"
                maxlength="4"
                placeholder="输入4位数字"
                :disabled="!isMyTurn || gameStatus !== 'PLAYING'"
                size="large"
                class="guess-input"
              >
              </el-input>
              <el-button 
                type="primary" 
                size="large" 
                @click="submitGuess"
                :disabled="!isMyTurn || myGuess.length !== 4"
              >
                猜测
              </el-button>
            </div>
          </div>
          
          <!-- 历史猜测 -->
          <div class="history-section">
            <h4>所有玩家猜测记录</h4>
            <div v-for="player in players" :key="player.id" class="player-history">
              <div class="history-header">
                <span>{{ player.name }}</span>
                <el-tag v-if="player.lastResult" size="small">
                  {{ player.lastResult.bulls }}A{{ player.lastResult.cows }}B
                </el-tag>
              </div>
              <div class="history-list">
                <span 
                  v-for="(guess, idx) in player.guesses" 
                  :key="idx"
                  class="history-item"
                  :class="{ 'win': player.hasWon && idx === player.guesses.length - 1 }"
                >
                  {{ guess }}
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="gameStatus === 'ENDED'" class="ended-section">
          <el-result
            :icon="winner ? 'success' : 'info'"
            :title="winner ? `${winner.name} 获胜！` : '游戏结束'"
            :sub-title="winner ? `用了 ${winner.guessCount} 次猜测` : ''"
          >
            <template #extra>
              <el-button type="primary" @click="playAgain">再来一局</el-button>
            </template>
          </el-result>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { collaborationApi } from '@/api/collaboration'

const loading = ref(false)
const loadingRooms = ref(false)
const activeTab = ref('create')
const isInRoom = ref(false)
const currentRoom = ref(null)
const players = ref([])
const gameStatus = ref('WAITING')
const currentTurn = ref(1)
const currentPlayerId = ref('')
const secretNumber = ref('')
const myGuess = ref('')

// 模拟数据
const availableRooms = ref([
  { roomCode: 'ABCD', roomName: '练习房间', playerCount: 2, status: 'WAITING' },
  { roomCode: '1234', roomName: '比赛房间', playerCount: 3, status: 'PLAYING' }
])

const createForm = ref({
  roomName: '',
  playerName: '',
  allowDuplicates: true
})

const joinForm = ref({
  roomCode: '',
  playerName: ''
})

const isHost = computed(() => {
  return players.value.find(p => p.id === currentPlayerId.value)?.isHost
})

const isMyTurn = computed(() => {
  return players.value.find(p => p.id === currentPlayerId.value)?.isMyTurn
})

const winner = computed(() => {
  return players.value.find(p => p.hasWon)
})

const loadRooms = async () => {
  loadingRooms.value = true
  try {
    const res = await collaborationApi.getRooms()
    if (res.data?.rooms) {
      availableRooms.value = res.data.rooms
    }
  } catch (error) {
    console.error('获取房间列表失败')
  } finally {
    loadingRooms.value = false
  }
}

const createRoom = async () => {
  if (!createForm.value.roomName || !createForm.value.playerName) {
    ElMessage.warning('请填写房间名称和昵称')
    return
  }
  loading.value = true
  try {
    const res = await collaborationApi.createRoom({
      roomName: createForm.value.roomName,
      playerName: createForm.value.playerName,
      allowDuplicates: createForm.value.allowDuplicates
    })
    currentRoom.value = res.data.room
    players.value = res.data.players
    currentPlayerId.value = res.data.playerId
    isInRoom.value = true
    gameStatus.value = 'WAITING'
    ElMessage.success('房间创建成功！')
  } catch (error) {
    ElMessage.error('创建房间失败')
  } finally {
    loading.value = false
  }
}

const joinRoom = async () => {
  if (!joinForm.value.roomCode || !joinForm.value.playerName) {
    ElMessage.warning('请填写房间号和昵称')
    return
  }
  loading.value = true
  try {
    const res = await collaborationApi.joinRoom({
      roomCode: joinForm.value.roomCode,
      playerName: joinForm.value.playerName
    })
    currentRoom.value = res.data.room
    players.value = res.data.players
    currentPlayerId.value = res.data.playerId
    gameStatus.value = res.data.room.status
    isInRoom.value = true
    ElMessage.success('加入房间成功！')
  } catch (error) {
    ElMessage.error('加入房间失败，请检查房间号是否正确')
  } finally {
    loading.value = false
  }
}

const quickJoin = (room) => {
  joinForm.value.roomCode = room.roomCode
  activeTab.value = 'join'
  joinRoom()
}

const leaveRoom = async () => {
  try {
    await collaborationApi.leaveRoom(currentRoom.value.roomCode)
  } catch (error) {
    console.error('离开房间失败')
  }
  isInRoom.value = false
  currentRoom.value = null
  players.value = []
  gameStatus.value = 'WAITING'
  ElMessage.info('已离开房间')
}

const startGame = async () => {
  try {
    const res = await collaborationApi.startGame(currentRoom.value.roomCode)
    gameStatus.value = 'PLAYING'
    secretNumber.value = res.data.secretNumber
    ElMessage.success('游戏开始！')
  } catch (error) {
    ElMessage.error('开始游戏失败')
  }
}

const submitGuess = async () => {
  if (myGuess.value.length !== 4) {
    ElMessage.warning('请输入4位数字')
    return
  }
  try {
    const res = await collaborationApi.makeGuess(currentRoom.value.roomCode, myGuess.value)
    const myPlayer = players.value.find(p => p.id === currentPlayerId.value)
    if (myPlayer) {
      myPlayer.guesses.push(myGuess.value)
      myPlayer.lastResult = res.data
      myPlayer.guessCount++
      if (res.data.bulls === 4) {
        myPlayer.hasWon = true
        gameStatus.value = 'ENDED'
      }
    }
    myGuess.value = ''
  } catch (error) {
    ElMessage.error('提交猜测失败')
  }
}

const playAgain = async () => {
  try {
    await collaborationApi.resetGame(currentRoom.value.roomCode)
    players.value.forEach(p => {
      p.guesses = []
      p.guessCount = 0
      p.lastResult = null
      p.hasWon = false
    })
    gameStatus.value = 'WAITING'
    secretNumber.value = ''
  } catch (error) {
    ElMessage.error('重置游戏失败')
  }
}

onMounted(() => {
  loadRooms()
})

onUnmounted(() => {
  if (isInRoom.value) {
    leaveRoom()
  }
})
</script>

<style scoped>
.info-alert {
  margin-bottom: 24px;
}

.info-alert p {
  margin: 0;
  line-height: 1.6;
}

.action-card {
  margin-bottom: 24px;
}

.create-form,
.join-form {
  padding: 20px 0;
}

.create-form .el-button,
.join-form .el-button {
  width: 100%;
  max-width: 300px;
  margin-top: 20px;
}

.rooms-card {
  margin-bottom: 24px;
}

.refresh-btn {
  margin-top: 16px;
  text-align: right;
}

.game-room {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-info h3 {
  margin: 0;
  font-size: 18px;
}

.room-info p {
  margin: 4px 0 0 0;
  color: #666;
}

.room-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.players-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.player-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.player-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.player-item.winner {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.player-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.player-name {
  font-weight: bold;
}

.player-score {
  font-size: 12px;
  opacity: 0.8;
}

.waiting-section {
  padding: 40px;
  text-align: center;
}

.playing-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.guess-input-area {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
}

.turn-info {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 16px;
}

.input-row {
  display: flex;
  gap: 12px;
  justify-content: center;
  max-width: 400px;
  margin: 0 auto;
}

.guess-input {
  flex: 1;
  max-width: 200px;
}

.guess-input :deep(.el-input__inner) {
  text-align: center;
  letter-spacing: 8px;
  font-size: 20px;
}

.history-section h4 {
  margin-bottom: 12px;
  color: #333;
}

.player-history {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-weight: bold;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-item {
  padding: 4px 12px;
  background: #e0e0e0;
  border-radius: 4px;
  font-family: monospace;
}

.history-item.win {
  background: #67c23a;
  color: white;
}

.ended-section {
  padding: 40px;
}
</style>
