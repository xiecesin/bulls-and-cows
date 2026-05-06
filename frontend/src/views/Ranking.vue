<template>
  <div class="ranking-container">
    <div class="page-card">
      <h1 class="page-title">排行榜</h1>

      <!-- 排行榜类型切换 -->
      <el-tabs v-model="activeTab" class="ranking-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="猜测次数榜" name="GUESS_COUNT" />
        <el-tab-pane label="速度榜" name="TIME" />
      </el-tabs>

      <!-- 我的排名 -->
      <el-card class="my-rank-card" shadow="hover" v-if="userStore.isLoggedIn">
        <div class="my-rank">
          <el-avatar :size="48" :src="userStore.avatarUrl">
            {{ userStore.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="my-rank-info">
            <div class="my-rank-title">我的排名</div>
            <div class="my-rank-position">
              <template v-if="myRanking && myRanking.ranked">
                <span class="rank-number">第 {{ myRanking.rank }} 名</span>
                <span class="rank-score">成绩: {{ myRanking.score }} 次</span>
              </template>
              <template v-else>
                <span class="no-rank">暂无排名，继续加油！</span>
              </template>
            </div>
          </div>
          <el-button type="primary" @click="$router.push('/game')">
            <el-icon><VideoPlay /></el-icon>
            去练习
          </el-button>
        </div>
      </el-card>

      <!-- 排行榜 -->
      <el-card class="ranking-list-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>TOP 20</span>
            <el-button type="primary" link @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>

        <!-- 排行榜头部 -->
        <div class="ranking-header">
          <div class="rank-col rank-num">排名</div>
          <div class="rank-col rank-user">用户</div>
          <div class="rank-col rank-score">成绩</div>
          <div class="rank-col rank-time">时间</div>
        </div>

        <!-- 排行榜内容 -->
        <div class="ranking-content" v-loading="loading">
          <div
            v-for="(item, index) in rankings"
            :key="item.userId"
            class="ranking-item"
            :class="{ 'is-me': item.userId === userStore.userId }"
          >
            <div class="rank-col rank-num">
              <span class="rank-badge" :class="getRankClass(index + 1)">
                {{ index + 1 }}
              </span>
            </div>
            <div class="rank-col rank-user">
              <el-avatar :size="32">{{ item.username?.charAt(0) || 'U' }}</el-avatar>
              <span class="username">{{ item.username }}</span>
              <el-tag v-if="item.userId === userStore.userId" type="primary" size="small">我</el-tag>
            </div>
            <div class="rank-col rank-score">
              <span class="score-value">{{ item.score }}</span>
              <span class="score-unit">次</span>
            </div>
            <div class="rank-col rank-time">
              {{ formatDate(item.achievedAt) }}
            </div>
          </div>

          <!-- 空状态 -->
          <el-empty v-if="!loading && rankings.length === 0" description="暂无排名记录" />
        </div>
      </el-card>

      <!-- 排行榜规则说明 -->
      <el-card class="rules-card" shadow="hover">
        <template #header>
          <span>排行榜规则</span>
        </template>
        <div class="rules-content">
          <div class="rule-item">
            <el-icon><Trophy /></el-icon>
            <div>
              <h4>猜测次数榜</h4>
              <p>以最少的猜测次数猜出答案排列名次。猜测次数越少，排名越高。</p>
            </div>
          </div>
          <div class="rule-item">
            <el-icon><Clock /></el-icon>
            <div>
              <h4>速度榜</h4>
              <p>以最短的时间完成游戏排列名次。用时越短，排名越高。</p>
            </div>
          </div>
          <div class="rule-item">
            <el-icon><Medal /></el-icon>
            <div>
              <h4>如何参与</h4>
              <p>在练习游戏中取得胜利后，系统会自动将你的成绩录入排行榜。</p>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { rankingApi } from '@/api/record'

const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('GUESS_COUNT')
const rankings = ref([])
const myRanking = ref(null)

const loadRankings = async () => {
  loading.value = true
  try {
    const res = await rankingApi.getRanking(activeTab.value, 20)
    if (res.data && res.data.rankings) {
      rankings.value = res.data.rankings
    }
  } catch (error) {
    console.error('获取排行榜失败:', error)
    ElMessage.error('获取排行榜失败')
  } finally {
    loading.value = false
  }
}

const loadMyRanking = async () => {
  if (!userStore.isLoggedIn) {
    myRanking.value = { ranked: false }
    return
  }

  try {
    const res = await rankingApi.getMyRanking(activeTab.value)
    if (res.data) {
      myRanking.value = res.data
    }
  } catch (error) {
    console.error('获取我的排名失败:', error)
  }
}

const handleTabChange = () => {
  loadRankings()
  loadMyRanking()
}

const handleRefresh = () => {
  loadRankings()
  loadMyRanking()
  ElMessage.success('刷新成功')
}

const getRankClass = (rank) => {
  if (rank === 1) return 'gold'
  if (rank === 2) return 'silver'
  if (rank === 3) return 'bronze'
  return ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadRankings()
  loadMyRanking()
})
</script>

<style scoped>
.ranking-container {
  max-width: 900px;
  margin: 0 auto;
}

.ranking-tabs {
  margin-bottom: 20px;
}

.my-rank-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.my-rank-card :deep(.el-card__body) {
  color: white;
}

.my-rank {
  display: flex;
  align-items: center;
  gap: 16px;
}

.my-rank-info {
  flex: 1;
}

.my-rank-title {
  font-size: 14px;
  opacity: 0.8;
}

.my-rank-position {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-top: 4px;
}

.rank-number {
  font-size: 24px;
  font-weight: bold;
}

.rank-score {
  font-size: 14px;
  opacity: 0.9;
}

.no-rank {
  font-size: 16px;
  opacity: 0.8;
}

.ranking-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ranking-header {
  display: flex;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  font-weight: bold;
  color: #666;
  margin-bottom: 8px;
}

.ranking-content {
  min-height: 200px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  transition: background 0.3s;
}

.ranking-item:hover {
  background: #f5f7fa;
}

.ranking-item.is-me {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border: 1px solid #667eea;
}

.rank-col {
  display: flex;
  align-items: center;
}

.rank-num {
  width: 80px;
}

.rank-user {
  flex: 1;
  gap: 12px;
}

.rank-score {
  width: 100px;
  justify-content: center;
}

.rank-time {
  width: 120px;
  color: #999;
  font-size: 14px;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #666;
  font-weight: bold;
}

.rank-badge.gold {
  background: linear-gradient(135deg, #ffd700 0%, #ffb347 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
}

.rank-badge.silver {
  background: linear-gradient(135deg, #c0c0c0 0%, #a0a0a0 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
}

.rank-badge.bronze {
  background: linear-gradient(135deg, #cd7f32 0%, #b87333 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
}

.username {
  font-weight: 500;
}

.score-value {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.score-unit {
  margin-left: 4px;
  color: #999;
}

.rules-card {
  margin-bottom: 20px;
}

.rules-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rule-item {
  display: flex;
  gap: 16px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
}

.rule-item .el-icon {
  font-size: 24px;
  color: #409eff;
}

.rule-item h4 {
  margin: 0 0 4px 0;
  color: #333;
}

.rule-item p {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>
