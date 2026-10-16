<script setup lang="ts">
import { ref, computed, onBeforeUnmount, nextTick, watch } from 'vue'

interface TocItem {
  id: string
  text: string
  level: number
}

const props = defineProps<{
  content: string
}>()

const tocItems = ref<TocItem[]>([])
const activeId = ref('')
const drawerVisible = ref(false)
const tocListRef = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

// A3: 计算已阅读进度
const activeIndex = computed(() => {
  const idx = tocItems.value.findIndex((item) => item.id === activeId.value)
  return idx >= 0 ? idx : 0
})

const progressPercent = computed(() => {
  if (tocItems.value.length <= 1) return 100
  return Math.round((activeIndex.value / (tocItems.value.length - 1)) * 100)
})

// 从 Markdown 内容提取标题
const extractHeadings = (content: string): TocItem[] => {
  if (!content) return []
  const regex = /^(#{2,3})\s+(.+)$/gm
  const items: TocItem[] = []
  let match
  while ((match = regex.exec(content)) !== null) {
    const level = match[1].length
    const text = match[2].trim()
    const id = text
      .toLowerCase()
      .replace(/\s+/g, '-')
      .replace(/[^\w\u4e00-\u9fa5-]/g, '')
    items.push({ id, text, level })
  }
  return items
}

// 滚动到指定标题
const scrollToHeading = (id: string) => {
  const el = document.getElementById(id)
  if (el) {
    const top = el.getBoundingClientRect().top + window.scrollY - 80
    window.scrollTo({ top, behavior: 'smooth' })
  }
}

// 处理移动端目录项点击
const handleItemClick = (id: string) => {
  scrollToHeading(id)
  drawerVisible.value = false
}

// A2: 高亮项自动滚动到 TOC 可视区域内（只滚动目录容器，不牵动页面滚动）
const scrollActiveIntoView = () => {
  const list = tocListRef.value
  if (!list) return
  const activeEl = list.querySelector('.toc-active-item') as HTMLElement | null
  if (!activeEl) return
  const itemRect = activeEl.getBoundingClientRect()
  const listRect = list.getBoundingClientRect()
  // 仅当高亮项超出目录可视范围时，在容器内滚动到中间位置
  if (itemRect.top < listRect.top || itemRect.bottom > listRect.bottom) {
    list.scrollTop += itemRect.top - listRect.top - listRect.height / 2 + itemRect.height / 2
  }
}

// 设置 IntersectionObserver 监听标题可见性
const setupObserver = () => {
  if (observer) observer.disconnect()
  observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          activeId.value = entry.target.id
        }
      }
    },
    { rootMargin: '-80px 0px -60% 0px', threshold: 0 }
  )
  // 等待 DOM 渲染完成
  nextTick(() => {
    tocItems.value.forEach((item) => {
      const el = document.getElementById(item.id)
      if (el) observer?.observe(el)
    })
  })
}

// 监听 activeId 变化，自动滚动 TOC 列表
watch(activeId, () => {
  nextTick(scrollActiveIntoView)
})

watch(
  () => props.content,
  (newContent) => {
    tocItems.value = extractHeadings(newContent)
    if (tocItems.value.length > 0) {
      setupObserver()
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  if (observer) observer.disconnect()
})
</script>

<template>
  <nav
    v-if="tocItems.length > 0"
    class="hidden lg:block sticky top-24 w-56 flex-shrink-0"
  >
    <div class="bg-white dark:bg-dark-card rounded-2xl border border-gray-100 dark:border-dark-border p-4 shadow-sm">
      <h4 class="text-sm font-bold text-gray-900 dark:text-dark-text-primary mb-3 flex items-center gap-2">
        <svg class="w-4 h-4 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7" />
        </svg>
        目录
      </h4>

      <!-- A3: 进度指示器 -->
      <div class="flex items-center gap-2 mb-3 px-1">
        <div class="flex-1 h-1 bg-gray-100 dark:bg-dark-elevated rounded-full overflow-hidden">
          <div
            class="h-full bg-gradient-to-r from-blue-500 to-violet-500 rounded-full transition-all duration-500 ease-out"
            :style="{ width: progressPercent + '%' }"
          ></div>
        </div>
        <span class="text-xs text-gray-400 dark:text-dark-text-muted tabular-nums">{{ progressPercent }}%</span>
      </div>

      <ul ref="tocListRef" class="relative max-h-[calc(100vh-220px)] overflow-y-auto scrollbar-thin">
        <!-- A3: 垂直进度线 -->
        <li class="absolute left-[7px] top-0 bottom-0 w-[2px] bg-gray-100 dark:bg-dark-elevated rounded-full" aria-hidden="true">
          <div
            class="w-full bg-gradient-to-b from-blue-500 to-violet-500 rounded-full transition-all duration-500 ease-out"
            :style="{ height: progressPercent + '%' }"
          ></div>
        </li>

        <li
          v-for="(item, index) in tocItems"
          :key="item.id"
          class="relative pl-5"
        >
          <!-- A3: 圆点指示器 -->
          <span
            class="absolute left-0 top-1/2 -translate-y-1/2 w-[10px] h-[10px] rounded-full border-2 transition-all duration-300 z-10"
            :class="index <= activeIndex
              ? 'bg-blue-500 border-blue-500 dark:bg-blue-400 dark:border-blue-400 scale-100'
              : 'bg-white dark:bg-dark-card border-gray-300 dark:border-gray-600 scale-90'"
          ></span>

          <a
            :class="[
              'toc-item block py-1.5 text-sm leading-snug rounded-lg px-2 transition-all duration-200 cursor-pointer',
              item.level === 3 ? 'ml-3' : '',
              activeId === item.id
                ? 'toc-active-item text-blue-600 dark:text-blue-400 bg-blue-50/80 dark:bg-blue-900/20 font-semibold border-l-2 border-blue-500 dark:border-blue-400 pl-2.5'
                : 'text-gray-500 dark:text-dark-text-muted hover:text-gray-900 dark:hover:text-dark-text-primary hover:bg-gray-50 dark:hover:bg-dark-elevated'
            ]"
            @click.prevent="scrollToHeading(item.id)"
          >
            {{ item.text }}
          </a>
        </li>
      </ul>
    </div>
  </nav>

  <!-- Mobile TOC toggle button -->
  <button
    v-if="tocItems.length > 0"
    class="lg:hidden fixed bottom-6 right-6 z-50 w-12 h-12 bg-white dark:bg-dark-card border border-gray-200 dark:border-dark-border rounded-full shadow-lg flex items-center justify-center hover:shadow-xl transition-all duration-200"
    @click="drawerVisible = true"
  >
    <svg class="w-5 h-5 text-gray-600 dark:text-dark-text-secondary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7" />
    </svg>
  </button>

  <!-- Mobile TOC Drawer -->
  <a-drawer
    :open="drawerVisible"
    title="目录"
    placement="bottom"
    :height="360"
    @close="drawerVisible = false"
    class="toc-drawer"
  >
    <!-- 移动端进度条 -->
    <div class="flex items-center gap-2 mb-3 px-1">
      <div class="flex-1 h-1 bg-gray-100 dark:bg-dark-elevated rounded-full overflow-hidden">
        <div
          class="h-full bg-gradient-to-r from-blue-500 to-violet-500 rounded-full transition-all duration-500 ease-out"
          :style="{ width: progressPercent + '%' }"
        ></div>
      </div>
      <span class="text-xs text-gray-400 dark:text-dark-text-muted">{{ progressPercent }}%</span>
    </div>
    <ul class="space-y-1">
      <li v-for="item in tocItems" :key="item.id">
        <a
          :class="[
            'block py-2 text-sm leading-snug rounded-lg px-3 transition-all duration-200',
            item.level === 3 ? 'ml-4' : '',
            activeId === item.id
              ? 'text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20 font-medium'
              : 'text-gray-600 dark:text-dark-text-secondary hover:text-gray-900 dark:hover:text-dark-text-primary hover:bg-gray-50 dark:hover:bg-dark-elevated'
          ]"
          @click.prevent="handleItemClick(item.id)"
        >
          {{ item.text }}
        </a>
      </li>
    </ul>
  </a-drawer>
</template>
