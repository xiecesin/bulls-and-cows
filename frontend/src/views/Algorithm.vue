<template>
  <div class="page-card">
    <h2 class="page-title">💻 算法展示</h2>
    
    <el-tabs v-model="activeLanguage" class="language-tabs">
      <el-tab-pane label="Java" name="java" />
      <el-tab-pane label="Python" name="python" />
    </el-tabs>
    
    <el-tabs v-model="activeAlgorithm" class="algorithm-tabs">
      <el-tab-pane label="基础算法" name="basic">
        <template #label>
          <span><el-icon><Connection /></el-icon> 基础算法</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="优化算法" name="advanced">
        <template #label>
          <span><el-icon><Lightning /></el-icon> 优化算法</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="哈希表算法" name="hash">
        <template #label>
          <span><el-icon><Grid /></el-icon> 哈希表算法</span>
        </template>
      </el-tab-pane>
    </el-tabs>
    
    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      加载中...
    </div>
    
    <div v-else-if="currentAlgorithm" class="algorithm-content">
      <el-card shadow="hover" class="algorithm-info">
        <template #header>
          <span>{{ currentAlgorithm.name }}</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="编程语言">
            <el-tag>{{ currentAlgorithm.language }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="复杂度">
            {{ currentAlgorithm.complexity || '时间: O(n), 空间: O(1)' }}
          </el-descriptions-item>
          <el-descriptions-item label="算法描述" :span="2">
            {{ currentAlgorithm.description }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <el-card shadow="hover" class="code-card">
        <template #header>
          <div class="code-header">
            <span>代码实现</span>
            <el-button type="primary" size="small" @click="copyCode">
              <el-icon><CopyDocument /></el-icon>
              复制代码
            </el-button>
          </div>
        </template>
        <pre class="code-block"><code>{{ currentAlgorithm.code }}</code></pre>
      </el-card>
    </div>
    
    <el-card shadow="hover" class="explanation-card">
      <template #header>
        <span>📖 算法核心思想</span>
      </template>
      <div class="explanation">
        <h4>1. 两遍扫描法（基础算法）</h4>
        <ul>
          <li><strong>第一遍</strong>：逐位比较，统计 Bulls（位置和数字都对）</li>
          <li>同时将秘密数字中非 Bulls 的位置数字加入计数数组</li>
          <li><strong>第二遍</strong>：对猜测数字中非 Bulls 的位置，检查计数数组</li>
          <li>如果计数大于0，说明秘密数字中有未匹配的该数字，统计为 Cow</li>
        </ul>
        
        <h4>2. 关键点</h4>
        <ul>
          <li>使用数组统计0-9数字出现次数，避免重复匹配</li>
          <li>B Bulls 必须先排除，因为它们已经被正确匹配</li>
          <li>Cows 的匹配遵循"谁少谁优先"原则</li>
        </ul>
        
        <h4>3. 复杂度分析</h4>
        <ul>
          <li>时间复杂度：O(n)，只需遍历两遍（n=4固定）</li>
          <li>空间复杂度：O(1)，使用固定大小的数组（长度为10）</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { algorithmApi } from '../api'

const activeLanguage = ref('java')
const activeAlgorithm = ref('basic')
const loading = ref(false)
const algorithms = ref({
  java: {
    basic: null,
    advanced: null,
    hash: null
  },
  python: {
    basic: null
  }
})

const currentAlgorithm = ref(null)

const loadAlgorithm = async () => {
  const key = `${activeLanguage.value}_${activeAlgorithm.value}`
  const cache = algorithms.value[activeLanguage.value]?.[activeAlgorithm.value]
  
  if (cache) {
    currentAlgorithm.value = cache
    return
  }
  
  loading.value = true
  try {
    let response
    if (activeAlgorithm.value === 'hash') {
      response = await algorithmApi.getAll()
      const algo = response.data.algorithms.find(a => a.name.includes('哈希'))
      if (algo) {
        currentAlgorithm.value = algo
        if (!algorithms.value.java.hash) {
          algorithms.value.java.hash = algo
        }
      }
    } else {
      const type = activeAlgorithm.value === 'basic' ? 'basic' : 'advanced'
      response = await algorithmApi.getBasic(activeLanguage.value)
      if (response.data.code) {
        currentAlgorithm.value = response.data
        if (!algorithms.value[activeLanguage.value][type]) {
          algorithms.value[activeLanguage.value][type] = response.data
        }
      } else {
        response = await algorithmApi.getAdvanced(activeLanguage.value)
        currentAlgorithm.value = response.data
        if (!algorithms.value[activeLanguage.value][type]) {
          algorithms.value[activeLanguage.value][type] = response.data
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载算法失败')
  } finally {
    loading.value = false
  }
}

const copyCode = () => {
  if (currentAlgorithm.value?.code) {
    navigator.clipboard.writeText(currentAlgorithm.value.code)
    ElMessage.success('代码已复制到剪贴板')
  }
}

watch([activeLanguage, activeAlgorithm], () => {
  loadAlgorithm()
})

onMounted(() => {
  loadAlgorithm()
})
</script>

<style scoped>
.language-tabs {
  margin-bottom: 16px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.loading .el-icon {
  margin-right: 8px;
}

.algorithm-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.code-card {
  background: #1e1e1e;
}

.code-block {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.explanation-card {
  margin-top: 20px;
}

.explanation h4 {
  color: #409eff;
  margin: 16px 0 8px 0;
}

.explanation ul {
  padding-left: 24px;
  line-height: 1.8;
}

.explanation li {
  margin-bottom: 4px;
}
</style>
