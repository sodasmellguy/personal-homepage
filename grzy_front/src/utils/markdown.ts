import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

// 代码高亮函数
const highlightCode = (str: string, lang: string): string => {
  if (lang && hljs.getLanguage(lang)) {
    try {
      const highlighted = hljs.highlight(str, { language: lang }).value
      return `<pre class="hljs-code-block"><code class="hljs language-${lang}">${highlighted}</code></pre>`
    } catch {
      // fall through
    }
  }
  const escaped = str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
  return `<pre class="hljs-code-block"><code class="hljs">${escaped}</code></pre>`
}

// 创建 markdown-it 实例
const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  breaks: true,
  highlight: highlightCode,
})

// 自定义渲染规则，为标题添加 id（用于 TOC 锚点）
md.renderer.rules.heading_open = (tokens: any[], idx: number): string => {
  const token = tokens[idx]
  const level = token.tag
  const inlineToken = tokens[idx + 1]
  const text = (inlineToken?.children || [])
    .filter((t: any) => t.type === 'text' || t.type === 'code_inline')
    .map((t: any) => t.content)
    .join('')
  const slug = text
    .toLowerCase()
    .replace(/\s+/g, '-')
    .replace(/[^\w\u4e00-\u9fa5-]/g, '')
  return `<${level} id="${slug}">`
}

// 链接在新窗口打开
const defaultLinkOpen = md.renderer.rules.link_open || ((tokens: any[], idx: number, options: any, _env: any, self: any) => {
  return self.renderToken(tokens, idx, options)
})
md.renderer.rules.link_open = (tokens: any[], idx: number, options: any, env: any, self: any): string => {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, idx, options, env, self)
}

/**
 * 渲染 Markdown 为 HTML
 */
export function renderMarkdown(content: string): string {
  if (!content) return ''
  return md.render(content)
}

export default md
