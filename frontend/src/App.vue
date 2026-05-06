<template>
  <div id="app">
    <el-container>
      <el-header class="app-header">
        <div class="header-content">
          <h1>🎯 猜数字游戏</h1>
          <span class="subtitle">Bulls and Cows</span>
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
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
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
