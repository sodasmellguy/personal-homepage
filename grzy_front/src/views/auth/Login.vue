<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, SafetyOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import type { LoginParams } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const formRef = ref()
const canvasRef = ref<HTMLCanvasElement>()
const isDev = import.meta.env.DEV

const form = reactive<LoginParams>({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
}

// 粒子背景
interface Particle { x: number; y: number; vx: number; vy: number; r: number; opacity: number }
let particles: Particle[] = []
let animId = 0

const initParticles = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')!
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  particles = Array.from({ length: 50 }, () => ({
    x: Math.random() * canvas.width,
    y: Math.random() * canvas.height,
    vx: (Math.random() - 0.5) * 0.5,
    vy: (Math.random() - 0.5) * 0.5,
    r: Math.random() * 2 + 1,
    opacity: Math.random() * 0.5 + 0.1,
  }))

  const draw = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    particles.forEach((p) => {
      p.x += p.vx
      p.y += p.vy
      if (p.x < 0 || p.x > canvas.width) p.vx *= -1
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(59, 130, 246, ${p.opacity})`
      ctx.fill()
    })

    // 连线
    for (let i = 0; i < particles.length; i++) {
      for (let j = i + 1; j < particles.length; j++) {
        const dx = particles[i].x - particles[j].x
        const dy = particles[i].y - particles[j].y
        const dist = Math.sqrt(dx * dx + dy * dy)
        if (dist < 150) {
          ctx.beginPath()
          ctx.moveTo(particles[i].x, particles[i].y)
          ctx.lineTo(particles[j].x, particles[j].y)
          ctx.strokeStyle = `rgba(59, 130, 246, ${0.08 * (1 - dist / 150)})`
          ctx.lineWidth = 0.5
          ctx.stroke()
        }
      }
    }
    animId = requestAnimationFrame(draw)
  }
  draw()
}

const handleResize = () => {
  const canvas = canvasRef.value
  if (canvas) {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
}

onMounted(() => {
  initParticles()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animId)
  window.removeEventListener('resize', handleResize)
})

// 校验登录后跳转地址：仅允许站内路径（以单个 / 开头），防止开放跳转/钓鱼
const sanitizeRedirect = (raw: unknown): string => {
  if (typeof raw !== 'string') return '/'
  return /^\/(?!\/)/.test(raw) ? raw : '/'
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    await userStore.login(form)
    message.success('登录成功')
    const redirect = sanitizeRedirect(route.query.redirect)
    router.push(redirect)
  } catch (error: any) {
    if (error.message) {
      message.error(error.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 dark:from-dark-base dark:via-dark-base dark:to-dark-card flex items-center justify-center p-4 relative overflow-hidden transition-colors duration-300">
    <!-- 粒子画布 -->
    <canvas ref="canvasRef" class="absolute inset-0 z-0 pointer-events-none"></canvas>
    <!-- 背景装饰 -->
    <div class="absolute top-20 right-10 w-96 h-96 bg-blue-200 dark:bg-blue-900/30 rounded-full mix-blend-multiply dark:mix-blend-screen filter blur-xl opacity-50 animate-blob"></div>
    <div class="absolute bottom-20 left-10 w-96 h-96 bg-purple-200 dark:bg-purple-900/30 rounded-full mix-blend-multiply dark:mix-blend-screen filter blur-xl opacity-50 animate-blob animation-delay-2000"></div>

    <div class="w-full max-w-md relative z-10">
      <!-- Logo -->
      <div class="text-center mb-8">
        <img src="/logo.svg" alt="Logo" class="inline-flex w-20 h-20 rounded-2xl shadow-lg mb-4 transform hover:rotate-6 transition-transform duration-300 object-contain" />
        <h1 class="text-3xl font-bold text-gray-900 dark:text-dark-text-primary">personal-homepage</h1>
        <p class="text-gray-500 dark:text-dark-text-muted mt-2">个人主页后台管理系统</p>
      </div>

      <!-- 登录表单 -->
      <div class="bg-white/80 dark:bg-dark-card/80 backdrop-blur-sm rounded-3xl shadow-xl p-6 sm:p-8 border border-gray-100 dark:border-dark-border hover:shadow-2xl transition-shadow duration-500 relative z-10">
        <a-form
          ref="formRef"
          :model="form"
          :rules="rules"
          layout="vertical"
          @finish="handleLogin"
        >
          <a-form-item name="username">
            <a-input
              v-model:value="form.username"
              size="large"
              placeholder="请输入用户名"
              class="!rounded-xl !h-12"
            >
              <template #prefix>
                <UserOutlined class="text-gray-400" />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item name="password">
            <a-input-password
              v-model:value="form.password"
              size="large"
              placeholder="请输入密码"
              class="!rounded-xl !h-12"
              @press-enter="handleLogin"
            >
              <template #prefix>
                <LockOutlined class="text-gray-400" />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loading"
              class="!h-12 !rounded-xl !text-base font-medium"
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>

      </div>

      <!-- 底部 -->
      <div class="text-center mt-8 text-gray-400 dark:text-dark-text-muted text-sm">
        <p>© 2024 Created by MR ZHAO. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes blob {
  0% {
    transform: translate(0px, 0px) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
  100% {
    transform: translate(0px, 0px) scale(1);
  }
}

.animate-blob {
  animation: blob 7s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}
</style>
