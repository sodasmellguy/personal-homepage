<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { HomeOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { useHead } from '@unhead/vue'

useHead({
  title: '404 - 页面未找到 | 赵先生',
})

const router = useRouter()
const searchText = ref('')

const goHome = () => {
  router.push('/')
}

const handleSearch = () => {
  if (searchText.value.trim()) {
    router.push({ path: '/article', query: { q: searchText.value.trim() } })
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-dark-base flex items-center justify-center p-4 transition-colors duration-300">
    <div class="text-center max-w-lg">
      <!-- 趣味 404 插画 -->
      <div class="relative mb-8">
        <div class="text-8xl sm:text-[10rem] font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 via-purple-500 to-pink-500 leading-none select-none">
          404
        </div>
        <!-- 浮动装饰元素 -->
        <div class="absolute top-0 left-8 w-6 h-6 bg-blue-400/30 rounded-full animate-bounce" style="animation-delay: 0s;"></div>
        <div class="absolute top-8 right-12 w-4 h-4 bg-purple-400/30 rounded-full animate-bounce" style="animation-delay: 0.2s;"></div>
        <div class="absolute bottom-4 left-1/4 w-3 h-3 bg-pink-400/30 rounded-full animate-bounce" style="animation-delay: 0.4s;"></div>
        <div class="absolute top-1/2 right-8 w-5 h-5 bg-indigo-400/20 rounded-lg rotate-45 animate-pulse"></div>
      </div>

      <h2 class="text-xl sm:text-2xl font-semibold text-gray-800 dark:text-dark-text-primary mb-3">页面未找到</h2>
      <p class="text-gray-500 dark:text-dark-text-muted mb-8">
        抱歉，您访问的页面不存在或已被移除。
      </p>

      <!-- 搜索框 -->
      <div class="mb-8 max-w-xs mx-auto">
        <a-input-search
          v-model:value="searchText"
          placeholder="搜索文章..."
          size="large"
          class="!rounded-xl"
          allow-clear
          @search="handleSearch"
        >
          <template #prefix>
            <SearchOutlined class="text-gray-400" />
          </template>
        </a-input-search>
      </div>

      <!-- 操作按钮 -->
      <div class="flex justify-center gap-3">
        <a-button type="primary" size="large" class="!rounded-xl" @click="goHome">
          <template #icon>
            <HomeOutlined />
          </template>
          返回首页
        </a-button>
        <a-button size="large" class="!rounded-xl" @click="router.push('/article')">
          浏览博客
        </a-button>
      </div>
    </div>
  </div>
</template>
