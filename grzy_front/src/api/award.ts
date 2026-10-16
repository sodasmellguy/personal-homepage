import { get, post, put, del } from '@/utils/request'

// 获奖经历
export interface Award {
  id: number
  title: string
  issuer: string
  awardDate: string
  description: string
  sort: number
  status: number
  createTime: string
  updateTime: string
}

// 获奖查询参数
export interface AwardQuery {
  pageNum?: number
  pageSize?: number
  title?: string
  status?: number
}

/**
 * 获取获奖列表（分页）
 */
export function getAwardList(params: AwardQuery) {
  return get<{ total: number; rows: Award[] }>('/award/list', params)
}

/**
 * 获取所有获奖（前台展示）
 */
export function getAllAwards() {
  return get<Award[]>('/front/award/list')
}

/**
 * 获取获奖详情
 */
export function getAward(id: number) {
  return get<Award>(`/award/${id}`)
}

/**
 * 创建获奖
 */
export function createAward(data: Partial<Award>) {
  return post('/award', data)
}

/**
 * 更新获奖
 */
export function updateAward(id: number, data: Partial<Award>) {
  return put(`/award/${id}`, data)
}

/**
 * 删除获奖
 */
export function deleteAward(id: number) {
  return del(`/award/${id}`)
}

/**
 * 批量删除获奖
 */
export function batchDeleteAwards(ids: number[]) {
  return del('/award/batch', ids)
}
