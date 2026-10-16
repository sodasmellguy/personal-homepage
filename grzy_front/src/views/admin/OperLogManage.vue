<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ClockCircleOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { getOperLogList } from '@/api/operlog'
import type { OperLog, OperLogQuery } from '@/api/operlog'

const loading = ref(false)
const dataList = ref<OperLog[]>([])
const total = ref(0)
const queryParams = reactive<OperLogQuery>({
  pageNum: 1,
  pageSize: 10,
  module: '',
  username: '',
})

const columns = [
  { title: '操作模块', dataIndex: 'module', width: 120, ellipsis: true },
  { title: '操作描述', dataIndex: 'operation', width: 200, ellipsis: true },
  { title: '操作用户', dataIndex: 'username', width: 100, ellipsis: true },
  { title: '请求方式', dataIndex: 'method', width: 90 },
  { title: 'IP 地址', dataIndex: 'ip', width: 130 },
  { title: '状态', dataIndex: 'status', width: 80, key: 'status' },
  { title: '执行时长', dataIndex: 'duration', width: 100, key: 'duration' },
  { title: '操作时间', dataIndex: 'createTime', width: 170, key: 'createTime' },
]

const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getOperLogList(queryParams)
    dataList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取操作日志失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchData()
}

const handleReset = () => {
  queryParams.module = ''
  queryParams.username = ''
  handleSearch()
}

const handleTableChange = (pagination: any) => {
  queryParams.pageNum = pagination.current
  queryParams.pageSize = pagination.pageSize
  fetchData()
}
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-gray-500 to-slate-600 flex items-center justify-center shadow-sm">
          <ClockCircleOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">操作日志</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">查看系统操作记录</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
      <div class="flex flex-wrap gap-3 items-center">
        <a-input
          v-model:value="queryParams.module"
          placeholder="搜索模块"
          allow-clear
          class="!w-48"
        >
          <template #prefix><SearchOutlined class="text-gray-400" /></template>
        </a-input>
        <a-input
          v-model:value="queryParams.username"
          placeholder="搜索操作用户"
          allow-clear
          class="!w-48"
        >
          <template #prefix><SearchOutlined class="text-gray-400" /></template>
        </a-input>
        <a-button type="primary" @click="handleSearch" class="!rounded-lg">
          <template #icon><SearchOutlined /></template>
          搜索
        </a-button>
        <a-button @click="handleReset" class="!rounded-lg">
          <template #icon><ReloadOutlined /></template>
          重置
        </a-button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm">
      <div class="p-4">
      <a-table
        :columns="columns"
        :data-source="dataList"
        :loading="loading"
        :scroll="{ x: 'max-content' }"
        :pagination="{
          current: queryParams.pageNum,
          pageSize: queryParams.pageSize,
          total,
          showSizeChanger: true,
          showTotal: (t: number) => `共 ${t} 条`,
        }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '成功' : '失败' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'duration'">
            <span v-if="record.duration != null" :class="record.duration > 1000 ? 'text-orange-500' : 'text-gray-500'">
              {{ record.duration }}ms
            </span>
            <span v-else class="text-gray-400">-</span>
          </template>
          <template v-else-if="column.key === 'createTime'">
            <span class="text-gray-500 text-sm">{{ record.createTime }}</span>
          </template>
        </template>
      </a-table>
      </div>
    </div>
  </div>
</template>
