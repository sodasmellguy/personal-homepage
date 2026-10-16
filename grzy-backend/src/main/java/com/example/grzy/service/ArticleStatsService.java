package com.example.grzy.service;

import java.util.List;
import java.util.Map;

/**
 * 文章数据统计服务
 *
 * @author sodasmell
 */
public interface ArticleStatsService {

    /**
     * 快照当日各文章累计阅读量（定时任务 + 启动时调用）
     */
    void snapshotDaily();

    /**
     * 获取文章数据趋势（近N天每日阅读/点赞/收藏量）
     *
     * @param days      天数
     * @param articleId 文章ID（为空则统计全部文章）
     */
    List<Map<String, Object>> getArticleTrend(Integer days, Long articleId);

    /**
     * 获取本月热门文章（按本月点赞+评论数排序）
     */
    List<Map<String, Object>> getMonthlyHotArticles(Integer limit);
}
