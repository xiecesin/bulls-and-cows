import { createRouter, createWebHistory } from 'vue-router'

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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
