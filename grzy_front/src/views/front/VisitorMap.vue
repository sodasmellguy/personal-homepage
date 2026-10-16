<script setup lang="ts">
import { ref, onMounted, onUnmounted, onBeforeUnmount, nextTick, computed, watch } from 'vue'
import { useHead } from '@unhead/vue'
import * as echarts from 'echarts'
import {
  EnvironmentOutlined,
  TeamOutlined,
  GlobalOutlined,
  ClockCircleOutlined,
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

useHead({
  title: '访客足迹地图 - 赵先生',
  meta: [
    { name: 'description', content: '全球访客足迹分布地图，展示访客来源' },
  ],
})

const appStore = useAppStore()
const isDark = computed(() => appStore.isDark)

const loading = ref(true)
const mapRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()
const activeTab = ref<'china' | 'world'>('china')

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

// 中国主要城市经纬度映射
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

// 加载世界地图 GeoJSON（ECharts 5+/6 已移除内置 world map）
// 优先使用本地静态资源（不依赖外网），原在线地址仅作兜底
const WORLD_MAP_SOURCES = [
  '/countries.json', // 本地：countries.json
  'https://geo.datav.aliyun.com/areas_v2/bound/100000_world.json',
  'https://geo.datav.aliyun.com/areas_v3/bound/100000_world.json',
]
let worldGeoJson: any = null
async function loadWorldMap() {
  if (worldGeoJson) return worldGeoJson
  for (const url of WORLD_MAP_SOURCES) {
    try {
      const resp = await fetch(url)
      if (!resp.ok) continue
      const json = await resp.json()
      // GeoJSON 必须有 features 数组且非空
      if (json && Array.isArray(json.features) && json.features.length > 0) {
        worldGeoJson = json
        echarts.registerMap('world', worldGeoJson)
        return worldGeoJson
      }
    } catch {
      // 静默继续下一个源
    }
  }
  console.error('加载世界地图失败：所有数据源均不可用')
  return null
}

// 国家/地区经纬度映射（覆盖主要访客来源国家）
const worldCountryCoords: Record<string, [number, number]> = {
  '中国': [116.4, 39.9], '美国': [-95.7, 37.1], '日本': [138.0, 36.2],
  '韩国': [127.7, 36.0], '英国': [-2.0, 54.0], '德国': [10.4, 51.2],
  '法国': [2.2, 46.2], '俄罗斯': [105.3, 61.5], '加拿大': [-106.3, 56.1],
  '澳大利亚': [133.7, -25.7], '印度': [78.9, 21.0], '巴西': [-51.9, -14.2],
  '新加坡': [103.8, 1.3], '马来西亚': [101.9, 4.2], '泰国': [100.9, 15.8],
  '越南': [108.3, 14.0], '印度尼西亚': [113.9, -0.7], '菲律宾': [121.7, 12.8],
  '意大利': [12.5, 41.9], '西班牙': [-3.7, 40.4], '荷兰': [5.3, 52.1],
  '瑞士': [8.2, 46.8], '瑞典': [18.6, 60.1], '挪威': [8.5, 60.5],
  '芬兰': [25.7, 61.9], '丹麦': [9.5, 56.3], '波兰': [19.1, 51.9],
  '土耳其': [35.2, 38.9], '墨西哥': [-102.5, 23.6], '阿根廷': [-63.6, -38.4],
  '南非': [22.9, -30.6], '埃及': [30.8, 26.8], '阿联酋': [53.8, 23.4],
  '沙特阿拉伯': [45.0, 23.8], '以色列': [34.8, 31.0], '伊朗': [53.6, 32.4],
  '新西兰': [174.8, -40.9], '乌克兰': [31.1, 48.4], '爱尔兰': [-7.7, 53.4],
  '葡萄牙': [-8.2, 39.4], '比利时': [4.4, 50.5], '奥地利': [14.5, 47.5],
  '捷克': [15.4, 49.8], '希腊': [21.8, 39.0], '匈牙利': [19.5, 47.2],
}

// 中文国家名 → GeoJSON 英文名 映射（datasets/geo-countries）
// 用于 ECharts geo.nameMap，让 ip2region 返回的中文名能关联到正确的地图区域
const COUNTRY_NAME_MAP: Record<string, string> = {
  '中国': 'China',
  '美国': 'United States of America',
  '日本': 'Japan',
  '韩国': 'South Korea',
  '朝鲜': 'North Korea',
  '英国': 'United Kingdom',
  '德国': 'Germany',
  '法国': 'France',
  '俄罗斯': 'Russia',
  '加拿大': 'Canada',
  '澳大利亚': 'Australia',
  '新西兰': 'New Zealand',
  '印度': 'India',
  '巴基斯坦': 'Pakistan',
  '孟加拉国': 'Bangladesh',
  '斯里兰卡': 'Sri Lanka',
  '尼泊尔': 'Nepal',
  '不丹': 'Bhutan',
  '阿富汗': 'Afghanistan',
  '伊朗': 'Iran',
  '伊拉克': 'Iraq',
  '土耳其': 'Turkey',
  '沙特阿拉伯': 'Saudi Arabia',
  '阿联酋': 'United Arab Emirates',
  '卡塔尔': 'Qatar',
  '巴林': 'Bahrain',
  '科威特': 'Kuwait',
  '阿曼': 'Oman',
  '也门': 'Yemen',
  '以色列': 'Israel',
  '约旦': 'Jordan',
  '黎巴嫩': 'Lebanon',
  '叙利亚': 'Syria',
  '塞浦路斯': 'Cyprus',
  '新加坡': 'Singapore',
  '马来西亚': 'Malaysia',
  '泰国': 'Thailand',
  '越南': 'Vietnam',
  '老挝': 'Laos',
  '柬埔寨': 'Cambodia',
  '缅甸': 'Myanmar',
  '菲律宾': 'Philippines',
  '印度尼西亚': 'Indonesia',
  '文莱': 'Brunei',
  '蒙古': 'Mongolia',
  '哈萨克斯坦': 'Kazakhstan',
  '乌兹别克斯坦': 'Uzbekistan',
  '土库曼斯坦': 'Turkmenistan',
  '吉尔吉斯斯坦': 'Kyrgyzstan',
  '塔吉克斯坦': 'Tajikistan',
  '格鲁吉亚': 'Georgia',
  '亚美尼亚': 'Armenia',
  '阿塞拜疆': 'Azerbaijan',
  '乌克兰': 'Ukraine',
  '白俄罗斯': 'Belarus',
  '摩尔多瓦': 'Moldova',
  '波兰': 'Poland',
  '捷克': 'Czechia',
  '斯洛伐克': 'Slovakia',
  '匈牙利': 'Hungary',
  '罗马尼亚': 'Romania',
  '保加利亚': 'Bulgaria',
  '塞尔维亚': 'Serbia',
  '克罗地亚': 'Croatia',
  '斯洛文尼亚': 'Slovenia',
  '波黑': 'Bosnia and Herzegovina',
  '黑山': 'Montenegro',
  '北马其顿': 'North Macedonia',
  '阿尔巴尼亚': 'Albania',
  '希腊': 'Greece',
  '意大利': 'Italy',
  '西班牙': 'Spain',
  '葡萄牙': 'Portugal',
  '荷兰': 'Netherlands',
  '比利时': 'Belgium',
  '卢森堡': 'Luxembourg',
  '瑞士': 'Switzerland',
  '奥地利': 'Austria',
  '列支敦士登': 'Liechtenstein',
  '爱尔兰': 'Ireland',
  '冰岛': 'Iceland',
  '丹麦': 'Denmark',
  '挪威': 'Norway',
  '瑞典': 'Sweden',
  '芬兰': 'Finland',
  '爱沙尼亚': 'Estonia',
  '拉脱维亚': 'Latvia',
  '立陶宛': 'Lithuania',
  '巴西': 'Brazil',
  '阿根廷': 'Argentina',
  '智利': 'Chile',
  '秘鲁': 'Peru',
  '玻利维亚': 'Bolivia',
  '哥伦比亚': 'Colombia',
  '委内瑞拉': 'Venezuela',
  '厄瓜多尔': 'Ecuador',
  '乌拉圭': 'Uruguay',
  '巴拉圭': 'Paraguay',
  '圭亚那': 'Guyana',
  '苏里南': 'Suriname',
  '法属圭亚那': 'French Guiana',
  '墨西哥': 'Mexico',
  '古巴': 'Cuba',
  '牙买加': 'Jamaica',
  '海地': 'Haiti',
  '多米尼加': 'Dominican Republic',
  '巴哈马': 'Bahamas',
  '巴拿马': 'Panama',
  '哥斯达黎加': 'Costa Rica',
  '危地马拉': 'Guatemala',
  '洪都拉斯': 'Honduras',
  '萨尔瓦多': 'El Salvador',
  '尼加拉瓜': 'Nicaragua',
  '伯利兹': 'Belize',
  '波多黎各': 'Puerto Rico',
  '埃及': 'Egypt',
  '利比亚': 'Libya',
  '突尼斯': 'Tunisia',
  '阿尔及利亚': 'Algeria',
  '摩洛哥': 'Morocco',
  '苏丹': 'Sudan',
  '南苏丹': 'South Sudan',
  '埃塞俄比亚': 'Ethiopia',
  '厄立特里亚': 'Eritrea',
  '吉布提': 'Djibouti',
  '索马里': 'Somalia',
  '肯尼亚': 'Kenya',
  '乌干达': 'Uganda',
  '坦桑尼亚': 'Tanzania',
  '卢旺达': 'Rwanda',
  '布隆迪': 'Burundi',
  '刚果(金)': 'Democratic Republic of the Congo',
  '刚果(布)': 'Republic of the Congo',
  '安哥拉': 'Angola',
  '赞比亚': 'Zambia',
  '津巴布韦': 'Zimbabwe',
  '马拉维': 'Malawi',
  '莫桑比克': 'Mozambique',
  '博茨瓦纳': 'Botswana',
  '纳米比亚': 'Namibia',
  '南非': 'South Africa',
  '莱索托': 'Lesotho',
  '斯威士兰': 'Eswatini',
  '马达加斯加': 'Madagascar',
  '毛里求斯': 'Mauritius',
  '尼日利亚': 'Nigeria',
  '加纳': 'Ghana',
  '科特迪瓦': "Côte d'Ivoire",
  '塞内加尔': 'Senegal',
  '马里': 'Mali',
  '布基纳法索': 'Burkina Faso',
  '尼日尔': 'Niger',
  '乍得': 'Chad',
  '喀麦隆': 'Cameroon',
  '加蓬': 'Gabon',
  '赤道几内亚': 'Equatorial Guinea',
  '贝宁': 'Benin',
  '多哥': 'Togo',
  '利比里亚': 'Liberia',
  '塞拉利昂': 'Sierra Leone',
  '几内亚': 'Guinea',
  '佛得角': 'Cabo Verde',
  '香港': 'Hong Kong S.A.R.',
  '澳门': 'Macao S.A.R',
}

const REVERSE_COUNTRY_NAME_MAP = Object.fromEntries(Object.entries(COUNTRY_NAME_MAP).map(([k, v]) => [v, k]))

// 构建地图数据
function buildMapData() {
  if (activeTab.value === 'china') {
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
  } else {
    // 世界地图：按国家聚合
    const countryCountMap = new Map<string, { count: number; coord: [number, number] }>()
    locations.value.forEach(loc => {
      const country = loc.country
      if (!country) return
      const coord = worldCountryCoords[country]
      if (!coord) return
      const existing = countryCountMap.get(country)
      if (existing) {
        existing.count++
      } else {
        countryCountMap.set(country, { count: 1, coord })
      }
    })
    return Array.from(countryCountMap.entries()).map(([name, { count, coord }]) => ({
      name,
      value: [...coord, count] as [number, number, number],
    }))
  }
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

// 按省份聚合访客数据（用于 map series 区域着色）
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

// 渲染中国地图
async function renderChinaMap() {
  if (!mapRef.value || isUnmounted) return
  await loadChinaMap()
  if (isUnmounted || !mapRef.value) return

  if (isChartValid(mapChart)) {
    mapChart!.dispose()
  }
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
    // 视觉映射组件：人数不同显示不同颜色
    visualMap: {
      type: 'continuous',
      min: 1,
      max: maxCount,
      text: ['多', '少'],
      realtime: false,
      calculable: true,
      inRange: {
        // 绿色(少) -> 黄色(中) -> 橙色 -> 红色(多)
        color: ['#22c55e', '#eab308', '#f97316', '#ef4444']
      },
      right: 20,
      bottom: 30,
      textStyle: { color: isDark.value ? '#d1d5db' : '#4b5563', fontSize: 12 },
    },
    // geo 作为隐藏坐标系底图（供 scatter 使用）
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
      // 地图区域：省份颜色随访客数变化
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
      // 动画涟漪效果（Top 5 城市）
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
          // color removed, visualMap controls
          shadowBlur: 10,
          shadowColor: 'rgba(0,0,0,0.3)',
        },
        zlevel: 1,
      },
    ],
  })
}

// 渲染世界地图（散点图形式）
async function renderWorldMap() {
  if (!mapRef.value || isUnmounted) return
  const geo = await loadWorldMap()
  if (isUnmounted || !mapRef.value) return

  if (isChartValid(mapChart)) {
    mapChart!.dispose()
  }
  mapChart = echarts.init(mapRef.value)

  // 地图数据加载失败时给出提示
  if (!geo) {
    mapChart.setOption({
      backgroundColor: isDark.value ? '#111827' : '#f8fafc',
      title: {
        text: '世界地图加载失败，请检查网络后刷新',
        left: 'center',
        top: 'middle',
        textStyle: {
          color: isDark.value ? '#9ca3af' : '#6b7280',
          fontSize: 14,
          fontWeight: 'normal',
        },
      },
    })
    return
  }

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
                  <div style="color:#999;font-size:12px;">国家排名: 第 ${rank} 位</div>`
        }
        const displayName = REVERSE_COUNTRY_NAME_MAP[params.name] || params.name
        return displayName
      },
    },
    // 视觉映射组件：人数不同显示不同颜色
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
      map: 'world',
      // 中文国家名 → GeoJSON 英文名（让 ECharts 知道点位的归属区域）
      nameMap: REVERSE_COUNTRY_NAME_MAP,
      roam: true,
      zoom: 1.3,
      center: [0, 20],
      show: true,
      label: { show: false },
      itemStyle: {
        areaColor: isDark.value ? '#1f2937' : '#e8f4f8',
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
    },
    series: [
      {
        type: 'scatter',
        coordinateSystem: 'geo',
        data: scatterData,
        symbol: 'circle',
        symbolSize: (val: number[]) => Math.max(10, Math.min(12 + val[2] * 4, 50)),
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
        data: [...scatterData].sort((a: any, b: any) => b.value[2] - a.value[2]).slice(0, 5),
        symbolSize: (val: number[]) => Math.max(12, Math.min(14 + val[2] * 4, 50)),
        showEffectOn: 'render',
        rippleEffect: { brushType: 'stroke', scale: 4, period: 4 },
        itemStyle: {
          // color removed, visualMap controls
          shadowBlur: 10,
          shadowColor: 'rgba(0,0,0,0.3)',
        },
        zlevel: 1,
      },
    ],
  })
}

// 切换地图
async function switchMap(type: 'china' | 'world') {
  activeTab.value = type
  await nextTick()
  if (type === 'china') {
    await renderChinaMap()
  } else {
    await renderWorldMap()
  }
}

// 渲染城市统计柱状图
function renderBarChart() {
  if (!barChartRef.value || isUnmounted || cityStats.value.length === 0) return

  if (isChartValid(barChart)) {
    barChart!.dispose()
  }
  barChart = echarts.init(barChartRef.value)

  const top10 = cityStats.value.slice(0, 10).reverse()
  const maxBarCount = Math.max(...top10.map(i => i.count), 1)

  barChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
    },
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
  if (activeTab.value === 'china') {
    await renderChinaMap()
  } else {
    await renderWorldMap()
  }
  renderBarChart()
})

// 加载数据
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
  <div class="max-w-6xl mx-auto">
    <a-spin :spinning="loading">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-800 dark:text-dark-text-primary mb-2">
          <EnvironmentOutlined class="mr-2 text-blue-500" />
          访客足迹地图
        </h1>
        <p class="text-gray-500 dark:text-gray-400">记录每一位访客的足迹，感谢你的到来</p>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-3 gap-4 mb-6">
        <div class="bg-white dark:bg-gray-800 rounded-xl p-5 border border-gray-100 dark:border-gray-700 text-center hover:shadow-xl hover:shadow-blue-500/5 hover:-translate-y-1 transition-all duration-300">
          <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900/30 dark:to-blue-800/20 flex items-center justify-center mx-auto mb-2">
            <TeamOutlined class="text-lg text-blue-500" />
          </div>
          <p class="text-2xl font-bold text-gray-800 dark:text-white">{{ totalVisitors }}</p>
          <p class="text-sm text-gray-500 dark:text-gray-400">总访客数</p>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-xl p-5 border border-gray-100 dark:border-gray-700 text-center hover:shadow-xl hover:shadow-emerald-500/5 hover:-translate-y-1 transition-all duration-300">
          <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-emerald-50 to-emerald-100 dark:from-emerald-900/30 dark:to-emerald-800/20 flex items-center justify-center mx-auto mb-2">
            <EnvironmentOutlined class="text-lg text-emerald-500" />
          </div>
          <p class="text-2xl font-bold text-gray-800 dark:text-white">{{ totalCities }}</p>
          <p class="text-sm text-gray-500 dark:text-gray-400">覆盖城市</p>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-xl p-5 border border-gray-100 dark:border-gray-700 text-center hover:shadow-xl hover:shadow-purple-500/5 hover:-translate-y-1 transition-all duration-300">
          <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-900/30 dark:to-purple-800/20 flex items-center justify-center mx-auto mb-2">
            <GlobalOutlined class="text-lg text-purple-500" />
          </div>
          <p class="text-2xl font-bold text-gray-800 dark:text-white">{{ totalCountries }}</p>
          <p class="text-sm text-gray-500 dark:text-gray-400">覆盖国家</p>
        </div>
      </div>

      <!-- 地图区域 -->
      <div class="bg-white dark:bg-gray-800 rounded-xl border border-gray-100 dark:border-gray-700 mb-6 overflow-hidden">
        <div class="flex items-center justify-between px-5 py-3 border-b border-gray-100 dark:border-gray-700">
          <h3 class="text-base font-medium text-gray-800 dark:text-white">访客分布地图</h3>
          <div class="flex gap-2">
            <a-button
              size="small"
              :type="activeTab === 'china' ? 'primary' : 'default'"
              @click="switchMap('china')"
            >
              中国地图
            </a-button>
            <a-button
              size="small"
              :type="activeTab === 'world' ? 'primary' : 'default'"
              @click="switchMap('world')"
            >
              世界地图
            </a-button>
          </div>
        </div>
        <div ref="mapRef" style="width: 100%; height: 500px;"></div>
        <div v-if="!loading && locations.length === 0" class="flex items-center justify-center text-gray-400 text-sm py-4">
          暂无访客数据
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <!-- 城市排行 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl border border-gray-100 dark:border-gray-700 p-5">
          <h3 class="text-base font-medium text-gray-800 dark:text-white mb-4">访客城市 Top 10</h3>
          <div ref="barChartRef" style="width: 100%; height: 320px;"></div>
          <div v-if="cityStats.length === 0" class="flex items-center justify-center h-[320px] text-gray-400 text-sm">
            暂无数据
          </div>
        </div>

        <!-- 最近访客 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl border border-gray-100 dark:border-gray-700 p-5">
          <h3 class="text-base font-medium text-gray-800 dark:text-white mb-4">
            <ClockCircleOutlined class="mr-1" />
            最近访客
          </h3>
          <div class="space-y-0 max-h-[360px] overflow-y-auto">
            <div
              v-for="(visitor, index) in recentVisitors"
              :key="visitor.id"
              class="flex items-center justify-between py-2.5 border-b border-gray-50 dark:border-gray-700 last:border-0"
            >
              <div class="flex items-center gap-3">
                <span
                  class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium flex-shrink-0"
                  :class="index < 3 ? 'bg-blue-500 text-white' : 'bg-gray-200 dark:bg-gray-600 text-gray-500 dark:text-gray-300'"
                >
                  {{ index + 1 }}
                </span>
                <div>
                  <span class="text-sm text-gray-700 dark:text-gray-300">
                    {{ visitor.city || '未知' }}
                    <span v-if="visitor.province && visitor.province !== visitor.city" class="text-gray-400 dark:text-gray-500">
                      · {{ visitor.province }}
                    </span>
                  </span>
                </div>
              </div>
              <span class="text-xs text-gray-400 dark:text-gray-500 whitespace-nowrap">
                {{ formatTime(visitor.visitTime) }}
              </span>
            </div>
            <div v-if="recentVisitors.length === 0" class="text-center py-8 text-gray-400 dark:text-gray-500 text-sm">
              暂无访客记录
            </div>
          </div>
        </div>
      </div>

      <!-- 国家分布 -->
      <div v-if="countryStats.length > 0" class="bg-white dark:bg-gray-800 rounded-xl border border-gray-100 dark:border-gray-700 p-5">
        <h3 class="text-base font-medium text-gray-800 dark:text-white mb-4">访客国家/地区分布</h3>
        <div class="flex flex-wrap gap-3">
          <div
            v-for="stat in countryStats"
            :key="stat.country"
            class="flex items-center gap-2 bg-gray-50 dark:bg-gray-700 rounded-lg px-4 py-2"
          >
            <span class="text-sm text-gray-700 dark:text-gray-300">{{ stat.country }}</span>
            <span class="text-xs bg-blue-100 dark:bg-blue-900/50 text-blue-600 dark:text-blue-400 px-2 py-0.5 rounded-full font-medium">
              {{ stat.count }}
            </span>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>
