import { get, post, put, del } from '@/utils/request'

// 文章
export interface Article {
  id: number
  title: string
  summary: string
  content: string
  coverImage: string
  category: string
  tags: string
  tagList: string[]
  viewCount: number
  likeCount: number
  isTop: number
  /** 状态：0-草稿，1-已发布，2-定时发布 */
  status: number
  /** 定时发布时间（status=2 时生效） */
  publishTime?: string
  sort: number
  commentCount: number
  createTime: string
  updateTime: string
}

// 文章详情
export interface ArticleDetail extends Article {
  comments: Comment[]
  prevId?: number
  prevTitle?: string
  nextId?: number
  nextTitle?: string
}

// 评论
export interface Comment {
  id: number
  articleId: number
  parentId: number
  replyToId: number | null
  replyToNickname: string | null
  nickname: string
  email: string
  avatar: string
  content: string
  status: number
  children: Comment[]
  createTime: string
}

// 文章查询参数
export interface ArticleQuery {
  pageNum?: number
  pageSize?: number
  title?: string
  status?: number
  category?: string
  keyword?: string
  tag?: string
  sortType?: string
}

// 热门标签
export interface HotTag {
  name: string
  count: number
}

// 分页结果
export interface PageResult<T> {
  total: number
  rows: T[]
  pageNum: number
  pageSize: number
  totalPages: number
}

/**
 * 获取文章列表（后台分页）
 */
export function getArticleList(params: ArticleQuery) {
  return get<PageResult<Article>>('/article/list', params)
}

/**
 * 获取前台文章列表
 */
export function getFrontArticleList(params: ArticleQuery) {
  return get<PageResult<Article>>('/front/article/list', params)
}

/**
 * 获取文章详情
 */
export function getArticleDetail(id: number) {
  return get<ArticleDetail>(`/front/article/${id}`)
}

/**
 * 获取文章详情（后台）
 */
export function getArticle(id: number) {
  return get<ArticleDetail>(`/article/${id}`)
}

/**
 * 创建文章
 */
export function createArticle(data: Partial<Article>) {
  return post('/article', data)
}

/**
 * 更新文章
 */
export function updateArticle(id: number, data: Partial<Article>) {
  return put(`/article/${id}`, data)
}

/**
 * 删除文章
 */
export function deleteArticle(id: number) {
  return del(`/article/${id}`)
}

/**
 * 批量删除文章
 */
export function batchDeleteArticles(ids: number[]) {
  return del('/article/batch', ids)
}

/**
 * 获取文章评论
 */
export function getArticleComments(articleId: number) {
  return get<Comment[]>(`/front/article/${articleId}/comments`)
}

/**
 * 提交评论
 */
export function submitComment(data: {
  articleId: number
  parentId?: number
  replyToId?: number
  nickname: string
  email?: string
  content: string
}) {
  return post('/front/comment', data)
}

/**
 * 点赞文章
 */
export function likeArticle(id: number) {
  return post(`/front/article/${id}/like`)
}

/**
 * 取消点赞
 */
export function unlikeArticle(id: number) {
  return del(`/front/article/${id}/like`)
}

/**
 * 收藏文章
 */
export function collectArticle(id: number) {
  return post(`/front/article/${id}/collect`)
}

/**
 * 取消收藏
 */
export function uncollectArticle(id: number) {
  return del(`/front/article/${id}/collect`)
}

/**
 * 获取收藏列表
 */
export function getCollectedArticles(params?: { pageNum?: number; pageSize?: number }) {
  return get<PageResult<Article>>('/front/article/collected', params)
}

// 归档数据结构
export interface ArchiveYear {
  year: number
  months: {
    month: number
    articles: {
      id: number
      title: string
      createTime: string
      category: string
    }[]
  }[]
}

/**
 * 获取归档列表
 */
export function getArchives() {
  return get<ArchiveYear[]>('/front/article/archives')
}

/**
 * 获取热门标签
 */
export function getHotTags() {
  return get<HotTag[]>('/front/article/tags')
}

// 排行榜文章数据结构
export interface RankArticle {
  id: number
  title: string
  viewCount: number
  likeCount?: number
  hotScore?: number
}

/**
 * 获取阅读量排行 TOP N
 */
export function getTopArticles(limit: number = 5) {
  return get<RankArticle[]>('/front/article/top', { limit })
}

/**
 * 获取本月热门文章
 */
export function getMonthlyHotArticles(limit: number = 5) {
  return get<RankArticle[]>('/front/article/monthly-hot', { limit })
}
