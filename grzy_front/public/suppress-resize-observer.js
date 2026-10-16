// 抑制 ResizeObserver 无害警告（Ant Design Vue 组件触发，避免 Vite HMR overlay 弹出）
// 独立为外部脚本，以便启用严格的 CSP（script-src 'self'，禁止内联脚本）
window.addEventListener('error', function (e) {
  if (e.message && e.message.includes('ResizeObserver loop')) {
    e.stopImmediatePropagation()
  }
})
