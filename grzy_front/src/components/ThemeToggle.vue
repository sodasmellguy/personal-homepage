<script setup lang="ts">
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>

<template>
  <button
    class="theme-toggle-btn"
    :class="{ 'is-dark': appStore.isDark }"
    @click="appStore.toggleTheme"
    aria-label="切换主题"
  >
    <div class="toggle-track">
      <div class="toggle-thumb">
        <!-- 太阳 -->
        <svg v-if="!appStore.isDark" class="icon sun-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="5" />
          <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
        </svg>
        <!-- 月亮 -->
        <svg v-else class="icon moon-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" />
        </svg>
      </div>
    </div>
  </button>
</template>

<style scoped>
.theme-toggle-btn {
  position: relative;
  width: 52px;
  height: 28px;
  border: none;
  cursor: pointer;
  background: transparent;
  padding: 0;
  border-radius: 14px;
  outline: none;
}

.toggle-track {
  width: 100%;
  height: 100%;
  border-radius: 14px;
  /* 亮色：蓝→靛（与全站主色同族），避免琥珀黄与整体色调冲突 */
  background: linear-gradient(135deg, #60a5fa, #818cf8);
  transition: background 0.4s ease;
  position: relative;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.1);
}

.is-dark .toggle-track {
  background: linear-gradient(135deg, #3b82f6, #6366f1);
}

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.4s cubic-bezier(0.68, -0.15, 0.27, 1.15);
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
}

.is-dark .toggle-thumb {
  left: calc(100% - 26px);
}

.icon {
  width: 14px;
  height: 14px;
  transition: all 0.3s ease;
}

.sun-icon {
  color: #f59e0b;
}

.moon-icon {
  color: #6366f1;
}

.theme-toggle-btn:hover .toggle-thumb {
  transform: scale(1.1);
}
</style>
