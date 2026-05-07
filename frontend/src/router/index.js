import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/game',
    name: 'Game',
    component: () => import('../views/Game.vue')
  },
  {
    path: '/puzzle',
    name: 'Puzzle',
    component: () => import('../views/Puzzle.vue')
  },
  {
    path: '/reasoning',
    name: 'Reasoning',
    component: () => import('../views/Reasoning.vue')
  },
  {
    path: '/algorithm',
    name: 'Algorithm',
    component: () => import('../views/Algorithm.vue')
  },
  {
    path: '/variants',
    name: 'Variants',
    component: () => import('../views/Variants.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/records',
    name: 'Records',
    component: () => import('../views/Records.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/ranking',
    name: 'Ranking',
    component: () => import('../views/Ranking.vue')
  },
  {
    path: '/collaboration',
    name: 'Collaboration',
    component: () => import('../views/Collaboration.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminIndex',
    component: () => import('../views/admin/Index.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('../views/admin/Users.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/monitor',
    name: 'AdminMonitor',
    component: () => import('../views/admin/Monitor.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 初始化用户状态
  if (!userStore.isLoggedIn && localStorage.getItem('token')) {
    userStore.initUser()
  }
  
  // 游客-only 页面（如登录、注册）重定向
  if (to.meta.guestOnly && userStore.isLoggedIn) {
    next('/')
    return
  }
  
  // 需要登录的页面
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
    return
  }
  
  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/')
    return
  }
  
  next()
})

export default router
