<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  LoadingOutlined,
  DeleteOutlined,
  FileOutlined,
  PictureOutlined,
  UploadOutlined,
} from '@ant-design/icons-vue'
import { uploadFile, uploadImage } from '@/api/upload'

type UploadType = 'image' | 'file'

// 与后端 FileUploadServiceImpl 图片白名单保持一致
// （不含 svg：SVG 可内嵌 <script>，存在存储型 XSS 风险，服务端同样会拒绝）
const IMAGE_MIME_TYPES = [
  'image/jpeg',
  'image/pjpeg',
  'image/png',
  'image/gif',
  'image/webp',
  'image/bmp',
  'image/x-icon',
  'image/vnd.microsoft.icon',
]

const props = withDefaults(defineProps<{
  modelValue?: string
  type?: UploadType
  maxSize?: number
  disabled?: boolean
  accept?: string
  placeholder?: string
}>(), {
  modelValue: '',
  type: 'image',
  maxSize: 10,
  disabled: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const loading = ref(false)
const fileUrl = ref(props.modelValue)
const fileInputRef = ref<HTMLInputElement>()
const isDragging = ref(false)

const isImage = computed(() => props.type === 'image')

const acceptStr = computed(() => {
  if (props.accept) return props.accept
  // 图片模式收紧为白名单 MIME（原 image/* 会放行 image/svg+xml）
  return isImage.value ? IMAGE_MIME_TYPES.join(',') : '*'
})

const defaultPlaceholder = computed(() => {
  return props.placeholder || '上传文件'
})

// 同步外部值
watch(() => props.modelValue, (val) => {
  fileUrl.value = val || ''
}, { immediate: true })

// 触发文件选择
const triggerSelect = () => {
  if (props.disabled || loading.value) return
  fileInputRef.value?.click()
}

// 拖拽处理
const handleDragOver = (e: DragEvent) => {
  e.preventDefault()
  isDragging.value = true
}
const handleDragLeave = () => { isDragging.value = false }
const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  isDragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) processFile(file)
}

// 处理文件选择
const handleFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) processFile(file)
  input.value = ''
}

// 校验文件是否匹配 accept 规则（拖拽上传时原生 accept 不生效，需要手动校验）
const matchAccept = (file: File): boolean => {
  // accept 为 * 或空则放行
  if (!acceptStr.value || acceptStr.value === '*') return true
  const rules = acceptStr.value.split(',').map(s => s.trim())
  const fileType = file.type          // e.g. "image/png"
  const fileName = file.name.toLowerCase() // e.g. "photo.jpg"
  return rules.some(rule => {
    if (rule.endsWith('/*'))  return fileType.startsWith(rule.replace('/*', '/'))  // image/*
    if (rule.startsWith('.')) return fileName.endsWith(rule)                       // .pdf
    return fileType === rule                                                        // application/json
  })
}

// 生成可读的允许类型描述
const acceptLabel = computed(() => {
  const a = acceptStr.value
  if (!a || a === '*') return '所有格式'
  if (isImage.value && a === IMAGE_MIME_TYPES.join(',')) return 'JPG、PNG、GIF、WebP、BMP、ICO 格式'
  return a
})

const processFile = async (file: File) => {
  // 1. 图片模式：仅允许白名单内的位图格式（svg 等可内联脚本的类型一律拒绝）
  if (isImage.value && !IMAGE_MIME_TYPES.includes(file.type)) {
    message.error('只能上传图片文件，支持 JPG、PNG、GIF、WebP、BMP、ICO 格式（不支持 SVG）')
    return
  }
  // 2. accept 规则校验（覆盖拖拽场景）
  if (!matchAccept(file)) {
    message.error(`文件类型不正确，只允许上传 ${acceptLabel.value}`)
    return
  }
  // 3. 大小校验
  if (file.size / 1024 / 1024 > props.maxSize) {
    message.error(`文件大小不能超过 ${props.maxSize}MB`)
    return
  }

  loading.value = true
  try {
    // 图片走 /upload/image（访客匿名可用，如友链申请页），
    // 文件走 /upload/file（需登录），与后端两类白名单一一对应
    const uploadFn = isImage.value ? uploadImage : uploadFile
    const { data } = await uploadFn(file)
    fileUrl.value = data.url
    emit('update:modelValue', data.url)
    message.success('上传成功')
  } catch (error: any) {
    message.error(error.message || '上传失败')
  } finally {
    loading.value = false
  }
}

// 删除
const handleRemove = () => {
  fileUrl.value = ''
  emit('update:modelValue', '')
}

// 从 URL 提取文件名
const getFileName = (url: string) => {
  const name = url.split('/').pop() || ''
  return decodeURIComponent(name)
}
</script>

<template>
  <div class="image-upload">
    <input
      ref="fileInputRef"
      type="file"
      :accept="acceptStr"
      style="position: absolute; width: 0; height: 0; opacity: 0; pointer-events: none; overflow: hidden;"
      @change="handleFileChange"
    />

    <!-- ============ 已有文件 ============ -->
    <div
      v-if="fileUrl"
      class="upload-preview relative w-[140px] h-[140px] rounded-2xl overflow-hidden group cursor-pointer shadow-lg hover:shadow-2xl transition-all duration-300"
      @click="triggerSelect"
    >
      <!-- 图片预览 -->
      <img
        v-if="isImage"
        :src="fileUrl"
        class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
      />
      <!-- 文件预览 -->
      <div v-else class="w-full h-full flex flex-col items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 dark:from-dark-elevated/50 dark:to-dark-card">
        <div class="w-12 h-12 rounded-xl bg-blue-50 dark:bg-blue-900/30 flex items-center justify-center mb-2">
          <FileOutlined class="text-2xl text-blue-500" />
        </div>
        <span class="text-[11px] text-gray-500 dark:text-dark-text-muted px-3 text-center truncate w-full leading-tight">
          {{ getFileName(fileUrl) }}
        </span>
      </div>

      <!-- 悬浮蒙层 -->
      <div class="absolute inset-0 bg-black/0 group-hover:bg-black/40 transition-all duration-300 flex flex-col items-center justify-center gap-2 opacity-0 group-hover:opacity-100">
        <div class="w-9 h-9 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center transform scale-75 group-hover:scale-100 transition-transform duration-300">
          <UploadOutlined class="text-white text-sm" />
        </div>
        <span class="text-white text-[11px] font-medium backdrop-blur-sm">点击更换</span>
      </div>

      <!-- 删除按钮 -->
      <button
        class="absolute top-2 right-2 w-6 h-6 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all duration-300 transform scale-75 group-hover:scale-100 hover:!scale-110"
        :class="isImage ? 'bg-black/30 backdrop-blur-sm hover:bg-red-500' : 'bg-red-500 hover:bg-red-600 shadow-lg'"
        @click.stop="handleRemove"
      >
        <DeleteOutlined class="text-white text-[10px]" />
      </button>
    </div>

    <!-- ============ 上传按钮 ============ -->
    <div
      v-else
      class="upload-trigger w-[140px] h-[140px] rounded-2xl border-2 border-dashed transition-all duration-300 cursor-pointer flex flex-col items-center justify-center gap-2.5 group relative overflow-hidden"
      :class="[
        isDragging
          ? 'border-blue-500 bg-blue-50/80 dark:bg-blue-900/20 scale-[1.02]'
          : 'border-gray-200 dark:border-dark-border hover:border-blue-400 dark:hover:border-blue-500 bg-gray-50/30 dark:bg-dark-elevated/20 hover:bg-blue-50/50 dark:hover:bg-blue-900/10',
        { 'opacity-50 cursor-not-allowed': disabled }
      ]"
      @click="triggerSelect"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
    >
      <!-- 背景光晕 -->
      <div class="absolute inset-0 bg-gradient-to-br from-blue-500/0 to-blue-500/0 group-hover:from-blue-500/5 group-hover:to-purple-500/5 transition-all duration-500" />

      <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-blue-50 to-blue-100/80 dark:from-blue-900/20 dark:to-blue-800/10 group-hover:from-blue-100 group-hover:to-blue-200/60 dark:group-hover:from-blue-900/30 dark:group-hover:to-blue-800/20 flex items-center justify-center transition-all duration-300 group-hover:scale-110 group-hover:shadow-md shadow-blue-100/50 dark:shadow-blue-900/20">
        <LoadingOutlined v-if="loading" class="text-lg text-blue-500 animate-spin" />
        <PictureOutlined v-else-if="isImage" class="text-lg text-blue-400 group-hover:text-blue-500 transition-colors" />
        <UploadOutlined v-else class="text-lg text-blue-400 group-hover:text-blue-500 transition-colors" />
      </div>

      <div class="flex flex-col items-center gap-0.5 relative z-10">
        <span class="text-xs text-gray-500 dark:text-dark-text-muted group-hover:text-blue-500 transition-colors font-medium">
          {{ loading ? '上传中...' : defaultPlaceholder }}
        </span>
        <span class="text-[10px] text-gray-300 dark:text-gray-600">最大 {{ maxSize }}MB</span>
      </div>

      <!-- 拖拽提示 -->
      <div v-if="isDragging" class="absolute inset-0 bg-blue-500/10 flex items-center justify-center z-20">
        <span class="text-blue-500 text-xs font-medium">松开上传</span>
      </div>
    </div>
  </div>
</template>
