<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useHead } from '@unhead/vue'
import { ArrowLeftOutlined, FileTextOutlined, ReadOutlined } from '@ant-design/icons-vue'
import { getFrontSeriesDetail } from '@/api/series'
import type { SeriesDetail as SeriesDetailType } from '@/api/series'
import SkeletonCard from '@/components/SkeletonCard.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const series = ref<SeriesDetailType | null>(null)

const seriesId = Number(route.params.id)

useHead({
  title: () => series.value ? `${series.value.name} - 专栏系列 - 赵先生` : '专栏详情 - 赵先生',
  meta: [
    { name: 'description', content: () => series.value?.description || '专栏详情' },
  ],
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const { data } = await getFrontSeriesDetail(seriesId)
    series.value = data
  } catch (error) {
    console.error('获取专栏详情失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})

const goToArticle = (id: number) => {
  router.push(`/article/${id}`)
}
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-dark-base transition-colors duration-300">
    <SkeletonCard v-if="loading" type="detail" />
    <div v-else-if="series" class="max-w-4xl mx-auto px-4 sm:px-8 py-10">
      <!-- 返回按钮 -->
      <a-button
        type="text"
        class="!mb-8 !rounded-xl !text-gray-500 hover:!bg-gray-100 dark:hover:!bg-dark-elevated dark:!text-dark-text-muted"
        @click="router.push('/series')"
      >
        <template #icon>
          <ArrowLeftOutlined />
        </template>
        返回专栏列表
      </a-button>

      <!-- 专栏头部 -->
      <div class="mb-10">
        <!-- 封面图 -->
        <div v-if="series.coverImage" class="rounded-2xl overflow-hidden mb-8">
          <img
            :src="series.coverImage"
            :alt="series.name"
            loading="lazy"
            class="w-full h-auto max-h-72 object-cover"
          />
        </div>
        <div
          v-else
          class="rounded-2xl overflow-hidden mb-8 h-48 bg-gradient-to-br from-blue-500 to-violet-500 flex items-center justify-center"
        >
          <ReadOutlined class="text-white text-6xl" />
        </div>

        <!-- 标题 -->
        <h1 class="text-3xl sm:text-4xl font-bold text-gray-900 dark:text-dark-text-primary leading-tight tracking-tight mb-4">
          {{ series.name }}
        </h1>

        <!-- 描述 -->
        <p v-if="series.description" class="text-gray-500 dark:text-dark-text-secondary text-lg mb-4 leading-relaxed">
          {{ series.description }}
        </p>

        <!-- 文章数 -->
        <div class="flex items-center text-sm text-gray-400 dark:text-dark-text-muted pb-8 border-b border-gray-100 dark:border-dark-border">
          <FileTextOutlined class="mr-1.5" />
          共 {{ series.articles?.length || 0 }} 篇文章
        </div>
      </div>

      <!-- 文章列表 -->
      <div v-if="series.articles?.length" class="space-y-4">
        <div
          v-for="(article, index) in series.articles"
          :key="article.id"
          class="flex items-start gap-4 p-5 rounded-xl border border-gray-100 dark:border-dark-border hover:border-gray-200 dark:hover:border-gray-600 transition-all duration-200 cursor-pointer group"
          @click="goToArticle(article.id)"
        >
          <!-- 序号 -->
          <div class="flex-shrink-0 w-8 h-8 rounded-lg bg-gray-100 dark:bg-dark-elevated flex items-center justify-center text-sm font-semibold text-gray-500 dark:text-dark-text-muted group-hover:bg-blue-50 dark:group-hover:bg-blue-900/20 group-hover:text-blue-500 transition-colors">
            {{ index + 1 }}
          </div>

          <!-- 内容 -->
          <div class="flex-1 min-w-0">
            <h3 class="text-base font-medium text-gray-900 dark:text-dark-text-primary group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors mb-1">
              {{ article.title }}
            </h3>
            <p v-if="article.summary" class="text-sm text-gray-500 dark:text-dark-text-secondary line-clamp-2">
              {{ article.summary }}
            </p>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-else description="该专栏暂无文章" />
    </div>

    <!-- 空状态 -->
    <div v-else class="flex items-center justify-center min-h-[60vh]">
      <a-empty description="专栏不存在" />
    </div>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
