<script setup lang="ts">
import { ref, reactive, computed, watch, h, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  DashboardOutlined,
  UserOutlined,
  ThunderboltOutlined,
  ProjectOutlined,
  SolutionOutlined,
  TrophyOutlined,
  TeamOutlined,
  FileTextOutlined,
  CommentOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  HomeOutlined,
  SafetyOutlined,
  ClockCircleOutlined,
  AppstoreOutlined,
  LockOutlined,
  FileSearchOutlined,
  ReadOutlined,
  EnvironmentOutlined,
  LinkOutlined,
  CameraOutlined,
  BellOutlined,
  LaptopOutlined,
  GithubOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { changePassword } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const selectedKeys = ref<string[]>([route.path])

// ==================== 移动端检测 ====================
const isMobile = ref(false)
const mobileSidebarOpen = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    mobileSidebarOpen.value = false
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// 侧边栏样式
const sidebarStyle = computed(() => {
  if (isMobile.value) {
    return {
      width: '240px',
      transform: mobileSidebarOpen.value ? 'translateX(0)' : 'translateX(-100%)',
    }
  }
  return {
    width: appStore.sidebarCollapsed ? '76px' : '240px',
    transform: 'translateX(0)',
  }
})

const contentMarginLeft = computed(() => {
  if (isMobile.value) return '0px'
  return appStore.sidebarCollapsed ? '64px' : '240px'
})

// 菜单折叠状态：移动端始终展开
const menuCollapsed = computed(() => {
  if (isMobile.value) return false
  return appStore.sidebarCollapsed
})

// Logo 区域是否折叠：移动端始终展开
const logoCollapsed = computed(() => {
  if (isMobile.value) return false
  return appStore.sidebarCollapsed
})

// 切换侧边栏
const handleToggleSidebar = () => {
  if (isMobile.value) {
    mobileSidebarOpen.value = !mobileSidebarOpen.value
  } else {
    appStore.toggleSidebar()
  }
}

// 监听路由变化
watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
    // 移动端路由跳转后关闭侧边栏
    if (isMobile.value) {
      mobileSidebarOpen.value = false
    }
  }
)

// 菜单项 - 展开时带分组，折叠时平铺
// 注意：icon 必须传渲染函数 () => h(...)。折叠模式下菜单项会同时渲染进 Tooltip，
// 直接传 VNode 对象只能渲染一次，会导致折叠后菜单里的图标消失（只剩虚影）。
// 每个菜单项带 permission 标识，按当前用户权限过滤（admin 拥有通配权限 "*" 显示全部）
const menuItems = computed(() => {
  const can = (permission?: string) => !permission || userStore.hasPermission(permission)

  const dashboardItem = {
    key: '/admin/dashboard',
    icon: () => h(DashboardOutlined),
    label: '仪表盘',
    permission: 'system:dashboard:view',
  }
  // 内容管理
  const contentItems = [
    { key: '/admin/profile', icon: () => h(UserOutlined), label: '个人信息', permission: 'content:profile:view' },
    { key: '/admin/skill', icon: () => h(ThunderboltOutlined), label: '技能管理', permission: 'content:skill:view' },
    { key: '/admin/techstack', icon: () => h(AppstoreOutlined), label: '技术栈管理', permission: 'content:techstack:view' },
    { key: '/admin/project', icon: () => h(ProjectOutlined), label: '项目管理', permission: 'content:project:view' },
    { key: '/admin/internship', icon: () => h(SolutionOutlined), label: '实习管理', permission: 'content:internship:view' },
    { key: '/admin/award', icon: () => h(TrophyOutlined), label: '获奖管理', permission: 'content:award:view' },
    { key: '/admin/album', icon: () => h(CameraOutlined), label: '相册管理', permission: 'content:album:view' },
    { key: '/admin/open-source', icon: () => h(GithubOutlined), label: '开源项目', permission: 'content:opensource:view' },
  ].filter((item) => can(item.permission))
  // 发布管理
  const publishItems = [
    { key: '/admin/article', icon: () => h(FileTextOutlined), label: '文章管理', permission: 'blog:article:view' },
    { key: '/admin/series', icon: () => h(ReadOutlined), label: '专栏管理', permission: 'blog:series:view' },
    { key: '/admin/comment', icon: () => h(CommentOutlined), label: '评论管理', permission: 'blog:comment:view' },
    { key: '/admin/friendlink', icon: () => h(LinkOutlined), label: '友链管理', permission: 'blog:friendlink:view' },
  ].filter((item) => can(item.permission))
  // 系统管理
  const systemItems = [
    { key: '/admin/user', icon: () => h(TeamOutlined), label: '用户管理', permission: 'system:user:view' },
    { key: '/admin/role', icon: () => h(SafetyOutlined), label: '角色管理', permission: 'system:role:view' },
    { key: '/admin/apidoc', icon: () => h(FileSearchOutlined), label: 'API文档', permission: 'system:apidoc:view' },
    { key: '/admin/log', icon: () => h(ClockCircleOutlined), label: '操作日志', permission: 'system:log:view' },
    { key: '/admin/visitor-map', icon: () => h(EnvironmentOutlined), label: '访客地图', permission: 'system:visitor-map:view' },
    { key: '/admin/notify-setting', icon: () => h(BellOutlined), label: '通知设置', permission: 'system:notify:view' },
    { key: '/admin/online-session', icon: () => h(LaptopOutlined), label: '在线会话', permission: 'system:session:view' },
  ].filter((item) => can(item.permission))

  const dashboardVisible = can(dashboardItem.permission)
  const dashboardEntry = dashboardVisible
    ? [{ key: dashboardItem.key, icon: dashboardItem.icon, label: dashboardItem.label }]
    : []
  const groups = [
    ...(contentItems.length ? [contentItems] : []),
    ...(publishItems.length ? [publishItems] : []),
    ...(systemItems.length ? [systemItems] : []),
  ]

  // 无任何可用菜单时（理论上不会发生，路由层已拦截）
  if (!dashboardVisible && groups.length === 0) {
    return []
  }

  // 折叠时返回图标平铺，但保留分组分隔线，保持视觉节奏
  if (appStore.sidebarCollapsed) {
    return [
      ...dashboardEntry,
      ...(dashboardVisible && groups.length ? [{ type: 'divider' as const }] : []),
      ...groups.flatMap((items, index) => {
        const prev = index > 0 ? [{ type: 'divider' as const }] : []
        return [...prev, ...items]
      }),
    ]
  }

  // 展开时带分组
  return [
    ...dashboardEntry,
    ...(dashboardVisible && groups.length ? [{ type: 'divider' as const }] : []),
    ...groups.flatMap((items, index) => {
      const prev = index > 0 ? [{ type: 'divider' as const }] : []
      return [
        ...prev,
        {
          key: `group-${index}`,
          label: ['内容管理', '发布管理', '系统管理'][index],
          type: 'group' as const,
          children: items,
        },
      ]
    }),
  ]
})

// 菜单点击
const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
  if (isMobile.value) {
    mobileSidebarOpen.value = false
  }
}

// 返回前台
const goHome = () => {
  router.push('/')
}

// 退出登录
const handleLogout = async () => {
  await userStore.logout()
}

// 修改密码
const passwordModalVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const openPasswordModal = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordModalVisible.value = true
}

const validateConfirmPassword = (_rule: any, value: string) => {
  if (!value) return Promise.resolve()
  if (value !== passwordForm.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const handlePasswordSubmit = async () => {
  try {
    if (!passwordFormRef.value) return
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    message.success('密码修改成功，请重新登录')
    passwordModalVisible.value = false
    await userStore.logout()
  } catch (error: any) {
    if (error?.message) {
      message.error(error.message)
    }
  } finally {
    passwordLoading.value = false
  }
}
</script>

<template>
  <a-layout class="min-h-screen bg-[#f0f2f5] dark:bg-dark-base">
    <!-- 侧边栏 -->
    <aside
      class="admin-sidebar fixed left-0 top-0 bottom-0 z-50 transition-all duration-300"
      :style="sidebarStyle"
    >
      <!-- Logo 区域 -->
      <div
        class="admin-sidebar-logo h-[56px] flex items-center px-4 border-b border-white/10 transition-all duration-300"
        :class="{ 'admin-sidebar-logo-collapsed': logoCollapsed }"
      >
        <img src="/logo.svg" alt="Logo" class="w-7 h-7 flex-shrink-0 object-contain" />
        <transition name="fade">
          <span
            v-show="!logoCollapsed"
            class="ml-3 text-white text-[15px] font-semibold whitespace-nowrap overflow-hidden tracking-wide"
          >
            后台管理
          </span>
        </transition>
      </div>

      <!-- 菜单 -->
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="inline"
        theme="dark"
        :inline-collapsed="menuCollapsed"
        :items="menuItems"
        class="admin-sidebar-menu"
        @click="handleMenuClick"
      />
    </aside>

    <!-- 移动端遮罩层 -->
    <transition name="fade">
      <div
        v-if="isMobile && mobileSidebarOpen"
        class="admin-sidebar-overlay"
        @click="mobileSidebarOpen = false"
      />
    </transition>

    <!-- 主内容区 -->
    <a-layout
      class="transition-all duration-300"
      :style="{ marginLeft: contentMarginLeft }"
    >
      <!-- 顶部栏 -->
      <header class="admin-header sticky top-0 z-30 h-[56px] px-3 md:px-6 flex items-center justify-between">
        <!-- 左侧：折叠按钮 + 面包屑 -->
        <div class="flex items-center">
          <a-button
            type="text"
            class="admin-header-trigger"
            @click="handleToggleSidebar"
          >
            <template #icon>
              <MenuUnfoldOutlined v-if="(isMobile ? !mobileSidebarOpen : appStore.sidebarCollapsed)" />
              <MenuFoldOutlined v-else />
            </template>
          </a-button>

          <a-breadcrumb class="ml-3 hidden md:block">
            <a-breadcrumb-item>
              <HomeOutlined />
            </a-breadcrumb-item>
            <a-breadcrumb-item>{{ route.meta.title }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <!-- 右侧：主题 + 返回前台 + 用户 -->
        <div class="flex items-center gap-1">
          <!-- 主题切换 -->
          <ThemeToggle />

          <!-- 返回前台 -->
          <a-tooltip title="返回前台">
            <a-button type="text" class="admin-header-action" @click="goHome">
              <template #icon>
                <HomeOutlined />
              </template>
            </a-button>
          </a-tooltip>

          <!-- 用户下拉 -->
          <a-dropdown>
            <div class="admin-header-user flex items-center cursor-pointer ml-2 px-2.5 py-1.5 rounded-lg transition-all duration-200">
              <a-avatar :src="userStore.avatar" :size="28" class="!ring-2 !ring-gray-100 dark:!ring-dark-border">
                {{ userStore.nickname?.charAt(0) }}
              </a-avatar>
              <span class="ml-2 text-sm font-medium hidden md:inline text-gray-600 dark:text-dark-text-secondary">
                {{ userStore.nickname }}
              </span>
            </div>
            <template #overlay>
              <a-menu class="admin-dropdown-menu">
                <a-menu-item key="profile" @click="router.push('/admin/profile')">
                  <UserOutlined />
                  <span class="ml-2">个人中心</span>
                </a-menu-item>
                <a-menu-item v-if="userStore.isLoggedIn" key="password" @click="openPasswordModal">
                  <LockOutlined />
                  <span class="ml-2">修改密码</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined />
                  <span class="ml-2">退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </header>

      <!-- 内容区域 -->
      <main class="p-3 md:p-6 min-h-[calc(100vh-56px)] admin-content-area">
        <router-view v-slot="{ Component }">
          <transition name="admin-page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </a-layout>

    <!-- 修改密码弹窗 -->
    <a-modal
      v-model:open="passwordModalVisible"
      title="修改密码"
      :confirm-loading="passwordLoading"
      @ok="handlePasswordSubmit"
      @cancel="passwordModalVisible = false"
      :ok-button-props="{ htmlType: 'submit' }"
    >
      <a-form
        ref="passwordFormRef"
        :model="passwordForm"
        layout="vertical"
      >
        <a-form-item
          label="旧密码"
          name="oldPassword"
          :rules="[{ required: true, message: '请输入旧密码' }]"
        >
          <a-input-password
            v-model:value="passwordForm.oldPassword"
            placeholder="请输入旧密码"
          />
        </a-form-item>
        <a-form-item
          label="新密码"
          name="newPassword"
          :rules="[
            { required: true, message: '请输入新密码' },
            { min: 6, max: 20, message: '密码长度在 6 到 20 个字符' },
          ]"
        >
          <a-input-password
            v-model:value="passwordForm.newPassword"
            placeholder="请输入新密码（6-20位）"
          />
        </a-form-item>
        <a-form-item
          label="确认新密码"
          name="confirmPassword"
          :rules="[
            { required: true, message: '请再次输入新密码' },
            { validator: validateConfirmPassword },
          ]"
        >
          <a-input-password
            v-model:value="passwordForm.confirmPassword"
            placeholder="请再次输入新密码"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout>
</template>

<style scoped>
/* ==================== 侧边栏 ==================== */
.admin-sidebar {
  background: linear-gradient(180deg, #0c1222 0%, #0a1628 50%, #081420 100%);
  overflow-y: auto;
  overflow-x: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.admin-sidebar::-webkit-scrollbar {
  width: 4px;
}

.admin-sidebar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.12);
  border-radius: 4px;
}

.admin-sidebar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}

.admin-sidebar-logo {
  background: rgba(255, 255, 255, 0.03);
}

.admin-sidebar-menu {
  border-inline-end: none !important;
  background: transparent !important;
  padding: 8px 0;
}

:deep(.admin-sidebar-menu .ant-menu-item) {
  margin: 2px 8px;
  border-radius: 8px;
  height: 40px;
  line-height: 40px;
  color: rgba(255, 255, 255, 0.55) !important;
  transition: all 0.2s ease;
}

:deep(.admin-sidebar-menu .ant-menu-item:hover) {
  color: rgba(255, 255, 255, 0.9) !important;
  background: rgba(255, 255, 255, 0.06) !important;
}

:deep(.admin-sidebar-menu .ant-menu-item-selected) {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(99, 102, 241, 0.15)) !important;
  color: #fff !important;
  font-weight: 500;
  position: relative;
}

:deep(.admin-sidebar-menu .ant-menu-item-selected::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: linear-gradient(180deg, #3b82f6, #6366f1);
  border-radius: 0 3px 3px 0;
  box-shadow: 0 0 8px rgba(99, 102, 241, 0.6);
}

:deep(.admin-sidebar-menu .ant-menu-item-selected::after) {
  display: none;
}

:deep(.admin-sidebar-menu .ant-menu-submenu-title) {
  margin: 2px 8px;
  border-radius: 8px;
  height: 40px;
  line-height: 40px;
}

/* 分组标题样式 */
:deep(.admin-sidebar-menu .ant-menu-item-group-title) {
  color: rgba(255, 255, 255, 0.3) !important;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 12px 16px 4px;
  margin-top: 4px;
}

/* 分隔线 */
:deep(.admin-sidebar-menu .ant-menu-item-divider) {
  border-color: rgba(255, 255, 255, 0.06);
  margin: 4px 16px;
}

/* ==================== 折叠态：图标按钮化 ==================== */
:deep(.admin-sidebar-menu.ant-menu-inline-collapsed) {
  padding: 12px 0;
}

:deep(.admin-sidebar-menu.ant-menu-inline-collapsed .ant-menu-item) {
  margin: 3px auto;
  padding: 0 !important;
  width: 46px;
  display: flex !important;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  height: 42px;
  line-height: 42px;
}

/* 折叠态分隔线：各组之间的视觉节奏 */
:deep(.admin-sidebar-menu.ant-menu-inline-collapsed .ant-menu-item-divider) {
  margin: 8px 24px;
  border-color: rgba(255, 255, 255, 0.1);
  list-style: none;
}

:deep(.admin-sidebar-menu.ant-menu-inline-collapsed .ant-menu-item .ant-menu-item-icon) {
  margin-inline-end: 0;
  margin-inline-start: 0;
  font-size: 18px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;
}

:deep(.admin-sidebar-menu.ant-menu-inline-collapsed .ant-menu-item .ant-menu-item-icon svg) {
  display: block;
}

/* 折叠态关键修复：隐藏标题文字占位。
   antd-vue 4.1.2 折叠模式不会隐藏 .ant-menu-title-content，
   文字会把图标挤出图标块，导致图标只剩被裁切的"虚影" */
:deep(.admin-sidebar-menu.ant-menu-inline-collapsed .ant-menu-title-content) {
  display: none !important;
}

/* 折叠态 hover：图标微放大 + 背景提亮 */
:deep(.admin-sidebar-menu.ant-menu-inline-collapsed .ant-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
}

:deep(.admin-sidebar-menu.ant-menu-inline-collapsed .ant-menu-item:hover .ant-menu-item-icon) {
  transform: scale(1.15);
}

:deep(.admin-sidebar-menu.ant-menu-inline-collapsed .ant-menu-item-selected::before) {
  display: none;
}

/* 折叠态选中：渐变块 + 光晕 */
:deep(.admin-sidebar-menu.ant-menu-inline-collapsed .ant-menu-item-selected) {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.85), rgba(99, 102, 241, 0.75)) !important;
  color: #fff !important;
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.4);
}

:deep(.admin-sidebar-menu.ant-menu-inline-collapsed .ant-menu-item-selected .ant-menu-item-icon) {
  transform: scale(1.05);
}

/* 折叠时 Logo 居中 */
.admin-sidebar-logo-collapsed {
  justify-content: center;
  padding-left: 0 !important;
  padding-right: 0 !important;
}

/* ==================== Header ==================== */
.admin-header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(240, 240, 240, 0.8);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
}

:global(html.dark .admin-header) {
  background: linear-gradient(180deg, rgba(16, 24, 44, 0.88) 0%, rgba(12, 18, 34, 0.85) 100%);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom-color: rgba(255, 255, 255, 0.06);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    0 2px 8px rgba(0, 0, 0, 0.35);
}

.admin-header-trigger {
  font-size: 16px;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.admin-header-trigger:hover {
  background: rgba(0, 0, 0, 0.04);
  color: #3b82f6;
}

:global(html.dark .admin-header-trigger:hover) {
  background: rgba(255, 255, 255, 0.06);
  color: #60a5fa;
}

.admin-header-action {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.2s ease;
}

.admin-header-action:hover {
  background: rgba(0, 0, 0, 0.04);
  color: #3b82f6;
}

:global(html.dark .admin-header-action:hover) {
  background: rgba(255, 255, 255, 0.06);
  color: #60a5fa;
}

.admin-header-user:hover {
  background: rgba(59, 130, 246, 0.06);
}

:global(html.dark .admin-header-user:hover) {
  background: rgba(59, 130, 246, 0.1);
}

/* ==================== 下拉菜单 ==================== */
.admin-dropdown-menu {
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
  min-width: 160px;
  padding: 4px;
}

.admin-dropdown-menu :deep(.ant-dropdown-menu-item) {
  border-radius: 8px;
  margin: 2px 4px;
  padding: 8px 12px;
}

.admin-dropdown-menu :deep(.ant-dropdown-menu-item:hover) {
  background: #f5f5f5;
}

/* ==================== 内容区背景纹理 ==================== */
.admin-content-area {
  position: relative;
}

.admin-content-area::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(180deg, rgba(59, 130, 246, 0.03) 0%, transparent 100%);
  pointer-events: none;
  z-index: 0;
}

:global(html.dark .admin-content-area::before) {
  background: linear-gradient(180deg, rgba(59, 130, 246, 0.05) 0%, transparent 100%);
}

.admin-content-area > * {
  position: relative;
  z-index: 1;
}

/* ==================== 页面切换过渡动画 ==================== */
.admin-page-enter-active {
  transition: opacity 0.3s ease, transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.admin-page-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.admin-page-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.admin-page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ==================== Logo 文字过渡 ==================== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ==================== 移动端遮罩层 ==================== */
.admin-sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 45;
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
}

/* ==================== 移动端响应式优化 ==================== */
@media (max-width: 767px) {
  /* Header 在移动端的优化 */
  .admin-header {
    padding-left: 12px !important;
    padding-right: 12px !important;
  }

  .admin-header-trigger {
    width: 36px;
    height: 36px;
    font-size: 15px;
  }

  .admin-header-action {
    width: 32px;
    height: 32px;
    font-size: 15px;
  }

  /* 用户区域在移动端隐藏昵称 */
  .admin-header-user {
    padding: 4px !important;
    margin-left: 0 !important;
  }

  /* 侧边栏菜单在移动端的优化 */
  .admin-sidebar-menu {
    padding: 4px 0;
  }

  :deep(.admin-sidebar-menu .ant-menu-item) {
    margin: 2px 8px;
    height: 42px;
    line-height: 42px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 380px) {
  .admin-header {
    padding-left: 8px !important;
    padding-right: 8px !important;
  }
}
</style>
