<template>
  <div class="page-card">
    <h2 class="page-title">🔍 推理解析</h2>
    
    <el-card shadow="hover" class="input-card">
      <template #header>
        <span>输入数字进行解析</span>
      </template>
      <el-form :inline="true" class="input-form">
        <el-form-item label="秘密数字">
          <el-input v-model="secret" maxlength="4" placeholder="如 1123" style="width: 120px" />
        </el-form-item>
        <el-form-item label="猜测数字">
          <el-input v-model="guess" maxlength="4" placeholder="如 0111" style="width: 120px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="analyze" :disabled="!isValidInput">
            分析
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <div v-if="result" class="result-section">
      <el-card shadow="hover" class="result-card">
        <template #header>
          <span>计算结果</span>
        </template>
        <div class="result-display">
          <el-tag type="success" size="large" class="result-tag">
            {{ result.result }}
          </el-tag>
          <p class="result-desc">
            {{ result.bulls }} 个 Bull（数字和位置都对），
            {{ result.cows }} 个 Cow（数字对但位置不对）
          </p>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="reasoning-card">
        <template #header>
          <span>推理过程详解</span>
        </template>
        <el-steps direction="vertical" :space="80" :active="result.reasoning?.length">
          <el-step 
            v-for="step in result.reasoning" 
            :key="step.step"
            :title="step.description"
            :description="step.detail"
          />
        </el-steps>
      </el-card>
      
      <el-card shadow="hover" class="detail-card">
        <template #header>
          <span>逐位对比分析</span>
        </template>
        <div class="comparison">
          <div class="comparison-row">
            <div class="label">秘密数字：</div>
            <div class="digits secret-digits">
              <span 
                v-for="(digit, index) in secret.split('')" 
                :key="'s'+index"
                class="digit"
                :class="{ 'matched': isMatchedSecret(index) }"
              >
                {{ digit }}
              </span>
            </div>
          </div>
          <div class="comparison-row">
            <div class="label">猜测数字：</div>
            <div class="digits guess-digits">
              <span 
                v-for="(digit, index) in guess.split('')" 
                :key="'g'+index"
                class="digit"
                :class="{ 'matched': isMatchedGuess(index) }"
              >
                {{ digit }}
              </span>
            </div>
          </div>
          <div class="comparison-row">
            <div class="label">匹配状态：</div>
            <div class="match-status">
              <span 
                v-for="(digit, index) in guess.split('')" 
                :key="'m'+index"
                class="status"
                :class="getMatchClass(index)"
              >
                {{ getMatchSymbol(index) }}
              </span>
            </div>
          </div>
        </div>
        <div class="legend">
          <span class="legend-item"><span class="symbol bull">🐂</span> Bull（位置正确）</span>
          <span class="legend-item"><span class="symbol cow">🐄</span> Cow（数字正确）</span>
          <span class="legend-item"><span class="symbol miss">❌</span> 不匹配</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { gameApi } from '../api'

const secret = ref('1123')
const guess = ref('0111')
const result = ref(null)

const isValidInput = computed(() => {
  return /^\d{4}$/.test(secret.value) && /^\d{4}$/.test(guess.value)
})

const analyze = async () => {
  if (!isValidInput.value) {
    ElMessage.warning('请输入有效的4位数字')
    return
  }
  
  try {
    const response = await gameApi.calculate(secret.value, guess.value)
    result.value = response.data
  } catch (error) {
    ElMessage.error('分析失败，请检查后端服务')
  }
}

const isMatchedSecret = (index) => {
  return secret.value[index] === guess.value[index]
}

const isMatchedGuess = (index) => {
  return secret.value[index] === guess.value[index]
}

const getMatchClass = (index) => {
  if (secret.value[index] === guess.value[index]) {
    return 'bull'
  }
  
  const digit = guess.value[index]
  const secretDigits = secret.value.split('')
  const guessDigits = guess.value.split('')
  
  let availableSecret = [...secretDigits]
  let availableGuess = [...guessDigits]
  
  for (let i = 0; i < 4; i++) {
    if (i === index) continue
    if (secretDigits[i] === guessDigits[i]) {
      availableSecret[i] = null
      availableGuess[i] = null
    }
  }
  
  const remainingSecret = availableSecret.filter((d, i) => d !== null && i !== index)
  const remainingGuess = availableGuess.filter((d, i) => d !== null && i !== index)
  
  if (remainingSecret.includes(digit)) {
    return 'cow'
  }
  
  return 'miss'
}

const getMatchSymbol = (index) => {
  if (secret.value[index] === guess.value[index]) {
    return '🐂'
  }
  
  const digit = guess.value[index]
  const secretDigits = secret.value.split('')
  const guessDigits = guess.value.split('')
  
  let availableSecret = [...secretDigits]
  let availableGuess = [...guessDigits]
  
  for (let i = 0; i < 4; i++) {
    if (secretDigits[i] === guessDigits[i]) {
      availableSecret[i] = null
      availableGuess[i] = null
    }
  }
  
  const remainingSecret = availableSecret.filter((d, i) => i !== index && d !== null)
  
  if (remainingSecret.includes(digit)) {
    return '🐄'
  }
  
  return '❌'
}
</script>

<style scoped>
.input-card {
  margin-bottom: 24px;
}

.result-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-card {
  text-align: center;
}

.result-display {
  padding: 20px;
}

.result-tag {
  font-size: 28px;
  padding: 12px 24px;
}

.result-desc {
  margin-top: 12px;
  color: #666;
}

.reasoning-card {
  min-height: 300px;
}

.detail-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ed 100%);
}

.comparison {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.comparison-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.label {
  width: 100px;
  font-weight: bold;
  color: #333;
}

.digits, .match-status {
  display: flex;
  gap: 8px;
}

.digit, .status {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.digit.matched {
  background: #f56c6c;
  color: white;
}

.status.bull {
  background: #f56c6c;
  color: white;
}

.status.cow {
  background: #e6a23c;
  color: white;
}

.status.miss {
  background: #909399;
  color: white;
}

.legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid #dcdfe6;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.symbol {
  font-size: 18px;
}
</style>
