import { get } from '@/utils/request'

// 访客位置信息
export interface VisitorLocation {
  id: number
  country: string
  province: string
  city: string
  longitude: number
  latitude: number
  visitTime: string
}

// 城市统计
export interface CityStat {
  city: string
  count: number
}

// 国家统计
export interface CountryStat {
  country: string
  count: number
}

/**
 * 获取所有访客位置数据（地图打点用）
 */
export function getVisitorLocations() {
  return get<VisitorLocation[]>('/front/visitor-map/locations')
}

/**
 * 获取最近访客
 */
export function getRecentVisitors(limit: number = 20) {
  return get<VisitorLocation[]>('/front/visitor-map/recent', { limit })
}

/**
 * 获取访客城市统计
 */
export function getVisitorCityStats() {
  return get<CityStat[]>('/front/visitor-map/city-stats')
}

/**
 * 获取访客国家统计
 */
export function getVisitorCountryStats() {
  return get<CountryStat[]>('/front/visitor-map/country-stats')
}
