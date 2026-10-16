import { post } from '@/utils/request'

/**
 * 记录一次页面访问（调用后端 Redis 自增）
 * 每次页面加载只记录一次
 */
let recorded = false

export function recordVisit() {
  if (recorded) return
  recorded = true
  post('/front/visit').catch(() => {
    // 静默失败，不影响用户体验
    recorded = false
  })
}

/**
 * 记录文章访问（防刷）
 * 同一 IP + 文章 ID 在同一天内只记录一次
 * 返回 true 表示需要计数，false 表示已记录过
 */
export function recordArticleVisit(articleId: number): boolean {
  const key = `visited_article_${articleId}_${new Date().toDateString()}`
  if (localStorage.getItem(key)) return false
  localStorage.setItem(key, '1')
  return true
}
