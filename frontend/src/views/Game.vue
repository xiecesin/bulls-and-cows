<template>
  <div class="page-card">
    <h2 class="page-title">🎮 猜数字练习</h2>
    
    <div v-if="!gameStarted">
      <el-alert
        title="游戏规则"
        type="info"
        :closable="false"
        show-icon
        class="rules-alert"
      >
        <template #default>
          <ul class="rules-list">
            <li>系统会随机生成一个4位数字（可能有重复数字）</li>
            <li>每次猜测后，系统会告诉你 Bulls 和 Cows 的数量</li>
            <li><strong>Bulls</strong>：数字和位置都对了</li>
            <li><strong>Cows</strong>：数字对了但位置不对</li>
            <li>尝试用最少的次数猜出正确答案！</li>
          </ul>
        </template>
      </el-alert>
      
      <div class="start-options">
        <div class="option-item">
          <span class="option-label">允许重复数字：</span>
          <el-switch v-model="allowDuplicates" />
        </div>
      </div>
      
      <div class="start-section">
        <el-button type="primary" size="large" @click="startGame" :loading="loading">
          <el-icon><VideoPlay /></el-icon>
          开始新游戏
        </el-button>
      </div>
    </div>
    
    <div v-else class="game-section">
      <div class="game-header">
        <el-tag type="info" size="large">猜测次数：{{ guessCount }}</el-tag>
        <div class="header-actions">
          <el-button @click="showAnswer" type="warning" plain>查看答案</el-button>
          <el-button @click="resetGame">重新开始</el-button>
        </div>
      </div>
      
      <div class="guess-input">
        <el-input
          v-model="currentGuess"
          maxlength="4"
          placeholder="输入4位数字"
          :disabled="gameWon"
          size="large"
          class="guess-input-field"
          @keyup.enter="submitGuess"
        >
          <template #append>
            <el-button @click="submitGuess" :disabled="currentGuess.length !== 4 || gameWon">
              猜测
            </el-button>
          </template>
        </el-input>
        <div class="input-hint">请输入4位数字（可包含重复数字，如 1123）</div>
      </div>
      
      <div v-if="lastResult" class="result-display">
        <el-tag :type="gameWon ? 'success' : 'warning'" size="large" class="result-tag">
          {{ lastResult.bulls }}A {{ lastResult.cows }}B
        </el-tag>
        <span v-if="gameWon" class="win-message">🎉 恭喜你猜对了！用了 {{ guessCount }} 次</span>
      </div>
      
      <div class="history-section" v-if="history.length > 0">
        <h4>猜测历史</h4>
        <el-table :data="history" stripe style="width: 100%">
          <el-table-column prop="guess" label="猜测" width="120" align="center">
            <template #default="{ row }">
              <span class="guess-number">{{ row.guess }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="bulls" label="Bulls" width="100" align="center">
            <template #default="{ row }">
              <el-tag type="danger">{{ row.bulls }}A</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="cows" label="Cows" width="100" align="center">
            <template #default="{ row }">
              <el-tag type="warning">{{ row.cows }}B</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="时间" align="center">
            <template #default="{ row }">
              {{ row.time }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { gameApi } from '../api'

const loading = ref(false)
const gameStarted = ref(false)
const gameId = ref('')
const currentGuess = ref('')
const history = ref([])
const lastResult = ref(null)
const guessCount = ref(0)
const allowDuplicates = ref(true)

const gameWon = computed(() => lastResult.value?.bulls === 4)

const startGame = async () => {
  loading.value = true
  try {
    const response = await gameApi.startGame(allowDuplicates.value)
    gameId.value = response.data.gameId
    gameStarted.value = true
    history.value = []
    lastResult.value = null
    guessCount.value = 0
    currentGuess.value = ''
    ElMessage.success(response.data.message)
  } catch (error) {
    ElMessage.error('游戏初始化失败，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}

const showAnswer = async () => {
  try {
    const response = await gameApi.getAnswer(gameId.value)
    ElMessage.info('答案是：' + response.data.answer)
  } catch (error) {
    ElMessage.error('获取答案失败')
  }
}

const submitGuess = async () => {
  if (currentGuess.value.length !== 4) {
    ElMessage.warning('请输入4位数字')
    return
  }
  
  if (!/^\d+$/.test(currentGuess.value)) {
    ElMessage.warning('请只输入数字')
    return
  }
  
  try {
    const response = await gameApi.makeGuess(gameId.value, currentGuess.value)
    const data = response.data
    
    if (data.error) {
      ElMessage.error(data.error)
      return
    }
    
    guessCount.value = data.guessCount
    lastResult.value = {
      bulls: data.bulls,
      cows: data.cows,
      result: data.result
    }
    
    history.value.unshift({
      guess: currentGuess.value,
      bulls: data.bulls,
      cows: data.cows,
      time: new Date().toLocaleTimeString()
    })
    
    if (data.success) {
      ElMessage.success(data.message)
    }
    
    currentGuess.value = ''
  } catch (error) {
    ElMessage.error('提交猜测失败，请重试')
  }
}

const resetGame = () => {
  startGame()
}
</script>

<style scoped>
.rules-alert {
  margin-bottom: 24px;
}

.rules-list {
  padding-left: 20px;
  line-height: 2;
}

.start-section {
  text-align: center;
  padding: 20px 0 40px;
}

.start-options {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.option-label {
  font-size: 14px;
  color: #606266;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.game-section {
  max-width: 600px;
  margin: 0 auto;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.guess-input {
  margin-bottom: 24px;
}

.guess-input-field {
  font-size: 24px;
}

.guess-input-field :deep(.el-input__inner) {
  text-align: center;
  letter-spacing: 8px;
}

.input-hint {
  text-align: center;
  color: #909399;
  margin-top: 8px;
  font-size: 14px;
}

.result-display {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ed 100%);
  border-radius: 12px;
  margin-bottom: 24px;
}

.result-tag {
  font-size: 24px;
  padding: 8px 20px;
}

.win-message {
  display: block;
  margin-top: 12px;
  font-size: 18px;
  color: #67c23a;
  font-weight: bold;
}

.history-section {
  margin-top: 30px;
}

.history-section h4 {
  margin-bottom: 12px;
  color: #333;
}

.guess-number {
  font-family: 'Courier New', monospace;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 4px;
}
</style>
