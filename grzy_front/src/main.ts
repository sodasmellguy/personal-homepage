import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createHead } from '@unhead/vue/client'
import Antd from 'ant-design-vue'
import App from './App.vue'
import router from './router'
import { useAppStore } from './stores/app'
import { initCodeCopy } from './utils/codeCopy'

// 样式导入
import 'ant-design-vue/dist/reset.css'
import './styles/tailwind.css'
import './styles/global.less'

// 创建应用实例
const app = createApp(App)

// 全局错误处理
app.config.errorHandler = (err, _instance, info) => {
  console.error('Vue 全局错误:', err)
  console.error('错误信息:', info)
  // TODO: 接入错误监控平台（如 Sentry）
}

// 全局未捕获 Promise 异常
window.addEventListener('unhandledrejection', (event) => {
  console.error('未捕获的 Promise 异常:', event.reason)
})

// 全局 JS 错误
window.addEventListener('error', (event) => {
  console.error('全局 JS 错误:', event.message, event.filename, event.lineno)
})

// 创建 Pinia 实例
const pinia = createPinia()

// 使用插件
app.use(pinia)
app.use(router)
app.use(Antd)
app.use(createHead())

// 初始化应用设置
const appStore = useAppStore()
appStore.initSettings()

// 挂载应用
app.mount('#app')

// 初始化代码块复制按钮
initCodeCopy()
