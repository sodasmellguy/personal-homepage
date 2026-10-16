import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/auth'
import type { LoginParams, UserInfo } from '@/api/auth'
import { getToken, setToken, setUserInfo, clearAuth } from '@/utils/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(getToken())

  // 启动时同步 token 到 cookie（兼容已登录用户）
  if (token.value) {
    const secureFlag = window.location.protocol === 'https:' ? '; Secure' : ''
    document.cookie = `Authorization=${token.value}; path=/; SameSite=Strict${secureFlag}`
  }
  const userInfo = ref<UserInfo | null>(null)
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  /**
   * 登录
   */
  async function login(loginParams: LoginParams) {
    const { data } = await loginApi(loginParams)
    token.value = data.token
    setToken(data.token)
    setUserInfo(data)
    return data
  }

  /**
   * 获取用户信息
   */
  async function fetchUserInfo() {
    const { data } = await getUserInfoApi()
    userInfo.value = data
    roles.value = data.roles || []
    permissions.value = data.permissions || []
    setUserInfo(data)
    return data
  }

  /**
   * 登出
   */
  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      // 即使请求失败也要清除本地状态
      console.error('登出请求失败:', error)
    } finally {
      resetState()
      router.push('/login')
    }
  }

  /**
   * 重置状态
   */
  function resetState() {
    token.value = null
    userInfo.value = null
    roles.value = []
    permissions.value = []
    clearAuth()
  }

  /**
   * 检查权限
   */
  function hasPermission(permission: string) {
    if (permissions.value.includes('*') || permissions.value.includes('*:*:*')) {
      return true
    }
    return permissions.value.includes(permission)
  }

  /**
   * 检查角色
   */
  function hasRole(role: string) {
    if (roles.value.includes('admin')) {
      return true
    }
    return roles.value.includes(role)
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    isLoggedIn,
    username,
    nickname,
    avatar,
    login,
    fetchUserInfo,
    logout,
    resetState,
    hasPermission,
    hasRole,
  }
})
