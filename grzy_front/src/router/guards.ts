import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { getToken } from '@/utils/auth'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// NProgress 配置
NProgress.configure({ showSpinner: false })

// 白名单路由
const whiteList = ['/login', '/home', '/profile', '/project', '/opensource', '/internship', '/article', '/series', '/guestbook', '/album', '/links', '/archive', '/visitor-map', '/collections']

export function setupRouterGuards(router: Router) {
  // 前置守卫
  router.beforeEach(async (to, _from, next) => {
    NProgress.start()

    const token = getToken()

    if (token) {
      if (to.path === '/login') {
        // 已登录，跳转首页
        next({ path: '/' })
        NProgress.done()
      } else {
        const userStore = useUserStore()
        const permissionStore = usePermissionStore()

        if (userStore.roles.length === 0) {
          try {
            // 获取用户信息
            await userStore.fetchUserInfo()

            // 生成路由
            const roles = userStore.roles
            const accessRoutes = await permissionStore.generateRoutes(roles, userStore.permissions)

            // 动态添加路由
            accessRoutes.forEach((route) => {
              router.addRoute(route)
            })

            // 添加通配路由（放在最后）
            router.addRoute({
              path: '/:pathMatch(.*)*',
              redirect: '/404',
            })

            // 确保路由已完成
            next({ ...to, replace: true })
          } catch (error) {
            // 获取用户信息失败，清除 token 并跳转登录
            userStore.resetState()
            next(`/login?redirect=${to.path}`)
            NProgress.done()
          }
        } else {
          next()
        }
      }
    } else {
      // 未登录
      if (whiteList.includes(to.path) || whiteList.some(path => to.path.startsWith(path + '/'))) {
        next()
      } else {
        next(`/login?redirect=${to.path}`)
        NProgress.done()
      }
    }
  })

  // 后置守卫
  router.afterEach((to) => {
    NProgress.done()
    // 设置页面标题
    document.title = `${to.meta.title || ''} - personal-homepage`
  })
}
