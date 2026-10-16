<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useHead } from '@unhead/vue'
import {
  ArrowLeftOutlined,
  CalendarOutlined,
  LinkOutlined,
  TagOutlined,
  CheckCircleOutlined,
  ProjectOutlined,
} from '@ant-design/icons-vue'
import { getProject } from '@/api/project'
import type { Project } from '@/api/project'
import SkeletonCard from '@/components/SkeletonCard.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const project = ref<Project | null>(null)
const readingProgress = ref(0)

// 图片轮播
const currentImageIndex = ref(0)
let carouselTimer: ReturnType<typeof setInterval> | null = null

const projectImages = computed(() => {
  if (!project.value) return []
  const images: string[] = []
  if (project.value.images) {
    try {
      const parsed = JSON.parse(project.value.images)
      if (Array.isArray(parsed)) images.push(...parsed.filter(Boolean))
    } catch { /* ignore */ }
  }
  if (project.value.coverImage) {
    images.unshift(project.value.coverImage)
  }
  // 去重
  return [...new Set(images)]
})

const prevImage = () => {
  if (projectImages.value.length <= 1) return
  currentImageIndex.value = (currentImageIndex.value - 1 + projectImages.value.length) % projectImages.value.length
}

const nextImage = () => {
  if (projectImages.value.length <= 1) return
  currentImageIndex.value = (currentImageIndex.value + 1) % projectImages.value.length
}

const startCarousel = () => {
  if (projectImages.value.length > 1) {
    carouselTimer = setInterval(() => {
      nextImage()
    }, 4000)
  }
}

const projectId = Number(route.params.id)

const fetchProject = async () => {
  loading.value = true
  try {
    const { data } = await getProject(projectId)
    project.value = data
    startCarousel()
    // 动态设置 SEO meta
    if (data) {
      useHead({
        title: () => `${data.title} - 赵先生项目`,
        meta: [
          { name: 'description', content: () => data.description || data.title },
          { property: 'og:title', content: () => `${data.title} - 赵先生项目` },
          { property: 'og:description', content: () => data.description || '' },
          { property: 'og:type', content: 'website' },
          ...(data.coverImage ? [{ property: 'og:image', content: () => data.coverImage }] : []),
        ],
      })
    }
  } catch (error) {
    console.error('获取项目详情失败:', error)
  } finally {
    loading.value = false
  }
}

const parseTechStack = (techStack: string | undefined): string[] => {
  if (!techStack) return []
  return techStack.split(',').map((s) => s.trim()).filter(Boolean)
}

const parseResponsibilities = (text: string | undefined): string[] => {
  if (!text) return []
  return text.split('\n').filter(Boolean)
}

// 阅读进度
const updateProgress = () => {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  readingProgress.value = docHeight > 0 ? Math.min((scrollTop / docHeight) * 100, 100) : 0
}

const formatPeriod = (start: string, end: string | null | undefined) => {
  return `${start} - ${end || '至今'}`
}

onMounted(() => {
  fetchProject()
  window.addEventListener('scroll', updateProgress, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', updateProgress)
  if (carouselTimer) clearInterval(carouselTimer)
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-dark-base transition-colors duration-300">
    <!-- 阅读进度条 -->
    <div class="reading-progress-bar" :style="{ width: readingProgress + '%' }"></div>

    <SkeletonCard v-if="loading" type="detail" />
    <div v-else-if="project" class="max-w-4xl mx-auto px-4 sm:px-8 py-8">
        <!-- 返回按钮 -->
        <a-button
          type="text"
          class="!mb-6 !rounded-xl hover:!bg-gray-100 dark:hover:!bg-dark-elevated"
          @click="router.push('/project')"
        >
          <template #icon>
            <ArrowLeftOutlined />
          </template>
          返回项目列表
        </a-button>

        <!-- 项目头部卡片 -->
        <div class="bg-white dark:bg-dark-card rounded-2xl shadow-sm overflow-hidden mb-8 border border-gray-100 dark:border-dark-border">
          <!-- 图片轮播 -->
          <div v-if="projectImages.length > 0" class="h-64 sm:h-80 relative overflow-hidden group">
            <img
              :src="projectImages[currentImageIndex]"
              :alt="project.title"
              class="w-full h-full object-cover transition-opacity duration-500"
            />
            <!-- 轮播指示器 -->
            <div v-if="projectImages.length > 1" class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
              <span
                v-for="(_, idx) in projectImages"
                :key="idx"
                class="w-2 h-2 rounded-full cursor-pointer transition-all duration-300"
                :class="idx === currentImageIndex ? 'bg-white w-4' : 'bg-white/50 hover:bg-white/75'"
                @click="currentImageIndex = idx"
              ></span>
            </div>
            <!-- 左右箭头 -->
            <template v-if="projectImages.length > 1">
              <div
                class="absolute left-3 top-1/2 -translate-y-1/2 w-8 h-8 bg-black/30 hover:bg-black/50 rounded-full flex items-center justify-center text-white cursor-pointer opacity-0 group-hover:opacity-100 transition-opacity"
                @click="prevImage"
              >‹</div>
              <div
                class="absolute right-3 top-1/2 -translate-y-1/2 w-8 h-8 bg-black/30 hover:bg-black/50 rounded-full flex items-center justify-center text-white cursor-pointer opacity-0 group-hover:opacity-100 transition-opacity"
                @click="nextImage"
              >›</div>
            </template>
          </div>
          <!-- 封面图（无轮播图时） -->
          <div v-else-if="project.coverImage" class="h-64 sm:h-80 overflow-hidden">
            <img
              :src="project.coverImage"
              :alt="project.title"
              class="w-full h-full object-cover"
            />
          </div>
          <!-- 无封面时的装饰 -->
          <div v-else class="h-48 sm:h-64 bg-gradient-to-br from-blue-500 via-violet-600 to-purple-600 flex items-center justify-center relative overflow-hidden">
            <div class="absolute top-10 right-10 w-32 h-32 border-2 border-white/20 rounded-full"></div>
            <div class="absolute bottom-10 left-10 w-20 h-20 border-2 border-white/15 rounded-full"></div>
            <div class="text-center z-10">
              <div class="w-20 h-20 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center mx-auto mb-4">
                <ProjectOutlined class="text-white text-4xl" />
              </div>
              <p class="text-white/80 font-medium">{{ project.title }}</p>
            </div>
          </div>

          <!-- 项目信息 -->
          <div class="p-6 sm:p-8">
            <!-- 标题 -->
            <h1 class="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-dark-text-primary mb-4 leading-tight">
              {{ project.title }}
            </h1>

            <!-- 元信息 -->
            <div class="flex flex-wrap items-center gap-4 text-sm text-gray-500 dark:text-dark-text-muted mb-6 pb-6 border-b border-gray-100 dark:border-dark-border">
              <span class="flex items-center gap-1.5">
                <CalendarOutlined />
                {{ formatPeriod(project.startDate, project.endDate) }}
              </span>
              <a
                v-if="project.projectUrl"
                :href="project.projectUrl"
                target="_blank"
                class="flex items-center gap-1.5 text-blue-500 hover:text-blue-600 dark:text-blue-400"
              >
                <LinkOutlined />
                访问项目
              </a>
            </div>

            <!-- 技术栈 -->
            <div v-if="parseTechStack(project.techStack).length" class="mb-6">
              <h3 class="flex items-center gap-2 text-lg font-semibold text-gray-800 dark:text-dark-text-primary mb-3">
                <TagOutlined class="text-blue-500" />
                技术栈
              </h3>
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="tech in parseTechStack(project.techStack)"
                  :key="tech"
                  class="px-3 py-1.5 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 rounded-lg text-sm font-medium"
                >
                  {{ tech }}
                </span>
              </div>
            </div>

            <!-- 项目描述 -->
            <div v-if="project.description" class="mb-6">
              <h3 class="text-lg font-semibold text-gray-800 dark:text-dark-text-primary mb-3">项目介绍</h3>
              <p class="text-gray-600 dark:text-dark-text-secondary leading-relaxed text-base">
                {{ project.description }}
              </p>
            </div>

            <!-- 个人职责 -->
            <div v-if="project.responsibilities" class="mb-6">
              <h3 class="flex items-center gap-2 text-lg font-semibold text-gray-800 dark:text-dark-text-primary mb-3">
                <CheckCircleOutlined class="text-emerald-500" />
                个人职责
              </h3>
              <ul class="space-y-2.5">
                <li
                  v-for="(item, idx) in parseResponsibilities(project.responsibilities)"
                  :key="idx"
                  class="flex items-start gap-3"
                >
                  <span class="mt-2 w-1.5 h-1.5 rounded-full bg-emerald-500 flex-shrink-0"></span>
                  <span class="text-gray-600 dark:text-dark-text-secondary leading-relaxed">{{ item }}</span>
                </li>
              </ul>
            </div>

            <!-- 外链按钮 -->
            <div v-if="project.projectUrl" class="pt-6 border-t border-gray-100 dark:border-dark-border">
              <a-button type="primary" size="large" class="!rounded-xl" :href="project.projectUrl" target="_blank">
                <template #icon>
                  <LinkOutlined />
                </template>
                访问项目
              </a-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="flex items-center justify-center min-h-[60vh]">
        <a-empty description="项目不存在" />
      </div>
  </div>
</template>

<style scoped>
.reading-progress-bar {
  position: fixed;
  top: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6, #ec4899);
  z-index: 9999;
  transition: width 100ms linear;
  border-radius: 0 2px 2px 0;
}
</style>
