<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  CommentOutlined,
} from '@ant-design/icons-vue'
import { getCommentList, auditComment, deleteComment, batchAuditComments } from '@/api/comment'
import type { Comment, CommentQuery } from '@/api/comment'

// 列表数据
const loading = ref(false)
const dataList = ref<Comment[]>([])
const total = ref(0)
const queryParams = reactive<CommentQuery>({
  pageNum: 1,
  pageSize: 10,
  articleId: undefined,
  status: undefined,
})

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getCommentList(queryParams)
    dataList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取评论列表失败:', error)
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
  queryParams.articleId = undefined
  queryParams.status = undefined
  handleSearch()
}

// 分页变化
const handleTableChange = (pagination: any) => {
  queryParams.pageNum = pagination.current
  queryParams.pageSize = pagination.pageSize
  fetchData()
}

// 审核评论
const handleAudit = async (record: Comment, status: number) => {
  try {
    await auditComment(record.id, status)
    message.success(status === 1 ? '审核通过' : '已拒绝')
    fetchData()
  } catch (error) {
    message.error('操作失败')
  }
}

// 删除评论
const handleDelete = (record: Comment) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除该评论吗？`,
    onOk: async () => {
      try {
        await deleteComment(record.id)
        message.success('删除成功')
        fetchData()
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

// 格式化时间
const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 19)
}


// 表格列定义
const columns = [
  { title: '昵称', dataIndex: 'nickname', key: 'nickname', width: 120 },
  { title: '评论内容', dataIndex: 'content', key: 'content', ellipsis: true },
  { title: '文章ID', dataIndex: 'articleId', key: 'articleId', width: 80 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '评论时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 220, fixed: 'right' as const },
]
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-indigo-500 to-blue-500 flex items-center justify-center shadow-sm">
          <CommentOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">评论管理</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">审核和管理文章评论</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
      <a-form layout="inline" :model="queryParams" class="flex flex-wrap gap-3 items-center">
        <a-form-item label="文章ID">
          <a-input-number v-model:value="queryParams.articleId" placeholder="文章ID" style="width: 120px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option :value="1">已通过</a-select-option>
            <a-select-option :value="0">待审核</a-select-option>
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
          total: total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (t: number) => `共 ${t} 条`,
        }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'content'">
            <a-tooltip :title="record.content">
              <span class="line-clamp-2">{{ record.content }}</span>
            </a-tooltip>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'orange'">
              <template #icon>
                <CheckCircleOutlined v-if="record.status === 1" />
                <ExclamationCircleOutlined v-else />
              </template>
              {{ record.status === 1 ? '已通过' : '待审核' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'createTime'">
            {{ formatTime(record.createTime) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.status === 0"
                type="link"
                size="small"
                @click="handleAudit(record, 1)"
              >
                <template #icon><CheckCircleOutlined /></template>
                通过
              </a-button>
              <a-button
                v-if="record.status === 1"
                type="link"
                size="small"
                @click="handleAudit(record, 0)"
              >
                <template #icon><CloseCircleOutlined /></template>
                拒绝
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
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
