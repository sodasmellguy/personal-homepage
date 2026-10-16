import { createRouter, createWebHistory } from 'vue-router'
import { constantRoutes } from './routes'
import { setupRouterGuards } from './guards'

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 }),
})

// 设置路由守卫
setupRouterGuards(router)

export default router
