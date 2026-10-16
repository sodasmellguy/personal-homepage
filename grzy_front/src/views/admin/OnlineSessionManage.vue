<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { LaptopOutlined, ReloadOutlined, UserOutlined } from '@ant-design/icons-vue'
import { getOnlineSessionList, kickSession, type OnlineSession } from '@/api/session'

const loading = ref(false)
const kickingUserId = ref<number | null>(null)
const sessions = ref<OnlineSession[]>([])

const loadSessions = async () => {
  loading.value = true
  try {
    const { data } = await getOnlineSessionList()
    sessions.value = data || []
  } catch (error) {
    console.error('加载在线会话失败:', error)
  } finally {
    loading.value = false
  }
}

const handleKick = async (record: OnlineSession) => {
  kickingUserId.value = record.userId
  try {
    await kickSession(record.userId)
    message.success(`已将「${record.username}」踢下线`)
    loadSessions()
  } catch (error) {
    console.error('踢下线失败:', error)
  } finally {
    kickingUserId.value = null
  }
}

const columns = [
  { title: '用户', key: 'username', width: 140 },
  { title: 'Token', dataIndex: 'token', key: 'token', width: 120 },
  { title: '登录 IP', dataIndex: 'ip', key: 'ip', width: 140 },
  { title: '设备 / 浏览器', dataIndex: 'userAgent', key: 'userAgent', ellipsis: true },
  { title: '登录时间', dataIndex: 'loginTime', key: 'loginTime', width: 170 },
  { title: '操作', key: 'action', width: 120 },
]

onMounted(loadSessions)
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center shadow-sm">
          <LaptopOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">在线会话</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">查看当前登录会话，可将可疑会话强制下线</p>
        </div>
      </div>
      <a-button @click="loadSessions" class="!rounded-lg">
        <template #icon><ReloadOutlined /></template>
        刷新
      </a-button>
    </div>

    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm overflow-hidden">
      <a-table
        :columns="columns"
        :data-source="sessions"
        :loading="loading"
        :pagination="false"
        row-key="token"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'username'">
            <div class="flex items-center gap-2">
              <UserOutlined class="text-gray-400" />
              <span class="font-medium text-gray-700 dark:text-dark-text-primary">{{ record.username }}</span>
              <a-tag v-if="record.current" color="blue" class="!ml-1">当前</a-tag>
            </div>
          </template>
          <template v-if="column.key === 'token'">
            <span class="font-mono text-xs text-gray-400 dark:text-dark-text-muted">{{ record.token }}</span>
          </template>
          <template v-if="column.key === 'userAgent'">
            <span class="text-xs text-gray-500 dark:text-dark-text-muted">{{ record.userAgent || '-' }}</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-popconfirm
              v-if="!record.current"
              title="确定将该会话强制下线？"
              ok-text="踢下线"
              cancel-text="取消"
              @confirm="handleKick(record)"
            >
              <a-button danger size="small" :loading="kickingUserId === record.userId" class="!rounded-lg">
                踢下线
              </a-button>
            </a-popconfirm>
            <a-tooltip v-else title="不能踢出自己的当前会话">
              <a-button size="small" disabled class="!rounded-lg">当前会话</a-button>
            </a-tooltip>
          </template>
        </template>
        <template #emptyText>
          <div class="py-10 text-center text-gray-400 dark:text-dark-text-muted">
            <LaptopOutlined class="text-3xl mb-3 block" />
            当前没有在线会话
          </div>
        </template>
      </a-table>
    </div>
  </div>
</template>
