import { get, post, put, del } from '@/utils/request'

// 项目
export interface Project {
  id: number
  title: string
  description: string
  coverImage: string
  images: string
  techStack: string
  responsibilities: string
  projectUrl: string
  startDate: string
  endDate: string
  sort: number
  status: number
  createTime: string
  updateTime: string
}

// 项目查询参数
export interface ProjectQuery {
  pageNum?: number
  pageSize?: number
  title?: string
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
 * 获取项目列表（分页）
 */
export function getProjectList(params: ProjectQuery) {
  return get<PageResult<Project>>('/project/list', params)
}

/**
 * 获取所有项目（前台展示）
 */
export function getAllProjects() {
  return get<Project[]>('/front/project/list')
}

/**
 * 获取项目详情
 */
export function getProject(id: number) {
  return get<Project>(`/project/${id}`)
}

/**
 * 创建项目
 */
export function createProject(data: Partial<Project>) {
  return post('/project', data)
}

/**
 * 更新项目
 */
export function updateProject(id: number, data: Partial<Project>) {
  return put(`/project/${id}`, data)
}

/**
 * 删除项目
 */
export function deleteProject(id: number) {
  return del(`/project/${id}`)
}

/**
 * 批量删除项目
 */
export function batchDeleteProjects(ids: number[]) {
  return del('/project/batch', ids)
}
