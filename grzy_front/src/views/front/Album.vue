<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { CameraOutlined, EnvironmentOutlined, CalendarOutlined } from '@ant-design/icons-vue'
import { getFrontAlbumPhotos, getFrontAlbumGroups, type AlbumPhoto, type AlbumGroup } from '@/api/album'
import Lightbox from '@/components/Lightbox.vue'
import { useHead } from '@unhead/vue'

useHead({
  title: '相册 - 赵先生',
  meta: [{ name: 'description', content: '生活记录与旅行照片' }],
})

const loading = ref(false)
const photos = ref<AlbumPhoto[]>([])
const groups = ref<AlbumGroup[]>([])
const activeGroupId = ref<number | undefined>(undefined)

// 灯箱预览
const lightboxVisible = ref(false)
const lightboxSrc = ref('')

const fetchGroups = async () => {
  try {
    const { data } = await getFrontAlbumGroups()
    groups.value = data || []
  } catch (error) {
    console.error('获取分组失败:', error)
  }
}

const fetchPhotos = async () => {
  loading.value = true
  try {
    const { data } = await getFrontAlbumPhotos(activeGroupId.value)
    photos.value = data || []
  } catch (error) {
    console.error('获取相册失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换分组
const handleGroupClick = (groupId?: number) => {
  activeGroupId.value = groupId
  fetchPhotos()
}

const openPreview = (photo: AlbumPhoto) => {
  lightboxSrc.value = photo.image
  lightboxVisible.value = true
}

// 拼接 EXIF 拍摄参数展示串
const exifText = (photo: AlbumPhoto) => {
  return [
    photo.camera,
    photo.aperture,
    photo.shutter,
    photo.iso ? 'ISO' + photo.iso : '',
    photo.focalLength,
  ].filter(Boolean).join(' · ')
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return dateStr.substring(0, 10).replace(/-/g, '.')
}

onMounted(() => {
  fetchGroups()
  fetchPhotos()
})
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-dark-base transition-colors duration-300">
    <!-- 头部 -->
    <section class="py-16 sm:py-20 px-4 sm:px-8">
      <div class="max-w-5xl mx-auto">
        <h1 class="text-3xl sm:text-4xl font-bold text-gray-900 dark:text-dark-text-primary tracking-tight">相册</h1>
        <p class="text-gray-500 dark:text-dark-text-muted mt-3 text-base">生活不止代码，还有诗和远方</p>
      </div>
    </section>

    <!-- 分组筛选 -->
    <div v-if="groups.length" class="max-w-5xl mx-auto px-4 sm:px-8 pb-6">
      <div class="flex flex-wrap gap-2">
        <button
          class="px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-300 border"
          :class="!activeGroupId
            ? 'bg-gradient-to-r from-gray-900 to-gray-700 text-white border-gray-900 dark:from-white dark:to-gray-200 dark:text-gray-900 dark:border-white shadow-md'
            : 'bg-gray-100 text-gray-700 border-gray-100 hover:bg-gray-200 dark:bg-dark-elevated dark:text-dark-text-secondary dark:border-dark-border dark:hover:bg-dark-card'"
          @click="handleGroupClick(undefined)"
        >
          全部
        </button>
        <button
          v-for="group in groups"
          :key="group.id"
          class="px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-300 border"
          :class="activeGroupId === group.id
            ? 'bg-gradient-to-r from-gray-900 to-gray-700 text-white border-gray-900 dark:from-white dark:to-gray-200 dark:text-gray-900 dark:border-white shadow-md'
            : 'bg-gray-100 text-gray-700 border-gray-100 hover:bg-gray-200 dark:bg-dark-elevated dark:text-dark-text-secondary dark:border-dark-border dark:hover:bg-dark-card'"
          @click="handleGroupClick(group.id)"
        >
          {{ group.name }}
        </button>
      </div>
    </div>

    <!-- 照片墙 -->
    <div class="max-w-5xl mx-auto px-4 sm:px-8 pb-20">
      <a-spin :spinning="loading">
        <div class="columns-1 sm:columns-2 lg:columns-3 gap-4 [&>*]:mb-4">
          <div
            v-for="photo in photos"
            :key="photo.id"
            class="group relative rounded-2xl overflow-hidden cursor-pointer border border-gray-100 dark:border-dark-border bg-gray-50 dark:bg-dark-card break-inside-avoid hover:shadow-xl hover:shadow-black/5 transition-all duration-300"
            @click="openPreview(photo)"
          >
            <img
              :src="photo.image"
              :alt="photo.title || '照片'"
              loading="lazy"
              class="w-full block group-hover:scale-[1.03] transition-transform duration-500"
            />
            <!-- 悬浮信息层 -->
            <div class="absolute inset-0 bg-gradient-to-t from-black/70 via-black/0 to-black/0 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex flex-col justify-end p-4">
              <p v-if="photo.title" class="text-white font-medium text-sm">{{ photo.title }}</p>
              <p v-if="photo.description" class="text-white/80 text-xs mt-1 line-clamp-2">{{ photo.description }}</p>
              <div v-if="exifText(photo)" class="mt-2 text-white/70 text-xs tracking-wide">
                <span class="truncate">{{ exifText(photo) }}</span>
              </div>
              <div class="flex items-center gap-3 mt-2 text-white/60 text-xs">
                <span v-if="photo.location" class="flex items-center gap-1">
                  <EnvironmentOutlined />
                  {{ photo.location }}
                </span>
                <span v-if="photo.shootDate" class="flex items-center gap-1">
                  <CalendarOutlined />
                  {{ formatDate(photo.shootDate) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <a-empty
          v-if="!loading && photos.length === 0"
          description="暂无照片"
          class="py-16"
        >
          <template #image>
            <CameraOutlined class="text-5xl text-gray-200 dark:text-dark-elevated" />
          </template>
        </a-empty>
      </a-spin>
    </div>

    <!-- 图片灯箱 -->
    <Lightbox v-if="lightboxVisible" :src="lightboxSrc" @close="lightboxVisible = false" />
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
