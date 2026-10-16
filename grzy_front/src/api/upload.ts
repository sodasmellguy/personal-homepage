import { upload } from '@/utils/request'

export interface UploadResult {
  url: string
}

/**
 * 上传文件（统一接口，图片/文件通用）
 * @param file 文件对象
 * @param onProgress 上传进度回调
 */
export function uploadAny(file: File, onProgress?: (percent: number) => void) {
  return upload<UploadResult>('/upload', file, onProgress)
}

/**
 * 上传图片
 * @param file 文件对象
 * @param onProgress 上传进度回调
 */
export function uploadImage(file: File, onProgress?: (percent: number) => void) {
  return upload<UploadResult>('/upload/image', file, onProgress)
}

/**
 * 上传文件（通用）
 * @param file 文件对象
 * @param onProgress 上传进度回调
 */
export function uploadFile(file: File, onProgress?: (percent: number) => void) {
  return upload<UploadResult>('/upload/file', file, onProgress)
}
