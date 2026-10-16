import { get, post, put, del } from '@/utils/request'

// 用户
export interface User {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  status: number
  roleIds?: number[]
  roleNames?: string[]
  createTime: string
  updateTime: string
}

// 角色
export interface Role {
  id: number
  roleName: string
  roleCode: string
  description: string
  status: number
  createTime: string
}

// 菜单树节点（对应 sys_menu：type 0-目录，1-菜单，2-按钮）
export interface MenuNode {
  id: number
  parentId: number
  name: string
  path?: string
  component?: string
  icon?: string
  sort: number
  type: number
  permission?: string
  children?: MenuNode[]
}

// 用户查询参数
export interface UserQuery {
  pageNum?: number
  pageSize?: number
  username?: string
  status?: number
}

// 分页结果
export interface PageResult<T> {
  total: number
  rows: T[]
  pageNum: number
  pageSize: number
}

// ========== 用户管理 ==========

/**
 * 获取用户列表
 */
export function getUserList(params: UserQuery) {
  return get<PageResult<User>>('/system/user/list', params)
}

/**
 * 获取用户详情
 */
export function getUser(id: number) {
  return get<User>(`/system/user/${id}`)
}

/**
 * 创建用户
 */
export function createUser(data: Partial<User> & { password: string }) {
  return post('/system/user', data)
}

/**
 * 更新用户
 */
export function updateUser(id: number, data: Partial<User>) {
  return put(`/system/user/${id}`, data)
}

/**
 * 删除用户
 */
export function deleteUser(id: number) {
  return del(`/system/user/${id}`)
}

/**
 * 重置密码
 */
export function resetPassword(id: number, password: string) {
  return put(`/system/user/${id}/reset-password`, { password })
}

// ========== 角色管理 ==========

/**
 * 获取角色列表
 */
export function getRoleList(params?: { pageNum?: number; pageSize?: number }) {
  return get<PageResult<Role>>('/system/role/list', params)
}

/**
 * 获取所有角色
 */
export function getAllRoles() {
  return get<Role[]>('/system/role/all')
}

/**
 * 创建角色
 */
export function createRole(data: Partial<Role>) {
  return post('/system/role', data)
}

/**
 * 更新角色
 */
export function updateRole(id: number, data: Partial<Role>) {
  return put(`/system/role/${id}`, data)
}

/**
 * 删除角色
 */
export function deleteRole(id: number) {
  return del(`/system/role/${id}`)
}

/**
 * 批量删除用户
 */
export function batchDeleteUsers(ids: number[]) {
  return del('/system/user/batch', ids)
}

/**
 * 查询用户已分配的角色ID列表
 */
export function getUserRoles(id: number) {
  return get<number[]>(`/system/user/${id}/roles`)
}

/**
 * 给用户分配角色（全量覆盖）
 */
export function assignUserRoles(id: number, roleIds: number[]) {
  return put(`/system/user/${id}/roles`, roleIds)
}

// ========== 菜单与权限分配 ==========

/**
 * 获取菜单树（角色分配权限用）
 */
export function getMenuTree() {
  return get<MenuNode[]>('/system/menu/tree')
}

/**
 * 查询角色已分配的菜单ID列表
 */
export function getRoleMenus(id: number) {
  return get<number[]>(`/system/role/${id}/menus`)
}

/**
 * 给角色分配菜单权限（全量覆盖）
 */
export function assignRoleMenus(id: number, menuIds: number[]) {
  return put(`/system/role/${id}/menus`, menuIds)
}
