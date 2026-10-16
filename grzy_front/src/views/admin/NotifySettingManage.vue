<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { BellOutlined, MailOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import {
  getNotifySetting,
  updateNotifySetting,
  getNotifyLogList,
  type NotifySetting,
  type NotifyLog,
} from '@/api/notify'

// ==================== 通知设置 ====================
const settingLoading = ref(true)
const saving = ref(false)
const setting = reactive<NotifySetting>({
  id: 1,
  adminEmail: '',
  enableCommentNotify: 1,
  enableReplyNotify: 1,
  enableFriendlinkNotify: 1,
  enablePublishNotify: 1,
  enableSecurityNotify: 1,
  alertIntervalMinutes: 30,
  updateTime: '',
})

const loadSetting = async () => {
  settingLoading.value = true
  try {
    const { data } = await getNotifySetting()
    Object.assign(setting, data, {
      // null 传给 a-input 会告警，统一转空串
      adminEmail: data?.adminEmail ?? '',
    })
  } catch (error) {
    console.error('加载通知设置失败:', error)
  } finally {
    settingLoading.value = false
  }
}

const handleSave = async () => {
  if (setting.alertIntervalMinutes == null || setting.alertIntervalMinutes < 1) {
    message.warning('告警最小间隔至少为 1 分钟')
    return
  }
  if (setting.adminEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(setting.adminEmail)) {
    message.warning('站长邮箱格式不正确')
    return
  }
  saving.value = true
  try {
    await updateNotifySetting({
      adminEmail: setting.adminEmail || null,
      enableCommentNotify: setting.enableCommentNotify,
      enableReplyNotify: setting.enableReplyNotify,
      enableFriendlinkNotify: setting.enableFriendlinkNotify,
      enablePublishNotify: setting.enablePublishNotify,
      enableSecurityNotify: setting.enableSecurityNotify,
      alertIntervalMinutes: setting.alertIntervalMinutes,
    })
    message.success('通知设置已保存')
  } catch (error) {
    console.error('保存通知设置失败:', error)
  } finally {
    saving.value = false
  }
}

// ==================== 通知日志 ====================
const logLoading = ref(false)
const logs = ref<NotifyLog[]>([])
const logTotal = ref(0)
const logQuery = reactive({ pageNum: 1, pageSize: 10, type: undefined as string | undefined, status: undefined as number | undefined })

const typeLabels: Record<string, string> = {
  comment: '新评论',
  reply: '评论回复',
  friendlink: '友链申请',
  publish: '定时发布',
  security: '安全告警',
}

const typeColors: Record<string, string> = {
  comment: 'blue',
  reply: 'cyan',
  friendlink: 'purple',
  publish: 'green',
  security: 'red',
}

const loadLogs = async () => {
  logLoading.value = true
  try {
    const { data } = await getNotifyLogList({
      pageNum: logQuery.pageNum,
      pageSize: logQuery.pageSize,
      type: logQuery.type,
      status: logQuery.status,
    })
    logs.value = data.rows || []
    logTotal.value = data.total || 0
  } catch (error) {
    console.error('加载通知日志失败:', error)
  } finally {
    logLoading.value = false
  }
}

const handleLogSearch = () => {
  logQuery.pageNum = 1
  loadLogs()
}

const handleLogReset = () => {
  logQuery.type = undefined
  logQuery.status = undefined
  handleLogSearch()
}

const handleLogTableChange = (pagination: { current?: number; pageSize?: number }) => {
  logQuery.pageNum = pagination.current || 1
  logQuery.pageSize = pagination.pageSize || 10
  loadLogs()
}

const logColumns = [
  { title: '类型', key: 'type', width: 100 },
  { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
  { title: '收件人', dataIndex: 'recipient', key: 'recipient', width: 200, ellipsis: true },
  { title: '状态', key: 'status', width: 90 },
  { title: '失败原因', dataIndex: 'errorMsg', key: 'errorMsg', ellipsis: true },
  { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
]

onMounted(() => {
  loadSetting()
  loadLogs()
})
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center shadow-sm">
          <BellOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">通知设置</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">配置站点事件邮件通知与查看发送记录</p>
        </div>
      </div>
    </div>

    <a-tabs default-active-key="setting">
      <!-- 设置 Tab -->
      <a-tab-pane key="setting" tab="通知设置">
        <a-spin :spinning="settingLoading">
          <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm p-6 max-w-2xl">
            <div class="space-y-5">
              <div class="flex items-center justify-between">
                <div>
                  <div class="text-sm font-medium text-gray-700 dark:text-dark-text-primary">新评论通知</div>
                  <div class="text-xs text-gray-400 dark:text-dark-text-muted mt-0.5">收到新评论时邮件通知站长</div>
                </div>
                <a-switch v-model:checked="setting.enableCommentNotify" :checkedValue="1" :unCheckedValue="0" />
              </div>
              <div class="flex items-center justify-between">
                <div>
                  <div class="text-sm font-medium text-gray-700 dark:text-dark-text-primary">评论回复通知</div>
                  <div class="text-xs text-gray-400 dark:text-dark-text-muted mt-0.5">访客评论被回复时邮件通知对方（仅对方留过邮箱）</div>
                </div>
                <a-switch v-model:checked="setting.enableReplyNotify" :checkedValue="1" :unCheckedValue="0" />
              </div>
              <div class="flex items-center justify-between">
                <div>
                  <div class="text-sm font-medium text-gray-700 dark:text-dark-text-primary">友链申请通知</div>
                  <div class="text-xs text-gray-400 dark:text-dark-text-muted mt-0.5">收到新友链申请时邮件通知站长</div>
                </div>
                <a-switch v-model:checked="setting.enableFriendlinkNotify" :checkedValue="1" :unCheckedValue="0" />
              </div>
              <div class="flex items-center justify-between">
                <div>
                  <div class="text-sm font-medium text-gray-700 dark:text-dark-text-primary">定时发布通知</div>
                  <div class="text-xs text-gray-400 dark:text-dark-text-muted mt-0.5">文章定时发布成功或失败时邮件通知站长</div>
                </div>
                <a-switch v-model:checked="setting.enablePublishNotify" :checkedValue="1" :unCheckedValue="0" />
              </div>
              <div class="flex items-center justify-between">
                <div>
                  <div class="text-sm font-medium text-gray-700 dark:text-dark-text-primary">安全告警通知</div>
                  <div class="text-xs text-gray-400 dark:text-dark-text-muted mt-0.5">账号在新 IP 登录时邮件告警（受下方频控限制）</div>
                </div>
                <a-switch v-model:checked="setting.enableSecurityNotify" :checkedValue="1" :unCheckedValue="0" />
              </div>

              <a-divider class="!my-4" />

              <div class="flex items-center justify-between gap-4">
                <div class="shrink-0">
                  <div class="text-sm font-medium text-gray-700 dark:text-dark-text-primary">站长收件邮箱</div>
                  <div class="text-xs text-gray-400 dark:text-dark-text-muted mt-0.5">留空则使用系统配置的管理员邮箱</div>
                </div>
                <a-input v-model:value="setting.adminEmail" placeholder="留空使用默认邮箱" class="!w-64" allow-clear>
                  <template #prefix><MailOutlined class="text-gray-400" /></template>
                </a-input>
              </div>
              <div class="flex items-center justify-between gap-4">
                <div class="shrink-0">
                  <div class="text-sm font-medium text-gray-700 dark:text-dark-text-primary">安全告警最小间隔（分钟）</div>
                  <div class="text-xs text-gray-400 dark:text-dark-text-muted mt-0.5">同类告警在此间隔内只发送一封，防止邮件轰炸</div>
                </div>
                <a-input-number v-model:value="setting.alertIntervalMinutes" :min="1" :max="1440" class="!w-32" />
              </div>

              <div class="flex justify-end pt-2">
                <a-button type="primary" :loading="saving" @click="handleSave" class="!rounded-lg">保存设置</a-button>
              </div>
            </div>
          </div>
        </a-spin>
      </a-tab-pane>

      <!-- 日志 Tab -->
      <a-tab-pane key="logs" tab="通知日志">
        <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
          <div class="flex flex-wrap gap-3 items-center">
            <a-select
              v-model:value="logQuery.type"
              placeholder="通知类型"
              allow-clear
              class="!w-36"
              :options="[
                { value: 'comment', label: '新评论' },
                { value: 'reply', label: '评论回复' },
                { value: 'friendlink', label: '友链申请' },
                { value: 'publish', label: '定时发布' },
                { value: 'security', label: '安全告警' },
              ]"
            />
            <a-select
              v-model:value="logQuery.status"
              placeholder="发送状态"
              allow-clear
              class="!w-32"
              :options="[
                { value: 1, label: '成功' },
                { value: 0, label: '失败' },
              ]"
            />
            <a-button type="primary" @click="handleLogSearch" class="!rounded-lg">搜索</a-button>
            <a-button @click="handleLogReset" class="!rounded-lg">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </div>
        </div>

        <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm overflow-hidden">
          <a-table
            :columns="logColumns"
            :data-source="logs"
            :loading="logLoading"
            :pagination="{
              current: logQuery.pageNum,
              pageSize: logQuery.pageSize,
              total: logTotal,
              showSizeChanger: true,
              showTotal: (total: number) => `共 ${total} 条`,
            }"
            row-key="id"
            @change="handleLogTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'type'">
                <a-tag :color="typeColors[record.type as string] || 'default'">
                  {{ typeLabels[record.type as string] || record.type }}
                </a-tag>
              </template>
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">
                  {{ record.status === 1 ? '成功' : '失败' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'errorMsg'">
                <span class="text-xs text-gray-400 dark:text-dark-text-muted">{{ record.errorMsg || '-' }}</span>
              </template>
            </template>
          </a-table>
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>
