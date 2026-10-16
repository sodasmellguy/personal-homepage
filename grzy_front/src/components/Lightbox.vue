<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

defineProps<{
  src: string
}>()

const emit = defineEmits<{
  close: []
}>()

const scale = ref(1)
const visible = ref(false)

// ESC 关闭
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape') emit('close')
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  document.body.style.overflow = 'hidden'
  // 入场动画
  requestAnimationFrame(() => { visible.value = true })
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = ''
})

const handleBackdropClick = () => {
  emit('close')
}

const handleZoomIn = (e: Event) => {
  e.stopPropagation()
  scale.value = Math.min(scale.value + 0.5, 4)
}

const handleZoomOut = (e: Event) => {
  e.stopPropagation()
  scale.value = Math.max(scale.value - 0.5, 0.5)
}

const handleReset = (e: Event) => {
  e.stopPropagation()
  scale.value = 1
}
</script>

<template>
  <teleport to="body">
    <div
      class="lightbox-overlay"
      :class="{ 'opacity-100': visible, 'opacity-0': !visible }"
      @click="handleBackdropClick"
    >
      <!-- 关闭按钮 -->
      <button
        class="lightbox-close"
        @click="emit('close')"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      <!-- 缩放控制 -->
      <div class="lightbox-controls" @click.stop>
        <button @click="handleZoomOut" title="缩小">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
          </svg>
        </button>
        <span class="text-sm font-mono">{{ Math.round(scale * 100) }}%</span>
        <button @click="handleZoomIn" title="放大">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
        </button>
        <button @click="handleReset" title="重置" class="ml-2">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" />
          </svg>
        </button>
      </div>

      <!-- 图片 -->
      <img
        :src="src"
        :style="{ transform: `scale(${scale})` }"
        class="lightbox-image"
        @click.stop
        @wheel.prevent="(e: WheelEvent) => { scale = Math.max(0.5, Math.min(4, scale + (e.deltaY > 0 ? -0.1 : 0.1))) }"
      />
    </div>
  </teleport>
</template>

<style scoped>
.lightbox-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(8px);
  transition: opacity 0.3s ease;
}

.lightbox-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
  z-index: 10;
}

.lightbox-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.lightbox-controls {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  border-radius: 999px;
  color: white;
  z-index: 10;
}

.lightbox-controls button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

.lightbox-controls button:hover {
  background: rgba(255, 255, 255, 0.25);
}

.lightbox-image {
  max-width: 90vw;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 8px;
  transition: transform 0.3s ease;
  cursor: grab;
}
</style>
