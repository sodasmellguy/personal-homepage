import { post, get, put } from '@/utils/request'

// 登录参数
export interface LoginParams {
  username: string
  password: string
}

// 登录响应
export interface LoginResult {
  token: string
  userId: number
  username: string
  nickname: string
  avatar: string
}

// 用户信息
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  roles: string[]
  permissions: string[]
}

/**
 * 用户登录
 */
export function login(data: LoginParams) {
  return post<LoginResult>('/auth/login', data)
}

/**
 * 用户登出
 */
export function logout() {
  return post('/auth/logout')
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return get<UserInfo>('/auth/info')
}

/**
 * 修改密码
 */
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return put('/auth/change-password', data)
}
