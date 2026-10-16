/**
 * 本地存储工具类
 */

const PREFIX = 'grzy_'

/**
 * 本地存储
 */
export const localStorageUtil = {
  /**
   * 获取
   */
  get<T = any>(key: string): T | null {
    const value = localStorage.getItem(PREFIX + key)
    if (value) {
      try {
        return JSON.parse(value)
      } catch {
        return value as T
      }
    }
    return null
  },

  /**
   * 设置
   */
  set(key: string, value: any): void {
    if (typeof value === 'object') {
      localStorage.setItem(PREFIX + key, JSON.stringify(value))
    } else {
      localStorage.setItem(PREFIX + key, String(value))
    }
  },

  /**
   * 删除
   */
  remove(key: string): void {
    localStorage.removeItem(PREFIX + key)
  },

  /**
   * 清空
   */
  clear(): void {
    localStorage.clear()
  },
}

/**
 * 会话存储
 */
export const sessionStorageUtil = {
  /**
   * 获取
   */
  get<T = any>(key: string): T | null {
    const value = window.sessionStorage.getItem(PREFIX + key)
    if (value) {
      try {
        return JSON.parse(value)
      } catch {
        return value as T
      }
    }
    return null
  },

  /**
   * 设置
   */
  set(key: string, value: any): void {
    if (typeof value === 'object') {
      window.sessionStorage.setItem(PREFIX + key, JSON.stringify(value))
    } else {
      window.sessionStorage.setItem(PREFIX + key, String(value))
    }
  },

  /**
   * 删除
   */
  remove(key: string): void {
    window.sessionStorage.removeItem(PREFIX + key)
  },

  /**
   * 清空
   */
  clear(): void {
    window.sessionStorage.clear()
  },
}

export default {
  local: localStorageUtil,
  session: sessionStorageUtil,
}
