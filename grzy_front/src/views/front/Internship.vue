<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAllInternships } from '@/api/internship'
import type { Internship } from '@/api/internship'
import {
  BankOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons-vue'
import SkeletonCard from '@/components/SkeletonCard.vue'

const internships = ref<Internship[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const { data } = await getAllInternships()
    internships.value = data
  } catch (error) {
    console.error('获取实习经历失败:', error)
  } finally {
    loading.value = false
  }
})

const parseAchievements = (achievements: string | undefined): string[] => {
  if (!achievements) return []
  return achievements.split('\n').filter(Boolean)
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-dark-base py-12 px-4 sm:px-8 transition-colors duration-300">
    <div class="max-w-4xl mx-auto">
      <!-- 页面标题 — 左对齐 -->
      <div class="mb-10 sm:mb-14">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-8 h-0.5 bg-emerald-500 rounded-full"></div>
          <span class="text-emerald-600 dark:text-emerald-400 text-sm font-medium tracking-wider uppercase">Experience</span>
        </div>
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-dark-text-primary">实习经历</h1>
        <p class="text-gray-500 dark:text-dark-text-muted mt-2">我的工作经历和成长</p>
      </div>

      <SkeletonCard v-if="loading" type="timeline" :count="4" />
      <template v-else>
        <div class="relative">
          <!-- 时间线 — 更粗 + 渐变 -->
          <div class="absolute left-6 top-2 bottom-2 w-[3px] bg-gradient-to-b from-blue-500 via-purple-500 to-emerald-500 rounded-full hidden md:block"></div>

          <div class="space-y-6 sm:space-y-8">
            <div
              v-for="(internship, index) in internships"
              :key="internship.id"
              class="relative md:pl-16"
            >
              <!-- 时间线节点 — 带图标 + 外圈 pulse -->
              <div class="hidden md:flex absolute left-3 top-6 z-10">
                <div class="relative">
                  <div class="w-7 h-7 bg-blue-500 rounded-full flex items-center justify-center shadow-md shadow-blue-500/30 ring-4 ring-white dark:ring-dark-base">
                    <BankOutlined class="text-white text-xs" />
                  </div>
                  <!-- 第一个节点加 pulse -->
                  <div v-if="index === 0" class="absolute inset-0 w-7 h-7 bg-blue-400 rounded-full animate-ping opacity-20"></div>
                </div>
              </div>

              <!-- 卡片 — 第一个加左边蓝色高亮 -->
              <div
                class="bg-white dark:bg-dark-card rounded-xl p-5 sm:p-6 shadow-sm hover:shadow-2xl hover:shadow-blue-500/5 transition-all duration-500 hover:-translate-y-1 border border-gray-100 dark:border-dark-border"
                :class="index === 0 ? 'border-l-2 border-l-blue-500' : ''"
              >
                <div class="flex flex-col md:flex-row md:items-start md:justify-between mb-3">
                  <div>
                    <div class="flex items-center gap-2 mb-1">
                      <h3 class="text-lg sm:text-xl font-semibold text-gray-900 dark:text-dark-text-primary">
                        {{ internship.position }}
                      </h3>
                      <span v-if="index === 0" class="px-2 py-0.5 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 text-xs rounded font-medium">最近</span>
                    </div>
                    <div class="flex flex-wrap items-center text-gray-500 dark:text-dark-text-muted mt-1 gap-3 text-sm">
                      <span class="flex items-center gap-1.5">
                        <BankOutlined class="text-blue-500" />
                        <span class="font-medium text-gray-700 dark:text-dark-text-secondary">{{ internship.company }}</span>
                      </span>
                      <span class="text-gray-300 dark:text-dark-border">|</span>
                      <span class="flex items-center gap-1.5">
                        <CalendarOutlined />
                        {{ internship.startDate }} - {{ internship.endDate || '至今' }}
                      </span>
                    </div>
                  </div>
                </div>

                <p v-if="internship.description" class="text-gray-600 dark:text-dark-text-secondary mb-4 leading-relaxed text-sm">
                  {{ internship.description }}
                </p>

                <div v-if="internship.achievements">
                  <h4 class="font-medium text-gray-700 dark:text-dark-text-secondary mb-3 flex items-center gap-2 text-sm">
                    <CheckCircleOutlined class="text-emerald-500" />
                    工作成果
                  </h4>
                  <ul class="space-y-2">
                    <li
                      v-for="(achievement, idx) in parseAchievements(internship.achievements)"
                      :key="idx"
                      class="flex items-start gap-3"
                    >
                      <span class="mt-1 w-1.5 h-1.5 rounded-full bg-emerald-500 flex-shrink-0"></span>
                      <span class="text-gray-600 dark:text-dark-text-secondary text-sm leading-relaxed">{{ achievement }}</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>

        <a-empty v-if="internships.length === 0" description="暂无实习经历" />
      </template>
    </div>
  </div>
</template>
