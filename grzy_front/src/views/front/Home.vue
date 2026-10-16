<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@unhead/vue'
import {
  CodeOutlined,
  DatabaseOutlined,
  CloudOutlined,
  ToolOutlined,
  ArrowRightOutlined,
} from '@ant-design/icons-vue'
import { getAllSkills } from '@/api/skill'
import type { Skill } from '@/api/skill'
import { getAllProjects } from '@/api/project'
import { getAllInternships } from '@/api/internship'
import { getAllTechStacks } from '@/api/techstack'
import type { TechStack } from '@/api/techstack'

// SEO
useHead({
  title: '赵先生 - 全栈开发工程师',
  meta: [
    { name: 'description', content: '全栈开发工程师，擅长 Vue、Spring Boot、MySQL 等技术栈，展示项目经验与技术博客' },
  ],
})

const router = useRouter()
const skills = ref<Skill[]>([])
const projectCount = ref(0)
const internshipCount = ref(0)
const loading = ref(true)

// 打字动画
const roles = ['全栈开发工程师', 'Vue 爱好者', 'Spring Boot 实践者', '开源贡献者']
const currentRole = ref('')
const roleIndex = ref(0)
const charIndex = ref(0)
const isDeleting = ref(false)
let typeTimer: ReturnType<typeof setTimeout> | null = null

const typeRole = () => {
  const current = roles[roleIndex.value]
  if (!isDeleting.value) {
    currentRole.value = current.substring(0, charIndex.value + 1)
    charIndex.value++
    if (charIndex.value === current.length) {
      isDeleting.value = true
      typeTimer = setTimeout(typeRole, 2000)
      return
    }
    typeTimer = setTimeout(typeRole, 100)
  } else {
    currentRole.value = current.substring(0, charIndex.value - 1)
    charIndex.value--
    if (charIndex.value === 0) {
      isDeleting.value = false
      roleIndex.value = (roleIndex.value + 1) % roles.length
      typeTimer = setTimeout(typeRole, 500)
      return
    }
    typeTimer = setTimeout(typeRole, 50)
  }
}

// 鼠标视差效果
const mouseX = ref(0)
const mouseY = ref(0)
const handleMouseMove = (e: MouseEvent) => {
  mouseX.value = (e.clientX / window.innerWidth - 0.5) * 20
  mouseY.value = (e.clientY / window.innerHeight - 0.5) * 20
}

// 技术栈展示 — 从API动态获取
const techStackData = ref<TechStack[]>([])
const techStack = computed(() =>
  techStackData.value.map(t => ({
    name: t.name,
    icon: t.icon,
    featured: t.featured === 1,
  }))
)

// 分类技能 — 从API动态获取
const skillCategoryMap = computed(() => {
  const categories = [
    { key: '前端', icon: CodeOutlined, title: '前端开发', color: 'blue' },
    { key: '后端', icon: DatabaseOutlined, title: '后端开发', color: 'emerald' },
    { key: '数据库', icon: DatabaseOutlined, title: '数据库', color: 'violet' },
    { key: 'DevOps', icon: CloudOutlined, title: 'DevOps', color: 'orange' },
    { key: '工业协议', icon: ToolOutlined, title: '工业协议', color: 'pink' },
    { key: '运维', icon: CloudOutlined, title: '运维', color: 'cyan' },
  ]
  return categories.map(cat => ({
    ...cat,
    skills: skills.value.filter(s => s.category === cat.key).map(s => s.name),
  })).filter(cat => cat.skills.length > 0)
})

// 滚动显现动画
const observeSection = (el: HTMLElement) => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible')
          observer.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.1 }
  )
  observer.observe(el)
}

// 观察 stagger 动画元素
const observeStaggerItems = () => {
  document.querySelectorAll('.stagger-item:not(.is-visible)').forEach((el) => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible')
            observer.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.1 }
    )
    observer.observe(el)
  })
}

onMounted(async () => {
  typeRole()
  window.addEventListener('mousemove', handleMouseMove)
  // 观察滚动区域
  document.querySelectorAll('.scroll-reveal').forEach((el) => {
    observeSection(el as HTMLElement)
  })
  try {
    const [skillsRes, projectsRes, internshipsRes, techStackRes] = await Promise.all([
      getAllSkills(),
      getAllProjects(),
      getAllInternships(),
      getAllTechStacks(),
    ])
    skills.value = skillsRes.data
    projectCount.value = projectsRes.data.length
    internshipCount.value = internshipsRes.data.length
    techStackData.value = techStackRes.data
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
    // 数据加载完成后，DOM 渲染了 stagger-item 元素，再设置 observer
    await nextTick()
    observeStaggerItems()
    // 兜底：2秒后强制显示所有未触发的 stagger-item
    setTimeout(() => {
      document.querySelectorAll('.stagger-item:not(.is-visible)').forEach((el) => {
        el.classList.add('is-visible')
      })
    }, 2000)
  }
})

onBeforeUnmount(() => {
  if (typeTimer) clearTimeout(typeTimer)
  window.removeEventListener('mousemove', handleMouseMove)
})
</script>

<template>
  <div class="home-page">
    <!-- Hero Section - 增强渐变和动效 -->
    <section class="relative min-h-[600px] sm:min-h-[700px] flex items-center overflow-hidden bg-white dark:bg-dark-base">
      <!-- 背景装饰 -->
      <div class="absolute inset-0">
        <!-- 渐变光晕 -->
        <div class="absolute top-0 right-0 w-[600px] h-[600px] bg-gradient-to-br from-blue-100/60 via-indigo-100/40 to-transparent dark:from-blue-900/20 dark:via-indigo-900/10 dark:to-transparent rounded-full blur-3xl -translate-y-1/2 translate-x-1/3 mesh-blob"></div>
        <div class="absolute bottom-0 left-0 w-[500px] h-[500px] bg-gradient-to-tr from-violet-100/50 via-purple-100/30 to-transparent dark:from-violet-900/15 dark:via-purple-900/10 dark:to-transparent rounded-full blur-3xl translate-y-1/3 -translate-x-1/4 mesh-blob-2"></div>
        <!-- 网格点 -->
        <div class="absolute inset-0 opacity-[0.03] dark:opacity-[0.05]" style="background-image: radial-gradient(circle at 1px 1px, currentColor 1px, transparent 0); background-size: 48px 48px;"></div>
      </div>

      <div class="relative max-w-6xl mx-auto px-4 sm:px-8 py-20 sm:py-28 w-full">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 sm:gap-16 items-center">
          <!-- 左侧文字 -->
          <div class="text-center lg:text-left">
            <!-- 小标签 -->
            <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-blue-50 dark:bg-blue-900/20 mb-6">
              <span class="w-1.5 h-1.5 rounded-full bg-blue-500 animate-pulse"></span>
              <span class="text-xs font-medium text-blue-600 dark:text-blue-400">Open to Work</span>
            </div>

            <h1 class="text-5xl sm:text-6xl lg:text-7xl font-bold text-gray-900 dark:text-dark-text-primary mb-4 leading-[1.1] tracking-tight text-reveal">
              你好，我是<br />
              <span class="gradient-text-flow">赵先生</span>
            </h1>

            <!-- 打字动画角色 -->
            <div class="h-10 mb-8 flex items-center justify-center lg:justify-start">
              <span class="text-xl sm:text-2xl font-medium text-gray-400 dark:text-dark-text-muted">
                {{ currentRole }}
              </span>
              <span class="ml-0.5 w-[3px] h-7 bg-gradient-to-b from-blue-500 to-violet-500 animate-blink rounded-full"></span>
            </div>

            <p class="text-lg sm:text-xl text-gray-500 dark:text-dark-text-secondary mb-10 leading-relaxed max-w-lg">
              一名热爱技术的全栈开发工程师，专注于 Vue、Spring Boot 等技术栈，
              致力于构建高质量的 Web 应用。
            </p>

            <div class="flex flex-wrap justify-center lg:justify-start gap-4">
              <a-button type="primary" size="large" class="!h-12 !px-8 !rounded-xl !shadow-lg !shadow-blue-500/25 hover:!shadow-xl hover:!shadow-blue-500/40 hover:!scale-[1.03] transition-all duration-300" @click="router.push('/project')">
                查看项目
                <template #icon>
                  <ArrowRightOutlined />
                </template>
              </a-button>
              <a-button size="large" class="!h-12 !px-8 !rounded-xl !border-gray-300 dark:!border-dark-border hover:!border-blue-400 dark:hover:!border-blue-600 hover:!bg-blue-50 dark:hover:!bg-blue-900/10 transition-all duration-300" @click="router.push('/profile')">
                了解更多
              </a-button>
            </div>

            <!-- 统计数据 - 渐变数字 -->
            <div class="flex justify-center lg:justify-start gap-10 sm:gap-14 mt-14">
              <div v-for="stat in [
                { value: projectCount + '+', label: '项目经验', color: 'from-blue-600 to-cyan-500' },
                { value: internshipCount + '+', label: '实习经历', color: 'from-violet-600 to-purple-500' },
                { value: skills.length + '+', label: '技术技能', color: 'from-emerald-600 to-teal-500' },
              ]" :key="stat.label" class="text-center lg:text-left group cursor-default">
                <div class="text-3xl sm:text-4xl font-bold bg-gradient-to-r bg-clip-text text-transparent transition-transform duration-300 group-hover:scale-110" :class="stat.color">{{ stat.value }}</div>
                <div class="text-xs sm:text-sm text-gray-400 dark:text-dark-text-muted mt-1 tracking-wide">{{ stat.label }}</div>
              </div>
            </div>
          </div>

          <!-- 右侧头像/图片 — 带视差 -->
          <div class="hidden lg:flex justify-center">
            <div
                class="relative transition-transform duration-700 ease-out"
                :style="{ transform: `translate(${mouseX * 0.5}px, ${mouseY * 0.5}px)` }"
            >
              <!-- 浮动装饰几何体 -->
              <div class="absolute -top-8 -right-6 w-20 h-20 bg-gradient-to-br from-blue-400/20 to-violet-400/20 rounded-2xl rotate-12 blur-xl animate-float-card"></div>
              <div class="absolute -bottom-6 -left-8 w-24 h-24 bg-gradient-to-tr from-cyan-400/15 to-blue-400/15 rounded-full blur-xl animate-float-card-delayed"></div>

              <div class="absolute -inset-6 bg-gradient-to-br from-gray-100 to-gray-50 dark:from-dark-card/50 dark:to-dark-base/50 rounded-[2.5rem] -rotate-3"></div>
              <div class="relative w-80 h-96 bg-gray-50 dark:bg-dark-card/50 rounded-[2rem] overflow-hidden border border-gray-100 dark:border-dark-border shadow-2xl shadow-black/5">
                <img src="/logo.svg" alt="GRZY" class="w-full h-full object-cover" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部滚动指示器 -->
      <div class="absolute bottom-6 left-1/2 -translate-x-1/2 hidden sm:flex flex-col items-center gap-1 animate-bounce-slow">
        <span class="text-[10px] text-gray-400 dark:text-dark-text-muted tracking-widest uppercase">Scroll</span>
        <div class="w-px h-8 bg-gradient-to-b from-gray-300 to-transparent dark:from-dark-border"></div>
      </div>
    </section>

    <!-- 技术栈展示 — 大量留白，干净网格 -->
    <section class="py-24 sm:py-32 px-4 sm:px-8 bg-[#fafafa] dark:bg-dark-base scroll-reveal">
      <div class="max-w-6xl mx-auto">
        <div class="text-center mb-16 sm:mb-20">
<!--          <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-violet-50 dark:bg-violet-900/20 mb-4">-->
<!--            <CodeOutlined class="text-violet-500 text-sm" />-->
<!--            <span class="text-xs font-medium text-violet-600 dark:text-violet-400">技术工具</span>-->
<!--          </div>-->
          <h2 class="text-4xl sm:text-5xl font-bold text-gray-900 dark:text-dark-text-primary tracking-tight">技术栈</h2>
          <p class="text-gray-400 dark:text-dark-text-muted mt-4 text-lg">我常用的技术和工具</p>
        </div>

        <!-- 技术栈网格 -->
        <div class="grid grid-cols-3 sm:grid-cols-5 gap-4 sm:gap-5">
          <div
            v-for="(tech, idx) in techStack"
            :key="tech.name"
            class="stagger-item group flex flex-col items-center rounded-2xl cursor-pointer transition-all duration-300 hover:scale-[1.05] hover:-translate-y-1 bg-white dark:bg-dark-card border border-gray-100 dark:border-dark-border hover:shadow-xl hover:shadow-blue-500/10 hover:border-blue-200 dark:hover:border-blue-800"
            :class="tech.featured
              ? 'p-5 sm:p-6'
              : 'p-3 sm:p-4'"
            :style="{ transitionDelay: `${idx * 0.03}s` }"
          >
            <img
              :src="tech.icon"
              :alt="tech.name"
              class="mb-3 sm:mb-4 group-hover:scale-110 group-hover:rotate-6 transition-transform duration-300"
              :class="tech.featured ? 'w-12 h-12 sm:w-14 sm:h-14' : 'w-9 h-9 sm:w-11 sm:h-11'"
            />
            <span
              class="font-medium text-gray-700 dark:text-dark-text-secondary group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors"
              :class="tech.featured ? 'text-sm sm:text-base' : 'text-xs sm:text-sm'"
            >
              {{ tech.name }}
            </span>
          </div>
        </div>
      </div>
    </section>

    <!-- 技能分类 — 干净卡片，渐变装饰 -->
    <section class="py-24 sm:py-32 px-4 sm:px-8 bg-white dark:bg-dark-card scroll-reveal">
      <div class="max-w-6xl mx-auto">
        <div class="text-center mb-16 sm:mb-20">
<!--          <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-50 dark:bg-emerald-900/20 mb-4">-->
<!--            <ToolOutlined class="text-emerald-500 text-sm" />-->
<!--            <span class="text-xs font-medium text-emerald-600 dark:text-emerald-400">专业能力</span>-->
<!--          </div>-->
          <h2 class="text-4xl sm:text-5xl font-bold text-gray-900 dark:text-dark-text-primary tracking-tight">专业技能</h2>
          <p class="text-gray-400 dark:text-dark-text-muted mt-4 text-lg">我在不同领域的技术能力</p>
        </div>

        <!-- 技能卡片 — 带渐变顶边 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5 sm:gap-6">
          <div
            v-for="(category, idx) in skillCategoryMap"
            :key="category.title"
            class="group relative bg-white dark:bg-dark-elevated rounded-2xl overflow-hidden border border-gray-100 dark:border-dark-border hover:shadow-2xl hover:shadow-gray-200/50 dark:hover:shadow-black/30 transition-all duration-500 hover:scale-[1.02] stagger-item"
            :style="{ transitionDelay: `${idx * 0.08}s` }"
          >
            <!-- 顶部渐变线 -->
            <div
              class="h-1 w-full"
              :class="{
                'bg-gradient-to-r from-blue-500 to-cyan-500': category.color === 'blue',
                'bg-gradient-to-r from-emerald-500 to-teal-500': category.color === 'emerald',
                'bg-gradient-to-r from-violet-500 to-purple-500': category.color === 'violet',
                'bg-gradient-to-r from-orange-500 to-amber-500': category.color === 'orange',
                'bg-gradient-to-r from-pink-500 to-rose-500': category.color === 'pink',
                'bg-gradient-to-r from-cyan-500 to-blue-500': category.color === 'cyan',
              }"
            ></div>
            <div class="p-6 sm:p-8">
              <div class="flex items-center gap-3 mb-5">
                <div
                  class="w-10 h-10 rounded-xl flex items-center justify-center"
                  :class="{
                    'bg-blue-50 dark:bg-blue-900/20 text-blue-500': category.color === 'blue',
                    'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-500': category.color === 'emerald',
                    'bg-violet-50 dark:bg-violet-900/20 text-violet-500': category.color === 'violet',
                    'bg-orange-50 dark:bg-orange-900/20 text-orange-500': category.color === 'orange',
                    'bg-pink-50 dark:bg-pink-900/20 text-pink-500': category.color === 'pink',
                    'bg-cyan-50 dark:bg-cyan-900/20 text-cyan-500': category.color === 'cyan',
                  }"
                >
                  <component :is="category.icon" class="text-base" />
                </div>
                <h3 class="text-lg sm:text-xl font-semibold text-gray-900 dark:text-dark-text-primary">{{ category.title }}</h3>
              </div>
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="skill in category.skills"
                  :key="skill"
                  class="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                  :class="{
                    'bg-blue-50 dark:bg-blue-900/10 text-blue-600 dark:text-blue-400 group-hover:bg-blue-100 dark:group-hover:bg-blue-900/20': category.color === 'blue',
                    'bg-emerald-50 dark:bg-emerald-900/10 text-emerald-600 dark:text-emerald-400 group-hover:bg-emerald-100 dark:group-hover:bg-emerald-900/20': category.color === 'emerald',
                    'bg-violet-50 dark:bg-violet-900/10 text-violet-600 dark:text-violet-400 group-hover:bg-violet-100 dark:group-hover:bg-violet-900/20': category.color === 'violet',
                    'bg-orange-50 dark:bg-orange-900/10 text-orange-600 dark:text-orange-400 group-hover:bg-orange-100 dark:group-hover:bg-orange-900/20': category.color === 'orange',
                    'bg-pink-50 dark:bg-pink-900/10 text-pink-600 dark:text-pink-400 group-hover:bg-pink-100 dark:group-hover:bg-pink-900/20': category.color === 'pink',
                    'bg-cyan-50 dark:bg-cyan-900/10 text-cyan-600 dark:text-cyan-400 group-hover:bg-cyan-100 dark:group-hover:bg-cyan-900/20': category.color === 'cyan',
                  }"
                >
                  {{ skill }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
.animate-blink {
  animation: blink 1s step-end infinite;
}

/* 浮动卡片动画 */
@keyframes floatCard {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.animate-float-card {
  animation: floatCard 3s ease-in-out infinite;
}

.animate-float-card-delayed {
  animation: floatCard 3s ease-in-out infinite;
  animation-delay: 1.5s;
}

/* 滚动指示器动画 */
@keyframes bounceSlow {
  0%, 100% { transform: translateY(0); opacity: 0.6; }
  50% { transform: translateY(6px); opacity: 1; }
}

.animate-bounce-slow {
  animation: bounceSlow 2s ease-in-out infinite;
}

/* 滚动显现动画 */
.scroll-reveal {
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

.scroll-reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* stagger-item 初始状态在 JS onMounted 后由 IntersectionObserver 触发 */
.stagger-item.is-visible {
  opacity: 1 !important;
  transform: translateY(0) !important;
}
</style>
