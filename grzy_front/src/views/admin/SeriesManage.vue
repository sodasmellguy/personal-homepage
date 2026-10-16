<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  ReadOutlined,
} from '@ant-design/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import { getSeriesList, createSeries, updateSeries, deleteSeries, getSeriesDetail } from '@/api/series'
import type { Series, SeriesQuery, SeriesFormData } from '@/api/series'
import { getArticleList } from '@/api/article'
import type { Article } from '@/api/article'

// 列表数据
const loading = ref(false)
const dataList = ref<Series[]>([])
const total = ref(0)
const queryParams = reactive<SeriesQuery>({
  pageNum: 1,
  pageSize: 10,
  name: '',
})

// 弹窗控制
const modalVisible = ref(false)
const modalTitle = ref('添加专栏')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<SeriesFormData>({
  name: '',
  description: '',
  coverImage: '',
  sort: 0,
  status: 1,
  articleIds: [],
})

// 文章选项
const articleOptions = ref<Article[]>([])
const articleLoading = ref(false)

// 加载文章列表（用于下拉选择）
const fetchArticleOptions = async () => {
  articleLoading.value = true
  try {
    const { data } = await getArticleList({ pageNum: 1, pageSize: 999 })
    articleOptions.value = data.rows
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    articleLoading.value = false
  }
}

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入专栏名称', trigger: 'blur' }],
}

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getSeriesList(queryParams)
    dataList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取专栏列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchArticleOptions()
})

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.name = ''
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
  modalTitle.value = '添加专栏'
  formData.value = {
    name: '',
    description: '',
    coverImage: '',
    sort: 0,
    status: 1,
    articleIds: [],
  }
  modalVisible.value = true
}

// 编辑
const handleEdit = async (record: Series) => {
  modalTitle.value = '编辑专栏'
  try {
    const { data } = await getSeriesDetail(record.id)
    formData.value = {
      id: data.id,
      name: data.name,
      description: data.description || '',
      coverImage: data.coverImage || '',
      sort: data.sort || 0,
      status: data.status,
      articleIds: data.articles?.map((a) => a.id) || [],
    }
  } catch {
    formData.value = {
      id: record.id,
      name: record.name,
      description: record.description || '',
      coverImage: record.coverImage || '',
      sort: record.sort || 0,
      status: record.status,
      articleIds: [],
    }
  }
  modalVisible.value = true
}

// 删除
const handleDelete = (record: Series) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除专栏 "${record.name}" 吗？`,
    onOk: async () => {
      try {
        await deleteSeries(record.id)
        message.success('删除成功')
        fetchData()
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

// 状态切换
const handleStatusChange = async (record: Series) => {
  const newStatus = record.status === 1 ? 0 : 1
  try {
    await updateSeries(record.id, {
      name: record.name,
      status: newStatus,
    })
    message.success(newStatus === 1 ? '已启用' : '已禁用')
    fetchData()
  } catch (error) {
    message.error('操作失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    formLoading.value = true
    if (formData.value.id) {
      await updateSeries(formData.value.id, formData.value)
      message.success('更新成功')
    } else {
      await createSeries(formData.value)
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
  { title: '专栏名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
  { title: '文章数', dataIndex: 'articleCount', key: 'articleCount', width: 80 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' as const },
]
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center shadow-sm">
          <ReadOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">专栏管理</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">管理文章专栏和系列</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
      <a-form layout="inline" :model="queryParams" class="flex flex-wrap gap-3 items-center">
        <a-form-item label="专栏名称">
          <a-input v-model:value="queryParams.name" placeholder="请输入专栏名称" allow-clear />
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
          添加专栏
        </a-button>
        <span class="text-sm text-gray-400 dark:text-dark-text-muted">共 {{ total }} 条记录</span>
      </div>

      <!-- 表格 -->
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
          showTotal: (t: number) => `共 ${t} 条`,
        }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'description'">
            <a-tooltip :title="record.description" v-if="record.description">
              {{ record.description?.substring(0, 30) }}{{ record.description?.length > 30 ? '...' : '' }}
            </a-tooltip>
            <span v-else class="text-gray-400">-</span>
          </template>
          <template v-if="column.key === 'articleCount'">
            <a-tag color="blue">{{ record.articleCount || 0 }}</a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-switch
              :checked="record.status === 1"
              checked-children="启用"
              un-checked-children="禁用"
              @change="handleStatusChange(record)"
            />
          </template>
          <template v-if="column.key === 'createTime'">
            {{ record.createTime?.replace('T', ' ')?.substring(0, 19) }}
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

    <!-- 添加/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="formLoading"
      width="700px"
      @ok="handleSubmit"
    >
      <a-form ref="formRef" :model="formData" :rules="rules" layout="vertical">
        <a-form-item label="专栏名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入专栏名称" />
        </a-form-item>
        <a-form-item label="专栏描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入专栏描述" />
        </a-form-item>
        <a-form-item label="封面图" name="coverImage">
          <ImageUpload v-model="formData.coverImage" />
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
        <a-form-item label="关联文章" name="articleIds">
          <a-select
            v-model:value="formData.articleIds"
            mode="multiple"
            placeholder="请选择关联文章"
            :loading="articleLoading"
            :options="articleOptions.map((a) => ({ value: a.id, label: a.title }))"
            :filter-option="(input: string, option: any) => option.label.toLowerCase().includes(input.toLowerCase())"
            allow-clear
            show-search
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
