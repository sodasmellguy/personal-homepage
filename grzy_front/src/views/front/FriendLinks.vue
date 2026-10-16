<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { LinkOutlined, PlusOutlined, SendOutlined, GlobalOutlined } from '@ant-design/icons-vue'
import { getFrontFriendLinks, applyFriendLink, type FriendLink } from '@/api/friendLink'
import ImageUpload from '@/components/ImageUpload.vue'
import { useHead } from '@unhead/vue'

useHead({
  title: '友情链接 - 赵先生',
  meta: [{ name: 'description', content: '友情链接墙，欢迎交换友链' }],
})

const loading = ref(false)
const links = ref<FriendLink[]>([])

const fetchLinks = async () => {
  loading.value = true
  try {
    const { data } = await getFrontFriendLinks()
    links.value = data || []
  } catch (error) {
    console.error('获取友链失败:', error)
  } finally {
    loading.value = false
  }
}

// 申请弹窗
const applyOpen = ref(false)
const applyLoading = ref(false)
const applyForm = reactive<{ name: string; url: string; logo: string; description: string }>({
  name: '',
  url: '',
  logo: '',
  description: '',
})

const openApply = () => {
  applyForm.name = ''
  applyForm.url = ''
  applyForm.logo = ''
  applyForm.description = ''
  applyOpen.value = true
}

const handleApply = async () => {
  if (!applyForm.name.trim() || !applyForm.url.trim()) {
    message.warning('请填写站点名称和链接')
    return
  }
  let url = applyForm.url.trim()
  if (!/^https?:\/\//i.test(url)) {
    url = 'https://' + url
  }
  applyLoading.value = true
  try {
    await applyFriendLink({
      name: applyForm.name.trim(),
      url,
      logo: applyForm.logo.trim() || undefined,
      description: applyForm.description.trim() || undefined,
    })
    message.success('申请已提交，等待站长审核~')
    applyOpen.value = false
  } catch (error) {
    // 错误提示由请求拦截器统一处理
  } finally {
    applyLoading.value = false
  }
}

onMounted(fetchLinks)
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-dark-base transition-colors duration-300">
    <!-- 头部 -->
    <section class="py-16 sm:py-20 px-4 sm:px-8">
      <div class="max-w-4xl mx-auto flex flex-col sm:flex-row sm:items-end sm:justify-between gap-6">
        <div>
          <h1 class="text-3xl sm:text-4xl font-bold text-gray-900 dark:text-dark-text-primary tracking-tight">友情链接</h1>
          <p class="text-gray-500 dark:text-dark-text-muted mt-3 text-base">海内存知己，天涯若比邻</p>
        </div>
        <a-button type="primary" size="large" class="!rounded-xl" @click="openApply">
          <template #icon><PlusOutlined /></template>
          申请友链
        </a-button>
      </div>
    </section>

    <!-- 友链卡片墙 -->
    <div class="max-w-4xl mx-auto px-4 sm:px-8 pb-16">
      <a-spin :spinning="loading">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <a
            v-for="link in links"
            :key="link.id"
            :href="link.url"
            target="_blank"
            rel="noopener noreferrer nofollow"
            class="group flex items-center gap-4 p-5 bg-white dark:bg-dark-card rounded-2xl border border-gray-100 dark:border-dark-border hover:border-blue-200 dark:hover:border-blue-800 hover:shadow-xl hover:shadow-blue-500/5 hover:-translate-y-0.5 transition-all duration-300"
          >
            <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500 to-violet-500 flex items-center justify-center flex-shrink-0 overflow-hidden shadow-md">
              <img
                v-if="link.logo"
                :src="link.logo"
                :alt="link.name"
                loading="lazy"
                class="w-full h-full object-cover"
                @error="($event.target as HTMLImageElement).style.display = 'none'"
              />
              <GlobalOutlined v-else class="text-white text-xl" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-1.5 font-semibold text-gray-800 dark:text-dark-text-primary group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                {{ link.name }}
                <LinkOutlined class="text-xs opacity-0 group-hover:opacity-60 transition-opacity" />
              </div>
              <p class="text-sm text-gray-400 dark:text-dark-text-muted mt-1 truncate">
                {{ link.description || link.url }}
              </p>
            </div>
          </a>
        </div>

        <a-empty v-if="!loading && links.length === 0" description="暂无友链，快来申请第一个吧~" class="py-12" />
      </a-spin>
    </div>

    <!-- 申请弹窗 -->
    <a-modal
      v-model:open="applyOpen"
      title="申请友链"
      :confirm-loading="applyLoading"
      ok-text="提交申请"
      cancel-text="取消"
      @ok="handleApply"
    >
      <div class="space-y-4 pt-2">
        <div>
          <div class="text-sm text-gray-600 dark:text-dark-text-secondary mb-1.5">
            站点名称 <span class="text-red-500">*</span>
          </div>
          <a-input v-model:value="applyForm.name" placeholder="你的站点名称" :maxlength="64" />
        </div>
        <div>
          <div class="text-sm text-gray-600 dark:text-dark-text-secondary mb-1.5">
            站点链接 <span class="text-red-500">*</span>
          </div>
          <a-input v-model:value="applyForm.url" placeholder="https://example.com" :maxlength="255" />
        </div>
        <div>
          <div class="text-sm text-gray-600 dark:text-dark-text-secondary mb-1.5">站点图标（可选）</div>
          <ImageUpload v-model="applyForm.logo" />
        </div>
        <div>
          <div class="text-sm text-gray-600 dark:text-dark-text-secondary mb-1.5">站点描述（可选）</div>
          <a-textarea v-model:value="applyForm.description" placeholder="一句话介绍你的站点" :rows="3" :maxlength="255" show-count />
        </div>
        <p class="text-xs text-gray-400 dark:text-dark-text-muted flex items-start gap-1.5">
          <SendOutlined class="mt-0.5" />
          提交后需要站长审核，审核通过后会展示在这里
        </p>
      </div>
    </a-modal>
  </div>
</template>
