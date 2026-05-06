<template>
  <div class="page-card">
    <h2 class="page-title">🎮 欢迎来到猜数字游戏</h2>
    
    <div class="intro-section">
      <el-card shadow="hover" class="intro-card">
        <template #header>
          <div class="card-header">
            <el-icon size="24"><QuestionFilled /></el-icon>
            <span>什么是猜数字游戏？</span>
          </div>
        </template>
        <p class="intro-text">
          猜数字（Bulls and Cows）是一款经典的益智游戏。系统会随机生成一个秘密数字，
          玩家需要通过有限的猜测次数来猜出这个数字。每次猜测后，系统会给出提示：
        </p>
        <div class="hint-demo">
          <div class="hint-item">
            <el-tag type="danger" size="large">🐂 Bull</el-tag>
            <span>数字和位置都猜对了</span>
          </div>
          <div class="hint-item">
            <el-tag type="warning" size="large">🐄 Cow</el-tag>
            <span>数字猜对了但位置不对</span>
          </div>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="intro-card">
        <template #header>
          <div class="card-header">
            <el-icon size="24"><Star /></el-icon>
            <span>游戏特色</span>
          </div>
        </template>
        <ul class="feature-list">
          <li>
            <el-icon color="#67C23A"><Check /></el-icon>
            <span>适合小学生练习思维逻辑</span>
          </li>
          <li>
            <el-icon color="#67C23A"><Check /></el-icon>
            <span>详细展示推理过程</span>
          </li>
          <li>
            <el-icon color="#67C23A"><Check /></el-icon>
            <span>提供多种编程语言算法</span>
          </li>
          <li>
            <el-icon color="#67C23A"><Check /></el-icon>
            <span>探索经典题目的变形</span>
          </li>
        </ul>
      </el-card>
    </div>
    
    <div class="quick-start">
      <h3>🚀 快速开始</h3>
      <el-space wrap>
        <el-button type="primary" size="large" @click="$router.push('/game')">
          <el-icon><VideoPlay /></el-icon>
          开始练习游戏
        </el-button>
        <el-button size="large" @click="$router.push('/reasoning')">
          <el-icon><Connection /></el-icon>
          查看推理解析
        </el-button>
        <el-button size="large" @click="$router.push('/algorithm')">
          <el-icon><Document /></el-icon>
          学习算法
        </el-button>
      </el-space>
    </div>
    
    <div class="example-section">
      <h3>📖 示例说明</h3>
      <el-card class="example-card">
        <p><strong>秘密数字：</strong>1123</p>
        <p><strong>猜测数字：</strong>0111</p>
        <el-divider />
        <div class="example-analysis">
          <p><strong>分析：</strong></p>
          <ul>
            <li>第1位：秘密是"1"，猜测是"0" → ❌ 不匹配</li>
            <li>第2位：秘密是"1"，猜测是"1" → ✅ 匹配！<el-tag size="small" type="danger">Bull</el-tag></li>
            <li>第3位：秘密是"2"，猜测是"1" → 数字对但位置错 → <el-tag size="small" type="warning">Cow</el-tag></li>
            <li>第4位：秘密是"3"，猜测是"1" → 数字对但位置错 → <el-tag size="small" type="warning">Cow</el-tag></li>
          </ul>
          <p class="result-text">注意：两个不匹配的 1 中，只有一个会算作奶牛</p>
        </div>
        <el-divider />
        <div class="example-result">
          <el-tag type="success" size="large">最终结果：1A1B</el-tag>
          <span class="result-hint">（1个Bull，1个Cow）</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.intro-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.intro-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
}

.intro-text {
  line-height: 1.8;
  color: #666;
  margin-bottom: 20px;
}

.hint-demo {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hint-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feature-list {
  list-style: none;
  padding: 0;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  font-size: 16px;
}

.quick-start {
  margin-bottom: 30px;
}

.quick-start h3 {
  margin-bottom: 16px;
  color: #333;
}

.example-section h3 {
  margin-bottom: 16px;
  color: #333;
}

.example-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.example-analysis ul {
  padding-left: 20px;
  line-height: 2;
}

.example-analysis li {
  color: #555;
}

.result-text {
  color: #909399;
  font-style: italic;
  margin-top: 10px;
}

.example-result {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
}

.result-hint {
  color: #666;
}
</style>
