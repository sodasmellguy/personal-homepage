<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getUserList, createUser, updateUser, deleteUser, resetPassword, getAllRoles, assignUserRoles } from '@/api/system'
import type { User, UserQuery, Role } from '@/api/system'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  KeyOutlined,
  TeamOutlined,
  UserSwitchOutlined,
} from '@ant-design/icons-vue'

// 列表数据
const loading = ref(false)
const dataList = ref<User[]>([])
const total = ref(0)
const queryParams = reactive<UserQuery>({
  pageNum: 1,
  pageSize: 10,
  username: '',
  status: undefined,
})

// 弹窗控制
const modalVisible = ref(false)
const modalTitle = ref('添加用户')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<any>({})

// 重置密码弹窗
const resetPwdVisible = ref(false)
const resetPwdLoading = ref(false)
const resetPwdForm = ref({ userId: 0, password: '' })

// 角色选项与分配角色弹窗
const roleOptions = ref<Role[]>([])
const assignVisible = ref(false)
const assignLoading = ref(false)
const assignForm = ref({ userId: 0, username: '', roleIds: [] as number[] })

const roleSelectOptions = computed(() =>
  roleOptions.value.map((r) => ({
    label: r.status === 1 ? r.roleName : `${r.roleName}（已禁用）`,
    value: r.id,
  }))
)

const fetchRoleOptions = async () => {
  try {
    const { data } = await getAllRoles()
    roleOptions.value = data
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// 表单校验规则
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
}

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getUserList(queryParams)
    dataList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchRoleOptions()
})

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.username = ''
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
  modalTitle.value = '添加用户'
  formData.value = { status: 1, roleIds: [] }
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: User) => {
  modalTitle.value = '编辑用户'
  formData.value = { ...record, password: undefined }
  modalVisible.value = true
}

// 删除
const handleDelete = (record: User) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户 "${record.username}" 吗？`,
    onOk: async () => {
      try {
        await deleteUser(record.id)
        message.success('删除成功')
        fetchData()
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

// 重置密码
const handleResetPwd = (record: User) => {
  resetPwdForm.value = { userId: record.id, password: '' }
  resetPwdVisible.value = true
}

// 分配角色
const handleAssignRoles = (record: User) => {
  assignForm.value = {
    userId: record.id,
    username: record.username,
    roleIds: [...(record.roleIds || [])],
  }
  assignVisible.value = true
}

// 提交分配角色
const handleSubmitAssign = async () => {
  assignLoading.value = true
  try {
    await assignUserRoles(assignForm.value.userId, assignForm.value.roleIds)
    message.success('角色分配成功')
    assignVisible.value = false
    fetchData()
  } catch (error) {
    message.error('角色分配失败')
  } finally {
    assignLoading.value = false
  }
}

// 提交重置密码
const handleSubmitResetPwd = async () => {
  if (!resetPwdForm.value.password) {
    message.warning('请输入新密码')
    return
  }
  resetPwdLoading.value = true
  try {
    await resetPassword(resetPwdForm.value.userId, resetPwdForm.value.password)
    message.success('密码重置成功')
    resetPwdVisible.value = false
  } catch (error) {
    message.error('密码重置失败')
  } finally {
    resetPwdLoading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    formLoading.value = true
    if (formData.value.id) {
      await updateUser(formData.value.id, formData.value)
      message.success('更新成功')
    } else {
      await createUser(formData.value)
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
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '昵称', dataIndex: 'nickname', key: 'nickname' },
  { title: '角色', key: 'roles' },
  { title: '邮箱', dataIndex: 'email', key: 'email', ellipsis: true },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 280, fixed: 'right' as const },
]
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-500 flex items-center justify-center shadow-sm">
          <TeamOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">用户管理</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">管理系统用户和权限</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-4 p-4">
      <a-form layout="inline" :model="queryParams" class="flex flex-wrap gap-3 items-center">
        <a-form-item label="用户名">
          <a-input v-model:value="queryParams.username" placeholder="请输入用户名" allow-clear />
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
              <template #icon>
                <SearchOutlined />
              </template>
              搜索
            </a-button>
            <a-button @click="handleReset" class="!rounded-lg">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 操作栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm">
      <div class="p-4 border-b border-gray-100 dark:border-dark-border flex items-center justify-between">
        <a-space>
          <a-button type="primary" @click="handleAdd" class="!rounded-lg">
            <template #icon>
              <PlusOutlined />
            </template>
            添加用户
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
          <template v-if="column.key === 'roles'">
            <template v-if="record.roleNames && record.roleNames.length">
              <a-tag
                v-for="name in record.roleNames"
                :key="name"
                color="blue"
                class="!rounded-md"
              >
                {{ name }}
              </a-tag>
            </template>
            <span v-else class="text-gray-400">未分配</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                <template #icon>
                  <EditOutlined />
                </template>
                编辑
              </a-button>
              <a-button type="link" size="small" @click="handleAssignRoles(record)">
                <template #icon>
                  <UserSwitchOutlined />
                </template>
                分配角色
              </a-button>
              <a-button type="link" size="small" @click="handleResetPwd(record)">
                <template #icon>
                  <KeyOutlined />
                </template>
                重置密码
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                <template #icon>
                  <DeleteOutlined />
                </template>
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
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="formData.username" placeholder="请输入用户名" :disabled="!!formData.id" />
        </a-form-item>
        <a-form-item v-if="!formData.id" label="密码" name="password">
          <a-input-password v-model:value="formData.password" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item label="昵称" name="nickname">
          <a-input v-model:value="formData.nickname" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formData.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="角色" name="roleIds">
          <a-select
            v-model:value="formData.roleIds"
            mode="multiple"
            :options="roleSelectOptions"
            placeholder="请选择角色（可多选）"
            allow-clear
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 分配角色弹窗 -->
    <a-modal
      v-model:open="assignVisible"
      :title="`分配角色 - ${assignForm.username}`"
      :confirm-loading="assignLoading"
      @ok="handleSubmitAssign"
    >
      <a-form layout="vertical">
        <a-form-item label="角色">
          <a-select
            v-model:value="assignForm.roleIds"
            mode="multiple"
            :options="roleSelectOptions"
            placeholder="请选择角色（可多选）"
            allow-clear
          />
        </a-form-item>
        <p class="text-xs text-gray-400">提示：admin 用户不能移除自己的 admin 角色；保存后将覆盖该用户原有角色。</p>
      </a-form>
    </a-modal>

    <!-- 重置密码弹窗 -->
    <a-modal
      v-model:open="resetPwdVisible"
      title="重置密码"
      :confirm-loading="resetPwdLoading"
      @ok="handleSubmitResetPwd"
    >
      <a-form layout="vertical">
        <a-form-item label="新密码">
          <a-input-password
            v-model:value="resetPwdForm.password"
            placeholder="请输入新密码"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
