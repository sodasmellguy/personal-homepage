<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useHead } from '@unhead/vue'
import { message } from 'ant-design-vue'
import {
  MessageOutlined,
  UserOutlined,
  SendOutlined,
  HeartOutlined,
  EditOutlined,
} from '@ant-design/icons-vue'
import { post } from '@/utils/request'
import { get } from '@/utils/request'

useHead({
  title: '留言墙 - 赵先生',
  meta: [
    { name: 'description', content: '赵先生的留言板，欢迎留下你的想法和建议' },
  ],
})

interface GuestMessage {
  id: number
  nickname: string
  content: string
  color: string
  likeCount: number
  createTime: string
}

const messages = ref<GuestMessage[]>([])
const loading = ref(false)
const submitting = ref(false)
const showForm = ref(false)
const formRef = ref()
const formData = reactive({
  nickname: '',
  content: '',
})
const selectedColor = ref('#3b82f6')

const colorOptions = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#f97316']

// 回复功能
const replyingTo = ref<GuestMessage | null>(null)

const startReply = (msg: GuestMessage) => {
  replyingTo.value = msg
  formData.content = `@${msg.nickname} `
  showForm.value = true
}

const cancelReply = () => {
  replyingTo.value = null
  formData.content = ''
}

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  content: [{ required: true, message: '请输入留言内容', trigger: 'blur' }],
}

// 获取留言列表
const fetchMessages = async () => {
  loading.value = true
  try {
    const { data } = await get('/front/guestbook')
    messages.value = data || []
  } catch (error) {
    console.error('获取留言列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchMessages()
})

// 提交留言
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    await post('/front/guestbook', {
      ...formData,
      color: selectedColor.value,
    })
    message.success('留言成功！')
    formData.nickname = ''
    formData.content = ''
    showForm.value = false
    replyingTo.value = null
    fetchMessages()
  } catch (error: any) {
    message.error(error.message || '留言失败')
  } finally {
    submitting.value = false
  }
}

// 获取头像文字
const getAvatarText = (name: string) => {
  return name?.charAt(0)?.toUpperCase() || '?'
}

// 格式化时间
const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 点赞留言
const toggleLike = async (msg: GuestMessage) => {
  try {
    await post(`/front/guestbook/${msg.id}/like`)
    msg.likeCount = (msg.likeCount || 0) + 1
    messages.value = [...messages.value] // trigger reactivity
  } catch (error: any) {
    message.error(error.message || '您已经点赞过这条留言了')
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-dark-base transition-colors duration-300">
    <!-- Hero -->
    <section class="relative py-16 sm:py-20 px-4 sm:px-8 bg-gradient-to-br from-blue-50 via-indigo-50 to-violet-50 dark:from-dark-card dark:via-dark-base dark:to-dark-card">
      <div class="absolute inset-0 opacity-[0.03] dark:opacity-[0.05]" style="background-image: radial-gradient(circle, #8b5cf6 1px, transparent 1px); background-size: 20px 20px;"></div>
      <div class="absolute top-10 right-20 w-32 h-32 bg-blue-200 dark:bg-blue-900/30 rounded-full blur-2xl opacity-60 mesh-blob"></div>
      <div class="absolute bottom-10 left-10 w-40 h-40 bg-violet-200 dark:bg-violet-900/30 rounded-full blur-2xl opacity-60 mesh-blob-2"></div>
      <div class="max-w-4xl mx-auto relative text-center">
        <div class="flex items-center justify-center gap-3 mb-3">
          <div class="w-8 h-0.5 bg-gradient-to-r from-transparent to-indigo-500 rounded-full"></div>
          <span class="text-indigo-600 dark:text-indigo-400 text-sm font-medium tracking-wider uppercase">Guest Book</span>
          <div class="w-8 h-0.5 bg-gradient-to-l from-transparent to-indigo-500 rounded-full"></div>
        </div>
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-dark-text-primary">留言墙</h1>
        <p class="text-gray-600 dark:text-dark-text-secondary mt-2">
          欢迎留下你的想法、建议或问候
        </p>
        <a-button
          type="primary"
          size="large"
          class="!rounded-xl mt-4 hover:!scale-105 transition-all duration-300"
          @click="showForm = !showForm"
        >
          <template #icon>
            <SendOutlined />
          </template>
          写留言
        </a-button>
      </div>
    </section>

    <div class="max-w-4xl mx-auto px-4 sm:px-8 py-8">
      <!-- 留言表单 -->
      <transition name="slide-down">
        <div v-if="showForm" class="mb-8 bg-white dark:bg-dark-card rounded-2xl p-6 border border-gray-100 dark:border-dark-border shadow-sm">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-dark-text-primary mb-4 flex items-center gap-2">
            <EditOutlined />
            写留言
          </h3>
          <div v-if="replyingTo" class="mb-3 flex items-center gap-2 px-3 py-2 bg-blue-50 dark:bg-blue-900/10 rounded-lg text-sm">
            <span class="text-blue-600 dark:text-blue-400">回复 @{{ replyingTo.nickname }}</span>
            <button class="text-gray-400 hover:text-gray-600 ml-auto" @click="cancelReply">取消</button>
          </div>
          <a-form ref="formRef" :model="formData" :rules="rules" layout="vertical">
            <a-form-item label="昵称" name="nickname">
              <a-input v-model:value="formData.nickname" placeholder="请输入昵称" :maxlength="20">
                <template #prefix>
                  <UserOutlined class="text-gray-400" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item label="留言内容" name="content">
              <a-textarea v-model:value="formData.content" placeholder="写下你想说的话..." :rows="4" :maxlength="200" show-count />
            </a-form-item>
            <a-form-item label="选择颜色">
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="color in colorOptions"
                  :key="color"
                  class="w-8 h-8 rounded-full border-2 transition-all duration-200 hover:scale-110"
                  :style="{ backgroundColor: color }"
                  :class="selectedColor === color ? 'border-gray-900 dark:border-white scale-110 ring-2 ring-offset-2 ring-gray-300' : 'border-transparent'"
                  @click="selectedColor = color"
                ></button>
              </div>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" :loading="submitting" class="!rounded-xl" @click="handleSubmit">
                <template #icon>
                  <SendOutlined />
                </template>
                发表留言
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </transition>

      <!-- 留言列表 - 瀑布流卡片 -->
      <div v-if="loading" class="columns-1 sm:columns-2 lg:columns-3 gap-4 space-y-4">
        <div v-for="i in 6" :key="i" class="break-inside-avoid bg-white dark:bg-dark-card rounded-2xl p-5 border border-gray-100 dark:border-dark-border">
          <div class="space-y-3">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 skeleton-shimmer rounded-full"></div>
              <div class="h-4 skeleton-shimmer rounded w-20"></div>
            </div>
            <div class="space-y-2">
              <div class="h-3 skeleton-shimmer rounded w-full"></div>
              <div class="h-3 skeleton-shimmer rounded w-3/4"></div>
            </div>
            <div class="h-3 skeleton-shimmer rounded w-24"></div>
          </div>
        </div>
      </div>

      <template v-else>
        <div v-if="messages.length > 0" class="columns-1 sm:columns-2 lg:columns-3 gap-4 space-y-4">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="break-inside-avoid bg-white dark:bg-dark-card rounded-2xl p-5 border border-gray-100 dark:border-dark-border hover:shadow-2xl hover:shadow-pink-500/5 hover:-translate-y-1 transition-all duration-500 group"
          >
            <!-- 顶部色条 -->
            <div class="w-full h-1 rounded-full mb-4" :style="{ background: msg.color || '#3b82f6' }"></div>

            <div class="flex items-center gap-3 mb-3">
              <a-avatar
                :style="{ backgroundColor: msg.color || '#3b82f6' }"
                class="flex-shrink-0 shadow-sm"
                size="default"
              >
                {{ getAvatarText(msg.nickname) }}
              </a-avatar>
              <div>
                <div class="font-semibold text-gray-900 dark:text-dark-text-primary text-sm">{{ msg.nickname }}</div>
                <div class="text-xs text-gray-400 dark:text-dark-text-muted">{{ formatTime(msg.createTime) }}</div>
              </div>
            </div>

            <p class="text-gray-700 dark:text-dark-text-secondary text-sm leading-relaxed whitespace-pre-wrap break-words mb-3">
              {{ msg.content }}
            </p>

            <div class="flex items-center">
              <button
                class="flex items-center gap-1.5 text-xs transition-colors duration-200 text-gray-400 hover:text-red-500 hover:scale-110"
                @click="toggleLike(msg)"
              >
                <HeartOutlined class="transition-transform duration-300 group-hover:scale-125" />
                <span>{{ msg.likeCount || 0 }}</span>
              </button>
              <button
                class="flex items-center gap-1.5 text-xs transition-colors duration-200 text-gray-400 hover:text-blue-500 ml-3"
                @click="startReply(msg)"
              >
                <MessageOutlined />
                <span>回复</span>
              </button>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-20">
          <MessageOutlined class="text-5xl text-gray-300 dark:text-gray-600 mb-4" />
          <p class="text-gray-500 dark:text-dark-text-muted mb-4">还没有留言，来写下第一条吧！</p>
          <a-button type="primary" class="!rounded-xl" @click="showForm = true">写留言</a-button>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
