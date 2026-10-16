import { get } from '@/utils/request'

// 日志条目
export interface LogItem {
  id: number
  module: string
  operation: string
  username: string
  createTime: string
}

// 仪表盘统计数据
export interface DashboardStats {
  visitCount: number
  articleCount: number
  projectCount: number
  skillCount: number
  internshipCount: number
  awardCount: number
  commentCount: number
  recentLogs: LogItem[]
}

// 访问趋势数据
export interface VisitTrendItem {
  date: string
  count: number
}

// 文章分类统计
export interface ArticleStatItem {
  name: string
  value: number
}

/**
 * 获取仪表盘统计数据
 */
export function getDashboardStats() {
  return get<DashboardStats>('/dashboard/stats')
}

/**
 * 获取访问趋势
 */
export function getVisitTrend(days: number = 30) {
  return get<VisitTrendItem[]>('/dashboard/visit-trend', { days })
}

/**
 * 获取文章分类统计
 */
export function getArticleStats() {
  return get<ArticleStatItem[]>('/dashboard/article-stats')
}

/**
 * 获取热门文章
 */
export function getTopArticles(limit: number = 10) {
  return get<{ id: number; title: string; viewCount: number }[]>('/dashboard/top-articles', { limit })
}

// 文章数据趋势项
export interface ArticleTrendItem {
  date: string
  viewCount: number
  likeCount: number
  collectCount: number
}

/**
 * 获取文章数据趋势（近N天每日阅读/点赞/收藏量，可指定单篇文章）
 */
export function getArticleTrend(days: number = 30, articleId?: number) {
  return get<ArticleTrendItem[]>('/dashboard/article-trend', { days, articleId })
}

// 网站运行监控数据
export interface MonitorStats {
  todayPv: number
  todayUv: number
  /** 近7天接口平均耗时（ms） */
  avgDuration: number
  /** normal-正常，其他为异常描述 */
  redisStatus: string
  /** JVM 运行秒数 */
  uptimeSeconds: number
}

/**
 * 获取网站运行监控数据
 */
export function getMonitorStats() {
  return get<MonitorStats>('/dashboard/monitor')
}
