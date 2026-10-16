import { get, put, del } from '@/utils/request'

// 评论
export interface Comment {
  id: number
  articleId: number
  parentId: number
  replyToId: number | null
  replyToNickname: string | null
  nickname: string
  email: string
  avatar: string
  content: string
  status: number
  children: Comment[]
  createTime: string
}

// 评论查询参数
export interface CommentQuery {
  pageNum?: number
  pageSize?: number
  articleId?: number
  status?: number
}

// 分页结果
export interface PageResult<T> {
  total: number
  rows: T[]
  pageNum: number
  pageSize: number
  totalPages: number
}

/**
 * 获取评论列表（后台分页）
 */
export function getCommentList(params: CommentQuery) {
  return get<PageResult<Comment>>('/comment/list', params)
}

/**
 * 审核评论
 */
export function auditComment(id: number, status: number) {
  return put(`/comment/${id}/status`, { status })
}

/**
 * 删除评论
 */
export function deleteComment(id: number) {
  return del(`/comment/${id}`)
}

/**
 * 批量审核评论
 */
export function batchAuditComments(ids: number[], status: number) {
  return put('/comment/batch-audit', { ids, status })
}
