<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeOutlined,
  ProjectOutlined,
  ReadOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()

const tabs = [
  { key: '/home', icon: HomeOutlined, label: '首页' },
  { key: '/project', icon: ProjectOutlined, label: '项目' },
  { key: '/article', icon: ReadOutlined, label: '博客' },
  { key: '/profile', icon: UserOutlined, label: '我的' },
]

const currentPath = computed(() => route.path)

const isActive = (key: string) => {
  return currentPath.value === key || currentPath.value.startsWith(key + '/')
}

const handleClick = (key: string) => {
  router.push(key)
}
</script>

<template>
  <nav class="lg:hidden fixed bottom-0 left-0 right-0 z-50 bg-white/95 dark:bg-dark-card/95 backdrop-blur-xl border-t border-gray-200/60 dark:border-dark-border/60" style="padding-bottom: env(safe-area-inset-bottom, 0px)">
    <div class="flex items-center justify-around h-14 max-w-lg mx-auto">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="relative flex flex-col items-center justify-center gap-0.5 flex-1 h-full transition-all duration-300"
        :class="isActive(tab.key) ? 'text-blue-600 dark:text-blue-400 scale-105' : 'text-gray-400 dark:text-dark-text-muted'"
        @click="handleClick(tab.key)"
      >
        <component :is="tab.icon" class="text-lg transition-transform duration-300" :class="isActive(tab.key) ? 'scale-110' : ''" />
        <span class="text-[10px] font-medium leading-tight">{{ tab.label }}</span>
        <transition name="tab-indicator">
          <span
            v-if="isActive(tab.key)"
            class="absolute bottom-1 w-5 h-0.5 bg-gradient-to-r from-blue-500 to-violet-500 rounded-full"
          ></span>
        </transition>
      </button>
    </div>
  </nav>
</template>

<style scoped>
.tab-indicator-enter-active,
.tab-indicator-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.tab-indicator-enter-from {
  opacity: 0;
  transform: scaleX(0);
}

.tab-indicator-leave-to {
  opacity: 0;
  transform: scaleX(0);
}
</style>
