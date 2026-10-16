<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getTechStackList, createTechStack, updateTechStack, deleteTechStack } from '@/api/techstack'
import type { TechStack, TechStackQuery } from '@/api/techstack'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  AppstoreOutlined,
} from '@ant-design/icons-vue'

// 列表数据
const loading = ref(false)
const dataList = ref<TechStack[]>([])
const total = ref(0)
const queryParams = reactive<TechStackQuery>({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: undefined,
})

// 弹窗控制
const modalVisible = ref(false)
const modalTitle = ref('添加技术栈')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<Partial<TechStack>>({})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入技术栈名称', trigger: 'blur' }],
  icon: [{ required: true, message: '请上传图标', trigger: 'change' }],
}

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getTechStackList(queryParams)
    dataList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取技术栈列表失败:', error)
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
  modalTitle.value = '添加技术栈'
  formData.value = { sort: 0, status: 1, featured: 0 }
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: TechStack) => {
  modalTitle.value = '编辑技术栈'
  formData.value = { ...record }
  modalVisible.value = true
}

// 删除
const handleDelete = (record: TechStack) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除技术栈 "${record.name}" 吗？`,
    onOk: async () => {
      try {
        await deleteTechStack(record.id)
        message.success('删除成功')
        fetchData()
      } catch (error) {
        message.error('删除失败')
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
      await updateTechStack(formData.value.id, formData.value)
      message.success('更新成功')
    } else {
      await createTechStack(formData.value)
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


// 表格列定义
const columns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '图标', dataIndex: 'icon', key: 'icon', width: 100 },
  { title: '精选', dataIndex: 'featured', key: 'featured', width: 80 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const },
]
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-indigo-500 to-purple-500 flex items-center justify-center shadow-sm">
          <AppstoreOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">技术栈管理</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">管理首页展示的技术栈图标</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
      <a-form layout="inline" :model="queryParams" class="flex flex-wrap gap-3 items-center">
        <a-form-item label="名称">
          <a-input v-model:value="queryParams.name" placeholder="请输入技术栈名称" allow-clear class="!w-48" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
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

    <!-- 操作栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm">
      <div class="p-4 border-b border-gray-100 dark:border-dark-border flex items-center justify-between">
        <a-space>
          <a-button type="primary" @click="handleAdd" class="!rounded-lg">
            <template #icon><PlusOutlined /></template>
            添加技术栈
          </a-button>
        </a-space>
        <span class="text-sm text-gray-400 dark:text-dark-text-muted">共 {{ total }} 条记录</span>
      </div>

      <!-- 表格 -->
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
            <template v-if="column.key === 'icon'">
              <img
                v-if="record.icon"
                :src="record.icon"
                alt="icon"
                class="w-8 h-8 rounded object-contain"
              />
            </template>
            <template v-if="column.key === 'featured'">
              <a-tag :color="record.featured === 1 ? 'blue' : 'default'" class="!rounded-md">
                {{ record.featured === 1 ? '精选' : '普通' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 1 ? 'green' : 'red'" class="!rounded-md">
                {{ record.status === 1 ? '启用' : '禁用' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-space>
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
        <a-form-item label="名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入技术栈名称" />
        </a-form-item>
        <a-form-item label="图标" name="icon">
          <ImageUpload v-model="formData.icon" placeholder="上传技术栈图标" />
        </a-form-item>
        <a-form-item label="精选" name="featured">
          <a-radio-group v-model:value="formData.featured">
            <a-radio :value="1">精选</a-radio>
            <a-radio :value="0">普通</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="排序" name="sort">
          <a-input-number v-model:value="formData.sort" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
