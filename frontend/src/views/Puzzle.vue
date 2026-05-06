<template>
  <div class="page-card">
    <h2 class="page-title">🧩 推理练习</h2>
    
    <div v-if="!puzzleStarted">
      <el-alert
        title="推理练习规则"
        type="info"
        :closable="false"
        show-icon
        class="rules-alert"
      >
        <template #default>
          <ul class="rules-list">
            <li>系统会给出一些历史猜测记录和对应的 Bulls/Cows 结果</li>
            <li>你需要根据这些信息，推断出秘密数字是什么</li>
            <li>每条记录包含猜测数字和反馈结果</li>
            <li>点击"查看答案"可随时查看正确答案</li>
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
        <el-button type="primary" size="large" @click="generatePuzzle" :loading="loading">
          <el-icon><MagicStick /></el-icon>
          生成推理题
        </el-button>
      </div>
    </div>
    
    <div v-else class="puzzle-section">
      <div class="puzzle-header">
        <el-tag type="info" size="large">
          {{ puzzleData.history.length }} 条记录待分析
        </el-tag>
        <div class="header-actions">
          <el-button @click="showAnswer" type="warning" plain>查看答案</el-button>
          <el-button @click="generatePuzzle" type="success">换一道</el-button>
        </div>
      </div>
      
      <div class="history-section">
        <h4>历史记录</h4>
        <el-table :data="puzzleData.history" stripe style="width: 100%">
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
          <el-table-column prop="hint" label="结果" align="center">
            <template #default="{ row }">
              <span class="hint-text">{{ row.hint }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="answer-section">
        <h4>输入你的答案</h4>
        <div class="answer-input">
          <el-input
            v-model="userAnswer"
            maxlength="4"
            placeholder="输入4位数字"
            size="large"
            class="answer-input-field"
            @keyup.enter="submitAnswer"
          >
          </el-input>
          <el-button type="primary" size="large" @click="submitAnswer" :disabled="userAnswer.length !== 4">
            提交答案
          </el-button>
        </div>
      </div>
      
      <div v-if="answerFeedback" class="feedback-display">
        <el-result
          :icon="answerFeedback.correct ? 'success' : 'warning'"
          :title="answerFeedback.correct ? '回答正确！' : '回答错误'"
        >
          <template #sub-title>
            <p>{{ answerFeedback.message }}</p>
            <p v-if="!answerFeedback.correct" class="correct-answer">
              正确答案是：<strong>{{ answerFeedback.secret }}</strong>
            </p>
          </template>
        </el-result>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { puzzleApi } from '../api'

const loading = ref(false)
const puzzleStarted = ref(false)
const puzzleData = ref({})
const puzzleId = ref('')
const userAnswer = ref('')
const answerFeedback = ref(null)
const allowDuplicates = ref(true)

const generatePuzzle = async () => {
  loading.value = true
  answerFeedback.value = null
  userAnswer.value = ''
  try {
    const response = await puzzleApi.generate(allowDuplicates.value)
    puzzleData.value = response.data
    puzzleId.value = response.data.puzzleId
    puzzleStarted.value = true
  } catch (error) {
    ElMessage.error('生成题目失败，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}

const submitAnswer = async () => {
  if (userAnswer.value.length !== 4) {
    ElMessage.warning('请输入4位数字')
    return
  }
  
  if (!/^\d+$/.test(userAnswer.value)) {
    ElMessage.warning('请只输入数字')
    return
  }
  
  try {
    const response = await puzzleApi.verify(puzzleId.value, userAnswer.value)
    answerFeedback.value = response.data
    if (response.data.correct) {
      ElMessage.success('恭喜你答对了！')
    } else {
      ElMessage.error('回答错误，再想想吧！')
    }
  } catch (error) {
    ElMessage.error('验证答案失败')
  }
}

const showAnswer = async () => {
  try {
    const response = await puzzleApi.verify(puzzleId.value, '0000')
    ElMessage.info('答案是：' + response.data.secret)
  } catch (error) {
    ElMessage.error('获取答案失败')
  }
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

.puzzle-section {
  max-width: 600px;
  margin: 0 auto;
}

.puzzle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.history-section {
  margin-bottom: 30px;
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

.hint-text {
  font-weight: bold;
  color: #409EFF;
}

.answer-section {
  margin-top: 30px;
}

.answer-section h4 {
  margin-bottom: 12px;
  color: #333;
}

.answer-input {
  display: flex;
  gap: 12px;
}

.answer-input-field {
  flex: 1;
  font-size: 24px;
}

.answer-input-field :deep(.el-input__inner) {
  text-align: center;
  letter-spacing: 8px;
}

.feedback-display {
  margin-top: 30px;
}

.correct-answer {
  color: #67c23a;
  font-size: 16px;
  margin-top: 10px;
}
</style>
