<script setup lang="ts">
import { computed, ref } from 'vue'
import { SafetyCertificateOutlined } from '@ant-design/icons-vue'

const loading = ref(true)

// 安全说明：不再通过 URL 查询参数传递 Token。
// URL 中的 Token 会泄露到浏览器历史、访问日志和 Referer 头。
// 开发环境下登录态通过同源 Authorization Cookie 传给后端 DocAuthFilter。
const docUrl = '/doc.html'

// 生产环境已通过 application-master.yml 关闭 springdoc/knife4j，
// Nginx 同样拒绝 /doc.html 等路径，iframe 必然 404，直接显示提示页。
const isProd = import.meta.env.PROD

const envTips = computed(() => [
  { title: '启动后端', desc: '使用 dev profile 启动 Spring Boot（默认 8080 端口）' },
  { title: '启动前端', desc: 'npm run dev，访问 localhost:3000 并登录管理员账号' },
  { title: '打开本页', desc: '左侧菜单「API 文档」，Knife4j 文档会以当前登录态加载' },
])

const onIframeLoad = () => {
  loading.value = false
}
</script>

<template>
  <!-- 生产环境：文档已禁用，显示说明页 -->
  <div v-if="isProd" class="h-full flex items-center justify-center p-6">
    <div class="max-w-lg w-full text-center py-12 px-8 rounded-2xl bg-white dark:bg-[#1a2234] border border-gray-100 dark:border-white/10 shadow-sm">
      <div class="w-16 h-16 mx-auto rounded-2xl bg-blue-50 dark:bg-blue-500/10 flex items-center justify-center mb-5">
        <SafetyCertificateOutlined class="text-3xl text-blue-500" />
      </div>
      <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-2">生产环境已禁用 API 文档</h2>
      <p class="text-sm text-gray-500 dark:text-gray-400 leading-6 mb-8">
        为避免接口结构对外泄露，Knife4j / Swagger 仅在本地开发环境开放。
        如需查看或调试接口文档，请在本地运行项目。
      </p>
      <div class="text-left space-y-3">
        <div v-for="(tip, i) in envTips" :key="i" class="flex gap-3 items-start">
          <span class="shrink-0 w-6 h-6 rounded-full bg-blue-500/10 text-blue-500 text-xs flex items-center justify-center font-medium mt-0.5">
            {{ i + 1 }}
          </span>
          <div>
            <div class="text-sm font-medium text-gray-700 dark:text-gray-200">{{ tip.title }}</div>
            <div class="text-xs text-gray-400 dark:text-gray-500 mt-0.5">{{ tip.desc }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 开发环境：iframe 嵌入 Knife4j -->
  <div v-else class="relative h-[calc(110vh-128px)] -m-6">
    <!-- 加载状态 -->
    <div
      v-if="loading"
      class="absolute inset-0 flex items-center justify-center bg-white dark:bg-[#141414] z-10"
    >
      <a-spin size="large" tip="API文档加载中..." />
    </div>
    <iframe
      :src="docUrl"
      class="w-full h-full border-0"
      @load="onIframeLoad"
    />
  </div>
</template>
