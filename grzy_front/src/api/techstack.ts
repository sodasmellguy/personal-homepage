import { get, post, put, del } from '@/utils/request'

export interface TechStack {
  id: number
  name: string
  icon: string
  featured: number
  sort: number
  status: number
  createTime: string
  updateTime: string
}

export interface TechStackQuery {
  pageNum?: number
  pageSize?: number
  name?: string
  status?: number
}

/**
 * 获取技术栈列表（分页）
 */
export function getTechStackList(params: TechStackQuery) {
  return get<{ total: number; rows: TechStack[] }>('/techstack/list', params)
}

/**
 * 获取所有技术栈（前台展示）
 */
export function getAllTechStacks() {
  return get<TechStack[]>('/front/techstack/list')
}

/**
 * 获取技术栈详情
 */
export function getTechStack(id: number) {
  return get<TechStack>(`/techstack/${id}`)
}

/**
 * 创建技术栈
 */
export function createTechStack(data: Partial<TechStack>) {
  return post('/techstack', data)
}

/**
 * 更新技术栈
 */
export function updateTechStack(id: number, data: Partial<TechStack>) {
  return put(`/techstack/${id}`, data)
}

/**
 * 删除技术栈
 */
export function deleteTechStack(id: number) {
  return del(`/techstack/${id}`)
}

/**
 * 批量删除技术栈
 */
export function batchDeleteTechStacks(ids: number[]) {
  return del('/techstack/batch', ids)
}
