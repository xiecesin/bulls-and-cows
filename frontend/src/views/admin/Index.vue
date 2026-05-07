<template>
  <div class="page-card">
    <h2 class="page-title">⚙️ 管理后台</h2>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon blue">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon green">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalGames || 0 }}</div>
            <div class="stat-label">总游戏数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon orange">
            <el-icon :size="32"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.todayGames || 0 }}</div>
            <div class="stat-label">今日游戏</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon purple">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.onlineUsers || 0 }}</div>
            <div class="stat-label">在线用户</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>游戏趋势（最近7天）</span>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表加载中..." />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="activity-card">
          <template #header>
            <span>最近活动</span>
          </template>
          <div class="activity-list">
            <div v-for="(activity, index) in recentActivities" :key="index" class="activity-item">
              <el-avatar :size="32">{{ activity.user?.charAt(0) || 'U' }}</el-avatar>
              <div class="activity-content">
                <span class="activity-user">{{ activity.user }}</span>
                <span class="activity-text">{{ activity.action }}</span>
              </div>
              <span class="activity-time">{{ activity.time }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="quick-links">
      <el-col :span="8">
        <el-card shadow="hover" class="quick-card" @click="$router.push('/admin/users')">
          <el-icon :size="48" color="#409eff"><UserFilled /></el-icon>
          <h3>用户管理</h3>
          <p>管理用户账号、角色和权限</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="quick-card" @click="$router.push('/admin/monitor')">
          <el-icon :size="48" color="#67c23a"><Monitor /></el-icon>
          <h3>系统监控</h3>
          <p>查看系统运行状态和性能</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="quick-card">
          <el-icon :size="48" color="#e6a23c"><Setting /></el-icon>
          <h3>系统设置</h3>
          <p>配置系统参数和选项</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'

const stats = ref({
  totalUsers: 156,
  totalGames: 2340,
  todayGames: 45,
  onlineUsers: 12
})

const recentActivities = ref([
  { user: 'testuser', action: '完成了一局游戏', time: '2分钟前' },
  { user: 'admin', action: '登录了后台', time: '5分钟前' },
  { user: 'player1', action: '注册了新账号', time: '10分钟前' },
  { user: 'guest', action: '完成了推理练习', time: '15分钟前' },
  { user: 'player2', action: '查看了排行榜', time: '20分钟前' }
])

const loadStats = async () => {
  try {
    const res = await adminApi.getStats()
    if (res.data) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败')
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
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

.chart-card,
.activity-card {
  margin-bottom: 20px;
}

.chart-placeholder {
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.activity-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.activity-user {
  font-weight: bold;
  color: #333;
}

.activity-text {
  font-size: 12px;
  color: #666;
}

.activity-time {
  font-size: 12px;
  color: #999;
}

.quick-links {
  margin-top: 20px;
}

.quick-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-card:hover {
  transform: translateY(-5px);
}

.quick-card h3 {
  margin: 16px 0 8px 0;
  color: #333;
}

.quick-card p {
  color: #666;
  font-size: 14px;
  margin: 0;
}
</style>
