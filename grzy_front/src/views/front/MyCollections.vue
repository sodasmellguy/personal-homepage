<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@unhead/vue'
import {
  BookOutlined,
  EyeOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import { getCollectedArticles } from '@/api/article'
import type { Article } from '@/api/article'
import SkeletonCard from '@/components/SkeletonCard.vue'

useHead({
  title: '我的收藏 - 赵先生',
})

const router = useRouter()
const loading = ref(false)
const articles = ref<Article[]>([])
const total = ref(0)

const fetchCollections = async () => {
  loading.value = true
  try {
    const { data } = await getCollectedArticles({ pageNum: 1, pageSize: 50 })
    articles.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取收藏列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCollections()
})

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const goToDetail = (id: number) => {
  router.push(`/article/${id}`)
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-dark-base transition-colors duration-300">
    <!-- Hero -->
    <section class="relative py-16 sm:py-20 px-4 sm:px-8 bg-gradient-to-br from-blue-50 via-indigo-50 to-violet-50 dark:from-dark-card dark:via-dark-base dark:to-dark-card">
      <div class="absolute inset-0 opacity-[0.03] dark:opacity-[0.05]" style="background-image: radial-gradient(circle, #8b5cf6 1px, transparent 1px); background-size: 20px 20px;"></div>
      <div class="max-w-5xl mx-auto relative text-center">
        <div class="flex items-center justify-center gap-3 mb-3">
          <div class="w-8 h-0.5 bg-indigo-500 rounded-full"></div>
          <span class="text-indigo-600 dark:text-indigo-400 text-sm font-medium tracking-wider uppercase">Collections</span>
          <div class="w-8 h-0.5 bg-indigo-500 rounded-full"></div>
        </div>
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-dark-text-primary">我的收藏</h1>
        <p class="text-gray-600 dark:text-dark-text-secondary mt-2">
          共收藏了 <span class="text-amber-600 dark:text-amber-400 font-bold">{{ total }}</span> 篇文章
        </p>
      </div>
    </section>

    <!-- 内容 -->
    <div class="max-w-5xl mx-auto px-4 sm:px-8 py-8">
      <SkeletonCard v-if="loading" type="article" :count="5" />

      <template v-else>
        <div v-if="articles.length > 0" class="space-y-6">
          <div
            v-for="article in articles"
            :key="article.id"
            class="bg-white dark:bg-dark-card rounded-2xl shadow-sm hover:shadow-lg transition-all duration-300 cursor-pointer overflow-hidden group border border-gray-100 dark:border-dark-border"
            @click="goToDetail(article.id)"
          >
            <div class="flex flex-col sm:flex-row">
              <div v-if="article.coverImage" class="sm:w-64 h-48 sm:h-auto flex-shrink-0 overflow-hidden">
                <img :src="article.coverImage" :alt="article.title" loading="lazy" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
              </div>
              <div class="flex-1 p-6">
                <h2 class="text-xl font-bold text-gray-900 dark:text-dark-text-primary mb-3 group-hover:text-amber-600 dark:group-hover:text-amber-400 transition-colors duration-200 line-clamp-2">
                  {{ article.title }}
                </h2>
                <p class="text-gray-600 dark:text-dark-text-secondary mb-4 line-clamp-2 text-sm leading-relaxed">
                  {{ article.summary }}
                </p>
                <div class="flex flex-wrap items-center gap-4 text-sm text-gray-500 dark:text-dark-text-muted">
                  <span class="flex items-center gap-1">
                    <ClockCircleOutlined />
                    {{ formatDate(article.createTime) }}
                  </span>
                  <span class="flex items-center gap-1">
                    <EyeOutlined />
                    {{ article.viewCount }} 阅读
                  </span>
                  <a-tag v-if="article.category" class="!rounded-full !text-xs">{{ article.category }}</a-tag>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-20">
          <BookOutlined class="text-5xl text-gray-300 dark:text-gray-600 mb-4" />
          <p class="text-gray-500 dark:text-dark-text-muted">暂无收藏文章</p>
          <a-button type="primary" class="!rounded-xl mt-4" @click="router.push('/article')">去浏览文章</a-button>
        </div>
      </template>
    </div>
  </div>
</template>
