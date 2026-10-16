import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      dts: 'src/components.d.ts',
    }),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['logo.svg', 'avatar.png'],
      manifest: {
        name: '赵先生 - 个人主页',
        short_name: '赵先生',
        description: '全栈开发工程师的个人网站',
        theme_color: '#3b82f6',
        background_color: '#ffffff',
        display: 'standalone',
        icons: [
          {
            src: 'logo.svg',
            sizes: '192x192',
            type: 'image/svg+xml',
            purpose: 'any maskable',
          },
          {
            src: 'logo.svg',
            sizes: '512x512',
            type: 'image/svg+xml',
            purpose: 'any maskable',
          },
        ],
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,svg,png,woff2}'],
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/fonts\.googleapis\.com\/.*/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'google-fonts-cache',
              expiration: {
                maxEntries: 10,
                maxAgeSeconds: 60 * 60 * 24 * 365,
              },
              cacheableResponse: {
                statuses: [0, 200],
              },
            },
          },
          // 安全说明：不缓存 /api/* 响应 —— API 响应可能携带登录用户
          // 的个人/管理数据，写入 Cache Storage 后在共用电脑上可被
          // 下一个使用者读取。
        ],
      },
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/rss.xml': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/doc.html': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/webjars': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/v3/api-docs': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/swagger-resources': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      onwarn(warning, warn) {
        if (warning.code === 'INVALID_ANNOTATION') return
        warn(warning)
      },
      output: {
        manualChunks(id: string) {
          if (id.includes('node_modules/vue') || id.includes('node_modules/vue-router') || id.includes('node_modules/pinia') || id.includes('node_modules/@vue')) {
            return 'vendor-vue'
          }
          if (id.includes('node_modules/ant-design-vue') || id.includes('node_modules/@ant-design')) {
            return 'vendor-antd'
          }
          if (id.includes('node_modules/echarts') || id.includes('node_modules/vue-echarts') || id.includes('node_modules/zrender')) {
            return 'vendor-echarts'
          }
          if (id.includes('node_modules/markdown-it') || id.includes('node_modules/highlight.js') || id.includes('node_modules/md-editor-v3')) {
            return 'vendor-md'
          }
        },
      },
    },
  },
  css: {
    preprocessorOptions: {
      less: {
        javascriptEnabled: true,
        modifyVars: {},
      },
    },
  },
})
