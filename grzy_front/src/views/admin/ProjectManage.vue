<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getProjectList, createProject, updateProject, deleteProject } from '@/api/project'
import type { Project, ProjectQuery } from '@/api/project'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  ProjectOutlined,
} from '@ant-design/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import { uploadAny } from '@/api/upload'

// 列表数据
const loading = ref(false)
const dataList = ref<Project[]>([])
const total = ref(0)
const queryParams = reactive<ProjectQuery>({
  pageNum: 1,
  pageSize: 10,
  title: '',
  status: undefined,
})

// 弹窗控制
const modalVisible = ref(false)
const modalTitle = ref('添加项目')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<Partial<Project>>({})

// 项目截图管理
const imageInputRef = ref<HTMLInputElement>()
const projectImages = ref<string[]>([])

// 打开弹窗时解析 images JSON
const parseImages = () => {
  try {
    if (formData.value.images) {
      projectImages.value = JSON.parse(formData.value.images as string)
    } else {
      projectImages.value = []
    }
  } catch { projectImages.value = [] }
}

// 保存时序列化为 JSON
const serializeImages = () => {
  formData.value.images = projectImages.value.length ? JSON.stringify(projectImages.value) : undefined
}

const moveImage = (idx: number, dir: number) => {
  const newIdx = idx + dir
  if (newIdx < 0 || newIdx >= projectImages.value.length) return
  const arr = [...projectImages.value]
  ;[arr[idx], arr[newIdx]] = [arr[newIdx], arr[idx]]
  projectImages.value = arr
}

const removeImage = (idx: number) => {
  projectImages.value.splice(idx, 1)
}

// 与后端图片白名单一致（不含 svg，防止存储型 XSS）
const IMAGE_MIME_TYPES = [
  'image/jpeg', 'image/pjpeg', 'image/png', 'image/gif',
  'image/webp', 'image/bmp', 'image/x-icon', 'image/vnd.microsoft.icon',
]

const handleImageUpload = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!IMAGE_MIME_TYPES.includes(file.type)) {
    message.error('只能上传 JPG、PNG、GIF、WebP、BMP、ICO 格式图片（不支持 SVG）')
    input.value = ''
    return
  }
  if (projectImages.value.length >= 8) { message.warning('最多上传 8 张截图'); return }
  try {
    const { data } = await uploadAny(file)
    projectImages.value.push(data.url)
  } catch { message.error('上传失败') }
  input.value = ''
}

// 表单校验规则
const rules = {
  title: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入项目描述', trigger: 'blur' }],
}

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getProjectList(queryParams)
    dataList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取项目列表失败:', error)
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
  queryParams.title = ''
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
  modalTitle.value = '添加项目'
  formData.value = { sort: 0, status: 1 }
  parseImages()
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: Project) => {
  modalTitle.value = '编辑项目'
  formData.value = { ...record }
  parseImages()
  modalVisible.value = true
}

// 删除
const handleDelete = (record: Project) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除项目 "${record.title}" 吗？`,
    onOk: async () => {
      try {
        await deleteProject(record.id)
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
    serializeImages()
    if (formData.value.id) {
      await updateProject(formData.value.id, formData.value)
      message.success('更新成功')
    } else {
      await createProject(formData.value)
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
  { title: '项目名称', dataIndex: 'title', key: 'title', width: 200 },
  { title: '技术栈', dataIndex: 'techStack', key: 'techStack', ellipsis: true },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 120 },
  { title: '结束日期', dataIndex: 'endDate', key: 'endDate', width: 120 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const },
]
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-violet-500 to-purple-500 flex items-center justify-center shadow-sm">
          <ProjectOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">项目管理</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">管理你的项目经历和展示</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
      <a-form layout="inline" :model="queryParams" class="flex flex-wrap gap-3 items-center">
        <a-form-item label="项目名称">
          <a-input v-model:value="queryParams.title" placeholder="请输入项目名称" allow-clear class="!w-48" />
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
            添加项目
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
        <a-form-item label="项目名称" name="title">
          <a-input v-model:value="formData.title" placeholder="请输入项目名称" />
        </a-form-item>
        <a-form-item label="项目描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入项目描述" />
        </a-form-item>
        <a-form-item label="封面图" name="coverImage">
          <ImageUpload v-model="formData.coverImage" />
        </a-form-item>
        <a-form-item label="项目截图" name="images">
          <div class="flex flex-wrap gap-3">
            <div
              v-for="(img, idx) in projectImages"
              :key="idx"
              class="relative w-[100px] h-[100px] rounded-xl overflow-hidden group shadow-md hover:shadow-xl transition-all duration-300 cursor-pointer"
            >
              <img :src="img" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" />
              <div class="absolute inset-0 bg-black/0 group-hover:bg-black/40 transition-all duration-300 flex items-center justify-center gap-1 opacity-0 group-hover:opacity-100">
                <button v-if="idx > 0" class="w-6 h-6 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center hover:bg-white/40 transition-colors text-white text-xs" @click="moveImage(idx, -1)">←</button>
                <button v-if="idx < projectImages.length - 1" class="w-6 h-6 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center hover:bg-white/40 transition-colors text-white text-xs" @click="moveImage(idx, 1)">→</button>
                <button class="w-6 h-6 rounded-full bg-red-500/80 backdrop-blur-sm flex items-center justify-center hover:bg-red-500 transition-colors text-white text-xs" @click="removeImage(idx)">✕</button>
              </div>
            </div>
            <input ref="imageInputRef" type="file" accept="image/jpeg,image/png,image/gif,image/webp,image/bmp,image/x-icon" style="position: absolute; width: 0; height: 0; opacity: 0; pointer-events: none; overflow: hidden;" @change="handleImageUpload" />
            <div
              v-if="projectImages.length < 8"
              class="w-[100px] h-[100px] rounded-xl border-2 border-dashed border-gray-200 dark:border-dark-border hover:border-blue-400 dark:hover:border-blue-500 bg-gray-50/30 dark:bg-dark-elevated/20 hover:bg-blue-50/50 dark:hover:bg-blue-900/10 cursor-pointer flex flex-col items-center justify-center gap-1.5 group transition-all duration-300"
              @click="imageInputRef?.click()"
            >
              <div class="w-8 h-8 rounded-lg bg-blue-50 dark:bg-blue-900/20 group-hover:bg-blue-100 dark:group-hover:bg-blue-900/30 flex items-center justify-center transition-all duration-300 group-hover:scale-110">
                <PlusOutlined class="text-sm text-blue-400 group-hover:text-blue-500 transition-colors" />
              </div>
              <span class="text-[11px] text-gray-400 dark:text-dark-text-muted group-hover:text-blue-500 transition-colors font-medium">添加截图</span>
            </div>
          </div>
          <div class="text-[11px] text-gray-300 dark:text-gray-600 mt-2">最多 8 张，支持 JPG/PNG/WebP</div>
        </a-form-item>
        <a-form-item label="技术栈" name="techStack">
          <a-input v-model:value="formData.techStack" placeholder="多个技术用逗号分隔，如：Vue,Spring Boot,MySQL" />
        </a-form-item>
        <a-form-item label="职责描述" name="responsibilities">
          <a-textarea v-model:value="formData.responsibilities" :rows="3" placeholder="请输入职责描述" />
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
        <a-form-item label="项目链接" name="projectUrl">
          <a-input v-model:value="formData.projectUrl" placeholder="请输入项目链接" />
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
