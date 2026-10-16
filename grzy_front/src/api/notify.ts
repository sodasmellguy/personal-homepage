import { get, put } from '@/utils/request'

// 通知设置（单行配置）
export interface NotifySetting {
  id: number
  adminEmail: string | null
  enableCommentNotify: number
  enableReplyNotify: number
  enableFriendlinkNotify: number
  enablePublishNotify: number
  enableSecurityNotify: number
  alertIntervalMinutes: number
  updateTime: string
}

// 通知日志
export interface NotifyLog {
  id: number
  type: string
  title: string | null
  recipient: string
  status: number
  errorMsg: string | null
  retryCount: number
  createTime: string
}

export interface NotifyLogQuery {
  pageNum?: number
  pageSize?: number
  type?: string
  status?: number
}

export interface PageResult<T> {
  total: number
  rows: T[]
  pageNum: number
  pageSize: number
}

export function getNotifySetting() {
  return get<NotifySetting>('/system/notify/setting')
}

export function updateNotifySetting(data: Partial<NotifySetting>) {
  return put<void>('/system/notify/setting', data)
}

export function getNotifyLogList(params: NotifyLogQuery) {
  return get<PageResult<NotifyLog>>('/system/notify/log/list', params)
}
