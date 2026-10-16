<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@unhead/vue'
import { CalendarOutlined } from '@ant-design/icons-vue'
import { getArchives } from '@/api/article'
import type { ArchiveYear } from '@/api/article'

// SEO
useHead({
  title: '文章归档 - 赵先生',
  meta: [
    { name: 'description', content: '赵先生博客文章归档，按时间线浏览所有技术文章' },
  ],
})

const router = useRouter()
const loading = ref(false)
const archives = ref<ArchiveYear[]>([])
const totalArticles = ref(0)

const monthNames = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']

// 获取归档数据
const fetchArchives = async () => {
  loading.value = true
  try {
    const { data } = await getArchives()
    archives.value = data || []
    totalArticles.value = archives.value.reduce((sum, y) =>
      sum + y.months.reduce((s, m) => s + m.articles.length, 0), 0
    )
  } catch (error) {
    console.error('获取归档数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchArchives()
})

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
}

// 跳转详情
const goToDetail = (id: number) => {
  router.push(`/article/${id}`)
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-dark-base transition-colors duration-300">
    <!-- Hero 区域 -->
    <section class="relative py-16 sm:py-20 px-4 sm:px-8 bg-gradient-to-br from-violet-50 via-purple-50 to-fuchsia-50 dark:from-dark-card dark:via-dark-base dark:to-dark-card">
      <div class="absolute inset-0 opacity-[0.03] dark:opacity-[0.05]" style="background-image: radial-gradient(circle, #8b5cf6 1px, transparent 1px); background-size: 20px 20px;"></div>
      <div class="absolute top-10 right-20 w-32 h-32 bg-violet-200 dark:bg-violet-900/30 rounded-full blur-2xl opacity-60 mesh-blob"></div>
      <div class="absolute bottom-10 left-10 w-40 h-40 bg-fuchsia-200 dark:bg-fuchsia-900/30 rounded-full blur-2xl opacity-60 mesh-blob-2"></div>
      <div class="max-w-4xl mx-auto relative text-center">
        <div class="flex items-center justify-center gap-3 mb-3">
          <div class="w-8 h-0.5 bg-gradient-to-r from-transparent to-violet-500 rounded-full"></div>
          <span class="text-violet-600 dark:text-violet-400 text-sm font-medium tracking-wider uppercase">Archive</span>
          <div class="w-8 h-0.5 bg-gradient-to-l from-transparent to-violet-500 rounded-full"></div>
        </div>
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-dark-text-primary">文章归档</h1>
        <p class="text-gray-600 dark:text-dark-text-secondary mt-2">
          共 <span class="text-violet-600 dark:text-violet-400 font-bold">{{ totalArticles }}</span> 篇文章，记录成长的每一步
        </p>
      </div>
    </section>

    <!-- 归档内容 -->
    <div class="max-w-4xl mx-auto px-4 sm:px-8 py-8 sm:py-12">
      <!-- 加载骨架屏 -->
      <div v-if="loading" class="space-y-8">
        <div v-for="i in 2" :key="i" class="space-y-4">
          <div class="h-8 w-24 skeleton-shimmer rounded-lg"></div>
          <div class="ml-6 space-y-4">
            <div v-for="j in 3" :key="j" class="flex gap-4">
              <div class="w-3 h-3 skeleton-shimmer rounded-full flex-shrink-0 mt-1.5"></div>
              <div class="flex-1 space-y-2">
                <div class="h-4 skeleton-shimmer rounded-lg w-48"></div>
                <div class="h-3 skeleton-shimmer rounded-lg w-20"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 归档列表 -->
      <template v-else>
        <div v-if="archives.length > 0">
          <div v-for="yearData in archives" :key="yearData.year" class="mb-12 last:mb-0">
            <!-- 年份标题 -->
            <div class="flex items-center gap-3 mb-6">
              <div class="w-10 h-10 bg-gradient-to-br from-violet-500 to-purple-600 rounded-xl flex items-center justify-center shadow-lg shadow-violet-500/20">
                <CalendarOutlined class="text-white text-sm" />
              </div>
              <h2 class="text-2xl font-bold text-gray-900 dark:text-dark-text-primary">{{ yearData.year }}</h2>
              <div class="h-px flex-1 bg-gradient-to-r from-gray-200 to-transparent dark:from-dark-border"></div>
            </div>

            <!-- 月份分组 -->
            <div class="relative pl-6 sm:pl-8">
              <!-- 左侧竖线 -->
              <div class="absolute left-3 sm:left-4 top-2 bottom-2 w-0.5 bg-gradient-to-b from-violet-400 via-purple-400 to-fuchsia-400 rounded-full"></div>

              <div v-for="monthData in yearData.months" :key="monthData.month" class="mb-8 last:mb-0">
                <!-- 月份标题 -->
                <div class="flex items-center gap-2 mb-4 relative">
                  <div class="absolute -left-6 sm:left-[-20px] w-3 h-3 bg-violet-500 rounded-full ring-4 ring-violet-50 dark:ring-violet-900/30"></div>
                  <h3 class="text-lg font-semibold text-gray-800 dark:text-dark-text-primary">
                    {{ monthNames[monthData.month - 1] }}
                  </h3>
                  <span class="text-xs text-gray-400 dark:text-dark-text-muted bg-gray-100 dark:bg-dark-elevated px-2 py-0.5 rounded-full">
                    {{ monthData.articles.length }} 篇
                  </span>
                </div>

                <!-- 文章列表 -->
                <div class="space-y-3">
                  <div
                    v-for="article in monthData.articles"
                    :key="article.id"
                    class="group flex items-center gap-4 p-3 rounded-xl hover:bg-gradient-to-r hover:from-violet-50 hover:to-transparent dark:hover:from-violet-900/10 dark:hover:to-transparent cursor-pointer transition-all duration-300 hover:shadow-sm hover:translate-x-1"
                    @click="goToDetail(article.id)"
                  >
                    <!-- 日期 -->
                    <span class="text-xs text-gray-400 dark:text-dark-text-muted font-mono w-14 flex-shrink-0 text-right">
                      {{ formatDate(article.createTime) }}
                    </span>

                    <!-- 小圆点 -->
                    <div class="w-1.5 h-1.5 rounded-full bg-gray-300 dark:bg-gray-600 group-hover:bg-violet-500 transition-colors flex-shrink-0"></div>

                    <!-- 标题 -->
                    <span class="text-gray-700 dark:text-dark-text-secondary group-hover:text-violet-600 dark:group-hover:text-violet-400 transition-colors font-medium flex-1">
                      {{ article.title }}
                    </span>

                    <!-- 分类标签 -->
                    <a-tag v-if="article.category" class="!rounded-full !text-xs !m-0 flex-shrink-0">
                      {{ article.category }}
                    </a-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="text-center py-20">
          <CalendarOutlined class="text-5xl text-gray-300 dark:text-gray-600 mb-4" />
          <p class="text-gray-500 dark:text-dark-text-muted">暂无文章</p>
        </div>
      </template>
    </div>
  </div>
</template>
