import { get, del } from '@/utils/request'

// 在线会话
export interface OnlineSession {
  token: string
  userId: number
  username: string
  ip: string
  userAgent: string
  loginTime: string
  current: boolean
}

export function getOnlineSessionList() {
  return get<OnlineSession[]>('/system/online-session/list')
}

export function kickSession(userId: number) {
  return del<void>(`/system/online-session/${userId}`)
}
