<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getInternshipList, createInternship, updateInternship, deleteInternship } from '@/api/internship'
import type { Internship, InternshipQuery } from '@/api/internship'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  SolutionOutlined,
} from '@ant-design/icons-vue'

// 列表数据
const loading = ref(false)
const dataList = ref<Internship[]>([])
const total = ref(0)
const queryParams = reactive<InternshipQuery>({
  pageNum: 1,
  pageSize: 10,
  company: '',
  status: undefined,
})

// 弹窗控制
const modalVisible = ref(false)
const modalTitle = ref('添加实习经历')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<Partial<Internship>>({})

// 表单校验规则
const rules = {
  company: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  position: [{ required: true, message: '请输入职位', trigger: 'blur' }],
}

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getInternshipList(queryParams)
    dataList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取实习列表失败:', error)
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
  queryParams.company = ''
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
  modalTitle.value = '添加实习经历'
  formData.value = { sort: 0, status: 1 }
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: Internship) => {
  modalTitle.value = '编辑实习经历'
  formData.value = { ...record }
  modalVisible.value = true
}

// 删除
const handleDelete = (record: Internship) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除 ${record.company} 的实习经历吗？`,
    onOk: async () => {
      try {
        await deleteInternship(record.id)
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
      await updateInternship(formData.value.id, formData.value)
      message.success('更新成功')
    } else {
      await createInternship(formData.value)
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
  { title: '公司名称', dataIndex: 'company', key: 'company' },
  { title: '职位', dataIndex: 'position', key: 'position' },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 120 },
  { title: '结束日期', dataIndex: 'endDate', key: 'endDate', width: 120 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const },
]
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-pink-500 to-rose-500 flex items-center justify-center shadow-sm">
          <SolutionOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">实习管理</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">管理你的实习经历记录</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
      <a-form layout="inline" :model="queryParams" class="flex flex-wrap gap-3 items-center">
        <a-form-item label="公司名称">
          <a-input v-model:value="queryParams.company" placeholder="请输入公司名称" allow-clear class="!w-48" />
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
        <a-button type="primary" @click="handleAdd" class="!rounded-lg">
          <template #icon><PlusOutlined /></template>
          添加实习
        </a-button>
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
      width="600px"
      @ok="handleSubmit"
    >
      <a-form ref="formRef" :model="formData" :rules="rules" layout="vertical">
        <a-form-item label="公司名称" name="company">
          <a-input v-model:value="formData.company" placeholder="请输入公司名称" />
        </a-form-item>
        <a-form-item label="职位" name="position">
          <a-input v-model:value="formData.position" placeholder="请输入职位" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始日期" name="startDate">
              <a-date-picker v-model:value="formData.startDate" style="width: 100%" value-format="YYYY-MM-DD" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束日期" name="endDate">
              <a-date-picker v-model:value="formData.endDate" style="width: 100%" value-format="YYYY-MM-DD" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="工作描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入工作描述" />
        </a-form-item>
        <a-form-item label="工作成果" name="achievements">
          <a-textarea v-model:value="formData.achievements" :rows="3" placeholder="请输入工作成果，每行一条" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="排序" name="sort">
              <a-input-number v-model:value="formData.sort" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态" name="status">
              <a-radio-group v-model:value="formData.status">
                <a-radio :value="1">启用</a-radio>
                <a-radio :value="0">禁用</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>
