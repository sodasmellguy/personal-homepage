import { get, post, put, del } from '@/utils/request'

// 开源项目
export interface OpenSource {
  id: number
  name: string
  description: string
  detail?: string | null
  repoUrl: string
  demoUrl?: string | null
  logo?: string | null
  language?: string | null
  techStack?: string | null
  license?: string | null
  stars: number
  forks: number
  status: number
  isTop: number
  sort: number
  syncTime?: string | null
  createTime: string
  updateTime: string
}

export interface OpenSourceQuery {
  pageNum?: number
  pageSize?: number
  status?: number
}

export interface PageResult<T> {
  total: number
  rows: T[]
  pageNum: number
  pageSize: number
}

// ========== 前台公开 ==========

export function getFrontOpenSourceList() {
  return get<OpenSource[]>('/front/open-source/list')
}

// ========== 后台管理 ==========

export function getOpenSourcePage(params: OpenSourceQuery) {
  return get<PageResult<OpenSource>>('/open-source/list', params)
}

export function createOpenSource(data: Partial<OpenSource>) {
  return post<void>('/open-source', data)
}

export function updateOpenSource(id: number, data: Partial<OpenSource>) {
  return put<void>(`/open-source/${id}`, data)
}

export function deleteOpenSource(id: number) {
  return del<void>(`/open-source/${id}`)
}

export function syncOpenSource(id: number) {
  return post<OpenSource>(`/open-source/${id}/sync`)
}
