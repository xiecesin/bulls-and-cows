<template>
  <div class="page-card">
    <h2 class="page-title">📊 系统监控</h2>
    
    <!-- 实时状态卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-header">
            <span class="stat-label">CPU 使用率</span>
            <el-tag :type="getCpuStatus().type" size="small">{{ getCpuStatus().text }}</el-tag>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ systemInfo.cpuUsage }}%</div>
            <el-progress 
              :percentage="systemInfo.cpuUsage" 
              :color="getCpuColor()" 
              :show-text="false"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-header">
            <span class="stat-label">内存使用</span>
            <el-tag :type="getMemoryStatus().type" size="small">{{ getMemoryStatus().text }}</el-tag>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ systemInfo.memoryUsage }}%</div>
            <el-progress 
              :percentage="systemInfo.memoryUsage" 
              :color="getMemoryColor()" 
              :show-text="false"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-header">
            <span class="stat-label">磁盘使用</span>
            <el-tag type="success" size="small">正常</el-tag>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ systemInfo.diskUsage }}%</div>
            <el-progress 
              :percentage="systemInfo.diskUsage" 
              color="#67c23a" 
              :show-text="false"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-header">
            <span class="stat-label">在线用户</span>
            <el-tag type="info" size="small">活跃</el-tag>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ systemInfo.onlineUsers }}</div>
            <div class="stat-sub">峰值: {{ systemInfo.peakUsers }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 系统信息 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <span>系统信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="操作系统">
              {{ systemInfo.osName }} {{ systemInfo.osVersion }}
            </el-descriptions-item>
            <el-descriptions-item label="Java 版本">
              {{ systemInfo.javaVersion }}
            </el-descriptions-item>
            <el-descriptions-item label="Spring Boot 版本">
              {{ systemInfo.springBootVersion }}
            </el-descriptions-item>
            <el-descriptions-item label="运行时间">
              {{ systemInfo.uptime }}
            </el-descriptions-item>
            <el-descriptions-item label="启动时间">
              {{ systemInfo.startTime }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <span>应用信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="应用名称">
              {{ systemInfo.appName }}
            </el-descriptions-item>
            <el-descriptions-item label="应用版本">
              {{ systemInfo.appVersion }}
            </el-descriptions-item>
            <el-descriptions-item label="端口">
              {{ systemInfo.port }}
            </el-descriptions-item>
            <el-descriptions-item label="数据库">
              {{ systemInfo.database }}
            </el-descriptions-item>
            <el-descriptions-item label="环境">
              <el-tag size="small">{{ systemInfo.profile }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- API 性能监控 -->
    <el-card shadow="hover" class="api-card">
      <template #header>
        <div class="card-header">
          <span>API 性能监控</span>
          <el-button size="small" @click="loadApiStats">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table :data="apiStats" v-loading="loading">
        <el-table-column prop="endpoint" label="接口路径" min-width="200" />
        <el-table-column prop="method" label="方法" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getMethodColor(row.method)" size="small">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requests" label="请求数" width="100" align="center" />
        <el-table-column prop="avgTime" label="平均耗时" width="100" align="center">
          <template #default="{ row }">
            <span :class="getTimeClass(row.avgTime)">{{ row.avgTime }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxTime" label="最大耗时" width="100" align="center" />
        <el-table-column prop="errorRate" label="错误率" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.errorRate > 1 ? 'danger' : row.errorRate > 0.5 ? 'warning' : 'success'" size="small">
              {{ row.errorRate }}%
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 日志查看 -->
    <el-card shadow="hover" class="logs-card">
      <template #header>
        <div class="card-header">
          <span>最近日志</span>
          <el-select v-model="logLevel" size="small" style="width: 100px" @change="loadLogs">
            <el-option label="全部" value="ALL" />
            <el-option label="ERROR" value="ERROR" />
            <el-option label="WARN" value="WARN" />
            <el-option label="INFO" value="INFO" />
          </el-select>
        </div>
      </template>
      <div class="logs-container" v-loading="loadingLogs">
        <div v-for="(log, index) in logs" :key="index" class="log-item" :class="log.level.toLowerCase()">
          <span class="log-time">{{ log.time }}</span>
          <span class="log-level">{{ log.level }}</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
        <el-empty v-if="logs.length === 0" description="暂无日志" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { monitorApi } from '@/api/admin'

const loading = ref(false)
const loadingLogs = ref(false)
const logLevel = ref('ALL')

const systemInfo = ref({
  cpuUsage: 35,
  memoryUsage: 62,
  diskUsage: 45,
  onlineUsers: 12,
  peakUsers: 28,
  osName: 'Linux',
  osVersion: 'Ubuntu 22.04',
  javaVersion: '21.0.1',
  springBootVersion: '3.2.0',
  uptime: '3天 5小时 32分钟',
  startTime: '2024-01-12 10:00:00',
  appName: 'Bulls and Cows',
  appVersion: '1.0.0',
  port: 8080,
  database: 'PostgreSQL 15',
  profile: '开发环境'
})

const apiStats = ref([
  { endpoint: '/api/game/start', method: 'POST', requests: 1256, avgTime: 45, maxTime: 120, errorRate: 0.2 },
  { endpoint: '/api/game/guess', method: 'POST', requests: 8934, avgTime: 23, maxTime: 85, errorRate: 0.1 },
  { endpoint: '/api/auth/login', method: 'POST', requests: 567, avgTime: 89, maxTime: 234, errorRate: 0.5 },
  { endpoint: '/api/records', method: 'GET', requests: 2345, avgTime: 56, maxTime: 180, errorRate: 0.3 },
  { endpoint: '/api/ranking', method: 'GET', requests: 1890, avgTime: 34, maxTime: 98, errorRate: 0.1 },
  { endpoint: '/api/algorithm', method: 'GET', requests: 456, avgTime: 28, maxTime: 67, errorRate: 0.0 }
])

const logs = ref([
  { time: '10:23:45', level: 'INFO', message: '用户 testuser 登录成功' },
  { time: '10:23:50', level: 'INFO', message: '创建新游戏 gameId=abc123' },
  { time: '10:24:12', level: 'INFO', message: '用户提交猜测: 1234' },
  { time: '10:24:12', level: 'DEBUG', message: '猜测结果: 1A2B' },
  { time: '10:25:30', level: 'WARN', message: '请求频率过高: /api/game/guess from IP 192.168.1.100' },
  { time: '10:26:15', level: 'ERROR', message: '数据库连接超时' },
  { time: '10:26:20', level: 'INFO', message: '连接池已恢复' },
  { time: '10:27:00', level: 'INFO', message: '游戏结束: gameId=abc123, 用时 3分钟' }
])

let refreshTimer = null

const getCpuStatus = () => {
  if (systemInfo.value.cpuUsage > 80) return { type: 'danger', text: '过高' }
  if (systemInfo.value.cpuUsage > 60) return { type: 'warning', text: '中等' }
  return { type: 'success', text: '正常' }
}

const getMemoryStatus = () => {
  if (systemInfo.value.memoryUsage > 85) return { type: 'danger', text: '过高' }
  if (systemInfo.value.memoryUsage > 70) return { type: 'warning', text: '中等' }
  return { type: 'success', text: '正常' }
}

const getCpuColor = () => {
  if (systemInfo.value.cpuUsage > 80) return '#f56c6c'
  if (systemInfo.value.cpuUsage > 60) return '#e6a23c'
  return '#67c23a'
}

const getMemoryColor = () => {
  if (systemInfo.value.memoryUsage > 85) return '#f56c6c'
  if (systemInfo.value.memoryUsage > 70) return '#e6a23c'
  return '#67c23a'
}

const getMethodColor = (method) => {
  const colors = {
    'GET': 'success',
    'POST': 'primary',
    'PUT': 'warning',
    'DELETE': 'danger'
  }
  return colors[method] || 'info'
}

const getTimeClass = (time) => {
  if (time > 100) return 'time-slow'
  if (time > 50) return 'time-medium'
  return 'time-fast'
}

const loadSystemInfo = async () => {
  try {
    const res = await monitorApi.getSystemInfo()
    if (res.data) {
      systemInfo.value = res.data
    }
  } catch (error) {
    console.error('获取系统信息失败')
  }
}

const loadApiStats = async () => {
  loading.value = true
  try {
    const res = await monitorApi.getApiStats()
    if (res.data) {
      apiStats.value = res.data
    }
  } catch (error) {
    console.error('获取API统计失败')
  } finally {
    loading.value = false
  }
}

const loadLogs = async () => {
  loadingLogs.value = true
  try {
    const res = await monitorApi.getLogs(logLevel.value)
    if (res.data) {
      logs.value = res.data
    }
  } catch (error) {
    console.error('获取日志失败')
  } finally {
    loadingLogs.value = false
  }
}

const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadSystemInfo()
  }, 5000)
}

onMounted(() => {
  loadSystemInfo()
  loadApiStats()
  loadLogs()
  startAutoRefresh()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-sub {
  font-size: 12px;
  color: #999;
}

.info-card {
  margin-bottom: 20px;
}

.api-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logs-card {
  margin-bottom: 20px;
}

.logs-container {
  max-height: 300px;
  overflow-y: auto;
  background: #1e1e1e;
  border-radius: 8px;
  padding: 12px;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 12px;
}

.log-item {
  display: flex;
  gap: 12px;
  padding: 4px 0;
  border-bottom: 1px solid #333;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: #888;
  flex-shrink: 0;
}

.log-level {
  font-weight: bold;
  flex-shrink: 0;
  width: 60px;
}

.log-item.info .log-level { color: #409eff; }
.log-item.warn .log-level { color: #e6a23c; }
.log-item.error .log-level { color: #f56c6c; }
.log-item.debug .log-level { color: #909399; }

.log-message {
  color: #d4d4d4;
  word-break: break-all;
}

.time-slow {
  color: #f56c6c;
  font-weight: bold;
}

.time-medium {
  color: #e6a23c;
}

.time-fast {
  color: #67c23a;
}
</style>
