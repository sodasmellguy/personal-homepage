import { get } from '@/utils/request'

export interface OperLog {
  id: number
  module: string
  operation: string
  method: string
  requestUrl: string
  requestParams: string
  responseData: string
  ip: string
  userAgent: string
  userId: number
  username: string
  status: number
  errorMsg: string
  duration: number
  createTime: string
}

export interface OperLogQuery {
  pageNum?: number
  pageSize?: number
  module?: string
  username?: string
}

export interface PageResult<T> {
  total: number
  rows: T[]
  pageNum: number
  pageSize: number
}

export function getOperLogList(params: OperLogQuery) {
  return get<PageResult<OperLog>>('/system/log/list', params)
}
