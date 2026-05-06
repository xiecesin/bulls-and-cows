<template>
  <div class="page-card">
    <h2 class="page-title">📚 变形研究</h2>
    
    <el-card shadow="hover" class="intro-card">
      <p>
        Bulls and Cows（猜数字）是一个经典的益智游戏，有着丰富的变形题目和算法研究。
        下面介绍几种经典的变形和最新研究进展。
      </p>
    </el-card>
    
    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      加载中...
    </div>
    
    <div v-else class="variants-grid">
      <el-card 
        v-for="variant in variants" 
        :key="variant.id"
        shadow="hover"
        class="variant-card"
        :class="{ 'active': selectedVariant?.id === variant.id }"
        @click="selectVariant(variant)"
      >
        <template #header>
          <div class="variant-header">
            <span class="variant-name">{{ variant.name }}</span>
            <el-tag size="small" type="info">{{ variant.keywords?.length || 0 }} 个标签</el-tag>
          </div>
        </template>
        <p class="variant-desc">{{ variant.description }}</p>
        <div class="variant-keywords">
          <el-tag 
            v-for="keyword in variant.keywords" 
            :key="keyword" 
            size="small"
            class="keyword-tag"
          >
            {{ keyword }}
          </el-tag>
        </div>
      </el-card>
    </div>
    
    <el-dialog v-model="dialogVisible" :title="selectedVariant?.name" width="70%" top="5vh">
      <div v-if="selectedVariant" class="variant-detail">
        <el-card shadow="hover" class="detail-section">
          <template #header>
            <span>📝 问题描述</span>
          </template>
          <p>{{ selectedVariant.description }}</p>
        </el-card>
        
        <el-card shadow="hover" class="detail-section">
          <template #header>
            <span>💡 示例</span>
          </template>
          <pre class="example-block">{{ selectedVariant.example }}</pre>
        </el-card>
        
        <el-card shadow="hover" class="detail-section">
          <template #header>
            <span>🔧 解题思路</span>
          </template>
          <p>{{ selectedVariant.solution }}</p>
        </el-card>
        
        <el-card shadow="hover" class="detail-section">
          <template #header>
            <span>🏷️ 关键词</span>
          </template>
          <div class="keywords-list">
            <el-tag 
              v-for="keyword in selectedVariant.keywords" 
              :key="keyword"
              size="large"
              class="keyword-tag"
            >
              {{ keyword }}
            </el-tag>
          </div>
        </el-card>
      </div>
    </el-dialog>
    
    <el-card shadow="hover" class="research-card">
      <template #header>
        <span>🔬 最新研究进展</span>
      </template>
      <div class="research-content">
        <h4>1. AI 智能猜测算法</h4>
        <p>
          近年来，研究者将机器学习应用于猜数字游戏。通过强化学习，AI 可以学习最优猜测策略，
          平均在 5-6 次内猜出 4 位数字。关键算法包括：
        </p>
        <ul>
          <li><strong>Minimax 搜索</strong>：每次选择能最大化最小信息增益的猜测</li>
          <li><strong>候选集剪枝</strong>：根据反馈快速排除不可能的答案</li>
          <li><strong>信息熵优化</strong>：选择能最大程度减少不确定性的猜测</li>
        </ul>
        
        <h4>2. 多人协作模式</h4>
        <p>
          社交版本的猜数字游戏允许多个玩家同时猜测并共享结果。这种模式下的算法需要考虑：
        </p>
        <ul>
          <li>如何高效合并多个玩家的反馈信息</li>
          <li>如何在保密和协作之间取得平衡</li>
          <li>分布式计算在约束满足问题中的应用</li>
        </ul>
        
        <h4>3. 变体扩展研究</h4>
        <p>
          研究者还在探索更多有趣的变形：
        </p>
        <ul>
          <li>彩色序列版（Mastermind 的现代变体）</li>
          <li>时间限制版（添加计时压力）</li>
          <li>作弊模式（部分信息泄露）</li>
          <li>区块链版本（验证公平性）</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { variantsApi } from '../api'

const loading = ref(false)
const variants = ref([])
const selectedVariant = ref(null)
const dialogVisible = ref(false)

const loadVariants = async () => {
  loading.value = true
  try {
    const response = await variantsApi.getAll()
    variants.value = response.data.variants
  } catch (error) {
    ElMessage.error('加载变形题目失败')
  } finally {
    loading.value = false
  }
}

const selectVariant = (variant) => {
  selectedVariant.value = variant
  dialogVisible.value = true
}

onMounted(() => {
  loadVariants()
})
</script>

<style scoped>
.intro-card {
  margin-bottom: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.intro-card p {
  font-size: 16px;
  line-height: 1.8;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.variants-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.variant-card {
  cursor: pointer;
  transition: all 0.3s;
}

.variant-card:hover {
  transform: translateY(-5px);
}

.variant-card.active {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.variant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.variant-name {
  font-weight: bold;
  font-size: 16px;
}

.variant-desc {
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
  min-height: 60px;
}

.variant-keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  margin-right: 4px;
}

.variant-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-section {
  background: #fafafa;
}

.example-block {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  font-family: 'Courier New', Consolas, monospace;
  white-space: pre-wrap;
  margin: 0;
}

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.research-content h4 {
  color: #409eff;
  margin: 20px 0 10px 0;
}

.research-content h4:first-child {
  margin-top: 0;
}

.research-content p {
  line-height: 1.8;
  color: #666;
}

.research-content ul {
  padding-left: 24px;
  line-height: 2;
}

.research-content li {
  color: #555;
}
</style>
