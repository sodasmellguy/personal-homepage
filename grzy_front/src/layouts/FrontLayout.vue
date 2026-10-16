<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowUpOutlined } from '@ant-design/icons-vue'
import {
  HomeOutlined,
  UserOutlined,
  ProjectOutlined,
  SolutionOutlined,
  ReadOutlined,
  CalendarOutlined,
  MessageOutlined,
  SettingOutlined,
  MenuOutlined,
  CloseOutlined,
  SearchOutlined,
  EnvironmentOutlined,
  LinkOutlined,
  CameraOutlined,
  GithubOutlined,
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import ThemeToggle from '@/components/ThemeToggle.vue'
import AppFooter from '@/components/AppFooter.vue'
import MobileTabBar from '@/components/MobileTabBar.vue'
import GlobalSearch from '@/components/GlobalSearch.vue'
import { recordVisit } from '@/utils/visit'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedKeys = ref<string[]>([route.path])
const mobileMenuVisible = ref(false)
const globalSearchRef = ref()

// 滚动进度与返回顶部
const scrollProgress = ref(0)
const showBackToTop = ref(false)
const headerScrolled = ref(false)

const handleScroll = () => {
  const scrollTop = window.scrollY
  const scrollHeight = document.documentElement.scrollHeight - window.innerHeight
  scrollProgress.value = scrollHeight > 0 ? (scrollTop / scrollHeight) * 100 : 0
  showBackToTop.value = scrollTop > 300
  headerScrolled.value = scrollTop > 10
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 返回顶部按钮环形进度计算
const ringCircumference = 2 * Math.PI * 22
const ringOffset = ref(ringCircumference)
watch(scrollProgress, (val) => {
  ringOffset.value = ringCircumference - (val / 100) * ringCircumference
})

// 记录访问量
onMounted(() => {
  recordVisit()
  window.addEventListener('scroll', handleScroll, { passive: true })
  handleScroll()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 监听路由变化
watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
    mobileMenuVisible.value = false
  }
)

// 导航菜单
const menuItems = [
  { key: '/home', icon: HomeOutlined, label: '首页' },
  { key: '/profile', icon: UserOutlined, label: '关于' },
  { key: '/project', icon: ProjectOutlined, label: '项目' },
  { key: '/opensource', icon: GithubOutlined, label: '开源' },
  { key: '/internship', icon: SolutionOutlined, label: '实习' },
  { key: '/article', icon: ReadOutlined, label: '博客' },
  { key: '/series', icon: ReadOutlined, label: '专栏' },
  { key: '/guestbook', icon: MessageOutlined, label: '留言' },
  { key: '/album', icon: CameraOutlined, label: '相册' },
  { key: '/links', icon: LinkOutlined, label: '友链' },
  { key: '/archive', icon: CalendarOutlined, label: '归档' },
  { key: '/visitor-map', icon: EnvironmentOutlined, label: '足迹' },
]

// 跳转后台
const goToAdmin = () => {
  router.push('/admin')
}

// 点击菜单项后关闭菜单
const handleMenuClick = (key: string) => {
  router.push(key)
  mobileMenuVisible.value = false
}
</script>

<template>
  <a-layout class="min-h-screen">
    <!-- 滚动进度条 -->
    <div class="scroll-progress-bar" :style="{ width: scrollProgress + '%' }"></div>

    <!-- 顶部导航 -->
    <header
      class="fixed top-0 left-0 right-0 z-50 transition-all duration-300"
      :class="headerScrolled
        ? 'bg-white/85 dark:bg-dark-card/85 backdrop-blur-xl border-b border-gray-100/80 dark:border-dark-border/80 shadow-sm shadow-black/5'
        : 'bg-white/60 dark:bg-dark-card/60 backdrop-blur-md border-b border-transparent'"
    >
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between gap-4">
        <!-- Logo -->
        <div class="flex items-center cursor-pointer group flex-shrink-0" @click="router.push('/')">
          <img src="/logo.svg" alt="Logo" class="w-9 h-9 rounded-xl shadow-md group-hover:shadow-lg transition-shadow duration-200 object-contain" />
          <span class="ml-2 text-base font-bold text-gray-900 dark:text-dark-text-primary whitespace-nowrap hidden sm:inline">赵先生主页</span>
        </div>

        <!-- 桌面端导航菜单 -->
        <nav class="hidden md:flex items-center gap-0.5 flex-1 justify-center min-w-0 overflow-hidden">
          <router-link
            v-for="item in menuItems"
            :key="item.key"
            :to="item.key"
            class="nav-link relative px-2.5 py-2 rounded-lg text-sm text-gray-600 dark:text-dark-text-secondary hover:text-gray-900 dark:hover:text-dark-text-primary hover:bg-gray-100/80 dark:hover:bg-dark-elevated/80 transition-all duration-300 font-medium whitespace-nowrap flex-shrink-0"
            :class="{ 'nav-active': route.path === item.key }"
          >
            {{ item.label }}
            <span
              v-if="route.path === item.key"
              class="absolute bottom-0 left-1/2 -translate-x-1/2 w-5 h-0.5 bg-gradient-to-r from-blue-500 to-violet-500 rounded-full"
            ></span>
          </router-link>
        </nav>

        <!-- 右侧操作 -->
        <div class="flex items-center gap-1.5 flex-shrink-0">
          <!-- 搜索按钮 -->
          <a-button
            type="text"
            class="!rounded-lg hidden lg:!flex"
            @click="globalSearchRef?.openSearch()"
          >
            <template #icon>
              <SearchOutlined />
            </template>
          </a-button>

          <!-- 暗黑模式切换 -->
          <ThemeToggle />

          <!-- 后台入口 -->
          <a-button
            v-if="userStore.isLoggedIn"
            type="primary"
            class="!rounded-lg !hidden md:!flex"
            @click="goToAdmin"
          >
            <template #icon>
              <SettingOutlined />
            </template>
            后台管理
          </a-button>

          <!-- 用户信息 / 登录 -->
          <a-dropdown v-if="userStore.isLoggedIn">
            <div class="hidden md:flex items-center cursor-pointer px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-dark-elevated transition-colors duration-200">
              <a-avatar :src="userStore.avatar" size="small" class="ring-2 ring-gray-100 dark:ring-dark-border">
                {{ userStore.nickname?.charAt(0) }}
              </a-avatar>
              <span class="ml-2 text-gray-700 dark:text-dark-text-secondary font-medium">{{ userStore.nickname }}</span>
            </div>
            <template #overlay>
              <a-menu class="!rounded-xl !shadow-lg">
                <a-menu-item key="profile" @click="router.push('/admin/profile')">
                  <UserOutlined class="mr-2" />
                  个人中心
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="userStore.logout()">
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-button v-else type="primary" class="!rounded-lg !hidden md:!flex" @click="router.push('/login')">
            登录
          </a-button>

          <!-- 移动端菜单按钮 -->
          <a-button
            type="text"
            class="!flex md:!hidden"
            @click="mobileMenuVisible = !mobileMenuVisible"
          >
            <template #icon>
              <MenuOutlined v-if="!mobileMenuVisible" />
              <CloseOutlined v-else />
            </template>
          </a-button>
        </div>
      </div>
    </header>

    <!-- 内容区域 -->
    <main class="pt-16 pb-16 lg:pb-0">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部 -->
    <AppFooter />

    <!-- 移动端底部导航 -->
    <MobileTabBar />

    <!-- 全站搜索 -->
    <GlobalSearch ref="globalSearchRef" />

    <!-- 返回顶部按钮 -->
    <transition name="backtop-fade">
      <div
        v-if="showBackToTop"
        class="back-to-top visible"
        @click="scrollToTop"
      >
        <svg class="progress-ring" width="48" height="48">
          <defs>
            <linearGradient id="progressGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stop-color="#3b82f6" />
              <stop offset="100%" stop-color="#8b5cf6" />
            </linearGradient>
          </defs>
          <circle class="ring-bg" cx="24" cy="24" r="22" />
          <circle
            class="ring-progress"
            cx="24" cy="24" r="22"
            :stroke-dasharray="ringCircumference"
            :stroke-dashoffset="ringOffset"
          />
        </svg>
        <ArrowUpOutlined class="arrow-icon text-sm" />
      </div>
    </transition>
  </a-layout>

  <!-- 移动端菜单：Teleport 到 body，完全脱离 a-layout 的 flex 布局影响 -->
  <Teleport to="body">
    <transition name="mobile-menu-fade">
      <div
        v-if="mobileMenuVisible"
        class="mobile-menu-overlay md:hidden"
        @click.self="mobileMenuVisible = false"
      >
        <div class="mobile-menu-panel">
          <nav class="space-y-1">
            <a
              v-for="item in menuItems"
              :key="item.key"
              href="javascript:void(0)"
              class="flex items-center px-4 py-3 rounded-xl text-gray-600 dark:text-dark-text-secondary hover:text-gray-900 dark:hover:text-dark-text-primary hover:bg-gray-100 dark:hover:bg-dark-elevated transition-all duration-200"
              :class="{ '!text-blue-600 dark:!text-blue-400 !bg-blue-50 dark:!bg-blue-900/20': route.path === item.key }"
              @click="handleMenuClick(item.key)"
            >
              <component :is="item.icon" class="mr-3" />
              {{ item.label }}
            </a>
          </nav>
          <div class="mt-3 pt-3 border-t border-gray-100 dark:border-dark-border">
            <a-button
              v-if="userStore.isLoggedIn"
              type="primary"
              block
              class="!rounded-xl"
              @click="goToAdmin(); mobileMenuVisible = false"
            >
              <template #icon>
                <SettingOutlined />
              </template>
              后台管理
            </a-button>
            <a-button
              v-else
              type="primary"
              block
              class="!rounded-xl"
              @click="router.push('/login'); mobileMenuVisible = false"
            >
              登录
            </a-button>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.nav-active {
  color: #2563eb;
  background-color: rgba(59, 130, 246, 0.08);
}

:deep(.dark) .nav-active {
  color: #60a5fa;
  background-color: rgba(59, 130, 246, 0.15);
}

.nav-link {
  position: relative;
  overflow: hidden;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
  transition: all 0.3s ease;
  transform: translateX(-50%);
  border-radius: 1px;
}

.nav-link:hover::after {
  width: 60%;
}

.nav-active::after {
  width: 0 !important;
}

.backtop-fade-enter-active,
.backtop-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.backtop-fade-enter-from,
.backtop-fade-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.8);
}
</style>

<style>
/* 移动端菜单：全局样式（因为 Teleport 到 body，scoped 不生效）
   核心要点：
   1. overlay 是全屏 fixed 遮罩 + flex 布局，顶部留出 header 高度
   2. panel 是可滚动容器，菜单项和登录按钮都在里面，一起滚动
   3. 不锁定 body，不影响页面原有滚动行为 */
.mobile-menu-overlay {
  position: fixed;
  top: 4rem;        /* 64px，留出 header */
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  -webkit-overflow-scrolling: touch;
}

.mobile-menu-panel {
  background: #fff;
  padding: 1rem 1.5rem 1.5rem;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
  flex-shrink: 1;
  min-height: 0;
  max-height: 100%;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

html.dark .mobile-menu-panel {
  background: #1a1f2e;
}

/* 滚动条样式 */
.mobile-menu-panel::-webkit-scrollbar {
  width: 3px;
}
.mobile-menu-panel::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
}
html.dark .mobile-menu-panel::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
}

/* 菜单进场/离场动画 */
.mobile-menu-fade-enter-active,
.mobile-menu-fade-leave-active {
  transition: opacity 0.25s ease;
}
.mobile-menu-fade-enter-from,
.mobile-menu-fade-leave-to {
  opacity: 0;
}
.mobile-menu-fade-enter-active .mobile-menu-panel,
.mobile-menu-fade-leave-active .mobile-menu-panel {
  transition: transform 0.25s ease;
}
.mobile-menu-fade-enter-from .mobile-menu-panel,
.mobile-menu-fade-leave-to .mobile-menu-panel {
  transform: translateY(-12px);
}
</style>
