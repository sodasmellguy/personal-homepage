import { get, post, put, del } from '@/utils/request'

// 专栏
export interface Series {
  id: number
  name: string
  coverImage: string
  description: string
  sort: number
  status: number
  articleCount: number
  createTime: string
  updateTime: string
}

// 专栏文章
export interface SeriesArticle {
  id: number
  title: string
  summary: string
  coverImage: string
  sort: number
  createTime: string
}

// 专栏详情
export interface SeriesDetail extends Series {
  articles: SeriesArticle[]
}

// 专栏查询参数
export interface SeriesQuery {
  pageNum?: number
  pageSize?: number
  name?: string
}

// 创建/更新专栏
export interface SeriesFormData {
  id?: number
  name: string
  coverImage?: string
  description?: string
  sort?: number
  status?: number
  articleIds?: number[]
}

/**
 * 获取专栏列表（后台分页）
 */
export function getSeriesList(params: SeriesQuery) {
  return get<{ total: number; rows: Series[] }>('/series/list', params)
}

/**
 * 获取专栏详情（后台）
 */
export function getSeriesDetail(id: number) {
  return get<SeriesDetail>(`/series/${id}`)
}

/**
 * 创建专栏
 */
export function createSeries(data: SeriesFormData) {
  return post('/series', data)
}

/**
 * 更新专栏
 */
export function updateSeries(id: number, data: SeriesFormData) {
  return put(`/series/${id}`, data)
}

/**
 * 删除专栏
 */
export function deleteSeries(id: number) {
  return del(`/series/${id}`)
}

/**
 * 获取前台专栏列表
 */
export function getFrontSeriesList() {
  return get<Series[]>('/front/series/list')
}

/**
 * 获取前台专栏详情
 */
export function getFrontSeriesDetail(id: number) {
  return get<SeriesDetail>(`/front/series/${id}`)
}
