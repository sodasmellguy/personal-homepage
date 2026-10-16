<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getProfile, updateProfile } from '@/api/profile'
import type { Profile } from '@/api/profile'
import {
  SaveOutlined,
  UserOutlined,
  BankOutlined,
  MailOutlined,
  WechatOutlined,
  LinkOutlined,
  GithubOutlined,
  AimOutlined,
  CameraOutlined,
} from '@ant-design/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const loading = ref(false)
const saving = ref(false)
const formRef = ref()
const profile = ref<Partial<Profile>>({})

onMounted(async () => {
  loading.value = true
  try {
    const { data } = await getProfile()
    profile.value = data
  } catch (error) {
    console.error('获取个人信息失败:', error)
  } finally {
    loading.value = false
  }
})

const handleSubmit = async () => {
  saving.value = true
  try {
    await updateProfile(profile.value)
    message.success('保存成功')
  } catch (error) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-5">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-500 to-violet-500 flex items-center justify-center shadow-sm">
          <UserOutlined class="text-white text-sm" />
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800 dark:text-dark-text-primary">个人信息管理</h1>
          <p class="text-xs text-gray-400 dark:text-dark-text-muted">管理你的个人资料和公开展示信息</p>
        </div>
      </div>
      <a-button type="primary" :loading="saving" @click="handleSubmit" class="!rounded-lg !shadow-sm">
        <template #icon><SaveOutlined /></template>
        保存修改
      </a-button>
    </div>

    <a-spin :spinning="loading">
      <!-- 用户信息预览区 -->
      <div class="bg-white dark:bg-dark-card rounded-2xl border border-gray-100 dark:border-dark-border shadow-sm hover:shadow-md transition-shadow duration-300 mb-5 p-5 sm:p-6">
        <div class="flex items-center gap-4">
          <!-- 头像 -->
          <a-avatar
              :size="72"
              :src="profile.avatar"
              class="!ring-2 !ring-gray-50 dark:!ring-dark-border !shadow-sm flex-shrink-0"
          >
            {{ profile.name?.charAt(0) || 'U' }}
          </a-avatar>

          <!-- 姓名与岗位 -->
          <div class="min-w-0 flex-1">
            <h2 class="text-lg font-bold text-gray-900 dark:text-dark-text-primary truncate">
              {{ profile.name || '未设置姓名' }}
            </h2>
            <p class="text-sm text-gray-500 dark:text-dark-text-secondary truncate mt-0.5 flex items-center gap-1.5">
              <!-- 岗位图标 -->
              <svg class="w-3.5 h-3.5 flex-shrink-0 text-gray-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
              </svg>
              <span class="truncate">{{ profile.careerGoal || '未设置职业目标' }}</span>
            </p>
          </div>
        </div>
      </div>


      <!-- 基本信息表单 -->
      <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-5">
        <div class="px-6 py-4 border-b border-gray-100 dark:border-dark-border flex items-center gap-2">
          <UserOutlined class="text-blue-500" />
          <h3 class="text-sm font-semibold text-gray-700 dark:text-dark-text-primary">基本信息</h3>
        </div>
        <div class="p-6">
          <a-form
            ref="formRef"
            :model="profile"
            layout="vertical"
          >
            <a-row :gutter="20">
              <a-col :xs="24" :sm="12">
                <a-form-item label="姓名" name="name">
                  <a-input v-model:value="profile.name" placeholder="请输入姓名">
                    <template #prefix><UserOutlined class="text-gray-400" /></template>
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="12">
                <a-form-item label="职业目标" name="careerGoal">
                  <a-input v-model:value="profile.careerGoal" placeholder="请输入职业目标">
                    <template #prefix><AimOutlined class="text-gray-400" /></template>
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="12">
                <a-form-item label="学校" name="university">
                  <a-input v-model:value="profile.university" placeholder="请输入学校">
                    <template #prefix><BankOutlined class="text-gray-400" /></template>
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="12">
                <a-form-item label="专业" name="major">
                  <a-input v-model:value="profile.major" placeholder="请输入专业">
                    <template #prefix><UserOutlined class="text-gray-400" /></template>
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="12">
                <a-form-item label="邮箱" name="email">
                  <a-input v-model:value="profile.email" placeholder="请输入邮箱">
                    <template #prefix><MailOutlined class="text-gray-400" /></template>
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="12">
                <a-form-item label="微信号" name="wechat">
                  <a-input v-model:value="profile.wechat" placeholder="请输入微信号">
                    <template #prefix><WechatOutlined class="text-gray-400" /></template>
                  </a-input>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>
      </div>

      <!-- 头像上传 -->
      <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-5">
        <div class="px-6 py-4 border-b border-gray-100 dark:border-dark-border flex items-center gap-2">
          <CameraOutlined class="text-violet-500" />
          <h3 class="text-sm font-semibold text-gray-700 dark:text-dark-text-primary">头像设置</h3>
        </div>
        <div class="p-6">
          <a-form :model="profile" layout="vertical">
            <a-form-item label="头像" name="avatar">
              <ImageUpload v-model="profile.avatar" />
            </a-form-item>
          </a-form>
        </div>
      </div>

      <!-- 个人介绍 -->
      <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm mb-5">
        <div class="px-6 py-4 border-b border-gray-100 dark:border-dark-border flex items-center gap-2">
          <BankOutlined class="text-emerald-500" />
          <h3 class="text-sm font-semibold text-gray-700 dark:text-dark-text-primary">个人介绍</h3>
        </div>
        <div class="p-6">
          <a-form :model="profile" layout="vertical">
            <a-form-item label="个人介绍" name="introduction">
              <MdEditor
                v-model="profile.introduction"
                :preview="true"
                :theme="appStore.isDark ? 'dark' : 'light'"
                :toolbarsExclude="['github', 'htmlPreview', 'fullscreen']"
                style="height: 300px"
              />
            </a-form-item>
          </a-form>
        </div>
      </div>

      <!-- 社交链接 -->
      <div class="bg-white dark:bg-dark-card rounded-xl border border-gray-100 dark:border-dark-border shadow-sm">
        <div class="px-6 py-4 border-b border-gray-100 dark:border-dark-border flex items-center gap-2">
          <LinkOutlined class="text-blue-500" />
          <h3 class="text-sm font-semibold text-gray-700 dark:text-dark-text-primary">社交链接</h3>
        </div>
        <div class="p-6">
          <a-form :model="profile" layout="vertical">
            <a-row :gutter="20">
              <a-col :xs="24" :sm="12">
                <a-form-item label="Gitee 地址" name="giteeUrl">
                  <a-input v-model:value="profile.giteeUrl" placeholder="请输入 Gitee 地址">
                    <template #prefix><LinkOutlined class="text-gray-400" /></template>
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="12">
                <a-form-item label="GitHub 地址" name="githubUrl">
                  <a-input v-model:value="profile.githubUrl" placeholder="请输入 GitHub 地址">
                    <template #prefix><GithubOutlined class="text-gray-400" /></template>
                  </a-input>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>
      </div>
    </a-spin>
  </div>
</template>
