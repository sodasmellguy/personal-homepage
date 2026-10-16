import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { message } from 'ant-design-vue'
import { getToken, clearAuth } from './auth'
import router from '@/router'

// 响应数据结构
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 请求去重：存储进行中的请求
const pendingRequests = new Map<string, { controller: AbortController; timestamp: number }>()
const REQUEST_DEDUP_MS = 500 // 500ms 内相同请求自动去重

/**
 * 生成请求唯一标识
 */
function getRequestKey(config: InternalAxiosRequestConfig): string {
  return `${config.method}:${config.url}:${JSON.stringify(config.params || '')}`
}

/**
 * 清理已完成的请求
 */
function removePendingRequest(key: string) {
  pendingRequests.delete(key)
}

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 请求去重（仅对 GET 请求生效，POST/PUT/DELETE 不去重）
    if (config.method === 'get') {
      const requestKey = getRequestKey(config)
      const existing = pendingRequests.get(requestKey)
      if (existing && (Date.now() - existing.timestamp) < REQUEST_DEDUP_MS) {
        // 取消重复请求
        existing.controller.abort()
        removePendingRequest(requestKey)
      }
      const controller = new AbortController()
      config.signal = controller.signal
      pendingRequests.set(requestKey, { controller, timestamp: Date.now() })
    }

    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>): any => {
    // 清理已完成的请求
    const requestKey = getRequestKey(response.config as InternalAxiosRequestConfig)
    removePendingRequest(requestKey)

    const { data } = response

    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 业务成功
    if (data.code === 200) {
      return data
    }

    // 未登录
    if (data.code === 401) {
      message.error('请先登录')
      clearAuth()
      router.push('/login')
      return Promise.reject(new Error(data.message))
    }

    // 无权限
    if (data.code === 403) {
      message.error('暂无权限')
      return Promise.reject(new Error(data.message))
    }

    // 其他业务错误
    message.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message))
  },
  (error) => {
    // 清理已完成的请求（取消的请求不报错）
    if (error.config) {
      const requestKey = getRequestKey(error.config as InternalAxiosRequestConfig)
      removePendingRequest(requestKey)
    }
    // 请求被取消时不报错
    if (axios.isCancel(error) || error.code === 'ERR_CANCELED') {
      return Promise.reject(error)
    }

    console.error('响应错误:', error)

    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 401:
          message.error('请先登录')
          clearAuth()
          router.push('/login')
          break
        case 403:
          message.error('暂无权限')
          break
        case 404:
          message.error('请求资源不存在')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error(error.message || '网络错误')
      }
    } else if (error.message.includes('timeout')) {
      message.error('请求超时，请稍后再试')
    } else if (error.message.includes('Network Error')) {
      message.error('网络连接异常')
    } else {
      message.error(error.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

// 封装 GET 请求
export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.get(url, { params, ...config })
}

// 封装 POST 请求
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.post(url, data, config)
}

// 封装 PUT 请求
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.put(url, data, config)
}

// 封装 DELETE 请求
export function del<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.delete(url, { params, ...config })
}

// 封装文件上传
export function upload<T = any>(url: string, file: File, onProgress?: (percent: number) => void): Promise<ApiResponse<T>> {
  const formData = new FormData()
  formData.append('file', file)
  return service.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        onProgress(Math.round((progressEvent.loaded * 100) / progressEvent.total))
      }
    },
  })
}

// 封装文件下载
export function download(url: string, filename: string): Promise<void> {
  return service
    .get(url, {
      responseType: 'blob',
    })
    .then((response: any) => {
      const blob = new Blob([response.data])
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = filename
      link.click()
      URL.revokeObjectURL(link.href)
    })
}

export default service
