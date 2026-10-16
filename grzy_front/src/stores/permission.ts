import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { constantRoutes, asyncRoutes } from '@/router/routes'

/**
 * 判断是否有权限访问路由：
 * - 无 permission 标识的路由（如布局父级）默认放行
 * - admin 的通配权限 "*" 直接通过
 * - 其他用户按 meta.permission 与后端返回的权限标识比对
 */
function hasPermission(permissions: string[], route: RouteRecordRaw): boolean {
  if (route.meta && route.meta.permission) {
    if (permissions.includes('*') || permissions.includes('*:*:*')) {
      return true
    }
    return permissions.includes(route.meta.permission as string)
  }
  return true
}

/**
 * 过滤异步路由
 */
function filterAsyncRoutes(routes: RouteRecordRaw[], permissions: string[]): RouteRecordRaw[] {
  const res: RouteRecordRaw[] = []
  routes.forEach((route) => {
    const tmp = { ...route }
    if (hasPermission(permissions, tmp)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, permissions)
      }
      res.push(tmp)
    }
  })
  return res
}

export const usePermissionStore = defineStore('permission', () => {
  // 状态
  const routes = ref<RouteRecordRaw[]>([])
  const addRoutes = ref<RouteRecordRaw[]>([])

  /**
   * 生成路由
   */
  async function generateRoutes(roles: string[], permissions: string[] = []) {
    let accessedRoutes: RouteRecordRaw[] = []

    // 无角色的用户（普通注册访客）不生成任何后台路由
    if (roles.length === 0) {
      addRoutes.value = []
      routes.value = constantRoutes
      return accessedRoutes
    }

    accessedRoutes = filterAsyncRoutes(asyncRoutes, permissions)

    addRoutes.value = accessedRoutes
    routes.value = constantRoutes.concat(accessedRoutes)
    return accessedRoutes
  }

  /**
   * 重置路由
   */
  function resetRoutes() {
    routes.value = []
    addRoutes.value = []
  }

  return {
    routes,
    addRoutes,
    generateRoutes,
    resetRoutes,
  }
})
