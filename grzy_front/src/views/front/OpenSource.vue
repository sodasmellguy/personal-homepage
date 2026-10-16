<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { GithubOutlined, StarOutlined, ForkOutlined, LinkOutlined, ExportOutlined } from '@ant-design/icons-vue'
import { getFrontOpenSourceList, type OpenSource } from '@/api/openSource'

const loading = ref(true)
const items = ref<OpenSource[]>([])

// 维护中 / 已归档 分组
const activeItems = computed(() => items.value.filter(i => i.status === 1))
const archivedItems = computed(() => items.value.filter(i => i.status !== 1))

// 主语言色点（GitHub 语言色惯例）
const languageColors: Record<string, string> = {
  Java: '#b07219',
  TypeScript: '#3178c6',
  JavaScript: '#f1e05a',
  Vue: '#41b883',
  Python: '#3572A5',
  Go: '#00ADD8',
  Rust: '#dea584',
  Shell: '#89e051',
  'C++': '#f34b7d',
  'C#': '#17834c',
  C: '#555555',
  Kotlin: '#A97BFF',
  Dart: '#00B4AB',
  PHP: '#4F5D95',
  HTML: '#e34c26',
  CSS: '#563d7c',
}

const languageColor = (lang?: string | null) =>
  (lang && languageColors[lang]) || '#3b82f6'

// 技术栈标签拆分
const techTags = (item: OpenSource) =>
  (item.techStack || '').split(',').map(t => t.trim()).filter(Boolean)

const formatCount = (n?: number | null) => {
  const v = n || 0
  return v >= 1000 ? (v / 1000).toFixed(1).replace(/\.0$/, '') + 'k' : String(v)
}

const formatSyncTime = (t?: string | null) => {
  if (!t) return '从未同步'
  return t.slice(0, 10)
}

const fetchList = async () => {
  loading.value = true
  try {
    const { data } = await getFrontOpenSourceList()
    items.value = data || []
  } catch (error) {
    console.error('加载开源项目失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-dark-base transition-colors duration-300">
    <!-- 头部 -->
    <section class="py-16 sm:py-20 px-4 sm:px-8">
      <div class="max-w-5xl mx-auto">
        <h1 class="text-3xl sm:text-4xl font-bold text-gray-900 dark:text-dark-text-primary tracking-tight">开源项目</h1>
        <p class="text-gray-500 dark:text-dark-text-muted mt-3 text-base">
          我在 GitHub / Gitee 上的开源作品，点卡片直达仓库
        </p>
      </div>
    </section>

    <!-- 维护中的项目 -->
    <div class="max-w-5xl mx-auto px-4 sm:px-8 pb-16">
      <a-spin :spinning="loading">
        <div v-if="activeItems.length" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div
            v-for="item in activeItems"
            :key="item.id"
            class="group relative flex flex-col p-5 bg-white dark:bg-dark-card rounded-2xl border border-gray-100 dark:border-dark-border hover:border-blue-200 dark:hover:border-blue-800 hover:shadow-xl hover:shadow-blue-500/5 hover:-translate-y-0.5 transition-all duration-300"
          >
            <!-- 置顶角标 -->
            <span
              v-if="item.isTop === 1"
              class="absolute top-3 right-3 text-[10px] px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-500 font-medium"
            >置顶</span>

            <!-- 头部：图标 + 名称 + 语言 -->
            <div class="flex items-start gap-3 mb-3">
              <div
                class="w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0 overflow-hidden shadow-md"
                :style="{ background: `linear-gradient(135deg, ${languageColor(item.language)}, ${languageColor(item.language)}cc)` }"
              >
                <img
                  v-if="item.logo"
                  :src="item.logo"
                  :alt="item.name"
                  loading="lazy"
                  class="w-full h-full object-cover"
                  @error="($event.target as HTMLImageElement).style.display = 'none'"
                />
                <span v-else class="text-white text-lg font-bold">{{ item.name.charAt(0).toUpperCase() }}</span>
              </div>
              <div class="min-w-0">
                <h3 class="font-semibold text-gray-800 dark:text-dark-text-primary truncate group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                  {{ item.name }}
                </h3>
                <div class="flex items-center gap-1.5 mt-0.5">
                  <span
                    v-if="item.language"
                    class="w-2.5 h-2.5 rounded-full inline-block"
                    :style="{ background: languageColor(item.language) }"
                  />
                  <span class="text-xs text-gray-400 dark:text-dark-text-muted">{{ item.language || '未知语言' }}</span>
                </div>
              </div>
            </div>

            <!-- 简介 -->
            <p class="text-sm text-gray-500 dark:text-dark-text-muted leading-6 mb-3 line-clamp-2">{{ item.description }}</p>

            <!-- 技术栈标签 -->
            <div v-if="techTags(item).length" class="flex flex-wrap gap-1.5 mb-4">
              <span
                v-for="tag in techTags(item).slice(0, 5)"
                :key="tag"
                class="text-[11px] px-2 py-0.5 rounded-full bg-gray-50 dark:bg-white/5 text-gray-500 dark:text-dark-text-muted border border-gray-100 dark:border-dark-border"
              >{{ tag }}</span>
            </div>
            <div v-else class="mb-4" />

            <!-- 底部：统计 + 按钮 -->
            <div class="mt-auto flex items-center justify-between gap-2 pt-3 border-t border-gray-50 dark:border-dark-border">
              <div class="flex items-center gap-3 text-xs text-gray-400 dark:text-dark-text-muted">
                <span class="flex items-center gap-1" :title="`Star ${item.stars}`">
                  <StarOutlined />
                  {{ formatCount(item.stars) }}
                </span>
                <span class="flex items-center gap-1" :title="`Fork ${item.forks}`">
                  <ForkOutlined />
                  {{ formatCount(item.forks) }}
                </span>
                <span v-if="item.license" class="text-[11px] px-1.5 py-0.5 rounded bg-gray-50 dark:bg-white/5 border border-gray-100 dark:border-dark-border">
                  {{ item.license }}
                </span>
              </div>
              <div class="flex items-center gap-1.5">
                <a
                  v-if="item.demoUrl"
                  :href="item.demoUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="flex items-center gap-1 text-xs px-2.5 py-1.5 rounded-lg bg-blue-500/10 text-blue-500 hover:bg-blue-500/20 transition-colors"
                  title="在线体验"
                >
                  <ExportOutlined />
                </a>
                <a
                  :href="item.repoUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="flex items-center gap-1 text-xs px-2.5 py-1.5 rounded-lg bg-gray-50 dark:bg-white/5 text-gray-600 dark:text-dark-text-muted border border-gray-100 dark:border-dark-border hover:text-blue-500 hover:border-blue-200 dark:hover:border-blue-800 transition-colors"
                  title="查看仓库"
                >
                  <GithubOutlined v-if="item.repoUrl.includes('github.com')" />
                  <LinkOutlined v-else />
                  仓库
                </a>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading" class="py-20 text-center text-gray-400 dark:text-dark-text-muted">
          <GithubOutlined class="text-4xl mb-4 block" />
          <p>开源项目整理中，敬请期待</p>
        </div>

        <!-- 已归档项目 -->
        <div v-if="archivedItems.length" class="mt-12">
          <h2 class="text-sm font-semibold text-gray-400 dark:text-dark-text-muted uppercase tracking-wider mb-4 flex items-center gap-2">
            历史项目
            <span class="text-xs font-normal normal-case">（已归档，不再维护）</span>
          </h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 opacity-75">
            <a
              v-for="item in archivedItems"
              :key="item.id"
              :href="item.repoUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="flex items-center gap-3 p-4 bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border hover:border-gray-200 dark:hover:border-gray-700 transition-all"
            >
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <span class="font-medium text-gray-600 dark:text-dark-text-primary truncate">{{ item.name }}</span>
                  <span class="text-[10px] px-1.5 py-0.5 rounded-full bg-gray-100 dark:bg-white/5 text-gray-400 flex-shrink-0">已归档</span>
                </div>
                <p class="text-xs text-gray-400 dark:text-dark-text-muted truncate mt-1">{{ item.description }}</p>
              </div>
              <span class="flex items-center gap-1 text-xs text-gray-400 flex-shrink-0">
                <StarOutlined />
                {{ formatCount(item.stars) }}
              </span>
            </a>
          </div>
        </div>

        <!-- 数据说明 -->
        <p v-if="items.length" class="text-center text-xs text-gray-300 dark:text-gray-600 mt-10">
          Star / Fork 数据最近同步：{{ formatSyncTime(items.find(i => i.syncTime)?.syncTime) }}
        </p>
      </a-spin>
    </div>
  </div>
</template>
