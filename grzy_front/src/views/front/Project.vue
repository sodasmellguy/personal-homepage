<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllProjects } from '@/api/project'
import type { Project } from '@/api/project'
import {
  ProjectOutlined,
  LinkOutlined,
  CalendarOutlined,
  ArrowRightOutlined,
} from '@ant-design/icons-vue'
import SkeletonCard from '@/components/SkeletonCard.vue'

const router = useRouter()
const projects = ref<Project[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const { data } = await getAllProjects()
    projects.value = data
  } catch (error) {
    console.error('获取项目失败:', error)
  } finally {
    loading.value = false
  }
})

const parseTechStack = (techStack: string | undefined): string[] => {
  if (!techStack) return []
  return techStack.split(',').map((s) => s.trim()).filter(Boolean)
}

const projectThemes = [
  {
    gradient: 'from-blue-500 via-blue-600 to-cyan-500',
    pattern: 'bg-blue-400/20',
    accent: 'text-blue-500',
    tagBg: 'bg-blue-50 dark:bg-blue-900/20',
    tagText: 'text-blue-600 dark:text-blue-400',
    border: 'border-blue-200 dark:border-blue-800',
  },
  {
    gradient: 'from-purple-500 via-violet-600 to-pink-500',
    pattern: 'bg-purple-400/20',
    accent: 'text-purple-500',
    tagBg: 'bg-purple-50 dark:bg-purple-900/20',
    tagText: 'text-purple-600 dark:text-purple-400',
    border: 'border-purple-200 dark:border-purple-800',
  },
  {
    gradient: 'from-emerald-500 via-green-600 to-teal-500',
    pattern: 'bg-emerald-400/20',
    accent: 'text-emerald-500',
    tagBg: 'bg-emerald-50 dark:bg-emerald-900/20',
    tagText: 'text-emerald-600 dark:text-emerald-400',
    border: 'border-emerald-200 dark:border-emerald-800',
  },
  {
    gradient: 'from-orange-500 via-amber-500 to-red-500',
    pattern: 'bg-orange-400/20',
    accent: 'text-orange-500',
    tagBg: 'bg-orange-50 dark:bg-orange-900/20',
    tagText: 'text-orange-600 dark:text-orange-400',
    border: 'border-orange-200 dark:border-orange-800',
  },
]

const getTheme = (index: number) => projectThemes[index % projectThemes.length]

// 跳转项目详情
const goToDetail = (id: number) => {
  router.push(`/project/${id}`)
}


</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-dark-base transition-colors duration-300">
    <!-- 页面头部 Hero -->
    <section class="relative py-12 sm:py-16 px-4 sm:px-8 bg-gradient-to-br from-purple-50 via-indigo-50 to-blue-50 dark:from-dark-card dark:via-dark-base dark:to-dark-card overflow-hidden">
      <!-- 网格纹理 -->
      <div class="absolute inset-0 opacity-[0.03] dark:opacity-[0.05]" style="background-image: radial-gradient(circle, #8b5cf6 1px, transparent 1px); background-size: 20px 20px;"></div>
      <!-- 浮动装饰 -->
      <div class="absolute top-10 right-20 w-32 h-32 bg-purple-200 dark:bg-purple-900/30 rounded-full blur-2xl opacity-60 mesh-blob"></div>
      <div class="absolute bottom-10 left-10 w-40 h-40 bg-blue-200 dark:bg-blue-900/30 rounded-full blur-2xl opacity-60 mesh-blob-2"></div>

      <div class="max-w-6xl mx-auto relative">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-8 h-0.5 bg-gradient-to-r from-purple-500 to-blue-500 rounded-full"></div>
          <span class="text-purple-600 dark:text-purple-400 text-sm font-medium tracking-wider uppercase">Projects</span>
        </div>
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-dark-text-primary">项目展示</h1>
        <p class="text-gray-600 dark:text-dark-text-secondary mt-2">我参与开发的项目，每个项目都倾注了心血</p>

        <!-- 项目统计 -->
        <div class="flex gap-6 mt-6">
          <div class="flex items-center gap-2 px-4 py-2 bg-white/60 dark:bg-dark-card/60 backdrop-blur-sm rounded-full border border-purple-100 dark:border-dark-border">
            <span class="text-lg font-bold bg-gradient-to-r from-purple-600 to-blue-500 bg-clip-text text-transparent">{{ projects.length }}</span>
            <span class="text-sm text-gray-500 dark:text-dark-text-muted">个项目</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 项目内容 -->
    <div class="max-w-6xl mx-auto px-4 sm:px-8 py-10 sm:py-14">
      <SkeletonCard v-if="loading" type="project" :count="6" />
      <template v-else>
        <!-- Featured 项目 — 第一个项目大卡片 -->
        <div v-if="projects.length > 0" class="mb-8 sm:mb-10">
          <div
            class="group relative bg-white dark:bg-dark-card rounded-2xl overflow-hidden border border-gray-100 dark:border-dark-border hover:shadow-2xl hover:shadow-purple-500/10 hover:-translate-y-1 transition-all duration-500 cursor-pointer"
            @click="goToDetail(projects[0].id)"
          >
            <!-- 顶部渐变装饰条 -->
            <div class="h-1 w-full bg-gradient-to-r" :class="getTheme(0).gradient"></div>

            <div class="flex flex-col md:flex-row">
              <!-- 封面区域 -->
              <div
                class="md:w-2/5 min-h-[220px] md:min-h-[320px] bg-gradient-to-br flex items-center justify-center relative overflow-hidden"
                :class="getTheme(0).gradient"
              >
                <!-- 装饰图案 — 无图时显示 -->
                <template v-if="!projects[0].coverImage">
                  <!-- 装饰圆圈 -->
                  <div class="absolute top-8 right-8 w-24 h-24 border-2 border-white/20 rounded-full"></div>
                  <div class="absolute bottom-12 left-8 w-16 h-16 border-2 border-white/15 rounded-full"></div>
                  <div class="absolute top-1/2 left-1/4 w-8 h-8 bg-white/10 rounded-lg rotate-45"></div>
                  <!-- 中心内容 -->
                  <div class="text-center z-10">
                    <div class="w-20 h-20 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform duration-300">
                      <ProjectOutlined class="text-white text-4xl" />
                    </div>
                    <p class="text-white/80 text-sm font-medium">{{ projects[0].title }}</p>
                  </div>
                </template>
                <img
                  v-else
                  :src="projects[0].coverImage"
                  :alt="projects[0].title"
                  class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                />
                <!-- Featured 标签 -->
                <div class="absolute top-4 left-4 px-3 py-1.5 bg-white/20 backdrop-blur-md text-white text-xs font-semibold rounded-full border border-white/20 flex items-center gap-1.5">
                  <span class="w-1.5 h-1.5 bg-yellow-400 rounded-full animate-pulse"></span>
                  Featured
                </div>
              </div>

              <!-- 内容区 -->
              <div class="flex-1 p-6 sm:p-8 flex flex-col justify-center">
                <!-- 标题行 -->
                <div class="flex items-start gap-3 mb-3">
                  <h3 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-dark-text-primary group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors flex-1">
                    {{ projects[0].title }}
                  </h3>
                  <a
                    v-if="projects[0].projectUrl"
                    :href="projects[0].projectUrl"
                    target="_blank"
                    class="mt-1 w-8 h-8 flex items-center justify-center rounded-lg bg-gray-100 dark:bg-dark-elevated text-gray-400 hover:text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-all duration-200 flex-shrink-0"
                    @click.stop
                  >
                    <LinkOutlined />
                  </a>
                </div>

                <p class="text-gray-600 dark:text-dark-text-secondary mb-5 leading-relaxed">
                  {{ projects[0].description }}
                </p>

                <!-- 技术栈标签 -->
                <div class="flex flex-wrap gap-2 mb-5">
                  <span
                    v-for="tech in parseTechStack(projects[0].techStack)"
                    :key="tech"
                    class="px-3 py-1 rounded-lg text-sm font-medium transition-all duration-200 hover:scale-105"
                    :class="`${getTheme(0).tagBg} ${getTheme(0).tagText}`"
                  >
                    {{ tech }}
                  </span>
                </div>

                <!-- 底部信息 -->
                <div class="flex items-center justify-between pt-4 border-t border-gray-100 dark:border-dark-border">
                  <span class="flex items-center gap-1.5 text-sm text-gray-500 dark:text-dark-text-muted">
                    <CalendarOutlined />
                    {{ projects[0].startDate }} - {{ projects[0].endDate || '至今' }}
                  </span>
                  <div class="flex items-center gap-2">
                    <a
                      v-if="projects[0].projectUrl"
                      :href="projects[0].projectUrl"
                      target="_blank"
                      class="flex items-center gap-1 text-sm text-gray-500 hover:text-blue-500 dark:text-gray-400 dark:hover:text-blue-400 transition-colors"
                      @click.stop
                    >
                      <LinkOutlined /> 外链
                    </a>
                    <span class="flex items-center gap-1 text-sm font-medium text-blue-500 dark:text-blue-400 group-hover:gap-2 transition-all duration-300">
                      查看详情 <ArrowRightOutlined class="text-xs" />
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 其余项目 — 网格卡片 -->
        <div v-if="projects.length > 1">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-6 h-0.5 bg-gray-300 dark:bg-gray-600 rounded-full"></div>
            <span class="text-sm text-gray-500 dark:text-dark-text-muted font-medium">更多项目</span>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5 sm:gap-6">
            <div
              v-for="(project, index) in projects.slice(1)"
              :key="project.id"
              class="group bg-white dark:bg-dark-card rounded-2xl overflow-hidden shadow-sm hover:shadow-2xl hover:shadow-gray-200/50 dark:hover:shadow-black/30 transition-all duration-500 hover:-translate-y-1.5 border border-gray-100 dark:border-dark-border cursor-pointer"
              @click="goToDetail(project.id)"
            >
              <!-- 顶部渐变装饰条 -->
              <div class="h-0.5 w-full bg-gradient-to-r" :class="getTheme(index + 1).gradient"></div>

              <!-- 封面图 -->
              <div
                class="h-44 bg-gradient-to-br flex items-center justify-center relative overflow-hidden"
                :class="getTheme(index + 1).gradient"
              >
                <template v-if="!project.coverImage">
                  <!-- 装饰图案 -->
                  <div class="absolute -top-4 -right-4 w-20 h-20 border-2 border-white/15 rounded-full"></div>
                  <div class="absolute bottom-4 left-4 w-10 h-10 bg-white/10 rounded-lg rotate-12"></div>
                  <!-- 中心内容 -->
                  <div class="text-center z-10">
                    <div class="w-14 h-14 bg-white/20 backdrop-blur-sm rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:scale-110 group-hover:rotate-3 transition-all duration-300">
                      <ProjectOutlined class="text-white text-2xl" />
                    </div>
                    <p class="text-white/70 text-xs font-medium px-4 line-clamp-1">{{ project.title }}</p>
                  </div>
                </template>
                <img
                  v-else
                  :src="project.coverImage"
                  :alt="project.title"
                  loading="lazy"
                  class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700"
                />
                <!-- 序号徽章 -->
                <div class="absolute top-3 right-3 px-2.5 py-1 bg-black/30 backdrop-blur-sm text-white text-xs font-bold rounded-full">
                  {{ String(index + 2).padStart(2, '0') }}
                </div>
              </div>

              <!-- 内容区 -->
              <div class="p-5">
                <h3 class="text-base sm:text-lg font-semibold text-gray-900 dark:text-dark-text-primary mb-2 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                  {{ project.title }}
                </h3>
                <p class="text-gray-500 dark:text-dark-text-secondary mb-4 line-clamp-2 leading-relaxed text-sm">
                  {{ project.description }}
                </p>
                <!-- 技术标签 -->
                <div class="flex flex-wrap gap-1.5 mb-4">
                  <span
                    v-for="tech in parseTechStack(project.techStack).slice(0, 4)"
                    :key="tech"
                    class="px-2.5 py-0.5 rounded-md text-xs font-medium transition-colors duration-200"
                    :class="`${getTheme(index + 1).tagBg} ${getTheme(index + 1).tagText}`"
                  >
                    {{ tech }}
                  </span>
                </div>
                <!-- 底部 -->
                <div class="flex items-center justify-between text-xs text-gray-400 dark:text-dark-text-muted pt-3 border-t border-gray-100 dark:border-dark-border">
                  <span class="flex items-center gap-1">
                    <CalendarOutlined />
                    {{ project.startDate }}
                  </span>
                  <div class="flex items-center gap-2">
                    <a
                      v-if="project.projectUrl"
                      :href="project.projectUrl"
                      target="_blank"
                      class="w-7 h-7 flex items-center justify-center rounded-md hover:bg-gray-100 dark:hover:bg-dark-elevated text-gray-400 hover:text-blue-500 transition-all duration-200"
                      @click.stop
                    >
                      <LinkOutlined />
                    </a>
                    <span class="flex items-center gap-0.5 text-blue-500 dark:text-blue-400 group-hover:gap-1 transition-all duration-300">
                      详情 <ArrowRightOutlined class="text-[10px]" />
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <a-empty v-if="!loading && projects.length === 0" description="暂无项目" />
      </template>
    </div>
  </div>
</template>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
