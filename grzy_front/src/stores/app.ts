import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { localStorageUtil } from '@/utils/storage'

export const useAppStore = defineStore('app', () => {
  // 状态
  const sidebarCollapsed = ref(false)
  const theme = ref<'light' | 'dark'>('light')
  const primaryColor = ref('#3b82f6')

  // 计算属性
  const isDark = computed(() => theme.value === 'dark')

  /**
   * 初始化设置
   */
  function initSettings() {
    const savedTheme = localStorageUtil.get<'light' | 'dark'>('theme')
    const savedCollapsed = localStorageUtil.get<boolean>('sidebarCollapsed')

    if (savedTheme) {
      theme.value = savedTheme
    }
    if (savedCollapsed !== null) {
      sidebarCollapsed.value = savedCollapsed
    }

    // 应用主题
    applyTheme()
  }

  /**
   * 切换侧边栏
   */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
    localStorageUtil.set('sidebarCollapsed', sidebarCollapsed.value)
  }

  /**
   * 设置主题
   */
  function setTheme(newTheme: 'light' | 'dark') {
    theme.value = newTheme
    localStorageUtil.set('theme', newTheme)
    applyTheme()
  }

  /**
   * 切换主题
   */
  function toggleTheme() {
    setTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  /**
   * 应用主题
   */
  function applyTheme() {
    document.documentElement.setAttribute('data-theme', theme.value)
    if (theme.value === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  /**
   * 设置主色调
   */
  function setPrimaryColor(color: string) {
    primaryColor.value = color
    document.documentElement.style.setProperty('--primary-color', color)
  }

  return {
    sidebarCollapsed,
    theme,
    primaryColor,
    isDark,
    initSettings,
    toggleSidebar,
    setTheme,
    toggleTheme,
    setPrimaryColor,
  }
})
