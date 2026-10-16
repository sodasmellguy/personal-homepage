<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { EyeOutlined, FireOutlined, TrophyOutlined, LikeOutlined } from '@ant-design/icons-vue'
import { getTopArticles, getMonthlyHotArticles } from '@/api/article'

interface RankItem {
  id: number
  title: string
  viewCount: number
  likeCount?: number
  hotScore?: number
}

const router = useRouter()
const topList = ref<RankItem[]>([])
const monthlyList = ref<RankItem[]>([])

const fetchData = async () => {
  try {
    const [topRes, hotRes] = await Promise.allSettled([
      getTopArticles(5),
      getMonthlyHotArticles(5),
    ])
    if (topRes.status === 'fulfilled') topList.value = topRes.value.data || []
    if (hotRes.status === 'fulfilled') monthlyList.value = hotRes.value.data || []
  } catch (error) {
    console.error('获取文章排行失败:', error)
  }
}

const goDetail = (id: number) => {
  router.push(`/article/${id}`)
}

onMounted(fetchData)
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
    <!-- 阅读量排行 TOP5（仅一个榜单有数据时占满整行） -->
    <div
      v-if="topList.length"
      class="bg-white dark:bg-dark-card rounded-2xl border border-gray-100 dark:border-dark-border p-5 shadow-sm"
      :class="topList.length && monthlyList.length ? '' : 'md:col-span-2'"
    >
      <div class="flex items-center gap-2 mb-4">
        <span class="w-7 h-7 rounded-lg bg-amber-50 dark:bg-amber-900/20 flex items-center justify-center">
          <TrophyOutlined class="text-amber-500" />
        </span>
        <h3 class="text-sm font-semibold text-gray-800 dark:text-dark-text-primary">阅读量排行 TOP5</h3>
      </div>
      <div class="space-y-1">
        <div
          v-for="(article, index) in topList"
          :key="article.id"
          class="flex items-center gap-3 py-2 px-2 -mx-2 rounded-lg cursor-pointer group hover:bg-gray-50 dark:hover:bg-dark-elevated/60 transition-colors"
          @click="goDetail(article.id)"
        >
          <span
            class="w-6 h-6 rounded-md flex items-center justify-center text-xs font-bold flex-shrink-0"
            :class="index < 3
              ? 'bg-gradient-to-br from-amber-400 to-orange-500 text-white'
              : 'bg-gray-100 dark:bg-dark-elevated text-gray-400 dark:text-dark-text-muted'"
          >
            {{ index + 1 }}
          </span>
          <span class="flex-1 text-sm text-gray-700 dark:text-dark-text-secondary truncate group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
            {{ article.title }}
          </span>
          <span class="flex items-center gap-1 text-xs text-gray-400 dark:text-dark-text-muted flex-shrink-0">
            <EyeOutlined />
            {{ article.viewCount }}
          </span>
        </div>
      </div>
    </div>

    <!-- 本月热门（仅一个榜单有数据时占满整行） -->
    <div
      v-if="monthlyList.length"
      class="bg-white dark:bg-dark-card rounded-2xl border border-gray-100 dark:border-dark-border p-5 shadow-sm"
      :class="topList.length && monthlyList.length ? '' : 'md:col-span-2'"
    >
      <div class="flex items-center gap-2 mb-4">
        <span class="w-7 h-7 rounded-lg bg-red-50 dark:bg-red-900/20 flex items-center justify-center">
          <FireOutlined class="text-red-500" />
        </span>
        <h3 class="text-sm font-semibold text-gray-800 dark:text-dark-text-primary">本月热门</h3>
      </div>
      <div class="space-y-1">
        <div
          v-for="(article, index) in monthlyList"
          :key="article.id"
          class="flex items-center gap-3 py-2 px-2 -mx-2 rounded-lg cursor-pointer group hover:bg-gray-50 dark:hover:bg-dark-elevated/60 transition-colors"
          @click="goDetail(article.id)"
        >
          <span
            class="w-6 h-6 rounded-md flex items-center justify-center text-xs font-bold flex-shrink-0"
            :class="index < 3
              ? 'bg-gradient-to-br from-red-400 to-pink-500 text-white'
              : 'bg-gray-100 dark:bg-dark-elevated text-gray-400 dark:text-dark-text-muted'"
          >
            {{ index + 1 }}
          </span>
          <span class="flex-1 text-sm text-gray-700 dark:text-dark-text-secondary truncate group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
            {{ article.title }}
          </span>
          <span class="flex items-center gap-1 text-xs text-gray-400 dark:text-dark-text-muted flex-shrink-0">
            <LikeOutlined />
            {{ article.hotScore ?? article.likeCount ?? 0 }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
