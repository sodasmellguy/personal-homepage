<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@unhead/vue'
import { ReadOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import { getFrontSeriesList } from '@/api/series'
import type { Series } from '@/api/series'

useHead({
  title: '专栏系列 - 赵先生',
  meta: [
    { name: 'description', content: '赵先生的专栏系列，系统化的技术知识整理与分享' },
  ],
})

const router = useRouter()
const loading = ref(false)
const seriesList = ref<Series[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getFrontSeriesList()
    seriesList.value = data || []
  } catch (error) {
    console.error('获取专栏列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})

const goToDetail = (id: number) => {
  router.push(`/series/${id}`)
}
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-dark-base transition-colors duration-300">
    <!-- 头部区域 -->
    <section class="py-16 sm:py-20 px-4 sm:px-8">
      <div class="max-w-4xl mx-auto">
        <h1 class="text-3xl sm:text-4xl font-bold text-gray-900 dark:text-dark-text-primary tracking-tight">专栏系列</h1>
        <p class="text-gray-500 dark:text-dark-text-muted mt-3 text-base">系统化的技术知识整理，带你从入门到精通</p>
      </div>
    </section>

    <!-- 专栏列表 -->
    <div class="max-w-4xl mx-auto px-4 sm:px-8 pb-16">
      <a-spin :spinning="loading">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <div
            v-for="series in seriesList"
            :key="series.id"
            class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border hover:border-blue-200 dark:hover:border-blue-800 hover:shadow-2xl hover:shadow-blue-500/5 transition-all duration-500 cursor-pointer overflow-hidden group hover:-translate-y-1"
            @click="goToDetail(series.id)"
          >
            <!-- 封面图 -->
            <div class="h-40 overflow-hidden">
              <img
                v-if="series.coverImage"
                :src="series.coverImage"
                :alt="series.name"
                loading="lazy"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              />
              <div
                v-else
                class="w-full h-full bg-gradient-to-br from-blue-500 to-violet-500 flex items-center justify-center"
              >
                <ReadOutlined class="text-white text-4xl" />
              </div>
            </div>

            <!-- 内容区域 -->
            <div class="p-5">
              <h2 class="text-lg font-semibold text-gray-900 dark:text-dark-text-primary mb-2">
                {{ series.name }}
              </h2>
              <p class="text-gray-500 dark:text-dark-text-secondary text-sm leading-relaxed line-clamp-2 mb-3">
                {{ series.description || '暂无描述' }}
              </p>
              <div class="flex items-center text-xs text-gray-400 dark:text-dark-text-muted">
                <FileTextOutlined class="mr-1" />
                {{ series.articleCount || 0 }} 篇文章
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <a-empty v-if="!loading && seriesList.length === 0" description="暂无专栏" />
      </a-spin>
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
