<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  CameraOutlined,
  FolderOutlined,
} from '@ant-design/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  getAlbumPhotoPage,
  createAlbumPhoto,
  updateAlbumPhoto,
  deleteAlbumPhoto,
  getAlbumGroups,
  createAlbumGroup,
  updateAlbumGroup,
  deleteAlbumGroup,
  type AlbumPhoto,
  type AlbumGroup,
} from '@/api/album'

// 列表数据
const loading = ref(false)
const dataList = ref<AlbumPhoto[]>([])
const total = ref(0)
const queryParams = reactive<{ pageNum: number; pageSize: number; title: string; status?: number; groupId?: number }>({
  pageNum: 1,
  pageSize: 12,
  title: '',
  status: undefined,
  groupId: undefined,
})

// 分组数据
const groups = ref<AlbumGroup[]>([])
const groupNameMap = computed(() => {
  const map: Record<number, string> = {}
  groups.value.forEach((g) => {
    if (g.id) map[g.id] = g.name
  })
  return map
})

const fetchGroups = async () => {
  try {
    const { data } = await getAlbumGroups()
    groups.value = data || []
  } catch (error) {
    console.error('获取分组失败:', error)
  }
}

// 分组管理弹窗
const groupModalVisible = ref(false)
const groupSubmitting = ref(false)
const groupForm = ref<Partial<AlbumGroup>>({})

const openGroupModal = () => {
  groupForm.value = {}
  fetchGroups()
  groupModalVisible.value = true
}

const handleGroupEdit = (group: AlbumGroup) => {
  groupForm.value = { ...group }
}

const handleGroupSubmit = async () => {
  if (!groupForm.value.name?.trim()) {
    message.warning('请输入分组名称')
    return
  }
  groupSubmitting.value = true
  try {
    if (groupForm.value.id) {
      await updateAlbumGroup(groupForm.value.id, groupForm.value)
      message.success('分组已更新')
    } else {
      await createAlbumGroup(groupForm.value)
      message.success('分组已创建')
    }
    groupForm.value = {}
    fetchGroups()
  } catch (error) {
    console.error('保存分组失败:', error)
  } finally {
    groupSubmitting.value = false
  }
}

const handleGroupDelete = (group: AlbumGroup) => {
  Modal.confirm({
    title: '确认删除分组',
    content: `删除分组 "${group.name}" 后，分组下的照片将变为未分组，确认删除吗？`,
    onOk: async () => {
      try {
        await deleteAlbumGroup(group.id!)
        message.success('分组已删除')
        fetchGroups()
        fetchData()
      } catch (error) {
        console.error('删除分组失败:', error)
      }
    },
  })
}

// 弹窗控制
const modalVisible = ref(false)
const modalTitle = ref('上传照片')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<Partial<AlbumPhoto>>({})

// 表单校验规则
const rules = {
  image: [{ required: true, message: '请上传照片', trigger: 'change' }],
}

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getAlbumPhotoPage(queryParams)
    dataList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取照片列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchGroups()
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
  modalTitle.value = '上传照片'
  formData.value = { sort: 0, status: 1, groupId: null }
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: AlbumPhoto) => {
  modalTitle.value = '编辑照片'
  formData.value = { ...record }
  modalVisible.value = true
}

// 删除
const handleDelete = (record: AlbumPhoto) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除照片 "${record.title || record.image}" 吗？`,
    onOk: async () => {
      try {
        await deleteAlbumPhoto(record.id!)
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
      await updateAlbumPhoto(formData.value.id, formData.value)
      message.success('更新成功')
    } else {
      const { data: saved } = await createAlbumPhoto(formData.value)
      // 回填解析出的 EXIF，方便确认
      if (saved) formData.value = { ...formData.value, ...saved }
      message.success(saved?.camera ? '上传成功，已自动解析拍摄参数' : '上传成功')
    }
    modalVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    formLoading.value = false
  }
}

// 切换显示/隐藏（滑动开关，可逆操作不弹确认框）
const handleToggleStatus = async (record: AlbumPhoto, checked: boolean) => {
  try {
    await updateAlbumPhoto(record.id!, { status: checked ? 1 : 0 })
    message.success(checked ? '已显示' : '已隐藏')
    fetchData()
  } catch (error) {
    console.error('切换状态失败:', error)
    fetchData() // 失败时回滚开关状态
  }
}

// 弹窗中的显示开关（与 formData.status 双向映射）
const statusSwitch = computed({
  get: () => formData.value.status === 1,
  set: (v: boolean) => {
    formData.value.status = v ? 1 : 0
  },
})

// 表格列定义
const columns = [
  { title: '预览', key: 'image', width: 120 },
  { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
  { title: '分组', key: 'group', width: 120 },
  { title: '地点', dataIndex: 'location', key: 'location', width: 130 },
  { title: '拍摄参数', key: 'exif', width: 200, ellipsis: true },
  { title: '拍摄日期', dataIndex: 'shootDate', key: 'shootDate', width: 110 },
  { title: '显示', key: 'status', width: 100 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const },
]
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-pink-500 to-rose-500 flex items-center justify-center shadow-sm">
          <CameraOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">相册管理</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">管理前台相册页展示的照片</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
      <a-form layout="inline" :model="queryParams" class="flex flex-wrap gap-3 items-center">
        <a-form-item label="标题">
          <a-input v-model:value="queryParams.title" placeholder="请输入照片标题" allow-clear class="!w-48" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option :value="1">显示</a-select-option>
            <a-select-option :value="0">隐藏</a-select-option>
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
            上传照片
          </a-button>
          <a-button @click="openGroupModal" class="!rounded-lg">
            <template #icon><FolderOutlined /></template>
            分组管理
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
            <template v-if="column.key === 'image'">
              <img
                :src="record.image"
                :alt="record.title || '照片'"
                class="w-20 h-14 object-cover rounded-lg border border-gray-100 dark:border-dark-border"
              />
            </template>
            <template v-if="column.key === 'group'">
              <a-tag v-if="record.groupId && groupNameMap[record.groupId]" color="blue" class="!rounded-md">
                {{ groupNameMap[record.groupId] }}
              </a-tag>
              <span v-else class="text-gray-300 dark:text-dark-text-muted text-xs">未分组</span>
            </template>
            <template v-if="column.key === 'exif'">
              <span v-if="record.camera || record.aperture" class="text-xs text-gray-500 dark:text-dark-text-muted">
                {{ [record.camera, record.aperture, record.shutter, record.iso ? 'ISO' + record.iso : '', record.focalLength].filter(Boolean).join(' · ') }}
              </span>
              <span v-else class="text-gray-300 dark:text-dark-text-muted text-xs">无EXIF</span>
            </template>
            <template v-if="column.key === 'status'">
              <a-switch
                :checked="record.status === 1"
                checked-children="显示"
                un-checked-children="隐藏"
                @change="(checked: boolean) => handleToggleStatus(record, checked)"
              />
            </template>
            <template v-if="column.key === 'shootDate'">
              {{ record.shootDate?.substring(0, 10) }}
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

    <!-- 上传/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="formLoading"
      @ok="handleSubmit"
    >
      <a-form ref="formRef" :model="formData" :rules="rules" layout="vertical">
        <a-form-item label="照片" name="image">
          <ImageUpload v-model="formData.image" />
        </a-form-item>
        <a-form-item label="标题" name="title">
          <a-input v-model:value="formData.title" placeholder="照片标题（可选）" :maxlength="128" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formData.description" placeholder="照片背后的故事（可选）" :rows="3" :maxlength="500" show-count />
        </a-form-item>
        <a-form-item label="所属分组" name="groupId">
          <a-select
            v-model:value="formData.groupId"
            placeholder="未分组"
            allow-clear
            style="width: 100%"
            :options="groups.map((g) => ({ value: g.id, label: g.name }))"
          />
        </a-form-item>
        <a-form-item>
          <a-collapse ghost>
            <a-collapse-panel key="exif" header="拍摄参数 EXIF（上传后自动解析，可手动修改）">
              <a-row :gutter="12">
                <a-col :span="12">
                  <a-form-item label="相机" name="camera">
                    <a-input v-model:value="formData.camera" placeholder="如 iPhone 15 Pro" :maxlength="128" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="光圈" name="aperture">
                    <a-input v-model:value="formData.aperture" placeholder="如 f/2.8" :maxlength="32" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="快门" name="shutter">
                    <a-input v-model:value="formData.shutter" placeholder="如 1/250s" :maxlength="32" />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="ISO" name="iso">
                    <a-input v-model:value="formData.iso" placeholder="如 400" :maxlength="16" />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="焦距" name="focalLength">
                    <a-input v-model:value="formData.focalLength" placeholder="如 35mm" :maxlength="32" />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-collapse-panel>
          </a-collapse>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="拍摄地点" name="location">
              <a-input v-model:value="formData.location" placeholder="如：杭州西湖（可选）" :maxlength="128" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="拍摄日期" name="shootDate">
              <a-date-picker
                v-model:value="formData.shootDate"
                value-format="YYYY-MM-DD"
                placeholder="选择拍摄日期（可选）"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="对外显示" name="status">
              <div class="flex items-center gap-2">
                <a-switch v-model:checked="statusSwitch" checked-children="显示" un-checked-children="隐藏" />
                <span class="text-xs text-gray-400 dark:text-dark-text-muted">关闭后前台不展示该照片</span>
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="排序" name="sort">
              <a-input-number v-model:value="formData.sort" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 分组管理弹窗 -->
    <a-modal
      v-model:open="groupModalVisible"
      title="分组管理"
      :footer="null"
      width="560px"
    >
      <!-- 新建/编辑分组 -->
      <div class="flex gap-2 mb-4">
        <a-input v-model:value="groupForm.name" placeholder="分组名称，如：2026 毕业旅行" :maxlength="64" @pressEnter="handleGroupSubmit" />
        <a-button type="primary" :loading="groupSubmitting" @click="handleGroupSubmit">
          <template #icon><PlusOutlined /></template>
          {{ groupForm.id ? '保存' : '新建' }}
        </a-button>
      </div>
      <a-input v-if="groupForm.id || groupForm.name" v-model:value="groupForm.description" placeholder="分组描述（可选）" class="!mb-4" :maxlength="255" />

      <!-- 分组列表 -->
      <div class="space-y-2 max-h-80 overflow-y-auto">
        <div
          v-for="group in groups"
          :key="group.id"
          class="flex items-center gap-2 p-2.5 rounded-lg border border-gray-100 dark:border-dark-border hover:bg-gray-50 dark:hover:bg-dark-elevated/50 transition-colors"
          :class="{ '!border-blue-300 dark:!border-blue-700': groupForm.id === group.id }"
        >
          <FolderOutlined class="text-blue-400 flex-shrink-0" />
          <div class="flex-1 min-w-0">
            <div class="text-sm font-medium text-gray-700 dark:text-dark-text-secondary truncate">{{ group.name }}</div>
            <div v-if="group.description" class="text-xs text-gray-400 dark:text-dark-text-muted truncate">{{ group.description }}</div>
          </div>
          <a-button type="link" size="small" @click="handleGroupEdit(group)">
            <template #icon><EditOutlined /></template>
          </a-button>
          <a-button type="link" size="small" danger @click="handleGroupDelete(group)">
            <template #icon><DeleteOutlined /></template>
          </a-button>
        </div>
        <a-empty v-if="groups.length === 0" :image="false" description="暂无分组" class="!my-4" />
      </div>
      <p class="text-xs text-gray-400 dark:text-dark-text-muted mt-3">
        提示：删除分组不会删除照片，照片会回归「未分组」
      </p>
    </a-modal>
  </div>
</template>
