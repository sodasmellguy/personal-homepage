import { get, post, put, del } from '@/utils/request'

// 友情链接
export interface FriendLink {
  id?: number
  name: string
  url: string
  logo?: string
  description?: string
  status?: number // 0-待审核 1-已通过 2-已驳回
  sort?: number
  createTime?: string
}

/**
 * 前台获取已通过的友链列表
 */
export function getFrontFriendLinks() {
  return get<FriendLink[]>('/front/friendlink/list')
}

/**
 * 前台申请友链
 */
export function applyFriendLink(data: Pick<FriendLink, 'name' | 'url' | 'logo' | 'description'>) {
  return post('/front/friendlink/apply', data)
}

/**
 * 后台分页查询友链
 */
export function getFriendLinkPage(params: {
  pageNum?: number
  pageSize?: number
  name?: string
  status?: number
}) {
  return get<{ total: number; rows: FriendLink[] }>('/friendlink/list', params)
}

/**
 * 创建友链（后台）
 */
export function createFriendLink(data: Partial<FriendLink>) {
  return post('/friendlink', data)
}

/**
 * 更新友链（后台）
 */
export function updateFriendLink(id: number, data: Partial<FriendLink>) {
  return put(`/friendlink/${id}`, data)
}

/**
 * 审核友链：1-通过，2-驳回
 */
export function reviewFriendLink(id: number, status: number) {
  return put(`/friendlink/${id}/status`, null, { params: { status } })
}

/**
 * 删除友链（后台）
 */
export function deleteFriendLink(id: number) {
  return del(`/friendlink/${id}`)
}

/**
 * 批量删除友链（后台）
 */
export function batchDeleteFriendLinks(ids: number[]) {
  return del('/friendlink/batch', ids)
}
