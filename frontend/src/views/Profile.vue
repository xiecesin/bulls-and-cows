<template>
  <div class="profile-container">
    <div class="page-card">
      <h1 class="page-title">个人中心</h1>

      <!-- 用户信息卡片 -->
      <el-card class="user-info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-button type="primary" link @click="handleEdit">编辑资料</el-button>
          </div>
        </template>
        <div class="user-info">
          <el-avatar :size="80" :src="userStore.avatarUrl">
            {{ userStore.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="user-details">
            <h2>{{ userStore.nickname || userStore.username }}</h2>
            <p class="user-email">{{ userStore.email }}</p>
            <el-tag v-if="userStore.isAdmin" type="danger" size="small">管理员</el-tag>
          </div>
        </div>
      </el-card>

      <!-- 统计数据 -->
      <el-card class="stats-card" shadow="hover">
        <template #header>
          <span>练习统计</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ stats.totalGames || 0 }}</div>
              <div class="stat-label">总练习次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value success">{{ stats.winGames || 0 }}</div>
              <div class="stat-label">胜利次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ stats.winRate || 0 }}%</div>
              <div class="stat-label">胜率</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value warning">{{ stats.bestGuessCount || '-' }}</div>
              <div class="stat-label">最佳成绩</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 快速链接 -->
      <el-card class="quick-links" shadow="hover">
        <template #header>
          <span>快捷功能</span>
        </template>
        <el-space wrap>
          <el-button type="primary" @click="$router.push('/game')">
            <el-icon><VideoPlay /></el-icon>
            开始练习
          </el-button>
          <el-button type="success" @click="$router.push('/records')">
            <el-icon><History /></el-icon>
            查看记录
          </el-button>
          <el-button type="warning" @click="$router.push('/ranking')">
            <el-icon><Trophy /></el-icon>
            排行榜
          </el-button>
        </el-space>
      </el-card>

      <!-- 最近记录 -->
      <el-card class="recent-records" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>最近记录</span>
            <el-button type="primary" link @click="$router.push('/records')">
              查看全部
            </el-button>
          </div>
        </template>
        <el-table :data="recentRecords" style="width: 100%">
          <el-table-column prop="createdAt" label="时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="secretNumber" label="目标数字" width="120" />
          <el-table-column prop="guessCount" label="猜测次数" width="100" />
          <el-table-column prop="timeSpentMs" label="用时" width="100">
            <template #default="{ row }">
              {{ formatTime(row.timeSpentMs) }}
            </template>
          </el-table-column>
          <el-table-column prop="gameResult" label="结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.gameResult === 'WIN' ? 'success' : 'info'">
                {{ row.gameResult === 'WIN' ? '胜利' : '放弃' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { recordApi } from '@/api/record'

const userStore = useUserStore()

const stats = ref({})
const recentRecords = ref([])
const editDialogVisible = ref(false)
const editForm = ref({
  nickname: '',
  email: ''
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

const loadRecentRecords = async () => {
  try {
    const res = await recordApi.getRecords(0, 5)
    if (res.data && res.data.records) {
      recentRecords.value = res.data.records
    }
  } catch (error) {
    console.error('获取最近记录失败:', error)
  }
}

const handleEdit = () => {
  editForm.value = {
    nickname: userStore.nickname || '',
    email: userStore.email || ''
  }
  editDialogVisible.value = true
}

const handleSaveEdit = async () => {
  try {
    await userStore.updateProfile(editForm.value)
    ElMessage.success('资料更新成功')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error('更新失败')
  }
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
  loadRecentRecords()
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

.user-info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-details h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
}

.user-email {
  color: #666;
  margin-bottom: 8px;
}

.stats-card {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-label {
  margin-top: 8px;
  color: #666;
  font-size: 14px;
}

.quick-links {
  margin-bottom: 20px;
}

.recent-records {
  margin-bottom: 20px;
}
</style>
