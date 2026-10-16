<script setup lang="ts">
withDefaults(defineProps<{
  type?: 'article' | 'project' | 'profile' | 'detail' | 'timeline'
  count?: number
}>(), {
  type: 'article',
  count: 3,
})
</script>

<template>
  <!-- 文章骨架屏 -->
  <div v-if="type === 'article'" class="space-y-6">
    <div v-for="i in count" :key="i" class="bg-white dark:bg-dark-card rounded-2xl overflow-hidden border border-gray-100 dark:border-dark-border">
      <div class="flex flex-col sm:flex-row">
        <div class="sm:w-72 h-48 sm:h-auto skeleton-shimmer flex-shrink-0"></div>
        <div class="flex-1 p-6 space-y-4">
          <div class="h-5 skeleton-shimmer rounded-lg w-3/4"></div>
          <div class="space-y-2">
            <div class="h-3 skeleton-shimmer rounded-lg w-full"></div>
            <div class="h-3 skeleton-shimmer rounded-lg w-5/6"></div>
          </div>
          <div class="flex gap-2">
            <div class="h-6 w-16 skeleton-shimmer rounded-full"></div>
            <div class="h-6 w-20 skeleton-shimmer rounded-full"></div>
          </div>
          <div class="flex items-center gap-4">
            <div class="h-3 w-20 skeleton-shimmer rounded-lg"></div>
            <div class="h-3 w-16 skeleton-shimmer rounded-lg"></div>
            <div class="h-3 w-16 skeleton-shimmer rounded-lg"></div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 项目骨架屏 -->
  <div v-else-if="type === 'project'" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
    <div v-for="i in count" :key="i" class="bg-white dark:bg-dark-card rounded-2xl overflow-hidden border border-gray-100 dark:border-dark-border">
      <div class="h-44 skeleton-shimmer"></div>
      <div class="p-5 space-y-3">
        <div class="h-5 skeleton-shimmer rounded-lg w-3/4"></div>
        <div class="space-y-2">
          <div class="h-3 skeleton-shimmer rounded-lg w-full"></div>
          <div class="h-3 skeleton-shimmer rounded-lg w-2/3"></div>
        </div>
        <div class="flex gap-2">
          <div class="h-6 w-14 skeleton-shimmer rounded-full"></div>
          <div class="h-6 w-16 skeleton-shimmer rounded-full"></div>
        </div>
        <div class="h-3 w-24 skeleton-shimmer rounded-lg"></div>
      </div>
    </div>
  </div>

  <!-- 个人信息骨架屏 -->
  <div v-else-if="type === 'profile'" class="max-w-4xl mx-auto space-y-8">
    <!-- 顶部横幅 -->
    <div class="h-48 skeleton-shimmer rounded-2xl"></div>
    <!-- 信息卡片 -->
    <div class="bg-white dark:bg-dark-card rounded-2xl p-8 border border-gray-100 dark:border-dark-border">
      <div class="flex flex-col sm:flex-row items-center gap-6">
        <div class="w-28 h-28 skeleton-shimmer rounded-full flex-shrink-0"></div>
        <div class="space-y-3 w-full">
          <div class="h-6 skeleton-shimmer rounded-lg w-48"></div>
          <div class="h-4 skeleton-shimmer rounded-lg w-32"></div>
          <div class="h-4 skeleton-shimmer rounded-lg w-64"></div>
        </div>
      </div>
      <div class="mt-6 space-y-3">
        <div class="h-3 skeleton-shimmer rounded-lg w-full"></div>
        <div class="h-3 skeleton-shimmer rounded-lg w-5/6"></div>
        <div class="h-3 skeleton-shimmer rounded-lg w-2/3"></div>
      </div>
    </div>
    <!-- 获奖时间线 -->
    <div class="space-y-4">
      <div class="h-6 w-32 skeleton-shimmer rounded-lg"></div>
      <div v-for="i in 3" :key="i" class="flex gap-4">
        <div class="w-4 h-4 skeleton-shimmer rounded-full flex-shrink-0 mt-1"></div>
        <div class="space-y-2 flex-1">
          <div class="h-4 skeleton-shimmer rounded-lg w-40"></div>
          <div class="h-3 skeleton-shimmer rounded-lg w-56"></div>
        </div>
      </div>
    </div>
  </div>

  <!-- 详情页骨架屏 -->
  <div v-else-if="type === 'detail'" class="max-w-4xl mx-auto space-y-6">
    <div class="h-64 skeleton-shimmer rounded-2xl"></div>
    <div class="bg-white dark:bg-dark-card rounded-2xl p-8 border border-gray-100 dark:border-dark-border space-y-6">
      <div class="flex gap-2">
        <div class="h-6 w-16 skeleton-shimmer rounded-full"></div>
        <div class="h-6 w-20 skeleton-shimmer rounded-full"></div>
      </div>
      <div class="h-8 skeleton-shimmer rounded-lg w-3/4"></div>
      <div class="flex items-center gap-4">
        <div class="h-4 w-24 skeleton-shimmer rounded-lg"></div>
        <div class="h-4 w-20 skeleton-shimmer rounded-lg"></div>
      </div>
      <div class="space-y-3 pt-4 border-t border-gray-100 dark:border-dark-border">
        <div v-for="i in 8" :key="i" class="h-3.5 skeleton-shimmer rounded-lg" :style="{ width: (70 + Math.random() * 30) + '%' }"></div>
      </div>
    </div>
  </div>

  <!-- 时间线骨架屏 -->
  <div v-else-if="type === 'timeline'" class="max-w-3xl mx-auto space-y-8">
    <div class="h-8 w-48 skeleton-shimmer rounded-lg mx-auto"></div>
    <div v-for="i in count" :key="i" class="flex gap-6">
      <div class="flex flex-col items-center">
        <div class="w-10 h-10 skeleton-shimmer rounded-full flex-shrink-0"></div>
        <div v-if="i < count" class="w-0.5 flex-1 skeleton-shimmer mt-2"></div>
      </div>
      <div class="flex-1 pb-8 space-y-3">
        <div class="h-5 skeleton-shimmer rounded-lg w-40"></div>
        <div class="h-4 skeleton-shimmer rounded-lg w-32"></div>
        <div class="space-y-2">
          <div class="h-3 skeleton-shimmer rounded-lg w-full"></div>
          <div class="h-3 skeleton-shimmer rounded-lg w-4/5"></div>
        </div>
      </div>
    </div>
  </div>
</template>
