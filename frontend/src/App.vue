<template>
  <div id="app">
    <el-container>
      <el-header class="app-header">
        <div class="header-content">
          <h1>🎯 猜数字游戏</h1>
          <span class="subtitle">Bulls and Cows</span>
        </div>
        
        <div class="header-right">
          <!-- 未登录显示登录/注册按钮 -->
          <template v-if="!userStore.isLoggedIn">
            <el-button type="primary" plain @click="$router.push('/login')">
              登录
            </el-button>
            <el-button @click="$router.push('/register')">
              注册
            </el-button>
          </template>
          
          <!-- 已登录显示用户信息 -->
          <template v-else>
            <el-dropdown @command="handleUserCommand">
              <span class="user-info">
                <el-avatar 
                  :size="36" 
                  :src="userStore.avatarUrl || defaultAvatar"
                >
                  {{ userStore.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <span class="username">{{ userStore.nickname }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="records">
                    <el-icon><History /></el-icon>
                    练习记录
                  </el-dropdown-item>
                  <el-dropdown-item command="ranking">
                    <el-icon><Trophy /></el-icon>
                    排行榜
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </el-header>
      
      <el-container>
        <el-aside width="200px" class="app-aside">
          <el-menu
            :default-active="activeMenu"
            router
            class="el-menu-vertical"
          >
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/game">
              <el-icon><VideoPlay /></el-icon>
              <span>练习游戏</span>
            </el-menu-item>
            <el-menu-item index="/puzzle">
              <el-icon><MagicStick /></el-icon>
              <span>推理练习</span>
            </el-menu-item>
            <el-menu-item index="/reasoning">
              <el-icon><Connection /></el-icon>
              <span>推理解析</span>
            </el-menu-item>
            <el-menu-item index="/algorithm">
              <el-icon><Document /></el-icon>
              <span>算法展示</span>
            </el-menu-item>
            <el-menu-item index="/variants">
              <el-icon><Collection /></el-icon>
              <span>变形研究</span>
            </el-menu-item>
            
            <!-- 登录后显示的菜单 -->
            <el-menu-item-group title="用户功能" v-if="userStore.isLoggedIn">
              <el-menu-item index="/collaboration">
                <el-icon><Users /></el-icon>
                <span>多人协作</span>
              </el-menu-item>
            </el-menu-item-group>
            
            <!-- 管理员菜单 -->
            <el-menu-item-group title="管理后台" v-if="userStore.isAdmin">
              <el-menu-item index="/admin">
                <el-icon><Setting /></el-icon>
                <span>管理首页</span>
              </el-menu-item>
              <el-menu-item index="/admin/users">
                <el-icon><UserFilled /></el-icon>
                <span>用户管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/monitor">
                <el-icon><Monitor /></el-icon>
                <span>系统监控</span>
              </el-menu-item>
            </el-menu-item-group>
          </el-menu>
        </el-aside>
        
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

// 默认头像背景色
const defaultAvatar = ''

const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'records':
      router.push('/records')
      break
    case 'ranking':
      router.push('/ranking')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/')
      } catch {
        // 用户取消
      }
      break
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

#app {
  min-height: 100vh;
}

.app-header {
  background: linear-gradient(90deg, #1a1a2e 0%, #16213e 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-content h1 {
  font-size: 24px;
  margin: 0;
}

.header-content .subtitle {
  font-size: 14px;
  color: #a0a0a0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.2);
}

.username {
  color: white;
  font-size: 14px;
}

.app-aside {
  background: white;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.el-menu-vertical {
  border-right: none;
  padding-top: 20px;
}

.app-main {
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  min-height: calc(100vh - 60px);
}

.page-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 28px;
  margin-bottom: 24px;
  color: #333;
  border-bottom: 3px solid #667eea;
  padding-bottom: 12px;
}

.page-subtitle {
  font-size: 18px;
  color: #666;
  margin-bottom: 16px;
}
</style>
