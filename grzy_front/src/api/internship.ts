import { get, post, put, del } from '@/utils/request'

// 实习经历
export interface Internship {
  id: number
  company: string
  position: string
  startDate: string
  endDate: string
  description: string
  achievements: string
  sort: number
  status: number
  createTime: string
  updateTime: string
}

// 实习查询参数
export interface InternshipQuery {
  pageNum?: number
  pageSize?: number
  company?: string
  status?: number
}

/**
 * 获取实习列表（分页）
 */
export function getInternshipList(params: InternshipQuery) {
  return get<{ total: number; rows: Internship[] }>('/internship/list', params)
}

/**
 * 获取所有实习（前台展示）
 */
export function getAllInternships() {
  return get<Internship[]>('/front/internship/list')
}

/**
 * 获取实习详情
 */
export function getInternship(id: number) {
  return get<Internship>(`/internship/${id}`)
}

/**
 * 创建实习
 */
export function createInternship(data: Partial<Internship>) {
  return post('/internship', data)
}

/**
 * 更新实习
 */
export function updateInternship(id: number, data: Partial<Internship>) {
  return put(`/internship/${id}`, data)
}

/**
 * 删除实习
 */
export function deleteInternship(id: number) {
  return del(`/internship/${id}`)
}
