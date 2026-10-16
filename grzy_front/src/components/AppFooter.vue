<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  HomeOutlined,
  UserOutlined,
  ProjectOutlined,
  SolutionOutlined,
  ReadOutlined,
  GithubOutlined,
  MailOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons-vue'
import { getSiteStats } from '@/api/front'

// 站点统计（本站已运行 / 总访问）
const totalVisits = ref<number | null>(null)
const runDays = ref<number | null>(null)

const fetchSiteStats = async () => {
  try {
    const { data } = await getSiteStats()
    totalVisits.value = data?.totalVisits ?? null
    runDays.value = data?.runDays ?? null
  } catch {
    // 静默失败，页脚不展示该卡片
  }
}

onMounted(fetchSiteStats)

const currentYear = new Date().getFullYear()

const navLinks = [
  { key: '/home', label: '首页', icon: HomeOutlined },
  { key: '/profile', label: '个人信息', icon: UserOutlined },
  { key: '/project', label: '项目展示', icon: ProjectOutlined },
  { key: '/internship', label: '实习经历', icon: SolutionOutlined },
  { key: '/article', label: '技术博客', icon: ReadOutlined },
]

const socialLinks = [
  { label: 'GitHub', icon: GithubOutlined, href: 'https://github.com' },
  { label: '邮箱', icon: MailOutlined, href: 'mailto:ceshi@example.com' },
]
</script>

<template>
  <footer class="bg-dark-base transition-colors duration-300">
    <!-- 渐变分割线 -->
    <div class="gradient-divider"></div>
    <div class="max-w-7xl mx-auto px-4 sm:px-8">
      <!-- 上半部分：品牌 + 联系（各50%）+ 导航（独占一行） -->
      <div class="py-6 sm:py-8 space-y-3 sm:space-y-4">
        <!-- 第一行：品牌区 + 社交链接 -->
        <div class="grid grid-cols-1 sm:grid-cols-[7fr_3fr] gap-6 sm:gap-10">
          <!-- 品牌区 -->
          <div class="flex flex-col items-center sm:items-start">
            <div class="flex items-center gap-2 sm:gap-3 mb-2 sm:mb-4">
              <img src="/logo.svg" alt="Logo" class="w-8 h-8 sm:w-9 sm:h-9 rounded-lg shadow-md shadow-blue-500/20 object-contain" />
              <span class="text-dark-text-primary font-bold text-base sm:text-lg">赵先生的个人主页</span>
            </div>
            <p class="text-xs sm:text-sm text-dark-text-muted leading-relaxed text-center sm:text-left max-w-[700px]">
              全栈开发工程师，热爱技术，追求卓越。
            </p>
          </div>

          <!-- 社交链接 -->
          <div class="flex flex-col items-center sm:items-start">
            <h4 class="text-dark-text-primary font-semibold text-xs uppercase tracking-widest mb-2 sm:mb-4">联系</h4>
            <div class="flex gap-2">
              <a
                v-for="social in socialLinks"
                :key="social.label"
                :href="social.href"
                :title="social.label"
                target="_blank"
                class="group w-9 h-9 sm:w-10 sm:h-10 rounded-lg sm:rounded-xl bg-dark-card border border-dark-border flex items-center justify-center text-dark-text-muted hover:text-blue-400 hover:border-blue-500/40 hover:bg-blue-500/10 hover:scale-110 hover:-rotate-6 transition-all duration-300"
              >
                <component :is="social.icon" class="text-xs sm:text-sm group-hover:scale-110 transition-transform duration-300" />
              </a>
              <!-- RSS -->
              <a
                href="/rss.xml"
                title="RSS"
                target="_blank"
                class="group w-9 h-9 sm:w-10 sm:h-10 rounded-lg sm:rounded-xl bg-dark-card border border-dark-border flex items-center justify-center text-dark-text-muted hover:text-orange-400 hover:border-orange-500/40 hover:bg-orange-500/10 hover:scale-110 hover:-rotate-6 transition-all duration-300"
              >
                <svg class="text-xs sm:text-sm" viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                  <path d="M6.18 15.64a2.18 2.18 0 0 1 2.18 2.18C8.36 19 7.38 20 6.18 20C5 20 4 19 4 17.82a2.18 2.18 0 0 1 2.18-2.18M4 4.44A15.56 15.56 0 0 1 19.56 20h-2.83A12.73 12.73 0 0 0 4 7.27V4.44m0 5.66a9.9 9.9 0 0 1 9.9 9.9h-2.83A7.07 7.07 0 0 0 4 12.93V10.1Z"/>
                </svg>
              </a>
            </div>
          </div>
        </div>

        <!-- 第二行：导航链接 — 独占一行 -->
        <div class="flex flex-col items-center sm:items-start">
          <nav class="flex flex-wrap justify-center sm:justify-start gap-x-4 sm:gap-x-6 gap-y-1.5 sm:gap-y-2">
            <router-link
              v-for="link in navLinks"
              :key="link.key"
              :to="link.key"
              class="group inline-flex items-center gap-2 sm:gap-4 text-xs sm:text-[13px] text-dark-text-muted hover:text-blue-400 transition-colors duration-200"
            >
              <span class="w-1 h-1 rounded-full bg-dark-border group-hover:bg-blue-400 group-hover:scale-150 transition-all duration-200"></span>
              {{ link.label }}
            </router-link>
          </nav>
        </div>
      </div>

      <!-- 底部版权 -->
      <div class="border-t border-dark-border py-3 sm:py-4 flex flex-col sm:flex-row items-center justify-between gap-1">
        <p class="text-[10px] sm:text-[11px] text-dark-text-muted">
           Copyright©{{ currentYear }} MR ZHAO All Rights Reserved
        </p>
        <!-- 本站运行统计卡片 -->
        <div
          v-if="runDays !== null"
          class="inline-flex items-center gap-1.5 text-[10px] sm:text-[11px] text-dark-text-muted"
        >
          <ThunderboltOutlined class="text-amber-400/80" />
          <span>
            本站已运行
            <span class="text-blue-400/90 font-semibold">{{ runDays }}</span>
            天
            <template v-if="totalVisits !== null">
              · 总访问
              <span class="text-blue-400/90 font-semibold">{{ totalVisits }}</span>
              次
            </template>
          </span>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.gradient-divider {
  height: 2px;
  background: linear-gradient(90deg, transparent, #3b82f6, #8b5cf6, #ec4899, #8b5cf6, #3b82f6, transparent);
  background-size: 200% 100%;
  animation: divider-shimmer 4s linear infinite;
}

@keyframes divider-shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
