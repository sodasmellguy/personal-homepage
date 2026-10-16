<script setup lang="ts">
import { computed } from 'vue'
import { theme as antTheme } from 'ant-design-vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const antThemeConfig = computed(() => ({
  algorithm: appStore.isDark ? antTheme.darkAlgorithm : antTheme.defaultAlgorithm,
  token: {
    colorPrimary: '#3b82f6',
    borderRadius: 8,
  },
}))
</script>

<template>
  <a-config-provider :theme="antThemeConfig" :locale="zhCN">
    <router-view />
  </a-config-provider>
</template>

<style>
#app {
  min-height: 100vh;
}
</style>
