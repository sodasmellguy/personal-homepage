<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import {
  SearchOutlined,
  ReadOutlined,
  ProjectOutlined,
  SolutionOutlined,
  MessageOutlined,
} from '@ant-design/icons-vue'
import { getFrontArticleList } from '@/api/article'
import type { Article } from '@/api/article'

const router = useRouter()
const visible = ref(false)
const searchText = ref('')
const loading = ref(false)
const articles = ref<Article[]>([])
const selectedIndex = ref(0)

let debounceTimer: ReturnType<typeof setTimeout> | null = null

// 搜索文章
const handleSearch = () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  if (!searchText.value.trim()) {
    articles.value = []
    return
  }
  debounceTimer = setTimeout(async () => {
    loading.value = true
    try {
      const { data } = await getFrontArticleList({ pageNum: 1, pageSize: 8, keyword: searchText.value.trim() })
      articles.value = data.rows
      selectedIndex.value = 0
    } catch {
      articles.value = []
    } finally {
      loading.value = false
    }
  }, 300)
}

// 快捷导航
const quickLinks = [
  { icon: ReadOutlined, label: '技术博客', path: '/article' },
  { icon: ProjectOutlined, label: '项目展示', path: '/project' },
  { icon: SolutionOutlined, label: '实习经历', path: '/internship' },
  { icon: MessageOutlined, label: '留言墙', path: '/guestbook' },
]

// 快捷键
const handleKeydown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    visible.value = !visible.value
  }
  if (e.key === 'Escape') {
    visible.value = false
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

const openSearch = () => {
  visible.value = true
  searchText.value = ''
  articles.value = []
}

const closeSearch = () => {
  visible.value = false
}

const goToArticle = (id: number) => {
  closeSearch()
  router.push(`/article/${id}`)
}

const goToPath = (path: string) => {
  closeSearch()
  router.push(path)
}

defineExpose({ openSearch })
</script>

<template>
  <a-modal
    v-model:open="visible"
    :closable="false"
    :footer="null"
    :width="600"
    class="global-search-modal"
    @cancel="closeSearch"
  >
    <div class="py-2">
      <!-- 搜索输入框 -->
      <div class="relative mb-4">
        <SearchOutlined class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-lg" />
        <input
          v-model="searchText"
          type="text"
          placeholder="搜索文章..."
          class="w-full pl-12 pr-4 py-3 text-lg bg-gray-50 dark:bg-dark-elevated border border-gray-200 dark:border-dark-border rounded-xl outline-none focus:border-blue-500 dark:focus:border-blue-400 transition-colors"
          @input="handleSearch"
          autofocus
        />
        <kbd class="absolute right-4 top-1/2 -translate-y-1/2 hidden sm:inline-flex items-center px-2 py-0.5 text-xs text-gray-400 bg-gray-100 dark:bg-dark-border rounded">ESC</kbd>
      </div>

      <!-- 快捷导航 -->
      <div v-if="!searchText" class="mb-4">
        <div class="text-xs text-gray-400 dark:text-dark-text-muted mb-2 px-1">快捷导航</div>
        <div class="grid grid-cols-2 gap-2">
          <div
            v-for="link in quickLinks"
            :key="link.path"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg cursor-pointer hover:bg-gray-100 dark:hover:bg-dark-elevated transition-colors"
            @click="goToPath(link.path)"
          >
            <component :is="link.icon" class="text-blue-500" />
            <span class="text-sm text-gray-700 dark:text-dark-text-secondary">{{ link.label }}</span>
          </div>
        </div>
      </div>

      <!-- 搜索结果 -->
      <div v-if="searchText">
        <div v-if="loading" class="py-8 text-center text-gray-400">搜索中...</div>
        <div v-else-if="articles.length === 0" class="py-8 text-center text-gray-400">未找到相关文章</div>
        <div v-else class="space-y-1 max-h-80 overflow-y-auto">
          <div
            v-for="(article, index) in articles"
            :key="article.id"
            class="flex items-center gap-3 px-3 py-3 rounded-lg cursor-pointer transition-colors"
            :class="index === selectedIndex ? 'bg-blue-50 dark:bg-blue-900/20' : 'hover:bg-gray-100 dark:hover:bg-dark-elevated'"
            @click="goToArticle(article.id)"
            @mouseenter="selectedIndex = index"
          >
            <ReadOutlined class="text-blue-500 flex-shrink-0" />
            <div class="flex-1 min-w-0">
              <div class="text-sm font-medium text-gray-800 dark:text-dark-text-primary truncate">{{ article.title }}</div>
              <div class="text-xs text-gray-400 dark:text-dark-text-muted truncate mt-0.5">{{ article.summary }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部提示 -->
      <div class="flex items-center justify-between pt-3 mt-2 border-t border-gray-100 dark:border-dark-border text-xs text-gray-400">
        <span>输入关键词搜索文章</span>
        <span class="hidden sm:inline">
          <kbd class="px-1.5 py-0.5 bg-gray-100 dark:bg-dark-border rounded text-[10px]">Ctrl</kbd>
          <span class="mx-0.5">+</span>
          <kbd class="px-1.5 py-0.5 bg-gray-100 dark:bg-dark-border rounded text-[10px]">K</kbd>
          打开搜索
        </span>
      </div>
    </div>
  </a-modal>
</template>
