import { get, post, put, del } from '@/utils/request'

// 相册照片
export interface AlbumPhoto {
  id?: number
  title?: string
  description?: string
  image: string
  /** 所属分组ID（null 为未分组） */
  groupId?: number | null
  location?: string
  shootDate?: string
  /** 相机型号（EXIF解析） */
  camera?: string
  /** 光圈，如 f/2.8 */
  aperture?: string
  /** 快门，如 1/250s */
  shutter?: string
  /** 感光度 */
  iso?: string
  /** 焦距，如 35mm */
  focalLength?: string
  sort?: number
  status?: number // 0-隐藏 1-显示
  createTime?: string
}

// 相册分组
export interface AlbumGroup {
  id?: number
  name: string
  description?: string
  coverImage?: string
  sort?: number
  status?: number // 0-隐藏 1-显示
  createTime?: string
}

/**
 * 前台获取相册照片列表（可按分组筛选）
 */
export function getFrontAlbumPhotos(groupId?: number) {
  return get<AlbumPhoto[]>('/front/album/list', { groupId })
}

/**
 * 前台获取相册分组列表
 */
export function getFrontAlbumGroups() {
  return get<AlbumGroup[]>('/front/album/groups')
}

/**
 * 后台分页查询照片
 */
export function getAlbumPhotoPage(params: {
  pageNum?: number
  pageSize?: number
  title?: string
  status?: number
  groupId?: number
}) {
  return get<{ total: number; rows: AlbumPhoto[] }>('/album/list', params)
}

/**
 * 创建照片（后台），返回保存后的实体（含EXIF解析结果）
 */
export function createAlbumPhoto(data: Partial<AlbumPhoto>) {
  return post<AlbumPhoto>('/album', data)
}

/**
 * 更新照片（后台）
 */
export function updateAlbumPhoto(id: number, data: Partial<AlbumPhoto>) {
  return put(`/album/${id}`, data)
}

/**
 * 删除照片（后台）
 */
export function deleteAlbumPhoto(id: number) {
  return del(`/album/${id}`)
}

/**
 * 批量删除照片（后台）
 */
export function batchDeleteAlbumPhotos(ids: number[]) {
  return del('/album/batch', ids)
}

/**
 * 获取全部分组（后台，含隐藏）
 */
export function getAlbumGroups() {
  return get<AlbumGroup[]>('/album/group/list')
}

/**
 * 创建分组（后台）
 */
export function createAlbumGroup(data: Partial<AlbumGroup>) {
  return post('/album/group', data)
}

/**
 * 更新分组（后台）
 */
export function updateAlbumGroup(id: number, data: Partial<AlbumGroup>) {
  return put(`/album/group/${id}`, data)
}

/**
 * 删除分组（后台），分组下照片回归未分组
 */
export function deleteAlbumGroup(id: number) {
  return del(`/album/group/${id}`)
}
