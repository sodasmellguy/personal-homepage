<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  MessageOutlined,
  UserOutlined,
  MailOutlined,
  SendOutlined,
  RollbackOutlined,
  SmileOutlined,
} from '@ant-design/icons-vue'
import { submitComment } from '@/api/article'
import type { Comment } from '@/api/article'

const props = defineProps<{
  articleId: number
  comments: Comment[]
}>()

const emit = defineEmits<{
  refresh: []
}>()

// 评论表单
const formVisible = ref(false)
const submitting = ref(false)
const replyTo = ref<Comment | null>(null)
const formData = reactive({
  nickname: '',
  email: '',
  content: '',
})

// 显示评论表单
const showCommentForm = (comment?: Comment) => {
  replyTo.value = comment || null
  formVisible.value = true
  if (!comment) {
    formData.content = ''
  }
}

// Emoji 选择器
const showEmojiPicker = ref(false)
const emojiGroups = [
  { label: '常用', emojis: ['😀', '😂', '🥰', '😎', '🤔', '👍', '❤️', '🎉', '🔥', '✨', '💯', '👏', '🙏', '💪', '🎯', '⭐'] },
  { label: '表情', emojis: ['😊', '😍', '🤣', '😭', '😤', '🤗', '😴', '🤯', '🥺', '😏', '🙄', '😬', '🤪', '😎', '🤓', '😇'] },
  { label: '手势', emojis: ['👍', '👎', '👏', '🙌', '🤝', '✌️', '🤞', '👋', '💪', '🫶', '🖐️', '🤙', '👆', '👇', '🫡', '🤟'] },
  { label: '符号', emojis: ['❤️', '🔥', '⭐', '💯', '✅', '❌', '⚡', '💡', '🎯', '🏆', '🎉', '🎊', '✨', '💎', '🚀', '📌'] },
]

const insertEmoji = (emoji: string) => {
  formData.content += emoji
  showEmojiPicker.value = false
}

// 取消回复
const cancelReply = () => {
  replyTo.value = null
  formData.content = ''
}

// 提交评论
const handleSubmit = async () => {
  if (!formData.nickname.trim()) {
    message.warning('请输入昵称')
    return
  }
  if (!formData.content.trim()) {
    message.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    await submitComment({
      articleId: props.articleId,
      parentId: replyTo.value?.parentId || replyTo.value?.id || 0,
      replyToId: replyTo.value?.id,
      nickname: formData.nickname,
      email: formData.email,
      content: formData.content,
    })
    message.success('评论提交成功')
    formData.content = ''
    replyTo.value = null
    formVisible.value = false
    emit('refresh')
  } catch (error) {
    message.error('评论提交失败')
  } finally {
    submitting.value = false
  }
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

// HTML 转义（防止 XSS，必须在任何 HTML 拼接之前执行）
const escapeHtml = (str: string) => {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

// 链接地址白名单：仅允许 http(s) 协议，阻止 javascript: 等危险协议
const safeUrl = (url: string) => {
  const trimmed = url.trim()
  return /^(https?:\/\/|\/)/i.test(trimmed) ? trimmed : '#'
}

// 简易 Markdown 渲染（评论用）
// 注意：先做完整 HTML 转义，再做受限的格式替换，杜绝 XSS
const renderCommentContent = (content: string) => {
  if (!content) return ''
  return escapeHtml(content)
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code class="bg-gray-100 dark:bg-dark-base px-1.5 py-0.5 rounded text-sm text-pink-600 dark:text-pink-400 font-mono">$1</code>')
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, (_m, text: string, url: string) => {
      return `<a href="${safeUrl(url)}" target="_blank" rel="noopener noreferrer" class="text-blue-500 hover:underline">${text}</a>`
    })
    .replace(/\n/g, '<br>')
}

// 获取头像文字
const getAvatarText = (name: string) => {
  return name?.charAt(0)?.toUpperCase() || '?'
}

// 获取随机颜色
const getAvatarColor = (name: string) => {
  const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899']
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}
</script>

<template>
  <div>
    <!-- 标题 -->
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-xl font-bold text-gray-900 dark:text-dark-text-primary flex items-center gap-2">
        <MessageOutlined />
        评论 ({{ comments.length }})
      </h3>
      <a-button type="primary" class="!rounded-lg" @click="showCommentForm()">
        写评论
      </a-button>
    </div>

    <!-- 评论输入框 -->
    <transition name="slide-down">
      <div v-if="formVisible" class="mb-8 p-4 sm:p-6 bg-gray-50 dark:bg-dark-elevated rounded-2xl border border-gray-100 dark:border-dark-border">
        <!-- 回复提示 -->
        <div v-if="replyTo" class="mb-4 flex items-center justify-between">
          <span class="text-sm text-gray-600 dark:text-dark-text-secondary">
            回复 <span class="font-medium text-blue-600 dark:text-blue-400">{{ replyTo.nickname }}</span>
          </span>
          <a-button type="text" size="small" @click="cancelReply">取消回复</a-button>
        </div>

        <a-form layout="vertical">
          <a-row :gutter="16">
            <a-col :xs="24" :sm="12">
              <a-form-item label="昵称" required>
                <a-input
                  v-model:value="formData.nickname"
                  placeholder="请输入昵称"
                  :maxlength="50"
                >
                  <template #prefix>
                    <UserOutlined class="text-gray-400" />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12">
              <a-form-item label="邮箱（选填）">
                <a-input
                  v-model:value="formData.email"
                  placeholder="请输入邮箱"
                  :maxlength="100"
                >
                  <template #prefix>
                    <MailOutlined class="text-gray-400" />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
          </a-row>
          <a-form-item label="评论内容" required>
            <div class="relative">
              <a-textarea
                v-model:value="formData.content"
                :placeholder="replyTo ? `回复 ${replyTo.nickname}...` : '写下你的评论...（支持输入 Emoji）'"
                :rows="4"
                :maxlength="1000"
                show-count
              />
              <!-- Emoji 按钮 -->
              <div class="relative inline-block mt-2">
                <a-button
                  type="text"
                  size="small"
                  class="!text-gray-400 hover:!text-amber-500"
                  @click="showEmojiPicker = !showEmojiPicker"
                >
                  <template #icon>
                    <SmileOutlined />
                  </template>
                  Emoji
                </a-button>
                <!-- Emoji 面板 -->
                <transition name="slide-down">
                  <div
                    v-if="showEmojiPicker"
                    class="absolute bottom-full left-0 mb-2 bg-white dark:bg-dark-card rounded-xl shadow-lg border border-gray-200 dark:border-dark-border p-3 w-72 z-50"
                  >
                    <div v-for="group in emojiGroups" :key="group.label" class="mb-3 last:mb-0">
                      <div class="text-xs text-gray-400 dark:text-dark-text-muted mb-1.5">{{ group.label }}</div>
                      <div class="flex flex-wrap gap-1">
                        <button
                          v-for="emoji in group.emojis"
                          :key="emoji"
                          class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 dark:hover:bg-dark-elevated transition-colors text-lg cursor-pointer"
                          @click="insertEmoji(emoji)"
                        >
                          {{ emoji }}
                        </button>
                      </div>
                    </div>
                  </div>
                </transition>
              </div>
            </div>
          </a-form-item>
          <a-form-item>
            <a-button
              type="primary"
              :loading="submitting"
              class="!rounded-lg"
              @click="handleSubmit"
            >
              <template #icon>
                <SendOutlined />
              </template>
              发表评论
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </transition>

    <!-- 评论列表 -->
    <div class="space-y-6">
      <div v-for="comment in comments" :key="comment.id" class="comment-item group">
        <!-- 主评论 -->
        <div class="flex gap-3 sm:gap-4">
          <!-- 头像 -->
          <a-avatar
            :style="{ backgroundColor: getAvatarColor(comment.nickname) }"
            class="flex-shrink-0 shadow-sm"
          >
            {{ getAvatarText(comment.nickname) }}
          </a-avatar>

          <!-- 内容 -->
          <div class="flex-1 min-w-0">
            <div class="bg-gray-50 dark:bg-dark-elevated rounded-2xl p-4 transition-colors duration-200 group-hover:bg-gray-100 dark:group-hover:bg-dark-card">
              <div class="flex flex-wrap items-center gap-2 mb-2">
                <span class="font-semibold text-gray-900 dark:text-dark-text-primary">
                  {{ comment.nickname }}
                </span>
                <span class="text-xs text-gray-400 dark:text-dark-text-muted">
                  {{ formatTime(comment.createTime) }}
                </span>
              </div>

              <p class="text-gray-700 dark:text-dark-text-secondary mb-3 whitespace-pre-wrap break-words leading-relaxed" v-html="renderCommentContent(comment.content)">
              </p>

              <a-button
                type="text"
                size="small"
                class="!text-gray-400 hover:!text-blue-500 !px-2"
                @click="showCommentForm(comment)"
              >
                <template #icon>
                  <RollbackOutlined />
                </template>
                回复
              </a-button>
            </div>

            <!-- 子评论 -->
            <div v-if="comment.children?.length" class="mt-3 space-y-3 pl-2 sm:pl-4 border-l-2 border-gray-100 dark:border-dark-border">
              <div v-for="child in comment.children" :key="child.id" class="flex gap-3">
                <a-avatar
                  :style="{ backgroundColor: getAvatarColor(child.nickname), width: '28px', height: '28px', fontSize: '12px' }"
                  class="flex-shrink-0 shadow-sm"
                >
                  {{ getAvatarText(child.nickname) }}
                </a-avatar>
                <div class="flex-1 min-w-0">
                  <div class="flex flex-wrap items-center gap-2 mb-1">
                    <span class="font-medium text-gray-900 dark:text-dark-text-primary text-sm">
                      {{ child.nickname }}
                    </span>
                    <span v-if="child.replyToNickname" class="text-xs text-gray-400 dark:text-dark-text-muted">
                      回复 <span class="text-blue-500 dark:text-blue-400">{{ child.replyToNickname }}</span>
                    </span>
                    <span class="text-xs text-gray-400 dark:text-dark-text-muted">
                      {{ formatTime(child.createTime) }}
                    </span>
                  </div>
                  <p class="text-gray-600 dark:text-dark-text-secondary text-sm whitespace-pre-wrap break-words leading-relaxed" v-html="renderCommentContent(child.content)">
                  </p>
                  <a-button
                    type="text"
                    size="small"
                    class="!text-gray-400 hover:!text-blue-500 !text-xs !px-2"
                    @click="showCommentForm(child)"
                  >
                    <template #icon>
                      <RollbackOutlined />
                    </template>
                    回复
                  </a-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-if="comments.length === 0" description="暂无评论，来发表第一条评论吧~" />
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
