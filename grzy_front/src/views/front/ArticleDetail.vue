<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useHead } from '@unhead/vue'
import {
  ArrowLeftOutlined,
  ArrowRightOutlined,
  EyeOutlined,
  LikeOutlined,
  LikeFilled,
  ClockCircleOutlined,
  FieldTimeOutlined,
  TagOutlined,
  BookOutlined,
  BookFilled,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { getArticleDetail, likeArticle, unlikeArticle, collectArticle, uncollectArticle } from '@/api/article'
import { post } from '@/utils/request'
import type { ArticleDetail as ArticleDetailType } from '@/api/article'
import CommentSection from '@/components/CommentSection.vue'
import ArticleHotRank from '@/components/ArticleHotRank.vue'
import { renderMarkdown } from '@/utils/markdown'
import { recordArticleVisit } from '@/utils/visit'
import SkeletonCard from '@/components/SkeletonCard.vue'
import TableOfContents from '@/components/TableOfContents.vue'
import Lightbox from '@/components/Lightbox.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const article = ref<ArticleDetailType | null>(null)
const readingProgress = ref(0)

// 正文字数与预计阅读时长（去除图片/链接语法/HTML标签后按字符数估算）
const wordCount = computed(() => {
  if (!article.value?.content) return 0
  return article.value.content
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/<[^>]+>/g, '')
    .replace(/[#>*`~|\-]/g, '')
    .replace(/\s/g, '').length
})

// 中文约 400 字/分钟
const readingMinutes = computed(() => Math.max(1, Math.ceil(wordCount.value / 400)))

const articleId = Number(route.params.id)

// 灯箱状态
const lightboxSrc = ref('')
const lightboxVisible = ref(false)

const openLightbox = (src: string) => {
  lightboxSrc.value = src
  lightboxVisible.value = true
}

// 文章内容区域图片点击代理
const handleContentClick = (e: Event) => {
  const target = e.target as HTMLElement
  if (target.tagName === 'IMG') {
    const src = (target as HTMLImageElement).src
    if (src) openLightbox(src)
  }
}

// 点赞状态
const liked = ref(false)
const likeCount = ref(0)
const likeLoading = ref(false)

// 检查是否已点赞
const checkLiked = () => {
  const likedArticles = JSON.parse(localStorage.getItem('liked_articles') || '[]') as number[]
  liked.value = likedArticles.includes(articleId)
}

// 切换点赞
const toggleLike = async () => {
  if (likeLoading.value) return
  likeLoading.value = true
  try {
    if (liked.value) {
      await unlikeArticle(articleId)
      likeCount.value = Math.max(0, likeCount.value - 1)
      liked.value = false
      // 更新 localStorage
      const likedArticles = JSON.parse(localStorage.getItem('liked_articles') || '[]') as number[]
      localStorage.setItem('liked_articles', JSON.stringify(likedArticles.filter((id) => id !== articleId)))
    } else {
      await likeArticle(articleId)
      likeCount.value += 1
      liked.value = true
      message.success('点赞成功')
      // 更新 localStorage
      const likedArticles = JSON.parse(localStorage.getItem('liked_articles') || '[]') as number[]
      likedArticles.push(articleId)
      localStorage.setItem('liked_articles', JSON.stringify(likedArticles))
    }
  } catch (error: any) {
    message.error(error.message || '操作失败')
  } finally {
    likeLoading.value = false
  }
}

// 收藏状态
const collected = ref(false)
const collectLoading = ref(false)

// 检查是否已收藏
const checkCollected = () => {
  const collectedArticles = JSON.parse(localStorage.getItem('collected_articles') || '[]') as number[]
  collected.value = collectedArticles.includes(articleId)
}

// 切换收藏
const toggleCollect = async () => {
  if (collectLoading.value) return
  collectLoading.value = true
  try {
    if (collected.value) {
      await uncollectArticle(articleId)
      collected.value = false
      message.success('已取消收藏')
      const collectedArticles = JSON.parse(localStorage.getItem('collected_articles') || '[]') as number[]
      localStorage.setItem('collected_articles', JSON.stringify(collectedArticles.filter((id) => id !== articleId)))
    } else {
      await collectArticle(articleId)
      collected.value = true
      message.success('收藏成功')
      const collectedArticles = JSON.parse(localStorage.getItem('collected_articles') || '[]') as number[]
      collectedArticles.push(articleId)
      localStorage.setItem('collected_articles', JSON.stringify(collectedArticles))
    }
  } catch (error: any) {
    message.error(error.message || '操作失败')
  } finally {
    collectLoading.value = false
  }
}

// 获取文章详情
const fetchArticle = async () => {
  loading.value = true
  try {
    const { data } = await getArticleDetail(articleId)
    article.value = data
    likeCount.value = data.likeCount || 0
    checkLiked()
    checkCollected()
    // 文章阅读量防刷：同一天同一文章只计一次
    if (recordArticleVisit(articleId)) {
      // 通知后端递增文章阅读量
      post(`/front/article/${articleId}/view`).catch(() => {})
    }
    // 动态设置 SEO meta
    if (data) {
      useHead({
        title: () => `${data.title} - 赵先生博客`,
        meta: [
          { name: 'description', content: () => data.summary || data.title },
          { property: 'og:title', content: () => `${data.title} - 赵先生博客` },
          { property: 'og:description', content: () => data.summary || '' },
          { property: 'og:type', content: 'article' },
          ...(data.coverImage ? [{ property: 'og:image', content: () => data.coverImage }] : []),
        ],
      })
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
  } finally {
    loading.value = false
    restoreReadingProgress()
  }
}

// 阅读进度持久化
let saveTimer: ReturnType<typeof setInterval> | null = null

const saveReadingProgress = () => {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  if (docHeight > 0) {
    const progress = scrollTop / docHeight
    if (progress < 0.95) {
      localStorage.setItem(`reading_progress_${articleId}`, String(scrollTop))
    } else {
      localStorage.removeItem(`reading_progress_${articleId}`)
    }
  }
}

// 是否已恢复过阅读位置（只在首次加载时恢复一次，评论刷新等不再触发）
let hasRestoredProgress = false

const restoreReadingProgress = () => {
  // 已恢复过、或用户已经开始滚动，则不再干预，避免与用户滚动打架
  if (hasRestoredProgress || window.scrollY > 50) {
    return
  }
  const saved = localStorage.getItem(`reading_progress_${articleId}`)
  if (saved) {
    const scrollY = Number(saved)
    if (scrollY > 200) {
      hasRestoredProgress = true
      setTimeout(() => {
        // 瞬间跳转，避免平滑滚动与用户滚动产生拉扯感
        window.scrollTo({ top: scrollY, behavior: 'instant' as ScrollBehavior })
        message.info('已恢复上次阅读位置')
      }, 300)
    }
  }
}

// 阅读进度
const updateProgress = () => {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  readingProgress.value = docHeight > 0 ? Math.min((scrollTop / docHeight) * 100, 100) : 0
}

onMounted(() => {
  fetchArticle()
  window.addEventListener('scroll', updateProgress, { passive: true })
  saveTimer = setInterval(saveReadingProgress, 5000)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', updateProgress)
  if (saveTimer) clearInterval(saveTimer)
  saveReadingProgress()
})

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

// renderMarkdown 已迁移至 @/utils/markdown.ts
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-dark-base transition-colors duration-300">
    <!-- 阅读进度条 -->
    <div class="reading-progress-bar" :style="{ width: readingProgress + '%' }"></div>
    
    <SkeletonCard v-if="loading" type="detail" />
    <div v-else-if="article" class="max-w-5xl mx-auto px-4 sm:px-8 py-10 flex gap-10">
      <!-- 左侧文章内容 -->
      <div class="flex-1 min-w-0">
        <!-- 返回按钮 -->
        <a-button
          type="text"
          class="!mb-8 !rounded-xl !text-gray-500 hover:!bg-gray-100 dark:hover:!bg-dark-elevated dark:!text-dark-text-muted"
          @click="router.push('/article')"
        >
          <template #icon>
            <ArrowLeftOutlined />
          </template>
          返回文章列表
        </a-button>

        <!-- 文章头部 -->
        <div class="mb-10">
          <!-- 封面图 -->
          <div v-if="article.coverImage" class="rounded-2xl overflow-hidden mb-8">
            <img
              :src="article.coverImage"
              :alt="article.title"
              loading="lazy"
              class="w-full h-auto max-h-96 object-cover"
            />
          </div>

          <!-- 分类和标签 -->
          <div class="flex flex-wrap gap-2 mb-5">
            <span
              v-if="article.category"
              class="px-3 py-1 rounded-lg text-xs font-medium bg-gray-100 text-gray-700 dark:bg-dark-elevated dark:text-dark-text-secondary cursor-pointer hover:bg-gray-200 dark:hover:bg-dark-card transition-colors"
              @click="router.push({ path: '/article', query: { tag: article.category } })"
            >
              {{ article.category }}
            </span>
            <span
              v-for="tag in article.tagList"
              :key="tag"
              class="px-3 py-1 rounded-lg text-xs font-medium bg-gray-100 text-gray-600 dark:bg-dark-elevated dark:text-dark-text-secondary cursor-pointer hover:bg-gray-200 dark:hover:bg-dark-card transition-colors"
              @click="router.push({ path: '/article', query: { tag } })"
            >
              <TagOutlined class="mr-1" />
              {{ tag }}
            </span>
          </div>

          <!-- 标题 -->
          <h1 class="text-3xl sm:text-4xl lg:text-5xl font-bold text-gray-900 dark:text-dark-text-primary leading-tight tracking-tight mb-5">
            {{ article.title }}
          </h1>

          <!-- 摘要 -->
          <p v-if="article.summary" class="text-gray-500 dark:text-dark-text-secondary text-lg mb-6 leading-relaxed">
            {{ article.summary }}
          </p>

          <!-- 元信息 -->
          <div class="flex flex-wrap items-center gap-5 text-sm text-gray-400 dark:text-dark-text-muted pb-8 border-b border-gray-100 dark:border-dark-border">
            <span class="flex items-center gap-1.5">
              <ClockCircleOutlined />
              {{ formatDate(article.createTime) }}
            </span>
            <span class="flex items-center gap-1.5">
              <EyeOutlined />
              {{ article.viewCount }} 阅读
            </span>
            <span class="flex items-center gap-1.5">
              <FieldTimeOutlined />
              本文约 {{ wordCount }} 字 · 预计阅读 {{ readingMinutes }} 分钟
            </span>
            <button
              class="flex items-center gap-1.5 px-3 py-1 -ml-1 rounded-lg transition-all duration-300 group/btn"
              :class="liked
                ? 'text-red-500 bg-red-50 dark:bg-red-900/20'
                : 'text-gray-500 dark:text-dark-text-muted hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20'"
              :disabled="likeLoading"
              @click="toggleLike"
            >
              <component
                :is="liked ? LikeFilled : LikeOutlined"
                class="transition-transform duration-300 group-hover/btn:scale-110"
                :class="{ 'animate-bounce-once': liked }"
              />
              <span class="text-sm">{{ likeCount }} 点赞</span>
            </button>
            <button
              class="flex items-center gap-1.5 px-3 py-1 -ml-1 rounded-lg transition-all duration-300 group/col"
              :class="collected
                ? 'text-amber-500 bg-amber-50 dark:bg-amber-900/20'
                : 'text-gray-500 dark:text-dark-text-muted hover:text-amber-500 hover:bg-amber-50 dark:hover:bg-amber-900/20'"
              :disabled="collectLoading"
              @click="toggleCollect"
            >
              <component
                :is="collected ? BookFilled : BookOutlined"
                class="transition-transform duration-300 group-hover/col:scale-110"
              />
              <span class="text-sm">{{ collected ? '已收藏' : '收藏' }}</span>
            </button>
          </div>
        </div>

        <!-- 文章内容 -->
        <div class="max-w-3xl">
          <div
            class="markdown-body cursor-pointer"
            v-html="renderMarkdown(article.content)"
            @click="handleContentClick"
          ></div>
        </div>

        <!-- 阅读排行 / 本月热门（正文后、评论前） -->
        <div class="mt-12">
          <ArticleHotRank />
        </div>

        <!-- 评论区 -->
        <div class="mt-12 pt-10 border-t border-gray-100 dark:border-dark-border">
          <CommentSection
            :article-id="articleId"
            :comments="article.comments || []"
            @refresh="fetchArticle"
          />
        </div>

        <!-- 上一篇/下一篇导航 -->
        <div class="mt-10 flex flex-col sm:flex-row gap-4">
          <div
            v-if="article.prevId"
            class="flex-1 rounded-xl border border-gray-100 dark:border-dark-border p-5 cursor-pointer hover:border-gray-300 dark:hover:border-gray-600 transition-all duration-200 group"
            @click="router.push(`/article/${article.prevId}`)"
          >
            <div class="text-xs text-gray-400 dark:text-dark-text-muted mb-2 font-medium uppercase tracking-wider">上一篇</div>
            <div class="flex items-center gap-2 text-gray-800 dark:text-dark-text-primary font-medium text-sm group-hover:text-black dark:group-hover:text-white transition-colors">
              <ArrowLeftOutlined class="text-sm" />
              <span class="line-clamp-1">{{ article.prevTitle }}</span>
            </div>
          </div>
          <div v-else class="flex-1"></div>
          <div
            v-if="article.nextId"
            class="flex-1 rounded-xl border border-gray-100 dark:border-dark-border p-5 cursor-pointer hover:border-gray-300 dark:hover:border-gray-600 transition-all duration-200 group text-right"
            @click="router.push(`/article/${article.nextId}`)"
          >
            <div class="text-xs text-gray-400 dark:text-dark-text-muted mb-2 font-medium uppercase tracking-wider">下一篇</div>
            <div class="flex items-center justify-end gap-2 text-gray-800 dark:text-dark-text-primary font-medium text-sm group-hover:text-black dark:group-hover:text-white transition-colors">
              <span class="line-clamp-1">{{ article.nextTitle }}</span>
              <ArrowRightOutlined class="text-sm" />
            </div>
          </div>
          <div v-else class="flex-1"></div>
        </div>
      </div>

      <!-- 右侧目录导航 -->
      <TableOfContents :content="article?.content || ''" />
    </div>

    <!-- 空状态 -->
    <div v-else class="flex items-center justify-center min-h-[60vh]">
      <a-empty description="文章不存在" />
    </div>

    <!-- 图片灯箱 -->
    <Lightbox
      v-if="lightboxVisible"
      :src="lightboxSrc"
      @close="lightboxVisible = false"
    />
  </div>
</template>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.reading-progress-bar {
  position: fixed;
  top: 0;
  left: 0;
  height: 3px;
  border-radius: 0 2px 2px 0;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6, #ec4899);
  z-index: 9999;
  transition: width 100ms linear;
}

@keyframes bounceOnce {
  0% { transform: scale(1); }
  30% { transform: scale(1.4); }
  50% { transform: scale(0.9); }
  70% { transform: scale(1.15); }
  100% { transform: scale(1); }
}

.animate-bounce-once {
  animation: bounceOnce 0.5s ease;
}
</style>
