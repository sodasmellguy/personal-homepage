<script setup lang="ts">
import { ref, onMounted, onUnmounted, onBeforeUnmount, nextTick, computed, watch } from 'vue'
import * as echarts from 'echarts'
import {
  EnvironmentOutlined,
  TeamOutlined,
  GlobalOutlined,
  ClockCircleOutlined,
  RiseOutlined,
} from '@ant-design/icons-vue'
import {
  getVisitorLocations,
  getRecentVisitors,
  getVisitorCityStats,
  getVisitorCountryStats,
  type VisitorLocation,
  type CityStat,
  type CountryStat,
} from '@/api/visitorLocation'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const isDark = computed(() => appStore.isDark)

const loading = ref(true)
const mapRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()

const locations = ref<VisitorLocation[]>([])
const recentVisitors = ref<VisitorLocation[]>([])
const cityStats = ref<CityStat[]>([])
const countryStats = ref<CountryStat[]>([])

const totalVisitors = computed(() => locations.value.length)
const totalCities = computed(() => new Set(locations.value.map(l => l.city).filter(Boolean)).size)
const totalCountries = computed(() => new Set(locations.value.map(l => l.country).filter(Boolean)).size)

let mapChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null
let isUnmounted = false

const isChartValid = (chart: echarts.ECharts | null): boolean => {
  if (!chart) return false
  try {
    chart.getDom()
    return true
  } catch {
    return false
  }
}

const handleResize = () => {
  if (isChartValid(mapChart)) mapChart!.resize()
  if (isChartValid(barChart)) barChart!.resize()
}

const chinaCityCoords: Record<string, [number, number]> = {
  '北京': [116.46, 39.92], '上海': [121.48, 31.22], '广州': [113.23, 23.16],
  '深圳': [114.07, 22.62], '成都': [104.06, 30.67], '杭州': [120.19, 30.26],
  '武汉': [114.31, 30.52], '南京': [118.78, 32.04], '重庆': [106.54, 29.59],
  '西安': [108.95, 34.27], '苏州': [120.62, 31.32], '天津': [117.2, 39.13],
  '长沙': [112.94, 28.23], '沈阳': [123.38, 41.8], '哈尔滨': [126.63, 45.75],
  '济南': [117.0, 36.65], '青岛': [120.33, 36.07], '大连': [121.62, 38.92],
  '厦门': [118.1, 24.46], '昆明': [102.73, 25.04], '合肥': [117.27, 31.86],
  '福州': [119.3, 26.08], '郑州': [113.65, 34.76], '长春': [125.35, 43.88],
  '南昌': [115.89, 28.68], '贵阳': [106.71, 26.57], '南宁': [108.33, 22.84],
  '太原': [112.55, 37.87], '石家庄': [114.51, 38.04], '乌鲁木齐': [87.68, 43.77],
  '兰州': [103.73, 36.03], '呼和浩特': [111.65, 40.82], '海口': [110.35, 20.02],
  '拉萨': [91.11, 29.97], '银川': [106.27, 38.47], '西宁': [101.74, 36.56],
  '珠海': [113.52, 22.3], '佛山': [113.11, 23.05], '东莞': [113.75, 23.04],
  '无锡': [120.29, 31.59], '宁波': [121.56, 29.87], '温州': [120.65, 28.01],
  '烟台': [121.39, 37.52], '常州': [119.95, 31.79], '徐州': [117.2, 34.26],
}

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

// 加载中国地图 GeoJSON
// 优先使用本地静态资源（不依赖外网），阿里云 DataV 仅作兜底
const CHINA_MAP_SOURCES = [
  '/100000_full.json', // 本地：public/100000_full.json
  'https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json', // 阿里云 DataV
]
let chinaGeoJson: any = null
async function loadChinaMap() {
  if (chinaGeoJson) return chinaGeoJson
  for (const url of CHINA_MAP_SOURCES) {
    try {
      const resp = await fetch(url)
      if (!resp.ok) continue
      const json = await resp.json()
      // GeoJSON 必须有 features 数组且非空
      if (json && Array.isArray(json.features) && json.features.length > 0) {
        chinaGeoJson = json
        echarts.registerMap('china', chinaGeoJson)
        return chinaGeoJson
      }
    } catch {
      // 静默继续下一个源
    }
  }
  console.error('加载中国地图失败：所有数据源均不可用')
  return null
}

function buildMapData() {
  const cityCountMap = new Map<string, { count: number; coord: [number, number] }>()
  locations.value.forEach(loc => {
    if (!loc.city) return
    const coord = chinaCityCoords[loc.city]
    if (!coord) return
    const existing = cityCountMap.get(loc.city)
    if (existing) {
      existing.count++
    } else {
      cityCountMap.set(loc.city, { count: 1, coord })
    }
  })
  return Array.from(cityCountMap.entries()).map(([name, { count, coord }]) => ({
    name,
    value: [...coord, count] as [number, number, number],
  }))
}


// 省份简称 -> GeoJSON 全称映射
const PROVINCE_NAME_MAP: Record<string, string> = {
  '北京': '北京市', '天津': '天津市', '河北': '河北省', '山西': '山西省',
  '内蒙古': '内蒙古自治区', '辽宁': '辽宁省', '吉林': '吉林省', '黑龙江': '黑龙江省',
  '上海': '上海市', '江苏': '江苏省', '浙江': '浙江省', '安徽': '安徽省',
  '福建': '福建省', '江西': '江西省', '山东': '山东省', '河南': '河南省',
  '湖北': '湖北省', '湖南': '湖南省', '广东': '广东省', '广西': '广西壮族自治区',
  '海南': '海南省', '重庆': '重庆市', '四川': '四川省', '贵州': '贵州省',
  '云南': '云南省', '西藏': '西藏自治区', '陕西': '陕西省', '甘肃': '甘肃省',
  '青海': '青海省', '宁夏': '宁夏回族自治区', '新疆': '新疆维吾尔自治区',
  '台湾': '台湾省', '香港': '香港特别行政区', '澳门': '澳门特别行政区',
}

// 按省份聚合访客数据(map series)
function buildProvinceData() {
  const provinceCountMap = new Map<string, number>()
  locations.value.forEach(loc => {
    const province = loc.province
    if (!province) return
    const fullName = PROVINCE_NAME_MAP[province] || province
    const existing = provinceCountMap.get(fullName)
    if (existing !== undefined) {
      provinceCountMap.set(fullName, existing + 1)
    } else {
      provinceCountMap.set(fullName, 1)
    }
  })
  return Array.from(provinceCountMap.entries()).map(([name, count]) => ({
    name,
    value: count,
  }))
}

// 计算访客数量最大值
function getMaxCount(): number {
  const data = buildMapData() as { name: string; value: [number, number, number] }[]
  if (data.length === 0) return 10
  return Math.max(...data.map(d => d.value[2]), 10)
}

async function renderChinaMap() {
  if (!mapRef.value || isUnmounted) return
  await loadChinaMap()
  if (isUnmounted || !mapRef.value) return
  if (isChartValid(mapChart)) mapChart!.dispose()
  mapChart = echarts.init(mapRef.value)

  const scatterData = buildMapData()
  const bgColor = isDark.value ? '#111827' : '#f8fafc'
  const maxCount = getMaxCount()

  mapChart.setOption({
    backgroundColor: bgColor,
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        if (params.seriesType === 'scatter' || params.seriesType === 'effectScatter') {
          const count = params.value[2]
          const rank = scatterData.filter((d: any) => d.value[2] >= count).length
          return `<div style="font-size:14px;font-weight:600;margin-bottom:4px;">${params.name}</div>
                  <div style="color:#666;">访客数: <span style="font-weight:600;color:#3b82f6;">${count}</span> 人</div>
                  <div style="color:#999;font-size:12px;">城市排名: 第 ${rank} 位</div>`
        }
        if (params.seriesType === 'map' && params.value !== undefined && params.value !== '-') {
          return `<div style="font-size:14px;font-weight:600;margin-bottom:4px;">${params.name}</div>
                  <div style="color:#666;">访客数: <span style="font-weight:600;color:#3b82f6;">${params.value}</span> 人</div>`
        }
        return params.name
      },
    },
    visualMap: {
      type: 'continuous',
      min: 1,
      max: maxCount,
      text: ['多', '少'],
      realtime: false,
      calculable: true,
      inRange: {
        color: ['#22c55e', '#eab308', '#f97316', '#ef4444']
      },
      right: 20,
      bottom: 30,
      textStyle: { color: isDark.value ? '#d1d5db' : '#4b5563', fontSize: 12 },
    },
    geo: {
      map: 'china',
      roam: true,
      zoom: 1.2,
      center: [104, 36],
      show: false,
      label: { show: false },
      itemStyle: {
        areaColor: isDark.value ? '#1f2937' : '#e8f4f8',
        borderColor: isDark.value ? '#374151' : '#93c5fd',
        borderWidth: 1,
      },
    },
    series: [
      {
        type: 'map',
        map: 'china',
        roam: true,
        zoom: 1.2,
        center: [104, 36],
        label: { show: false },
        itemStyle: {
          borderColor: isDark.value ? '#374151' : '#93c5fd',
          borderWidth: 1,
        },
        emphasis: {
          label: {
            show: true,
            color: isDark.value ? '#e5e7eb' : '#1e40af',
            fontSize: 12,
          },
          itemStyle: {
            areaColor: isDark.value ? '#374151' : '#bfdbfe',
          },
        },
        data: buildProvinceData(),
        zlevel: 0,
      },
      {
        type: 'scatter',
        coordinateSystem: 'geo',
        data: scatterData,
        symbol: 'circle',
        symbolSize: (val: number[]) => {
          const count = val[2]
          return Math.max(10, Math.min(12 + count * 4, 50))
        },
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0,0,0,0.3)',
        },
        emphasis: {
          scale: 1.5,
          itemStyle: {
            shadowBlur: 20,
            shadowColor: 'rgba(0,0,0,0.4)',
          },
        },
        zlevel: 2,
      },
      {
        type: 'effectScatter',
        coordinateSystem: 'geo',
        data: [...scatterData]
          .sort((a: any, b: any) => b.value[2] - a.value[2])
          .slice(0, 5),
        symbolSize: (val: number[]) => {
          return Math.max(12, Math.min(14 + val[2] * 4, 50))
        },
        showEffectOn: 'render',
        rippleEffect: {
          brushType: 'stroke',
          scale: 4,
          period: 4,
        },
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0,0,0,0.3)',
        },
        zlevel: 1,
      },
    ],
  })
}

function renderBarChart() {
  if (!barChartRef.value || isUnmounted || cityStats.value.length === 0) return
  if (isChartValid(barChart)) barChart!.dispose()
  barChart = echarts.init(barChartRef.value)

  const top10 = cityStats.value.slice(0, 10).reverse()
  const maxBarCount = Math.max(...top10.map(i => i.count), 1)

  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 80, right: 30, top: 10, bottom: 20 },
    xAxis: {
      type: 'value',
      axisLabel: { color: isDark.value ? '#9ca3af' : '#6b7280', fontSize: 11 },
      splitLine: { lineStyle: { color: isDark.value ? '#374151' : '#f3f4f6' } },
    },
    yAxis: {
      type: 'category',
      data: top10.map(i => i.city),
      axisLabel: { color: isDark.value ? '#d1d5db' : '#374151', fontSize: 12 },
      axisLine: { lineStyle: { color: isDark.value ? '#374151' : '#e5e7eb' } },
    },
    series: [{
      type: 'bar',
      data: top10.map((i) => {
        const ratio = i.count / maxBarCount
        let color1 = '#22c55e', color2 = '#16a34a'
        if (ratio > 0.75) { color1 = '#ef4444'; color2 = '#dc2626' }
        else if (ratio > 0.5) { color1 = '#f97316'; color2 = '#ea580c' }
        else if (ratio > 0.25) { color1 = '#eab308'; color2 = '#ca8a04' }
        return {
          value: i.count,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: color1 },
              { offset: 1, color: color2 },
            ]),
            borderRadius: [0, 4, 4, 0],
          },
        }
      }),
      barWidth: 16,
      label: {
        show: true,
        position: 'right',
        color: isDark.value ? '#d1d5db' : '#374151',
        fontSize: 12,
      },
    }],
  })
}

// 暗黑模式变化时重新渲染图表
watch(isDark, async () => {
  await nextTick()
  await renderChinaMap()
  renderBarChart()
})

onMounted(async () => {
  try {
    const [locRes, recentRes, cityRes, countryRes] = await Promise.allSettled([
      getVisitorLocations(),
      getRecentVisitors(20),
      getVisitorCityStats(),
      getVisitorCountryStats(),
    ])

    if (locRes.status === 'fulfilled') locations.value = locRes.value.data || []
    if (recentRes.status === 'fulfilled') recentVisitors.value = recentRes.value.data || []
    if (cityRes.status === 'fulfilled') cityStats.value = cityRes.value.data || []
    if (countryRes.status === 'fulfilled') countryStats.value = countryRes.value.data || []
  } catch (e) {
    console.error('加载访客数据失败:', e)
  } finally {
    loading.value = false
  }

  await nextTick()
  await renderChinaMap()
  renderBarChart()

  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  isUnmounted = true
  window.removeEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (isChartValid(mapChart)) {
    mapChart!.dispose()
    mapChart = null
  }
  if (isChartValid(barChart)) {
    barChart!.dispose()
    barChart = null
  }
})
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center shadow-lg shadow-blue-500/20">
          <EnvironmentOutlined class="text-white text-lg" />
        </div>
        <div>
          <h1 class="text-xl font-bold text-gray-800 dark:text-dark-text-primary">访客足迹地图</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">追踪和可视化你的访客分布</p>
        </div>
      </div>
    </div>

    <a-spin :spinning="loading">
      <!-- 统计卡片 - 渐变风格 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div class="group relative overflow-hidden rounded-xl p-5 text-white shadow-md hover:shadow-xl transition-all duration-300 hover:-translate-y-1 cursor-default bg-gradient-to-br from-blue-500 to-cyan-500">
          <div class="absolute -right-4 -top-4 w-24 h-24 bg-white/10 rounded-full transition-transform duration-500 group-hover:scale-150"></div>
          <div class="relative flex items-center justify-between">
            <div>
              <p class="text-sm text-white/80 mb-1">总访客数</p>
              <p class="text-3xl font-bold tabular-nums">{{ totalVisitors }}</p>
            </div>
            <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/20 backdrop-blur-sm transition-transform duration-300 group-hover:scale-110 group-hover:rotate-6">
              <TeamOutlined class="text-2xl text-white" />
            </div>
          </div>
        </div>
        <div class="group relative overflow-hidden rounded-xl p-5 text-white shadow-md hover:shadow-xl transition-all duration-300 hover:-translate-y-1 cursor-default bg-gradient-to-br from-emerald-500 to-teal-500">
          <div class="absolute -right-4 -top-4 w-24 h-24 bg-white/10 rounded-full transition-transform duration-500 group-hover:scale-150"></div>
          <div class="relative flex items-center justify-between">
            <div>
              <p class="text-sm text-white/80 mb-1">覆盖城市</p>
              <p class="text-3xl font-bold tabular-nums">{{ totalCities }}</p>
            </div>
            <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/20 backdrop-blur-sm transition-transform duration-300 group-hover:scale-110 group-hover:rotate-6">
              <EnvironmentOutlined class="text-2xl text-white" />
            </div>
          </div>
        </div>
        <div class="group relative overflow-hidden rounded-xl p-5 text-white shadow-md hover:shadow-xl transition-all duration-300 hover:-translate-y-1 cursor-default bg-gradient-to-br from-violet-500 to-purple-500">
          <div class="absolute -right-4 -top-4 w-24 h-24 bg-white/10 rounded-full transition-transform duration-500 group-hover:scale-150"></div>
          <div class="relative flex items-center justify-between">
            <div>
              <p class="text-sm text-white/80 mb-1">覆盖国家</p>
              <p class="text-3xl font-bold tabular-nums">{{ totalCountries }}</p>
            </div>
            <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/20 backdrop-blur-sm transition-transform duration-300 group-hover:scale-110 group-hover:rotate-6">
              <GlobalOutlined class="text-2xl text-white" />
            </div>
          </div>
        </div>
      </div>

      <!-- 地图 -->
      <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300 mb-6 overflow-hidden relative">
        <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-blue-500 to-cyan-500"></div>
        <div class="px-5 py-4 border-b border-gray-100 dark:border-dark-border flex items-center gap-2">
          <div class="w-8 h-8 rounded-lg bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center">
            <EnvironmentOutlined class="text-blue-500" />
          </div>
          <h3 class="text-base font-semibold text-gray-800 dark:text-dark-text-primary">访客分布地图</h3>
        </div>
        <div ref="mapRef" style="width: 100%; height: 450px;"></div>
        <div v-if="!loading && locations.length === 0" class="flex items-center justify-center text-gray-400 text-sm py-4">
          暂无访客数据
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <!-- 城市排行 -->
        <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300 p-5 relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-blue-500 to-indigo-500"></div>
          <div class="flex items-center gap-2 mb-4">
            <div class="w-8 h-8 rounded-lg bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center">
              <RiseOutlined class="text-blue-500" />
            </div>
            <h3 class="text-base font-semibold text-gray-800 dark:text-dark-text-primary">访客城市 Top 10</h3>
          </div>
          <div ref="barChartRef" style="width: 100%; height: 320px;"></div>
          <div v-if="cityStats.length === 0" class="flex items-center justify-center h-[320px] text-gray-400 text-sm">
            暂无数据
          </div>
        </div>

        <!-- 最近访客 -->
        <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300 p-5 relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-violet-500 to-purple-500"></div>
          <div class="flex items-center gap-2 mb-4">
            <div class="w-8 h-8 rounded-lg bg-violet-50 dark:bg-violet-900/20 flex items-center justify-center">
              <ClockCircleOutlined class="text-violet-500" />
            </div>
            <h3 class="text-base font-semibold text-gray-800 dark:text-dark-text-primary">最近访客</h3>
          </div>
          <div class="space-y-0 max-h-[360px] overflow-y-auto">
            <div
              v-for="(visitor, index) in recentVisitors"
              :key="visitor.id"
              class="flex items-center justify-between py-2.5 border-b border-gray-50 dark:border-dark-border last:border-0 group hover:bg-gray-50 dark:hover:bg-dark-elevated/50 rounded-lg px-2 -mx-2 transition-colors duration-200"
            >
              <div class="flex items-center gap-3">
                <span
                  class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium flex-shrink-0"
                  :class="index < 3 ? 'bg-gradient-to-br from-blue-400 to-indigo-500 text-white shadow-sm' : 'bg-gray-100 dark:bg-dark-elevated text-gray-500 dark:text-dark-text-muted'"
                >
                  {{ index + 1 }}
                </span>
                <span class="text-sm text-gray-700 dark:text-dark-text-secondary group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                  {{ visitor.city || '未知' }}
                  <span v-if="visitor.province && visitor.province !== visitor.city" class="text-gray-400 dark:text-dark-text-muted">
                    · {{ visitor.province }}
                  </span>
                </span>
              </div>
              <span class="text-xs text-gray-400 dark:text-dark-text-muted whitespace-nowrap">
                {{ formatTime(visitor.visitTime) }}
              </span>
            </div>
            <div v-if="recentVisitors.length === 0" class="text-center py-8 text-gray-400 dark:text-dark-text-muted text-sm">
              暂无访客记录
            </div>
          </div>
        </div>
      </div>

      <!-- 国家分布 -->
      <div v-if="countryStats.length > 0" class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300 p-5 relative overflow-hidden">
        <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-emerald-500 to-teal-500"></div>
        <div class="flex items-center gap-2 mb-4">
          <div class="w-8 h-8 rounded-lg bg-emerald-50 dark:bg-emerald-900/20 flex items-center justify-center">
            <GlobalOutlined class="text-emerald-500" />
          </div>
          <h3 class="text-base font-semibold text-gray-800 dark:text-dark-text-primary">访客国家/地区分布</h3>
        </div>
        <div class="flex flex-wrap gap-3">
          <div
            v-for="stat in countryStats"
            :key="stat.country"
            class="flex items-center gap-2 bg-gray-50 dark:bg-dark-elevated rounded-lg px-4 py-2 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-colors duration-200 cursor-default"
          >
            <span class="text-sm text-gray-700 dark:text-dark-text-secondary">{{ stat.country }}</span>
            <span class="text-xs bg-blue-100 dark:bg-blue-900/50 text-blue-600 dark:text-blue-400 px-2 py-0.5 rounded-full font-medium">
              {{ stat.count }}
            </span>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>
