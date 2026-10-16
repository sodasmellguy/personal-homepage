<script setup lang="ts">
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import {
  EyeOutlined,
  ProjectOutlined,
  SolutionOutlined,
  TrophyOutlined,
  ThunderboltOutlined,
  FileTextOutlined,
  CommentOutlined,
  UserOutlined,
  DownloadOutlined,
  RiseOutlined,
  FireOutlined,
  ClockCircleOutlined,
  RocketOutlined,
  LineChartOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { getDashboardStats, getVisitTrend, getArticleStats, getTopArticles, getArticleTrend, getMonitorStats, type DashboardStats, type VisitTrendItem, type ArticleStatItem, type ArticleTrendItem, type MonitorStats } from '@/api/dashboard'
import { getArticleList } from '@/api/article'
import { get } from '@/utils/request'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const loading = ref(true)
const stats = ref<DashboardStats>({
  visitCount: 0,
  articleCount: 0,
  projectCount: 0,
  skillCount: 0,
  internshipCount: 0,
  awardCount: 0,
  commentCount: 0,
  recentLogs: [],
})

// 统计卡片配置 - 带渐变背景
const statCards = ref([
  { title: '总访问量', key: 'visitCount' as const, icon: EyeOutlined, gradient: 'from-blue-500 to-cyan-500', iconBg: 'bg-white/20' },
  { title: '文章数量', key: 'articleCount' as const, icon: FileTextOutlined, gradient: 'from-emerald-500 to-teal-500', iconBg: 'bg-white/20' },
  { title: '项目数量', key: 'projectCount' as const, icon: ProjectOutlined, gradient: 'from-violet-500 to-purple-500', iconBg: 'bg-white/20' },
  { title: '技能数量', key: 'skillCount' as const, icon: ThunderboltOutlined, gradient: 'from-amber-500 to-orange-500', iconBg: 'bg-white/20' },
  { title: '实习经历', key: 'internshipCount' as const, icon: SolutionOutlined, gradient: 'from-pink-500 to-rose-500', iconBg: 'bg-white/20' },
  { title: '获奖数量', key: 'awardCount' as const, icon: TrophyOutlined, gradient: 'from-yellow-500 to-amber-500', iconBg: 'bg-white/20' },
  { title: '评论数量', key: 'commentCount' as const, icon: CommentOutlined, gradient: 'from-indigo-500 to-blue-500', iconBg: 'bg-white/20' },
])

// 数字计数动画
const animatedStats = ref<Record<string, number>>({
  visitCount: 0,
  articleCount: 0,
  projectCount: 0,
  skillCount: 0,
  internshipCount: 0,
  awardCount: 0,
  commentCount: 0,
})

const animateCountUp = (key: string, target: number, duration = 1000) => {
  const start = animatedStats.value[key] || 0
  const startTime = performance.now()
  const animate = (now: number) => {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    // easeOutQuart
    const eased = 1 - Math.pow(1 - progress, 4)
    animatedStats.value[key] = Math.round(start + (target - start) * eased)
    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  requestAnimationFrame(animate)
}

// 卡片入场动画控制
const cardsVisible = ref(false)

// 格式化时间
const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 获取数据
onMounted(async () => {
  try {
    const { data } = await getDashboardStats()
    stats.value = data
    // 触发数字计数动画
    nextTick(() => {
      cardsVisible.value = true
      statCards.value.forEach((card, idx) => {
        setTimeout(() => {
          animateCountUp(card.key, data[card.key] || 0, 1200)
        }, idx * 80)
      })
    })
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    // 即使失败也显示卡片
    cardsVisible.value = true
  } finally {
    loading.value = false
    // 确保 cardsVisible 最终为 true
    if (!cardsVisible.value) cardsVisible.value = true
  }

  // 加载图表数据
  await nextTick()
  loadCharts()
  loadArticleOptions()
  loadArticleTrend()
  loadMonitor()
})

// CSV 导出
const exportLoading = ref(false)

const downloadCSV = (data: Record<string, any>[], filename: string) => {
  if (!data.length) { message.warning('暂无数据'); return }
  const headers = Object.keys(data[0])
  const csvContent = [
    '﻿' + headers.join(','),
    ...data.map(row => headers.map(h => `"${(row[h] ?? '').toString().replace(/"/g, '""')}"`).join(','))
  ].join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}

const handleExport = async (type: string) => {
  exportLoading.value = true
  try {
    const { data } = await get(`/system/export/${type}`)
    const filename = type === 'articles' ? '文章列表.csv' : '用户列表.csv'
    downloadCSV(data, filename)
    message.success('导出成功')
  } catch {
    message.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 图表数据
const visitTrendData = ref<VisitTrendItem[]>([])
const articleStatsData = ref<ArticleStatItem[]>([])
const topArticlesData = ref<{ id: number; title: string; viewCount: number }[]>([])

// 文章数据趋势
const articleTrendData = ref<ArticleTrendItem[]>([])
const articleTrendChartRef = ref<HTMLElement>()
const selectedArticleId = ref<number | undefined>(undefined)
const articleOptions = ref<{ id: number; title: string }[]>([])
const trendRange = ref(30)

const loadArticleOptions = async () => {
  try {
    const { data } = await getArticleList({ pageNum: 1, pageSize: 100 } as any)
    articleOptions.value = (data.rows || []).map((a: any) => ({ id: a.id, title: a.title }))
  } catch {
    // 文章下拉加载失败不影响其他图表
  }
}

const loadArticleTrend = async () => {
  try {
    const { data } = await getArticleTrend(trendRange.value, selectedArticleId.value)
    articleTrendData.value = data || []
    nextTick(() => renderArticleTrendChart())
  } catch {
    // 趋势加载失败不影响其他图表
  }
}

const handleTrendArticleChange = () => {
  loadArticleTrend()
}

const handleTrendRangeChange = () => {
  loadArticleTrend()
}

// 渲染文章数据趋势图（阅读/点赞/收藏 三条折线）
const renderArticleTrendChart = () => {
  if (!articleTrendChartRef.value || !articleTrendData.value.length) return
  const chart = echarts.getInstanceByDom(articleTrendChartRef.value) || echarts.init(articleTrendChartRef.value)
  const series = [
    { key: 'viewCount' as const, name: '阅读', color: '#3b82f6' },
    { key: 'likeCount' as const, name: '点赞', color: '#ef4444' },
    { key: 'collectCount' as const, name: '收藏', color: '#f59e0b' },
  ].map((s) => ({
    name: s.name,
    type: 'line' as const,
    smooth: true,
    symbol: 'circle',
    symbolSize: 5,
    data: articleTrendData.value.map((i) => i[s.key]),
    lineStyle: { color: s.color, width: 2.5 },
    itemStyle: { color: s.color },
    areaStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: s.color + '33' },
        { offset: 1, color: s.color + '05' },
      ]),
    },
  }))
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: isDark.value ? '#1c2333' : '#fff',
      borderColor: isDark.value ? '#2a3242' : '#e5e7eb',
      textStyle: { color: isDark.value ? '#e6edf3' : '#111827' },
    },
    legend: {
      data: ['阅读', '点赞', '收藏'],
      top: 0,
      textStyle: { color: isDark.value ? '#d1d5db' : '#374151', fontSize: 12 },
    },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: {
      type: 'category',
      data: articleTrendData.value.map((i) => i.date),
      axisLabel: { color: isDark.value ? '#9ca3af' : '#6b7280', fontSize: 11 },
      axisLine: { lineStyle: { color: isDark.value ? '#374151' : '#e5e7eb' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: isDark.value ? '#9ca3af' : '#6b7280', fontSize: 11 },
      splitLine: { lineStyle: { color: isDark.value ? '#374151' : '#f3f4f6' } },
    },
    series,
  })
}

// 图表 DOM refs
const visitChartRef = ref<HTMLElement>()
const articleChartRef = ref<HTMLElement>()

// 网站运行监控
const monitor = ref<MonitorStats | null>(null)

const formatUptime = (seconds: number) => {
  const d = Math.floor(seconds / 86400)
  const h = Math.floor((seconds % 86400) / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (d > 0) return `${d}天${h}小时`
  if (h > 0) return `${h}小时${m}分钟`
  return `${m}分钟`
}

const monitorCards = computed(() => {
  if (!monitor.value) return []
  return [
    { title: '今日 PV', value: monitor.value.todayPv, icon: EyeOutlined, color: 'blue', suffix: '次' },
    { title: '今日 UV', value: monitor.value.todayUv, icon: UserOutlined, color: 'violet', suffix: '人' },
    { title: '接口平均耗时', value: monitor.value.avgDuration, icon: RocketOutlined, color: 'emerald', suffix: 'ms' },
    { title: 'Redis 状态', value: monitor.value.redisStatus === '正常' ? '运行中' : monitor.value.redisStatus, icon: FireOutlined, color: monitor.value.redisStatus === '正常' ? 'amber' : 'red', suffix: '' },
    { title: '服务已运行', value: formatUptime(monitor.value.uptimeSeconds), icon: ClockCircleOutlined, color: 'cyan', suffix: '' },
  ]
})

const loadMonitor = async () => {
  try {
    const { data } = await getMonitorStats()
    monitor.value = data
  } catch {
    // 监控数据加载失败不影响主页面
  }
}

const monitorColorClass: Record<string, string> = {
  blue: 'bg-blue-50 dark:bg-blue-900/20 text-blue-500',
  violet: 'bg-violet-50 dark:bg-violet-900/20 text-violet-500',
  emerald: 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-500',
  amber: 'bg-amber-50 dark:bg-amber-900/20 text-amber-500',
  red: 'bg-red-50 dark:bg-red-900/20 text-red-500',
  cyan: 'bg-cyan-50 dark:bg-cyan-900/20 text-cyan-500',
}

const isDark = computed(() => appStore.isDark)

// 切换明暗主题时重绘所有图表，保证配色跟随主题
watch(isDark, () => {
  nextTick(() => {
    renderVisitChart()
    renderArticleChart()
    renderArticleTrendChart()
  })
})

const loadCharts = async () => {
  try {
    const [trendRes, statsRes, topRes] = await Promise.allSettled([
      getVisitTrend(30),
      getArticleStats(),
      getTopArticles(10),
    ])

    if (trendRes.status === 'fulfilled' && trendRes.value.data?.length) {
      visitTrendData.value = trendRes.value.data
      nextTick(() => renderVisitChart())
    }
    if (statsRes.status === 'fulfilled' && statsRes.value.data?.length) {
      articleStatsData.value = statsRes.value.data
      nextTick(() => renderArticleChart())
    }
    if (topRes.status === 'fulfilled' && topRes.value.data?.length) {
      topArticlesData.value = topRes.value.data
    }
  } catch (e) {
    // 图表加载失败不影响主页面
  }
}

// 渲染访问趋势图
const renderVisitChart = () => {
  if (!visitChartRef.value || !visitTrendData.value.length) return
  const chart = echarts.getInstanceByDom(visitChartRef.value) || echarts.init(visitChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: isDark.value ? '#1c2333' : '#fff', borderColor: isDark.value ? '#2a3242' : '#e5e7eb', textStyle: { color: isDark.value ? '#e6edf3' : '#111827' } },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: 'category',
      data: visitTrendData.value.map((i) => i.date),
      axisLabel: { color: isDark.value ? '#9ca3af' : '#6b7280', fontSize: 11 },
      axisLine: { lineStyle: { color: isDark.value ? '#374151' : '#e5e7eb' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: isDark.value ? '#9ca3af' : '#6b7280', fontSize: 11 },
      splitLine: { lineStyle: { color: isDark.value ? '#374151' : '#f3f4f6' } },
    },
    series: [{
      data: visitTrendData.value.map((i) => i.count),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#3b82f6', width: 2.5 },
      itemStyle: { color: '#3b82f6' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59,130,246,0.25)' },
          { offset: 1, color: 'rgba(59,130,246,0.02)' },
        ]),
      },
    }],
  })
}

// 渲染文章分类饼图
const renderArticleChart = () => {
  if (!articleChartRef.value || !articleStatsData.value.length) return
  const chart = echarts.getInstanceByDom(articleChartRef.value) || echarts.init(articleChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 篇 ({d}%)', backgroundColor: isDark.value ? '#1c2333' : '#fff', borderColor: isDark.value ? '#2a3242' : '#e5e7eb', textStyle: { color: isDark.value ? '#e6edf3' : '#111827' } },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: isDark.value ? '#d1d5db' : '#374151', fontSize: 12 },
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: isDark.value ? '#1f2937' : '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
      },
      data: articleStatsData.value.map((item, idx) => ({
        ...item,
        itemStyle: { color: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4'][idx % 7] },
      })),
    }],
  })
}

// 快速操作配置
const quickActions = [
  { title: '编辑个人信息', icon: UserOutlined, route: '/admin/profile', color: 'blue' },
  { title: '管理项目', icon: ProjectOutlined, route: '/admin/project', color: 'purple' },
  { title: '管理文章', icon: FileTextOutlined, route: '/admin/article', color: 'emerald' },
  { title: '查看前台', icon: RocketOutlined, route: '/', color: 'orange' },
]
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-violet-500 flex items-center justify-center shadow-lg shadow-blue-500/20">
          <DashboardOutlined class="text-white text-lg" />
        </div>
        <div>
          <h1 class="text-xl font-bold text-gray-800 dark:text-dark-text-primary">仪表盘</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">欢迎回来，这是你的数据概览</p>
        </div>
      </div>
      <a-dropdown :disabled="exportLoading">
        <a-button :loading="exportLoading" class="!rounded-lg !shadow-sm">
          <template #icon><DownloadOutlined /></template>
          导出数据
        </a-button>
        <template #overlay>
          <a-menu @click="(e: any) => handleExport(e.key)">
            <a-menu-item key="articles">导出文章列表</a-menu-item>
            <a-menu-item key="users">导出用户列表</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>

    <!-- 统计卡片 - 渐变风格 -->
    <a-spin :spinning="loading">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div
          v-for="card in statCards"
          :key="card.key"
          class="group relative overflow-hidden rounded-xl p-5 text-white shadow-md hover:shadow-xl transition-all duration-300 hover:-translate-y-1 cursor-default stagger-item"
          :class="[`bg-gradient-to-br ${card.gradient}`, { 'is-visible': cardsVisible }]"
        >
          <!-- 背景装饰 -->
          <div class="absolute -right-4 -top-4 w-24 h-24 bg-white/10 rounded-full transition-transform duration-500 group-hover:scale-150"></div>
          <div class="absolute -right-8 -bottom-8 w-32 h-32 bg-white/5 rounded-full transition-transform duration-700 group-hover:scale-125"></div>

          <div class="relative flex items-center justify-between">
            <div>
              <p class="text-sm text-white/80 mb-1">{{ card.title }}</p>
              <p class="text-3xl font-bold tabular-nums">{{ animatedStats[card.key] }}</p>
            </div>
            <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/20 backdrop-blur-sm transition-transform duration-300 group-hover:scale-110 group-hover:rotate-6">
              <component :is="card.icon" class="text-2xl text-white" />
            </div>
          </div>
        </div>
      </div>

      <!-- 运行监控卡片 -->
      <div v-if="monitor" class="grid grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
        <div
          v-for="card in monitorCards"
          :key="card.title"
          class="flex items-center gap-3 bg-white dark:bg-dark-card rounded-xl p-4 border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300"
        >
          <div class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0" :class="monitorColorClass[card.color]">
            <component :is="card.icon" class="text-lg" />
          </div>
          <div class="min-w-0">
            <p class="text-xs text-gray-400 dark:text-dark-text-muted">{{ card.title }}</p>
            <p class="text-lg font-bold text-gray-800 dark:text-dark-text-primary tabular-nums truncate">
              {{ card.value }}<span v-if="card.suffix" class="text-xs font-normal text-gray-400 ml-1">{{ card.suffix }}</span>
            </p>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <!-- 访问趋势图 -->
        <div class="bg-white dark:bg-dark-card rounded-xl p-5 border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300 relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-blue-500 to-cyan-500"></div>
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded-lg bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center">
                <RiseOutlined class="text-blue-500" />
              </div>
              <h3 class="text-base font-semibold text-gray-800 dark:text-dark-text-primary">近 30 天访问趋势</h3>
            </div>
          </div>
          <div ref="visitChartRef" style="width: 100%; height: 280px;"></div>
          <div v-if="!visitTrendData.length" class="flex items-center justify-center h-[280px] text-gray-400 text-sm">
            <div class="text-center">
              <EyeOutlined class="text-3xl mb-2 opacity-30" />
              <p>暂无数据</p>
            </div>
          </div>
        </div>

        <!-- 文章分类饼图 -->
        <div class="bg-white dark:bg-dark-card rounded-xl p-5 border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300 relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-orange-500 to-amber-500"></div>
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded-lg bg-orange-50 dark:bg-orange-900/20 flex items-center justify-center">
                <FireOutlined class="text-orange-500" />
              </div>
              <h3 class="text-base font-semibold text-gray-800 dark:text-dark-text-primary">文章分类分布</h3>
            </div>
          </div>
          <div ref="articleChartRef" style="width: 100%; height: 280px;"></div>
          <div v-if="!articleStatsData.length" class="flex items-center justify-center h-[280px] text-gray-400 text-sm">
            <div class="text-center">
              <FileTextOutlined class="text-3xl mb-2 opacity-30" />
              <p>暂无数据</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 文章数据趋势 -->
      <div class="bg-white dark:bg-dark-card rounded-xl p-5 border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300 mb-6 relative overflow-hidden">
        <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-emerald-500 to-teal-500"></div>
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-lg bg-emerald-50 dark:bg-emerald-900/20 flex items-center justify-center">
              <LineChartOutlined class="text-emerald-500" />
            </div>
            <h3 class="text-base font-semibold text-gray-800 dark:text-dark-text-primary">文章数据趋势</h3>
          </div>
          <div class="flex items-center gap-2">
            <a-select
              v-model:value="selectedArticleId"
              placeholder="全部文章"
              allow-clear
              show-search
              option-filter-prop="title"
              class="!w-56"
              :options="articleOptions.map((a) => ({ value: a.id, label: a.title }))"
              @change="handleTrendArticleChange"
            />
            <a-radio-group v-model:value="trendRange" size="small" button-style="solid" @change="handleTrendRangeChange">
              <a-radio-button :value="7">近7天</a-radio-button>
              <a-radio-button :value="30">近30天</a-radio-button>
              <a-radio-button :value="90">近90天</a-radio-button>
            </a-radio-group>
          </div>
        </div>
        <div ref="articleTrendChartRef" style="width: 100%; height: 300px;"></div>
        <div v-if="!articleTrendData.length" class="flex items-center justify-center h-[300px] text-gray-400 text-sm -mt-[300px] pointer-events-none">
          <div class="text-center">
            <LineChartOutlined class="text-3xl mb-2 opacity-30" />
            <p>暂无趋势数据（快照自上线起开始积累）</p>
          </div>
        </div>
      </div>

      <!-- 热门文章排行 -->
      <div v-if="topArticlesData.length" class="bg-white dark:bg-dark-card rounded-xl p-5 border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300 mb-6 relative overflow-hidden">
        <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-red-500 to-orange-500"></div>
        <div class="flex items-center gap-2 mb-5">
          <div class="w-8 h-8 rounded-lg bg-red-50 dark:bg-red-900/20 flex items-center justify-center">
            <FireOutlined class="text-red-500" />
          </div>
          <h3 class="text-base font-semibold text-gray-800 dark:text-dark-text-primary">热门文章 Top 10</h3>
        </div>
        <div>
          <div
            v-for="(article, index) in topArticlesData"
            :key="article.id"
            class="flex items-center gap-3 py-3 border-b border-gray-50 dark:border-dark-border last:border-0 group hover:bg-gray-50 dark:hover:bg-dark-elevated/50 rounded-lg px-2 -mx-2 transition-colors duration-200"
          >
            <span
              class="w-7 h-7 rounded-lg flex items-center justify-center text-xs font-bold flex-shrink-0"
              :class="index < 3
                ? 'bg-gradient-to-br from-amber-400 to-orange-500 text-white shadow-sm shadow-orange-500/20'
                : 'bg-gray-100 dark:bg-dark-elevated text-gray-500 dark:text-dark-text-muted'"
            >
              {{ index + 1 }}
            </span>
            <span class="flex-1 text-sm text-gray-700 dark:text-dark-text-secondary truncate group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">{{ article.title }}</span>
            <div class="w-28 bg-gray-100 dark:bg-dark-elevated rounded-full h-1.5 flex-shrink-0">
              <div
                class="h-1.5 rounded-full transition-all duration-500"
                :class="index < 3 ? 'bg-gradient-to-r from-amber-400 to-orange-500' : 'bg-blue-400'"
                :style="{ width: topArticlesData[0] ? (article.viewCount / topArticlesData[0].viewCount * 100) + '%' : '0%' }"
              ></div>
            </div>
            <span class="text-xs text-gray-400 dark:text-dark-text-muted w-14 text-right flex-shrink-0">{{ article.viewCount }} 次</span>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 最近活动 -->
        <div class="bg-white dark:bg-dark-card rounded-xl p-5 border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300 relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-blue-500 to-indigo-500"></div>
          <div class="flex items-center gap-2 mb-5">
            <div class="w-8 h-8 rounded-lg bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center">
              <ClockCircleOutlined class="text-blue-500" />
            </div>
            <h3 class="text-base font-semibold text-gray-800 dark:text-dark-text-primary">最近活动</h3>
          </div>
          <div v-if="stats.recentLogs.length > 0" class="space-y-0">
            <div
              v-for="(log, index) in stats.recentLogs"
              :key="log.id"
              class="flex items-center justify-between py-2.5 group"
              :class="{ 'border-b border-gray-50 dark:border-dark-border': index < stats.recentLogs.length - 1 }"
            >
              <div class="flex items-center gap-3">
                <div class="w-1.5 h-1.5 rounded-full bg-blue-400 flex-shrink-0"></div>
                <span class="text-sm text-gray-700 dark:text-dark-text-secondary">
                  <span class="inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 mr-1.5">{{ log.module }}</span>
                  {{ log.operation }}
                </span>
              </div>
              <span class="text-xs text-gray-400 dark:text-dark-text-muted whitespace-nowrap ml-4">
                {{ formatTime(log.createTime) }}
              </span>
            </div>
          </div>
          <div v-else class="flex flex-col items-center py-8 text-gray-400 dark:text-dark-text-muted text-sm">
            <ClockCircleOutlined class="text-3xl mb-2 opacity-30" />
            <p>暂无活动记录</p>
          </div>
        </div>

        <!-- 快速操作 -->
        <div class="bg-white dark:bg-dark-card rounded-xl p-5 border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300 relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-violet-500 to-purple-500"></div>
          <div class="flex items-center gap-2 mb-5">
            <div class="w-8 h-8 rounded-lg bg-violet-50 dark:bg-violet-900/20 flex items-center justify-center">
              <RocketOutlined class="text-violet-500" />
            </div>
            <h3 class="text-base font-semibold text-gray-800 dark:text-dark-text-primary">快速操作</h3>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div
              v-for="action in quickActions"
              :key="action.title"
              class="group flex flex-col items-center gap-2 p-4 rounded-xl border border-gray-100 dark:border-dark-border hover:border-blue-200 dark:hover:border-blue-800 hover:bg-blue-50/50 dark:hover:bg-blue-900/10 cursor-pointer transition-all duration-300 hover:-translate-y-0.5 hover:shadow-md"
              @click="$router.push(action.route)"
            >
              <div
                class="w-10 h-10 rounded-lg flex items-center justify-center transition-colors duration-300"
                :class="{
                  'bg-blue-50 dark:bg-blue-900/20 text-blue-500 group-hover:bg-blue-100 dark:group-hover:bg-blue-800/30': action.color === 'blue',
                  'bg-purple-50 dark:bg-purple-900/20 text-purple-500 group-hover:bg-purple-100 dark:group-hover:bg-purple-800/30': action.color === 'purple',
                  'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-500 group-hover:bg-emerald-100 dark:group-hover:bg-emerald-800/30': action.color === 'emerald',
                  'bg-orange-50 dark:bg-orange-900/20 text-orange-500 group-hover:bg-orange-100 dark:group-hover:bg-orange-800/30': action.color === 'orange',
                }"
              >
                <component :is="action.icon" class="text-lg" />
              </div>
              <span class="text-sm font-medium text-gray-600 dark:text-dark-text-secondary group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                {{ action.title }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>
