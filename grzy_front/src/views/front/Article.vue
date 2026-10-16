<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useHead } from '@unhead/vue'
import {
  EyeOutlined,
  LikeOutlined,
  MessageOutlined,
  SearchOutlined,
  TagOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import { getFrontArticleList, getHotTags } from '@/api/article'
import type { Article, ArticleQuery, HotTag } from '@/api/article'
import ArticleHotRank from '@/components/ArticleHotRank.vue'

// SEO
useHead({
  title: '技术博客 - 赵先生',
  meta: [
    { name: 'description', content: '赵先生的技术博客，分享前端开发、后端开发、数据库等技术文章' },
  ],
})

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const articles = ref<Article[]>([])
const total = ref(0)
const queryParams = reactive<ArticleQuery>({
  pageNum: 1,
  pageSize: 10,
  category: '',
  keyword: '',
})

// 排序选项
const sortOptions = [
  { key: '', label: '最新' },
  { key: 'hot', label: '最热' },
]
const activeSort = ref('')

// 分类列表
const categories = ref<string[]>([])
const activeCategory = ref('')
const categoryCounts = ref<Record<string, number>>({})

// B2: 热门标签
const hotTags = ref<HotTag[]>([])
const activeTag = ref('')

const fetchHotTags = async () => {
  try {
    const { data } = await getHotTags()
    hotTags.value = data || []
  } catch (error) {
    console.error('获取热门标签失败:', error)
  }
}

// 标签筛选
const handleTagClick = (tag: string) => {
  if (activeTag.value === tag) {
    activeTag.value = ''
    queryParams.tag = ''
    queryParams.keyword = ''
  } else {
    activeTag.value = tag
    queryParams.tag = tag
    queryParams.keyword = tag
  }
  queryParams.pageNum = 1
  fetchData()
  router.replace({
    query: {
      ...route.query,
      tag: activeTag.value || undefined,
      q: undefined,
    },
  })
}
// 防抖
let debounceTimer: ReturnType<typeof setTimeout> | null = null
const debounceSearch = () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    queryParams.pageNum = 1
    fetchData()
    // URL 同步
    router.replace({ query: { ...route.query, q: queryParams.keyword || undefined } })
  }, 300)
}

// HTML 转义（标题/摘要来自数据库，渲染前必须转义，防止二次注入 XSS）
const escapeHtml = (str: string) => {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

// 关键词高亮（先转义原文与关键词，再做替换，保证输出内无未转义的 HTML）
const highlightText = (text: string, keyword: string) => {
  const safeText = escapeHtml(text)
  if (!keyword || !text) return safeText
  const escapedKeyword = escapeHtml(keyword)
  const escaped = escapedKeyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escaped})`, 'gi')
  return safeText.replace(regex, '<mark class="bg-yellow-200 dark:bg-yellow-800/60 rounded px-0.5">$1</mark>')
}

// 获取文章列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getFrontArticleList(queryParams)
    articles.value = data.rows
    total.value = data.total
    // 提取分类
    const cats = new Set(data.rows.map((a) => a.category).filter(Boolean))
    categories.value = Array.from(cats)
    // 计算分类计数
    const counts: Record<string, number> = {}
    data.rows.forEach((a: Article) => {
      if (a.category) {
        counts[a.category] = (counts[a.category] || 0) + 1
      }
    })
    categoryCounts.value = counts
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 从 URL 读取搜索关键词
  const q = route.query.q as string
  if (q) {
    queryParams.keyword = q
  }
  // 从 URL 读取标签筛选
  const tag = route.query.tag as string
  if (tag) {
    activeTag.value = tag
    queryParams.tag = tag
  }
  fetchData()
  fetchHotTags()
})

// 搜索（按钮点击）
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchData()
  router.replace({ query: { ...route.query, q: queryParams.keyword || undefined } })
}

// 排序切换
const handleSortChange = (sort: string) => {
  activeSort.value = sort
  queryParams.sortType = sort
  queryParams.pageNum = 1
  fetchData()
}

// 分类筛选
const handleCategoryClick = (cat: string) => {
  activeCategory.value = activeCategory.value === cat ? '' : cat
  queryParams.category = activeCategory.value
  queryParams.pageNum = 1
  fetchData()
}

// 跳转详情
const goToDetail = (id: number) => {
  router.push(`/article/${id}`)
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-dark-base transition-colors duration-300">
    <!-- 头部区域 — 干净白色，大标题 -->
    <section class="py-16 sm:py-20 px-4 sm:px-8">
      <div class="max-w-4xl mx-auto">
        <div class="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-6">
          <div>
            <h1 class="text-3xl sm:text-4xl font-bold text-gray-900 dark:text-dark-text-primary tracking-tight">技术博客</h1>
            <p class="text-gray-500 dark:text-dark-text-muted mt-3 text-base">分享技术心得，记录成长历程</p>
          </div>
          <!-- 搜索框 -->
          <div class="w-full sm:w-72">
            <a-input-search
              v-model:value="queryParams.keyword"
              placeholder="搜索文章..."
              size="large"
              class="!rounded-xl article-search"
              allow-clear
              @search="handleSearch"
              @change="debounceSearch"
            >
              <template #prefix>
                <SearchOutlined class="text-gray-400" />
              </template>
            </a-input-search>
          </div>
        </div>
      </div>
    </section>

    <!-- 阅读排行 / 本月热门（页面顶部，进页即见） -->
    <div class="max-w-4xl mx-auto px-4 sm:px-8">
      <ArticleHotRank />
    </div>

    <!-- 分类标签 — Apple 风格 pill tabs -->
    <div class="max-w-4xl mx-auto px-4 sm:px-8 mt-6">
      <div class="flex flex-wrap gap-2">
          <button
          v-for="cat in categories"
          :key="cat"
          class="px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-300 border"
          :class="activeCategory === cat
            ? 'bg-gradient-to-r from-gray-900 to-gray-700 text-white border-gray-900 dark:from-white dark:to-gray-200 dark:text-gray-900 dark:border-white shadow-md'
            : 'bg-gray-100 text-gray-700 border-gray-100 hover:bg-gray-200 hover:scale-105 dark:bg-dark-elevated dark:text-dark-text-secondary dark:border-dark-border dark:hover:bg-dark-card'"
          @click="handleCategoryClick(cat)"
        >
          {{ cat }}
          <span class="ml-1 opacity-60">({{ categoryCounts[cat] || 0 }})</span>
        </button>
        <button
          v-if="activeCategory"
          class="px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-200 border border-gray-300 text-gray-600 hover:bg-gray-100 dark:border-dark-border dark:text-dark-text-muted dark:hover:bg-dark-elevated"
          @click="handleCategoryClick('')"
        >
          清除筛选
        </button>
      </div>
    </div>

    <!-- B2: 热门标签云 -->
    <div v-if="hotTags.length" class="max-w-4xl mx-auto px-4 sm:px-8 mt-4">
      <div class="flex flex-wrap items-center gap-2">
        <span class="text-xs text-gray-400 dark:text-dark-text-muted mr-1">
          <TagOutlined /> 标签：
        </span>
        <button
          v-for="tag in hotTags"
          :key="tag.name"
          class="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium transition-all duration-200 border"
          :class="activeTag === tag.name
            ? 'bg-blue-500 text-white border-blue-500 dark:bg-blue-600 dark:border-blue-600'
            : 'bg-gray-50 text-gray-600 border-gray-200 hover:bg-gray-100 hover:border-gray-300 dark:bg-dark-elevated dark:text-dark-text-secondary dark:border-dark-border dark:hover:bg-dark-card'"
          @click="handleTagClick(tag.name)"
        >
          {{ tag.name }}
          <span class="opacity-60">{{ tag.count }}</span>
        </button>
        <button
          v-if="activeTag"
          class="px-3 py-1 rounded-full text-xs font-medium border border-gray-300 text-gray-600 hover:bg-gray-100 dark:border-dark-border dark:text-dark-text-muted dark:hover:bg-dark-elevated"
          @click="handleTagClick('')"
        >
          清除标签
        </button>
      </div>
    </div>

    <!-- 排序标签 — Apple 风格 pill tabs -->
    <div class="max-w-4xl mx-auto px-4 sm:px-8 mt-4">
      <div class="flex items-center gap-1 bg-gray-100 dark:bg-dark-elevated rounded-full p-0.5 w-fit">
        <button
          v-for="opt in sortOptions"
          :key="opt.key"
          class="px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-200"
          :class="activeSort === opt.key
            ? 'bg-white dark:bg-dark-card text-gray-900 dark:text-dark-text-primary shadow-sm'
            : 'text-gray-500 dark:text-dark-text-muted hover:text-gray-700 dark:hover:text-dark-text-secondary'"
          @click="handleSortChange(opt.key)"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>

    <!-- 文章列表 -->
    <div class="max-w-4xl mx-auto px-4 sm:px-8 py-10">
      <a-spin :spinning="loading">
        <div class="space-y-4">
            <div
              v-for="article in articles"
              :key="article.id"
              class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border hover:border-blue-200 dark:hover:border-blue-800 hover:shadow-xl hover:shadow-blue-500/5 transition-all duration-300 cursor-pointer overflow-hidden group article-card"
              @click="goToDetail(article.id)"
            >
            <div class="flex flex-col sm:flex-row">
              <!-- 封面图 -->
              <div
                v-if="article.coverImage"
                class="sm:w-56 h-44 sm:h-auto flex-shrink-0 overflow-hidden"
              >
                <img
                  :src="article.coverImage"
                  :alt="article.title"
                  loading="lazy"
                  class="w-full h-full object-cover"
                />
              </div>

              <!-- 内容区域 -->
              <div class="flex-1 p-6 sm:p-7">
                <!-- 置顶标签 -->
                <div v-if="article.isTop" class="mb-2">
                  <a-tag color="red" class="!rounded-full !text-xs">置顶</a-tag>
                </div>

                <!-- 标题 -->
                <h2 class="text-lg font-semibold text-gray-900 dark:text-dark-text-primary mb-2 line-clamp-2" v-html="highlightText(article.title, queryParams.keyword || '')">
                </h2>

                <!-- 摘要 -->
                <p class="text-gray-500 dark:text-dark-text-secondary mb-4 line-clamp-2 text-sm leading-relaxed" v-html="highlightText(article.summary || '', queryParams.keyword || '')">
                </p>

                <!-- 标签 -->
                <div v-if="article.tagList?.length" class="flex flex-wrap gap-1.5 mb-4">
                  <span
                    v-for="tag in article.tagList"
                    :key="tag"
                    class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-lg text-xs font-medium bg-gray-100 text-gray-600 dark:bg-dark-elevated dark:text-dark-text-secondary cursor-pointer hover:bg-gray-200 dark:hover:bg-dark-card transition-colors"
                    @click.stop="router.push({ path: '/article', query: { tag } })"
                  >
                    <TagOutlined />
                    {{ tag }}
                  </span>
                </div>

                <!-- 底部信息 -->
                <div class="flex flex-wrap items-center gap-4 text-xs text-gray-400 dark:text-dark-text-muted">
                  <span class="flex items-center gap-1">
                    <ClockCircleOutlined />
                    {{ formatDate(article.createTime) }}
                  </span>
                  <span class="flex items-center gap-1">
                    <EyeOutlined />
                    {{ article.viewCount }}
                  </span>
                  <span class="flex items-center gap-1">
                    <LikeOutlined />
                    {{ article.likeCount }}
                  </span>
                  <span class="flex items-center gap-1">
                    <MessageOutlined />
                    {{ article.commentCount }}
                  </span>
                  <span v-if="article.category" class="px-2 py-0.5 rounded-md bg-gray-50 dark:bg-dark-elevated">
                    {{ article.category }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <a-empty v-if="articles.length === 0" description="暂无文章" />
        </div>
      </a-spin>

      <!-- 分页 -->
      <div v-if="total > (queryParams.pageSize || 10)" class="mt-10 text-center">
        <a-pagination
          v-model:current="queryParams.pageNum"
          :total="total"
          :page-size="queryParams.pageSize"
          show-quick-jumper
          @change="fetchData"
        />
      </div>
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

.article-search :deep(.ant-input) {
  transition: all 0.3s ease;
}

.article-search :deep(.ant-input-affix-wrapper-focused) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12) !important;
}

.article-card {
  position: relative;
}

.article-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(to bottom, #3b82f6, #8b5cf6);
  border-radius: 0 2px 2px 0;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.article-card:hover::before {
  opacity: 1;
}
</style>
