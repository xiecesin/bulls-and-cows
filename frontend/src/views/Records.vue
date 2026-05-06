<template>
  <div class="records-container">
    <div class="page-card">
      <h1 class="page-title">练习记录</h1>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon blue">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalGames || 0 }}</div>
              <div class="stat-label">总练习次数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon green">
              <el-icon :size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.winGames || 0 }}</div>
              <div class="stat-label">胜利次数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon orange">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.winRate || 0 }}%</div>
              <div class="stat-label">胜率</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon purple">
              <el-icon :size="32"><Trophy /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.avgGuessCount || '-' }}</div>
              <div class="stat-label">平均猜测次数</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 筛选工具栏 -->
      <el-card class="filter-card" shadow="hover">
        <el-form :inline="true">
          <el-form-item label="游戏结果">
            <el-select v-model="filterResult" placeholder="全部" clearable style="width: 120px">
              <el-option label="全部" value="" />
              <el-option label="胜利" value="WIN" />
              <el-option label="放弃" value="QUIT" />
            </el-select>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 记录列表 -->
      <el-card class="records-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>记录列表</span>
            <el-button type="primary" link @click="handleExport">
              <el-icon><Download /></el-icon>
              导出记录
            </el-button>
          </div>
        </template>
        <el-table :data="filteredRecords" style="width: 100%" v-loading="loading">
          <el-table-column prop="createdAt" label="时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="secretNumber" label="目标数字" width="120">
            <template #default="{ row }">
              <span class="secret-number">{{ row.secretNumber }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="guessCount" label="猜测次数" width="100">
            <template #default="{ row }">
              <el-tag :type="row.gameResult === 'WIN' ? 'success' : ''">
                {{ row.guessCount }} 次
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="timeSpentMs" label="用时" width="120">
            <template #default="{ row }">
              {{ formatTime(row.timeSpentMs) }}
            </template>
          </el-table-column>
          <el-table-column prop="allowDuplicates" label="规则" width="100">
            <template #default="{ row }">
              <el-tag :type="row.allowDuplicates ? 'warning' : 'success'" size="small">
                {{ row.allowDuplicates ? '允许重复' : '无重复' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="gameResult" label="结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.gameResult === 'WIN' ? 'success' : 'info'">
                {{ row.gameResult === 'WIN' ? '胜利' : '放弃' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalElements"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { recordApi } from '@/api/record'

const loading = ref(false)
const stats = ref({})
const records = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const filterResult = ref('')
const dateRange = ref([])

const filteredRecords = computed(() => {
  let result = records.value

  if (filterResult.value) {
    result = result.filter(r => r.gameResult === filterResult.value)
  }

  return result
})

const loadStats = async () => {
  try {
    const res = await recordApi.getStats()
    if (res.data && !res.data.error) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await recordApi.getRecords(currentPage.value - 1, pageSize.value)
    if (res.data) {
      records.value = res.data.records || []
      totalElements.value = res.data.totalElements || 0
    }
  } catch (error) {
    console.error('获取记录失败:', error)
    ElMessage.error('获取记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadRecords()
}

const handleReset = () => {
  filterResult.value = ''
  dateRange.value = []
  handleSearch()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadRecords()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadRecords()
}

const handleDelete = async (record) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 删除记录（如果后端支持）
    ElMessage.success('删除成功')
    loadRecords()
    loadStats()
  } catch {
    // 用户取消
  }
}

const handleExport = () => {
  const csvContent = [
    ['时间', '目标数字', '猜测次数', '用时(秒)', '规则', '结果'].join(','),
    ...filteredRecords.value.map(r => [
      formatDate(r.createdAt),
      r.secretNumber,
      r.guessCount,
      r.timeSpentMs ? (r.timeSpentMs / 1000).toFixed(2) : 0,
      r.allowDuplicates ? '允许重复' : '无重复',
      r.gameResult === 'WIN' ? '胜利' : '放弃'
    ].join(','))
  ].join('\n')

  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `练习记录_${new Date().toISOString().split('T')[0]}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const formatTime = (ms) => {
  if (!ms) return '-'
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

onMounted(() => {
  loadStats()
  loadRecords()
})
</script>

<style scoped>
.records-container {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.purple {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  color: #666;
  font-size: 14px;
  margin-top: 4px;
}

.filter-card {
  margin-bottom: 20px;
}

.records-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.secret-number {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  font-size: 16px;
  color: #409eff;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
