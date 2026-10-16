<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getRoleList, createRole, updateRole, deleteRole, getMenuTree, getRoleMenus, assignRoleMenus } from '@/api/system'
import type { Role, MenuNode } from '@/api/system'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  SafetyOutlined,
  ApartmentOutlined,
} from '@ant-design/icons-vue'

// 列表数据
const loading = ref(false)
const dataList = ref<Role[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
})

// 弹窗控制
const modalVisible = ref(false)
const modalTitle = ref('添加角色')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<any>({})

// 分配权限弹窗
const permVisible = ref(false)
const permLoading = ref(false)
const permSaving = ref(false)
const permTreeData = ref<MenuNode[]>([])
const permCheckedKeys = ref<number[]>([])
const permHalfCheckedKeys = ref<number[]>([])
const permRole = ref<{ id: number; roleName: string }>({ id: 0, roleName: '' })
const treeFieldNames = { children: 'children', title: 'name', key: 'id' }

const handleAssignPerms = async (record: Role) => {
  // admin 角色恒为全部权限，无需分配
  if (record.roleCode === 'admin') {
    message.info('admin 角色拥有全部权限，无需分配')
    return
  }
  permRole.value = { id: record.id, roleName: record.roleName }
  permVisible.value = true
  permLoading.value = true
  try {
    const [treeRes, menusRes] = await Promise.all([getMenuTree(), getRoleMenus(record.id)])
    permTreeData.value = treeRes.data || []
    // 仅勾选叶子菜单，父节点由树组件联动
    const treeIds = new Set<number>()
    const walk = (nodes: MenuNode[]) => {
      nodes.forEach((n) => {
        if (n.children && n.children.length) {
          walk(n.children)
        } else {
          treeIds.add(n.id)
        }
      })
    }
    walk(permTreeData.value)
    permCheckedKeys.value = (menusRes.data || []).filter((id) => treeIds.has(id))
    permHalfCheckedKeys.value = []
  } catch (error) {
    console.error('获取权限数据失败:', error)
    message.error('获取权限数据失败')
  } finally {
    permLoading.value = false
  }
}

const handlePermCheck = (checkedKeys: any, info: any) => {
  permCheckedKeys.value = checkedKeys as number[]
  permHalfCheckedKeys.value = (info?.halfCheckedKeys || []) as number[]
}

const handleSubmitPerms = async () => {
  permSaving.value = true
  try {
    const menuIds = [...new Set([...permHalfCheckedKeys.value, ...permCheckedKeys.value])]
    await assignRoleMenus(permRole.value.id, menuIds)
    message.success('权限分配成功')
    permVisible.value = false
  } catch (error) {
    message.error('权限分配失败')
  } finally {
    permSaving.value = false
  }
}

// 表单校验规则
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
}

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getRoleList(queryParams)
    dataList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取角色列表失败:', error)
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

// 分页变化
const handleTableChange = (pagination: any) => {
  queryParams.pageNum = pagination.current
  queryParams.pageSize = pagination.pageSize
  fetchData()
}

// 添加
const handleAdd = () => {
  modalTitle.value = '添加角色'
  formData.value = { status: 1 }
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: Role) => {
  modalTitle.value = '编辑角色'
  formData.value = { ...record }
  modalVisible.value = true
}

// 删除
const handleDelete = (record: Role) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除角色 "${record.roleName}" 吗？`,
    onOk: async () => {
      try {
        await deleteRole(record.id)
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
      await updateRole(formData.value.id, formData.value)
      message.success('更新成功')
    } else {
      await createRole(formData.value)
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
  { title: '角色名称', dataIndex: 'roleName', key: 'roleName' },
  { title: '角色标识', dataIndex: 'roleCode', key: 'roleCode' },
  { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
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
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-red-500 to-rose-500 flex items-center justify-center shadow-sm">
          <SafetyOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">角色管理</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">管理角色和权限分配</p>
        </div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm">
      <div class="p-4 border-b border-gray-100 dark:border-dark-border flex items-center justify-between">
        <a-space>
          <a-button type="primary" @click="handleAdd" class="!rounded-lg">
            <template #icon>
              <PlusOutlined />
            </template>
            添加角色
          </a-button>
          <a-button @click="handleSearch" class="!rounded-lg">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新
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
                <template #icon>
                  <EditOutlined />
                </template>
                编辑
              </a-button>
              <a-button type="link" size="small" @click="handleAssignPerms(record)">
                <template #icon>
                  <ApartmentOutlined />
                </template>
                分配权限
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
        <a-form-item label="角色名称" name="roleName">
          <a-input v-model:value="formData.roleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item label="角色标识" name="roleCode">
          <a-input v-model:value="formData.roleCode" placeholder="请输入角色标识，如 admin" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formData.description" placeholder="请输入角色描述" :rows="3" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 分配权限弹窗 -->
    <a-modal
      v-model:open="permVisible"
      :title="`分配权限 - ${permRole.roleName}`"
      :confirm-loading="permSaving"
      :ok-button-props="{ disabled: permLoading }"
      @ok="handleSubmitPerms"
    >
      <a-spin :spinning="permLoading">
        <a-tree
          v-if="permTreeData.length"
          checkable
          :tree-data="permTreeData"
          :field-names="treeFieldNames"
          v-model:checkedKeys="permCheckedKeys"
          default-expand-all
          @check="handlePermCheck"
        />
        <a-empty v-else-if="!permLoading" description="暂无菜单数据，请先执行 sql/06_rbac.sql" />
        <p class="text-xs text-gray-400 mt-3">提示：admin 角色拥有全部权限，无需分配；保存后将覆盖该角色原有权限。</p>
      </a-spin>
    </a-modal>
  </div>
</template>
