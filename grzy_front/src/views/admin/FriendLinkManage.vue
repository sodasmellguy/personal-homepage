<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  LinkOutlined,
  CheckOutlined,
  CloseOutlined,
} from '@ant-design/icons-vue'
import {
  getFriendLinkPage,
  createFriendLink,
  updateFriendLink,
  reviewFriendLink,
  deleteFriendLink,
  type FriendLink,
} from '@/api/friendLink'
import ImageUpload from '@/components/ImageUpload.vue'

// 列表数据
const loading = ref(false)
const dataList = ref<FriendLink[]>([])
const total = ref(0)
const queryParams = reactive<{ pageNum: number; pageSize: number; name: string; status?: number }>({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: undefined,
})

// 弹窗控制
const modalVisible = ref(false)
const modalTitle = ref('添加友链')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<Partial<FriendLink>>({})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入站点名称', trigger: 'blur' }],
  url: [{ required: true, message: '请输入站点链接', trigger: 'blur' }],
}

const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: '待审核', color: 'orange' },
  1: { text: '已通过', color: 'green' },
  2: { text: '已驳回', color: 'red' },
  3: { text: '已停用', color: 'default' },
}

// 状态操作文案
const statusActionText: Record<number, string> = {
  1: '通过',
  2: '驳回',
  3: '停用',
  0: '转为待审核',
}

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getFriendLinkPage(queryParams)
    dataList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取友链列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.name = ''
  queryParams.status = undefined
  handleSearch()
}

// 分页变化
const handleTableChange = (pagination: any) => {
  queryParams.pageNum = pagination.current
  queryParams.pageSize = pagination.pageSize
  fetchData()
}

// 添加
const handleAdd = () => {
  modalTitle.value = '添加友链'
  // 新增默认待审核，需显式「通过」才对外显示
  formData.value = { sort: 0, status: 0 }
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: FriendLink) => {
  modalTitle.value = '编辑友链'
  formData.value = { ...record }
  modalVisible.value = true
}

// 审核状态变更（通过/驳回/停用）
const handleReview = (record: FriendLink, status: number) => {
  const action = statusActionText[status] || '变更'
  const tip = status === 1
    ? (record.status === 1 ? `确定要重新启用友链 "${record.name}" 吗？` : `通过后将在前台友链墙展示 "${record.name}"，确认吗？`)
    : `确定要${action}友链 "${record.name}" 吗？`
  Modal.confirm({
    title: `确认${action}`,
    content: tip,
    onOk: async () => {
      try {
        await reviewFriendLink(record.id!, status)
        message.success(`${action}成功`)
        fetchData()
      } catch (error) {
        console.error(`${action}失败:`, error)
      }
    },
  })
}

// 删除
const handleDelete = (record: FriendLink) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除友链 "${record.name}" 吗？`,
    onOk: async () => {
      try {
        await deleteFriendLink(record.id!)
        message.success('删除成功')
        fetchData()
      } catch (error) {
        console.error('删除失败:', error)
      }
    },
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    formLoading.value = true
    if (formData.value.id) {
      await updateFriendLink(formData.value.id, formData.value)
      message.success('更新成功')
    } else {
      await createFriendLink(formData.value)
      message.success('添加成功')
    }
    modalVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    formLoading.value = false
  }
}

// 切换对外显示（滑动开关，可逆操作不弹确认框）
const handleToggleVisible = async (record: FriendLink, checked: boolean) => {
  try {
    await reviewFriendLink(record.id!, checked ? 1 : 3)
    message.success(checked ? '已启用，前台可见' : '已停用，前台隐藏')
    fetchData()
  } catch (error) {
    console.error('切换状态失败:', error)
    fetchData() // 失败时回滚开关状态
  }
}

// 表格列定义
const columns = [
  { title: '站点名称', dataIndex: 'name', key: 'name' },
  { title: '站点链接', dataIndex: 'url', key: 'url' },
  { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '对外显示', key: 'visible', width: 100 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '申请时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' as const },
]
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-500 to-violet-500 flex items-center justify-center shadow-sm">
          <LinkOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">友链管理</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">管理友情链接，审核访客的友链申请</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
      <a-form layout="inline" :model="queryParams" class="flex flex-wrap gap-3 items-center">
        <a-form-item label="站点名称">
          <a-input v-model:value="queryParams.name" placeholder="请输入站点名称" allow-clear class="!w-48" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">已通过</a-select-option>
            <a-select-option :value="2">已驳回</a-select-option>
            <a-select-option :value="3">已停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch" class="!rounded-lg">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset" class="!rounded-lg">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 操作栏 + 表格 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm">
      <div class="p-4 border-b border-gray-100 dark:border-dark-border flex items-center justify-between">
        <a-space>
          <a-button type="primary" @click="handleAdd" class="!rounded-lg">
            <template #icon><PlusOutlined /></template>
            添加友链
          </a-button>
        </a-space>
        <span class="text-sm text-gray-400 dark:text-dark-text-muted">共 {{ total }} 条记录</span>
      </div>

      <div class="p-4">
        <a-table
          :columns="columns"
          :data-source="dataList"
          :loading="loading"
          :scroll="{ x: 'max-content' }"
          :pagination="{
            current: queryParams.pageNum,
            pageSize: queryParams.pageSize,
            total: total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total: number) => `共 ${total} 条`,
          }"
          row-key="id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'name'">
              <div class="flex items-center gap-2">
                <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-violet-500 flex items-center justify-center flex-shrink-0 overflow-hidden">
                  <img
                    v-if="record.logo"
                    :src="record.logo"
                    :alt="record.name"
                    class="w-full h-full object-cover"
                    @error="($event.target as HTMLImageElement).style.display = 'none'"
                  />
                  <LinkOutlined v-else class="text-white text-xs" />
                </div>
                <span class="font-medium">{{ record.name }}</span>
              </div>
            </template>
            <template v-if="column.key === 'url'">
              <a :href="record.url" target="_blank" rel="noopener noreferrer" class="text-blue-500 hover:underline break-all">
                {{ record.url }}
              </a>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="statusMap[record.status]?.color" class="!rounded-md">
                {{ statusMap[record.status]?.text || '未知' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'visible'">
              <a-tooltip v-if="record.status === 0 || record.status === 2" title="请先审核通过后再启用">
                <a-switch :checked="false" disabled />
              </a-tooltip>
              <a-switch
                v-else
                :checked="record.status === 1"
                checked-children="显示"
                un-checked-children="隐藏"
                @change="(checked: boolean) => handleToggleVisible(record, checked)"
              />
            </template>
            <template v-if="column.key === 'action'">
              <a-space>
                <a-button
                  v-if="record.status === 0"
                  type="link"
                  size="small"
                  @click="handleReview(record, 1)"
                >
                  <template #icon><CheckOutlined /></template>
                  通过
                </a-button>
                <a-button
                  v-if="record.status === 0"
                  type="link"
                  size="small"
                  danger
                  @click="handleReview(record, 2)"
                >
                  <template #icon><CloseOutlined /></template>
                  驳回
                </a-button>
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
    </div>

    <!-- 添加/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="formLoading"
      @ok="handleSubmit"
    >
      <a-form ref="formRef" :model="formData" :rules="rules" layout="vertical">
        <a-form-item label="站点名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入站点名称" :maxlength="64" />
        </a-form-item>
        <a-form-item label="站点链接" name="url">
          <a-input v-model:value="formData.url" placeholder="https://example.com" :maxlength="255" />
        </a-form-item>
        <a-form-item label="站点图标" name="logo">
          <ImageUpload v-model="formData.logo" />
        </a-form-item>
        <a-form-item label="站点描述" name="description">
          <a-textarea v-model:value="formData.description" placeholder="一句话介绍站点（可选）" :rows="3" :maxlength="255" show-count />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="formData.status">
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">已通过（前台显示）</a-select-option>
            <a-select-option :value="2">已驳回</a-select-option>
            <a-select-option :value="3">已停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="排序" name="sort">
          <a-input-number v-model:value="formData.sort" :min="0" class="!w-full" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
