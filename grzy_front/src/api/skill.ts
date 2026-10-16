import { get, post, put, del } from '@/utils/request'

// 技能
export interface Skill {
  id: number
  name: string
  category: string
  icon: string
  sort: number
  status: number
  createTime: string
  updateTime: string
}

// 技能查询参数
export interface SkillQuery {
  pageNum?: number
  pageSize?: number
  name?: string
  category?: string
  status?: number
}

/**
 * 获取技能列表（分页）
 */
export function getSkillList(params: SkillQuery) {
  return get<{ total: number; rows: Skill[] }>('/skill/list', params)
}

/**
 * 获取所有技能（前台展示）
 */
export function getAllSkills() {
  return get<Skill[]>('/front/skill/list')
}

/**
 * 获取技能详情
 */
export function getSkill(id: number) {
  return get<Skill>(`/skill/${id}`)
}

/**
 * 创建技能
 */
export function createSkill(data: Partial<Skill>) {
  return post('/skill', data)
}

/**
 * 更新技能
 */
export function updateSkill(id: number, data: Partial<Skill>) {
  return put(`/skill/${id}`, data)
}

/**
 * 删除技能
 */
export function deleteSkill(id: number) {
  return del(`/skill/${id}`)
}

/**
 * 批量删除技能
 */
export function batchDeleteSkills(ids: number[]) {
  return del('/skill/batch', ids)
}
