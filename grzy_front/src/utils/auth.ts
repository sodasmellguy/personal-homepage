import { localStorageUtil } from './storage'

const TOKEN_KEY = 'grzy_token'
const USER_INFO_KEY = 'grzy_userInfo'
const TOKEN_EXPIRY_KEY = 'grzy_token_expiry'
// Token 有效期 1小时（与后端 Sa-Token timeout 一致）
const TOKEN_EXPIRY_MS = 1 * 60 * 60 * 1000;

/**
 * 获取 Token
 */
export function getToken(): string | null {
  // 检查 token 是否已过期
  const expiry = localStorageUtil.get<number>(TOKEN_EXPIRY_KEY)
  if (expiry && Date.now() > expiry) {
    clearAuth()
    return null
  }
  return localStorageUtil.get<string>(TOKEN_KEY)
}

/**
 * 设置 Token
 * 安全说明：仅存储于 localStorage，通过 Authorization Header 传递。
 * 不再写入 document.cookie —— JS 设置的 Cookie 无法携带 HttpOnly，
 * 反而会随所有同源请求自动发送，扩大 Token 泄露面。
 */
export function setToken(token: string): void {
  localStorageUtil.set(TOKEN_KEY, token)
  // 记录过期时间
  localStorageUtil.set(TOKEN_EXPIRY_KEY, Date.now() + TOKEN_EXPIRY_MS)
}

/**
 * 移除 Token
 */
export function removeToken(): void {
  localStorageUtil.remove(TOKEN_KEY)
  localStorageUtil.remove(TOKEN_EXPIRY_KEY)
  // 兼容清理：移除历史版本遗留的 Authorization Cookie
  document.cookie = 'Authorization=; path=/; max-age=0; SameSite=Strict'
}

/**
 * 获取用户信息
 */
export function getUserInfo<T = any>(): T | null {
  return localStorageUtil.get<T>(USER_INFO_KEY)
}

/**
 * 设置用户信息
 */
export function setUserInfo(userInfo: any): void {
  localStorageUtil.set(USER_INFO_KEY, userInfo)
}

/**
 * 移除用户信息
 */
export function removeUserInfo(): void {
  localStorageUtil.remove(USER_INFO_KEY)
}

/**
 * 清除所有认证信息
 */
export function clearAuth(): void {
  removeToken()
  removeUserInfo()
}

/**
 * 判断是否已登录
 */
export function isAuthenticated(): boolean {
  return !!getToken()
}
