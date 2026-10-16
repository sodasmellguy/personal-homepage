import { get } from '@/utils/request'

// 站点统计
export interface SiteStats {
  /** 总访问量 */
  totalVisits: number
  /** 建站天数 */
  runDays: number
}

/**
 * 获取站点统计（页脚「本站已运行」卡片）
 */
export function getSiteStats() {
  return get<SiteStats>('/front/site-stats')
}
