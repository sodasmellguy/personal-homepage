import { get, put, post } from '@/utils/request'

// 个人信息
export interface Profile {
  id: number
  name: string
  university: string
  major: string
  email: string
  wechat: string
  avatar: string
  introduction: string
  careerGoal: string
  giteeUrl: string
  githubUrl: string
  createTime: string
  updateTime: string
}

/**
 * 获取个人信息（后台）
 */
export function getProfile() {
  return get<Profile>('/profile')
}

/**
 * 获取个人信息（前台）
 */
export function getFrontProfile() {
  return get<Profile>('/front/profile')
}

/**
 * 更新个人信息
 */
export function updateProfile(data: Partial<Profile>) {
  return put('/profile', data)
}

/**
 * 上传头像
 */
export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return post<{ url: string }>('/profile/avatar', formData)
}
