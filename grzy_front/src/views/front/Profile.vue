<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useHead } from '@unhead/vue'
import { message } from 'ant-design-vue'
import { getFrontProfile } from '@/api/profile'
import type { Profile } from '@/api/profile'
import { getAllAwards } from '@/api/award'
import type { Award } from '@/api/award'
import { renderMarkdown } from '@/utils/markdown'
import { useUserStore } from '@/stores/user'
import {
  UserOutlined,
  MailOutlined,
  BankOutlined,
  GithubOutlined,
  LinkOutlined,
  DownloadOutlined,
  TrophyOutlined,
  CodeOutlined,
} from '@ant-design/icons-vue'

// SEO
useHead({
  title: '关于我 - 赵先生',
  meta: [
    { name: 'description', content: '了解赵先生 - 全栈开发工程师的个人信息、教育背景和获奖经历' },
  ],
})

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const profile = ref<Profile | null>(null)
const awards = ref<Award[]>([])
const loading = ref(true)
const exportLoading = ref(false)

// 导出简历 PDF
const exportResume = async () => {
  exportLoading.value = true
  try {
    const html2pdf = (await import('html2pdf.js')).default
    const el = document.getElementById('resume-content')
    if (!el) return
    await html2pdf()
      .set({
        margin: [10, 10],
        filename: `${profile.value?.name || '赵先生'}-简历.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' },
      })
      .from(el)
      .save()
    message.success('简历导出成功')
  } catch {
    message.error('简历导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 信息标签配置
const infoTags = computed(() => {
  if (!profile.value) return []
  const tags = [
    { icon: BankOutlined, label: profile.value.university || '野鸡大学', color: 'blue' },
    { icon: UserOutlined, label: profile.value.major || '垃圾专业', color: 'purple' },
    { icon: MailOutlined, label: profile.value.email || 'ceshi@example.com', color: 'emerald' },
  ]
  return tags
})

onMounted(async () => {
  try {
    const [profileRes, awardsRes] = await Promise.all([
      getFrontProfile(),
      getAllAwards(),
    ])
    profile.value = profileRes.data
    awards.value = awardsRes.data
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-dark-base transition-colors duration-300 overflow-x-hidden">
    <!-- 顶部横幅 - 流动渐变动画 -->
    <div class="relative h-64 sm:h-72 overflow-hidden profile-hero">
      <!-- 动态渐变背景 -->
      <div class="absolute inset-0 bg-gradient-to-br from-blue-600 via-indigo-600 to-violet-600 dark:from-blue-800 dark:via-indigo-900 dark:to-violet-900"></div>
      <!-- 装饰网格 -->
      <div class="absolute inset-0 opacity-10" style="background-image: radial-gradient(circle at 1px 1px, white 1px, transparent 0); background-size: 40px 40px;"></div>
      <!-- 装饰光圈 -->
      <div class="absolute top-10 right-20 w-64 h-64 bg-white/10 rounded-full blur-3xl animate-float-slow mesh-blob"></div>
      <div class="absolute -bottom-20 -left-10 w-80 h-80 bg-blue-300/20 rounded-full blur-3xl animate-float-medium mesh-blob-2"></div>
      <div class="absolute top-1/2 left-1/3 w-40 h-40 bg-violet-300/15 rounded-full blur-2xl animate-float-fast mesh-blob-3"></div>
      <!-- 底部波浪 -->
      <div class="absolute bottom-0 left-0 right-0">
        <svg viewBox="0 0 1440 100" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-full">
          <path d="M0 50L48 45C96 40 192 30 288 35C384 40 480 60 576 65C672 70 768 60 864 50C960 40 1056 30 1152 35C1248 40 1344 60 1392 70L1440 80V100H1392C1344 100 1248 100 1152 100C1056 100 960 100 864 100C768 100 672 100 576 100C480 100 384 100 288 100C192 100 96 100 48 100H0V50Z" class="fill-white dark:fill-dark-base"/>
        </svg>
      </div>
    </div>

    <div class="max-w-3xl mx-auto px-6 sm:px-8 -mt-36 relative z-10 pb-24">
      <!-- 导出简历按钮（登录后可见） -->
<!--      <div v-if="isLoggedIn" class="flex justify-end mb-6">-->
<!--        <a-button-->
<!--          class="!rounded-full !bg-white/90 dark:!bg-dark-card/90 !backdrop-blur-sm !border-gray-200 dark:!border-dark-border !text-gray-700 dark:!text-dark-text-secondary hover:!bg-white dark:hover:!bg-dark-card !shadow-lg !px-5 !h-10 transition-all duration-300"-->
<!--          :loading="exportLoading"-->
<!--          @click="exportResume"-->
<!--        >-->
<!--          <template #icon><DownloadOutlined /></template>-->
<!--          导出简历-->
<!--        </a-button>-->
<!--      </div>-->

      <!-- 简历内容区域 -->
      <div id="resume-content">
      <!-- 个人信息卡片 - 玻璃拟态 -->
      <a-card :loading="loading" class="!rounded-2xl !shadow-2xl !shadow-blue-500/10 !border-0 dark:!bg-dark-card dark:!border-dark-border mb-10 profile-main-card">
        <div class="flex flex-col items-center py-8 sm:py-10 px-6 sm:px-10">
          <!-- 头像 - 带装饰环 -->
          <div class="relative mb-6">
            <div class="absolute -inset-2 bg-gradient-to-br from-blue-500 via-indigo-500 to-violet-500 rounded-full opacity-75 blur-sm animate-pulse-ring"></div>
            <div class="absolute -inset-1 bg-gradient-to-br from-blue-400 via-indigo-400 to-violet-400 rounded-full"></div>
            <a-avatar :size="112" src="/avatar.png" class="relative !z-10 sm:!w-[120px] sm:!h-[120px] !ring-4 !ring-white dark:!ring-dark-card">
              {{ profile?.name?.charAt(0) || 'F' }}
            </a-avatar>
          </div>

          <!-- 姓名 -->
          <h1 class="text-3xl sm:text-4xl font-bold text-gray-900 dark:text-dark-text-primary tracking-tight mb-2">
            {{ profile?.name || '赵先生' }}
          </h1>

          <!-- 职业目标 - 渐变文字 -->
          <div class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-blue-50 dark:bg-blue-900/20 mb-6">
            <CodeOutlined class="text-blue-500 dark:text-blue-400" />
            <span class="text-base font-semibold bg-gradient-to-r from-blue-600 to-violet-600 dark:from-blue-400 dark:to-violet-400 bg-clip-text text-transparent">
              {{ profile?.careerGoal || '全栈开发工程师' }}
            </span>
          </div>

          <!-- 信息标签 -->
          <div class="flex flex-wrap justify-center gap-3 mb-6">
            <div
              v-for="tag in infoTags"
              :key="tag.label"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium transition-all duration-200 hover:scale-105 cursor-default"
              :class="{
                'bg-blue-50 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400': tag.color === 'blue',
                'bg-purple-50 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400': tag.color === 'purple',
                'bg-emerald-50 text-emerald-600 dark:bg-emerald-900/20 dark:text-emerald-400': tag.color === 'emerald',
              }"
            >
              <component :is="tag.icon" class="text-sm" />
              {{ tag.label }}
            </div>
          </div>

          <!-- 社交链接 - 图标化 -->
          <div class="flex gap-3">
            <a
              v-if="profile?.giteeUrl"
              :href="profile.giteeUrl"
              target="_blank"
              class="group flex items-center gap-2 px-4 py-2 rounded-xl bg-gray-50 dark:bg-dark-elevated border border-gray-200 dark:border-dark-border hover:border-blue-300 dark:hover:border-blue-700 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-all duration-300"
            >
              <LinkOutlined class="text-gray-500 group-hover:text-blue-500 transition-colors" />
              <span class="text-sm font-medium text-gray-600 dark:text-dark-text-secondary group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">Gitee</span>
              <span class="text-gray-300 dark:text-dark-border group-hover:text-blue-400 transition-colors">&rarr;</span>
            </a>
            <a
              v-if="profile?.githubUrl"
              :href="profile.githubUrl"
              target="_blank"
              class="group flex items-center gap-2 px-4 py-2 rounded-xl bg-gray-50 dark:bg-dark-elevated border border-gray-200 dark:border-dark-border hover:border-gray-400 dark:hover:border-gray-500 hover:bg-gray-100 dark:hover:bg-dark-elevated transition-all duration-300"
            >
              <GithubOutlined class="text-gray-500 group-hover:text-gray-900 dark:group-hover:text-white transition-colors" />
              <span class="text-sm font-medium text-gray-600 dark:text-dark-text-secondary group-hover:text-gray-900 dark:group-hover:text-white transition-colors">GitHub</span>
              <span class="text-gray-300 dark:text-dark-border group-hover:text-gray-500 transition-colors">&rarr;</span>
            </a>
          </div>
        </div>
      </a-card>

      <!-- 个人介绍 - 带装饰标题 -->
      <a-card :loading="loading" class="!rounded-2xl !shadow-lg !shadow-gray-200/50 !border-0 dark:!bg-dark-card dark:!border-dark-border mb-10 profile-intro-card">
        <div class="py-4">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-1 h-6 bg-gradient-to-b from-blue-500 to-violet-500 rounded-full"></div>
            <h2 class="text-lg font-bold text-gray-900 dark:text-dark-text-primary">关于我</h2>
          </div>
          <div
            class="text-gray-600 dark:text-dark-text-secondary leading-relaxed text-base markdown-body"
            v-html="profile?.introduction ? renderMarkdown(profile.introduction) : renderMarkdown('热爱技术，追求卓越的全栈开发工程师。')"
          ></div>
        </div>
      </a-card>

      <!-- 获奖经历 - 美化时间线 -->
      <div class="!rounded-2xl !shadow-lg !shadow-gray-200/50 dark:bg-dark-card border border-gray-100 dark:border-dark-border bg-white overflow-hidden profile-awards-card">
        <div v-if="loading" class="p-10 text-center text-gray-400 dark:text-gray-500">加载中...</div>
        <template v-else>
          <div class="py-10 px-6 sm:px-10">
            <div class="flex items-center gap-3 mb-8">
              <div class="w-1 h-6 bg-gradient-to-b from-blue-500 to-violet-500 rounded-full"></div>
              <h2 class="text-lg font-bold text-gray-900 dark:text-dark-text-primary flex items-center gap-2">
                获奖经历
              </h2>
              <TrophyOutlined class="text-amber-500 text-sm" />
              <span v-if="awards.length" class="text-xs text-gray-400 dark:text-dark-text-muted bg-gray-100 dark:bg-dark-elevated px-2 py-0.5 rounded-full">{{ awards.length }} 项</span>
            </div>

            <div v-if="awards.length > 0" class="space-y-0">
              <div
                v-for="(award, index) in awards"
                :key="award.id"
                class="relative flex gap-5 pb-8 last:pb-0 group"
              >
                <!-- 时间线 -->
                <div class="flex flex-col items-center">
                  <div
                    class="w-3 h-3 rounded-full flex-shrink-0 mt-1.5 ring-4 ring-white dark:ring-dark-card transition-colors duration-300"
                    :class="index === 0 ? 'bg-gradient-to-br from-amber-400 to-orange-500' : 'bg-gray-300 dark:bg-gray-600 group-hover:bg-blue-400 dark:group-hover:bg-blue-500'"
                  ></div>
                  <div v-if="index < awards.length - 1" class="w-0.5 flex-1 bg-gradient-to-b from-gray-200 to-gray-100 dark:from-dark-border dark:to-transparent mt-1"></div>
                </div>

                <!-- 奖项内容 -->
                <div class="flex-1 pb-2 group-hover:translate-x-1 transition-transform duration-300">
                  <div class="flex flex-col sm:flex-row sm:items-baseline sm:justify-between gap-1 mb-1.5">
                    <h3 class="text-base font-semibold text-gray-900 dark:text-dark-text-primary group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                      {{ award.title }}
                    </h3>
                    <span class="text-xs text-gray-400 dark:text-dark-text-muted flex-shrink-0 font-medium bg-gray-50 dark:bg-dark-elevated px-2 py-0.5 rounded-full">
                      {{ award.awardDate }}
                    </span>
                  </div>
                  <p v-if="award.issuer" class="text-sm text-blue-500 dark:text-blue-400 mb-1 font-medium">
                    {{ award.issuer }}
                  </p>
                  <p v-if="award.description" class="text-sm text-gray-500 dark:text-dark-text-muted leading-relaxed">
                    {{ award.description }}
                  </p>
                </div>
              </div>
            </div>

            <div v-else class="flex flex-col items-center py-8 text-gray-400 dark:text-dark-text-muted">
              <TrophyOutlined class="text-3xl mb-2 opacity-30" />
              <span class="text-sm">暂无获奖记录</span>
            </div>
          </div>
        </template>
      </div>
      </div><!-- /resume-content -->
    </div>
  </div>
</template>

<style scoped>
/* 头像脉冲环动画 */
@keyframes pulse-ring {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}

.animate-pulse-ring {
  animation: pulse-ring 3s ease-in-out infinite;
}

/* 浮动动画 */
@keyframes float-slow {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-10px, 15px); }
}

@keyframes float-medium {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(15px, -10px); }
}

@keyframes float-fast {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-8px, -12px); }
}

.animate-float-slow { animation: float-slow 8s ease-in-out infinite; }
.animate-float-medium { animation: float-medium 6s ease-in-out infinite; }
.animate-float-fast { animation: float-fast 4s ease-in-out infinite; }

/* 主卡片悬停效果 */
.profile-main-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.profile-main-card:hover {
  transform: translateY(-2px);
}

/* 介绍卡片 */
.profile-intro-card {
  border: 1px solid rgba(0, 0, 0, 0.06) !important;
}

:deep(.dark) .profile-intro-card {
  border-color: #2a3242 !important;
}

/* 获奖卡片 */
.profile-awards-card {
  border: 1px solid rgba(0, 0, 0, 0.06) !important;
}

:deep(.dark) .profile-awards-card {
  border-color: #2a3242 !important;
}
</style>
