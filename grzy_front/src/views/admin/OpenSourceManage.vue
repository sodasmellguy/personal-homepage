<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  GithubOutlined,
  SyncOutlined,
  StarOutlined,
  ForkOutlined,
} from '@ant-design/icons-vue'
import {
  getOpenSourcePage,
  createOpenSource,
  updateOpenSource,
  deleteOpenSource,
  syncOpenSource,
  type OpenSource,
} from '@/api/openSource'
import dayjs from 'dayjs'

// 列表数据
const loading = ref(false)
const dataList = ref<OpenSource[]>([])
const total = ref(0)
const queryParams = reactive<{ pageNum: number; pageSize: number; status?: number }>({
  pageNum: 1,
  pageSize: 10,
  status: undefined,
})

// 弹窗控制
const modalVisible = ref(false)
const modalTitle = ref('添加开源项目')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<Partial<OpenSource>>({})

// 同步中的项目 ID
const syncingId = ref<number | null>(null)

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入项目简介', trigger: 'blur' }],
  repoUrl: [
    { required: true, message: '请输入仓库地址', trigger: 'blur' },
    { pattern: /^https?:\/\/(www\.)?(github\.com|gitee\.com)\/\S+/i, message: '需为 Gitee / GitHub 仓库地址', trigger: 'blur' },
  ],
}

const statusMap: Record<number, { text: string; color: string }> = {
  1: { text: '维护中', color: 'green' },
  0: { text: '已归档', color: 'default' },
}

const formatTime = (t?: string | null) => (t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '从未同步')

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await getOpenSourcePage({
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      status: queryParams.status,
    })
    dataList.value = data.rows || []
    total.value = data.total || 0
  } catch (error) {
    console.error('加载开源项目失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.status = undefined
  handleSearch()
}

const handleTableChange = (pagination: { current?: number; pageSize?: number }) => {
  queryParams.pageNum = pagination.current || 1
  queryParams.pageSize = pagination.pageSize || 10
  loadData()
}

const emptyForm = (): Partial<OpenSource> => ({
  name: '',
  description: '',
  detail: '',
  repoUrl: '',
  demoUrl: '',
  language: '',
  techStack: '',
  license: '',
  stars: 0,
  forks: 0,
  status: 1,
  isTop: 0,
  sort: 0,
})

const handleAdd = () => {
  formData.value = emptyForm()
  modalTitle.value = '添加开源项目'
  modalVisible.value = true
}

const handleEdit = (record: OpenSource) => {
  formData.value = { ...record }
  modalTitle.value = '编辑开源项目'
  modalVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  formLoading.value = true
  try {
    if (formData.value.id) {
      await updateOpenSource(formData.value.id, formData.value)
      message.success('更新成功')
    } else {
      await createOpenSource(formData.value)
      message.success('添加成功')
    }
    modalVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存开源项目失败:', error)
  } finally {
    formLoading.value = false
  }
}

const handleDelete = (record: OpenSource) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除开源项目「${record.name}」吗？`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      await deleteOpenSource(record.id)
      message.success('删除成功')
      loadData()
    },
  })
}

const handleSync = async (record: OpenSource) => {
  syncingId.value = record.id
  try {
    const { data } = await syncOpenSource(record.id)
    message.success(`同步成功：Star ${data?.stars ?? '-'} / Fork ${data?.forks ?? '-'}`)
    loadData()
  } catch (error) {
    console.error('同步失败:', error)
  } finally {
    syncingId.value = null
  }
}

const columns = [
  { title: '项目', key: 'name', width: 220 },
  { title: '仓库', dataIndex: 'repoUrl', key: 'repoUrl', ellipsis: true },
  { title: '语言', dataIndex: 'language', key: 'language', width: 100 },
  { title: 'Star', key: 'stars', width: 80 },
  { title: 'Fork', key: 'forks', width: 80 },
  { title: '状态', key: 'status', width: 90 },
  { title: '最近同步', key: 'syncTime', width: 150 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const },
]

onMounted(loadData)
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-gray-600 to-gray-800 flex items-center justify-center shadow-sm">
          <GithubOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">开源项目</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">管理前台展示的开源项目，支持从 Gitee/GitHub 同步 Star 数据</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
      <div class="flex flex-wrap gap-3 items-center justify-between">
        <div class="flex flex-wrap gap-3 items-center">
          <a-select
            v-model:value="queryParams.status"
            placeholder="状态"
            allow-clear
            class="!w-32"
            :options="[
              { value: 1, label: '维护中' },
              { value: 0, label: '已归档' },
            ]"
          />
          <a-button type="primary" @click="handleSearch" class="!rounded-lg">搜索</a-button>
          <a-button @click="handleReset" class="!rounded-lg">
            <template #icon><SyncOutlined /></template>
            重置
          </a-button>
        </div>
        <a-button type="primary" @click="handleAdd" class="!rounded-lg">
          <template #icon><PlusOutlined /></template>
          添加项目
        </a-button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm overflow-hidden">
      <a-table
        :columns="columns"
        :data-source="dataList"
        :loading="loading"
        :scroll="{ x: 1000 }"
        :pagination="{
          current: queryParams.pageNum,
          pageSize: queryParams.pageSize,
          total: total,
          showSizeChanger: true,
          showTotal: (t: number) => `共 ${t} 条`,
        }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <div class="flex items-center gap-2">
              <span class="font-medium text-gray-700 dark:text-dark-text-primary">{{ record.name }}</span>
              <a-tag v-if="record.isTop === 1" color="blue" class="!text-[10px] !leading-4 !px-1.5">置顶</a-tag>
            </div>
            <div class="text-xs text-gray-400 dark:text-dark-text-muted truncate max-w-[200px]">{{ record.description }}</div>
          </template>
          <template v-if="column.key === 'repoUrl'">
            <a :href="record.repoUrl" target="_blank" rel="noopener noreferrer" class="text-xs text-blue-500 hover:underline break-all">
              {{ record.repoUrl }}
            </a>
          </template>
          <template v-if="column.key === 'stars'">
            <span class="flex items-center gap-1 text-xs text-gray-500 dark:text-dark-text-muted">
              <StarOutlined /> {{ record.stars }}
            </span>
          </template>
          <template v-if="column.key === 'forks'">
            <span class="flex items-center gap-1 text-xs text-gray-500 dark:text-dark-text-muted">
              <ForkOutlined /> {{ record.forks }}
            </span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="statusMap[record.status as number]?.color">
              {{ statusMap[record.status as number]?.text || '未知' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'syncTime'">
            <span class="text-xs text-gray-400 dark:text-dark-text-muted">{{ formatTime(record.syncTime) }}</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space :size="0">
              <a-tooltip title="同步 Star/Fork 数据">
                <a-button
                  type="link"
                  size="small"
                  :loading="syncingId === record.id"
                  @click="handleSync(record)"
                >
                  <template #icon><SyncOutlined /></template>
                  同步
                </a-button>
              </a-tooltip>
              <a-button type="link" size="small" @click="handleEdit(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="formLoading"
      :width="640"
      @ok="handleSubmit"
    >
      <a-form ref="formRef" :model="formData" :rules="rules" layout="vertical" class="mt-4">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-4">
          <a-form-item label="项目名称" name="name">
            <a-input v-model:value="formData.name" placeholder="如 grzy-blog" allow-clear />
          </a-form-item>
          <a-form-item label="仓库地址" name="repoUrl">
            <a-input v-model:value="formData.repoUrl" placeholder="https://github.com/用户名/仓库名" allow-clear />
          </a-form-item>
        </div>
        <a-form-item label="一句话简介" name="description">
          <a-input v-model:value="formData.description" placeholder="展示在卡片上的一句话介绍" allow-clear :maxlength="200" show-count />
        </a-form-item>
        <a-form-item label="详细描述" name="detail">
          <a-textarea v-model:value="formData.detail" placeholder="选填" :rows="3" />
        </a-form-item>
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-x-4">
          <a-form-item label="主语言" name="language">
            <a-input v-model:value="formData.language" placeholder="如 Java" allow-clear />
          </a-form-item>
          <a-form-item label="License" name="license">
            <a-input v-model:value="formData.license" placeholder="如 MIT" allow-clear />
          </a-form-item>
          <a-form-item label="Star" name="stars">
            <a-input-number v-model:value="formData.stars" :min="0" class="!w-full" />
          </a-form-item>
          <a-form-item label="Fork" name="forks">
            <a-input-number v-model:value="formData.forks" :min="0" class="!w-full" />
          </a-form-item>
        </div>
        <a-form-item label="技术栈标签" name="techStack">
          <a-input v-model:value="formData.techStack" placeholder="逗号分隔，如 Spring Boot, Vue3, Redis" allow-clear />
        </a-form-item>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-4">
          <a-form-item label="在线体验地址" name="demoUrl">
            <a-input v-model:value="formData.demoUrl" placeholder="选填" allow-clear />
          </a-form-item>
          <a-form-item label="Logo 地址" name="logo">
            <a-input v-model:value="formData.logo" placeholder="选填，缺省用首字母色块" allow-clear />
          </a-form-item>
        </div>
        <div class="grid grid-cols-3 gap-x-4">
          <a-form-item label="状态" name="status">
            <a-select v-model:value="formData.status">
              <a-select-option :value="1">维护中</a-select-option>
              <a-select-option :value="0">已归档</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="置顶" name="isTop">
            <a-select v-model:value="formData.isTop">
              <a-select-option :value="0">否</a-select-option>
              <a-select-option :value="1">是</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="排序" name="sort">
            <a-input-number v-model:value="formData.sort" :min="0" class="!w-full" />
          </a-form-item>
        </div>
      </a-form>
      <div class="bg-blue-50 dark:bg-blue-500/10 rounded-lg p-3 text-xs text-blue-600 dark:text-blue-400">
        提示：Star / Fork / License 可手动填写，也可在列表中点击「同步」从 Gitee / GitHub 拉取真实数据（每项目 10 分钟限一次）。
      </div>
    </a-modal>
  </div>
</template>
