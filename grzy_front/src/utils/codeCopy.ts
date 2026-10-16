// 为代码块添加复制按钮
export function initCodeCopy() {
  const observer = new MutationObserver(() => {
    document.querySelectorAll('.md-editor-preview pre').forEach((pre) => {
      if (pre.querySelector('.code-copy-btn')) return
      const btn = document.createElement('button')
      btn.className = 'code-copy-btn'
      btn.textContent = '复制'
      btn.addEventListener('click', async () => {
        const code = pre.querySelector('code')
        if (!code) return
        try {
          await navigator.clipboard.writeText(code.textContent || '')
          btn.textContent = '已复制'
          btn.style.color = '#10b981'
          setTimeout(() => {
            btn.textContent = '复制'
            btn.style.color = ''
          }, 2000)
        } catch {
          btn.textContent = '失败'
          setTimeout(() => { btn.textContent = '复制' }, 2000)
        }
      })
      pre.appendChild(btn)
    })
  })
  observer.observe(document.body, { childList: true, subtree: true })
  return observer
}
