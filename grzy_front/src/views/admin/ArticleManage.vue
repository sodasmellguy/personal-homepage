<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  FileTextOutlined,
} from '@ant-design/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import { getArticleList, createArticle, updateArticle, deleteArticle, batchDeleteArticles } from '@/api/article'
import type { Article, ArticleQuery } from '@/api/article'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useAppStore } from '@/stores/app'
import { uploadAny } from '@/api/upload'

const appStore = useAppStore()

// 清除草稿（清除当前编辑文章的本地草稿）
const clearDraft = () => {
  localStorage.removeItem(draftKey())
  message.success('草稿已清除')
}

// 列表数据
const loading = ref(false)
const dataList = ref<Article[]>([])
const total = ref(0)
const queryParams = reactive<ArticleQuery>({
  pageNum: 1,
  pageSize: 10,
  title: '',
  status: undefined,
  category: '',
})

// 弹窗控制
const modalVisible = ref(false)
const modalTitle = ref('添加文章')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<Partial<Article>>({})

// 草稿自动保存（按文章ID独立保存，新文章用 new）
const draftKey = () => `article_draft_${formData.value.id || 'new'}`

let draftTimer: ReturnType<typeof setInterval> | null = null

const startDraftSave = () => {
  stopDraftSave()
  draftTimer = setInterval(() => {
    if (formData.value.content && modalVisible.value) {
      localStorage.setItem(draftKey(), JSON.stringify({
        data: formData.value,
        timestamp: Date.now(),
      }))
    }
  }, 30000)
}

const stopDraftSave = () => {
  if (draftTimer) {
    clearInterval(draftTimer)
    draftTimer = null
  }
}

const loadDraft = (key: string) => {
  const saved = localStorage.getItem(key)
  if (saved) {
    try {
      const { data, timestamp } = JSON.parse(saved)
      if (Date.now() - timestamp < 86400000) {
        return data
      }
    } catch { /* ignore */ }
  }
  return null
}

// 快捷模板
const mdTemplates = [
  { key: 'code', label: '代码块', icon: '{}' },
  { key: 'table', label: '表格', icon: '⊞' },
  { key: 'info', label: '提示框(info)', icon: 'ℹ' },
  { key: 'warning', label: '提示框(warning)', icon: '⚠' },
  { key: 'danger', label: '提示框(danger)', icon: '⛔' },
  { key: 'details', label: '折叠块', icon: '▶' },
  { key: 'imageGallery', label: '图片画廊', icon: '🖼' },
]

const mdTemplateMap: Record<string, string> = {
  code: '\n```javascript\n// 在这里编写代码\n\n```\n',
  table: '\n| 列1 | 列2 | 列3 |\n| --- | --- | --- |\n| 内容 | 内容 | 内容 |\n| 内容 | 内容 | 内容 |\n',
  info: '\n> **ℹ️ 提示**\n> 这里是提示信息\n\n',
  warning: '\n> **⚠️ 注意**\n> 这里是警告信息\n\n',
  danger: '\n> **⛔ 危险**\n> 这里是危险操作提示\n\n',
  details: '\n<details>\n<summary>点击展开查看详情</summary>\n\n这里是折叠内容\n\n</details>\n',
  imageGallery: '\n<div style="display: flex; gap: 8px;">\n  <img src="图片1地址" alt="描述" width="48%" />\n  <img src="图片2地址" alt="描述" width="48%" />\n</div>\n',
}

const insertTemplate = (key: string) => {
  const template = mdTemplateMap[key]
  if (!template || !formData.value.content) {
    formData.value.content = (formData.value.content || '') + template
    return
  }
  formData.value.content += template
}

// 图片拖拽上传
const handleUploadImg = async (files: File[], callback: (urls: string[]) => void) => {
  const urls: string[] = []
  for (const file of files) {
    try {
      const { data } = await uploadAny(file)
      urls.push(data.url)
    } catch {
      message.error('图片上传失败')
    }
  }
  callback(urls)
}

watch(modalVisible, (val) => {
  if (!val) stopDraftSave()
})

// 表单校验规则
const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }],
}

// 分类选项
const categoryOptions = ['前端开发', '后端开发', 'DevOps', '数据库', '工具', '其他']

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getArticleList(queryParams)
    dataList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取文章列表失败:', error)
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
  queryParams.category = ''
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
  modalTitle.value = '添加文章'
  const draft = loadDraft('article_draft_new')
  if (draft && !draft.id) {
    draft.tagList = draft.tags ? draft.tags.split(',') : []
    formData.value = draft
    message.info('已恢复上次编辑的草稿')
  } else {
    formData.value = { sort: 0, status: 1, isTop: 0, viewCount: 0, likeCount: 0, tagList: [] }
  }
  startDraftSave()
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: Article) => {
  modalTitle.value = '编辑文章'
  formData.value = { ...record, tagList: record.tags ? record.tags.split(',') : [] }
  // 恢复该文章的本地草稿（存在未提交的修改时询问）
  const draft = loadDraft(`article_draft_${record.id}`)
  if (draft && draft.content !== formData.value.content) {
    Modal.confirm({
      title: '发现未提交的本地草稿',
      content: '是否恢复上次自动保存的编辑内容？',
      okText: '恢复草稿',
      cancelText: '放弃草稿',
      onOk: () => {
        formData.value = { ...draft, tagList: draft.tags ? draft.tags.split(',') : [] }
        message.info('已恢复本地草稿')
      },
      onCancel: () => localStorage.removeItem(`article_draft_${record.id}`),
    })
  }
  startDraftSave()
  modalVisible.value = true
}

// 删除
const handleDelete = (record: Article) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除文章 "${record.title}" 吗？`,
    onOk: async () => {
      try {
        await deleteArticle(record.id)
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
    // 定时发布必须选择发布时间
    if (formData.value.status === 2 && !formData.value.publishTime) {
      message.warning('定时发布请选择发布时间')
      return
    }
    formLoading.value = true
    const submitData = { ...formData.value }
    if (submitData.tagList && Array.isArray(submitData.tagList)) {
      submitData.tags = submitData.tagList.join(',')
    }
    if (submitData.id) {
      await updateArticle(submitData.id, submitData)
      message.success(submitData.status === 2 ? '已设置定时发布' : '更新成功')
    } else {
      await createArticle(submitData)
      message.success(submitData.status === 2 ? '已设置定时发布' : '添加成功')
    }
    modalVisible.value = false
    localStorage.removeItem(draftKey())
    stopDraftSave()
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    formLoading.value = false
  }
}

// 表格列定义
const columns = [
  { title: '文章标题', dataIndex: 'title', key: 'title', ellipsis: true },
  { title: '分类', dataIndex: 'category', key: 'category', width: 120 },
  { title: '浏览', dataIndex: 'viewCount', key: 'viewCount', width: 80 },
  { title: '评论', dataIndex: 'commentCount', key: 'commentCount', width: 80 },
  { title: '置顶', dataIndex: 'isTop', key: 'isTop', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
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
          <FileTextOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">文章管理</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">管理你的技术博客和文章</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
      <a-form layout="inline" :model="queryParams" class="flex flex-wrap gap-3 items-center">
        <a-form-item label="文章标题">
          <a-input v-model:value="queryParams.title" placeholder="请输入文章标题" allow-clear class="!w-48" />
        </a-form-item>
        <a-form-item label="分类">
          <a-select v-model:value="queryParams.category" placeholder="请选择分类" allow-clear style="width: 140px">
            <a-select-option v-for="cat in categoryOptions" :key="cat" :value="cat">{{ cat }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option :value="1">已发布</a-select-option>
            <a-select-option :value="0">草稿</a-select-option>
            <a-select-option :value="2">定时发布</a-select-option>
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
            添加文章
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
            showTotal: (t: number) => `共 ${t} 条`,
          }"
          row-key="id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'isTop'">
              <a-tag :color="record.isTop === 1 ? 'red' : 'default'" class="!rounded-md">
                {{ record.isTop === 1 ? '置顶' : '否' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag
                :color="record.status === 1 ? 'green' : record.status === 2 ? 'blue' : 'orange'"
                class="!rounded-md"
              >
                {{ record.status === 1 ? '已发布' : record.status === 2 ? '定时发布' : '草稿' }}
              </a-tag>
              <div
                v-if="record.status === 2 && record.publishTime"
                class="text-xs text-gray-400 dark:text-dark-text-muted mt-1"
              >
                {{ record.publishTime?.replace('T', ' ')?.substring(0, 16) }}
              </div>
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
    </div>

    <!-- 添加/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="formLoading"
      width="1000px"
      @ok="handleSubmit"
    >
      <a-form ref="formRef" :model="formData" :rules="rules" layout="vertical">
        <a-form-item label="文章标题" name="title">
          <a-input v-model:value="formData.title" placeholder="请输入文章标题" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="分类" name="category">
              <a-select v-model:value="formData.category" placeholder="请选择分类" allow-clear>
                <a-select-option v-for="cat in categoryOptions" :key="cat" :value="cat">{{ cat }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="标签" name="tags">
              <a-select
                v-model:value="formData.tagList"
                mode="tags"
                placeholder="输入标签后回车"
                :token-separators="[',']"
                allow-clear
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="文章摘要" name="summary">
          <a-textarea v-model:value="formData.summary" :rows="2" placeholder="请输入文章摘要" />
        </a-form-item>
        <a-form-item label="封面图" name="coverImage">
          <ImageUpload v-model="formData.coverImage" />
        </a-form-item>
        <a-form-item label="文章内容" name="content">
          <div class="flex items-center gap-1 mb-2 flex-wrap">
            <span class="text-xs text-gray-500 mr-1">快捷模板：</span>
            <a-dropdown v-for="tpl in mdTemplates" :key="tpl.key">
              <a-button size="small" class="!text-xs" @click="insertTemplate(tpl.key)">
                <span class="mr-1">{{ tpl.icon }}</span>{{ tpl.label }}
              </a-button>
            </a-dropdown>
          </div>
          <MdEditor
            v-model="formData.content"
            :theme="appStore.isDark ? 'dark' : 'light'"
            language="zh-CN"
            style="height: 500px"
            preview-theme="github"
            :on-upload-img="handleUploadImg"
          />
          <div v-if="formData.content" class="flex justify-between items-center mt-2 text-xs text-gray-400">
            <span>字数：{{ formData.content.replace(/\s/g, '').length }} | 预估阅读 {{ Math.max(1, Math.ceil(formData.content.replace(/\s/g, '').length / 500)) }} 分钟</span>
            <span class="text-green-500 cursor-pointer" @click="clearDraft">清除草稿</span>
          </div>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="排序" name="sort">
              <a-input-number v-model:value="formData.sort" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="置顶" name="isTop">
              <a-radio-group v-model:value="formData.isTop">
                <a-radio :value="1">是</a-radio>
                <a-radio :value="0">否</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="状态" name="status">
              <a-radio-group v-model:value="formData.status">
                <a-radio :value="1">已发布</a-radio>
                <a-radio :value="0">草稿</a-radio>
                <a-radio :value="2">定时</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item v-if="formData.status === 2" label="发布时间" name="publishTime">
          <a-date-picker
            v-model:value="formData.publishTime"
            show-time
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择到点自动发布的时间"
            style="width: 100%"
          />
          <p class="text-xs text-gray-400 dark:text-dark-text-muted mt-1.5">
            文章将在所选时间自动发布（后台每分钟检查一次）
          </p>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
