package com.example.grzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.entity.TbArticle;
import com.example.grzy.entity.TbArticleDailyStats;
import com.example.grzy.mapper.TbArticleDailyStatsMapper;
import com.example.grzy.mapper.TbArticleMapper;
import com.example.grzy.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章事务支撑 Bean
 * <p>
 * 定时任务（@Scheduled / @EventListener 入口）若经 this 自调用本类事务方法，会绕过 Spring AOP 代理
 * 导致 @Transactional 失效，因此把事务体统一抽到本独立 Bean，由调用方注入后跨 Bean 调用，保证事务生效。
 * <p>
 * 当前承载两块事务体（分属两个业务，仅因同为文章域而合于一类）：
 * 1. 定时发布：{@link #publishDueArticles()}
 * 2. 每日统计快照：{@link #doSnapshot(LocalDate)}
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleTxSupport {

    /** 定时发布状态 */
    private static final Integer STATUS_SCHEDULED = 2;
    /** 已发布状态 */
    private static final Integer STATUS_PUBLISHED = CommonConstant.STATUS_NORMAL;

    private final TbArticleMapper articleMapper;
    private final TbArticleDailyStatsMapper dailyStatsMapper;
    private final NotifyService notifyService;

    /**
     * 发布所有到达发布时间的定时文章（事务边界）
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishDueArticles() {
        List<TbArticle> dueArticles = articleMapper.selectList(
                new LambdaQueryWrapper<TbArticle>()
                        .select(TbArticle::getId, TbArticle::getTitle)
                        .eq(TbArticle::getStatus, STATUS_SCHEDULED)
                        .eq(TbArticle::getDeleted, CommonConstant.NOT_DELETED)
                        .le(TbArticle::getPublishTime, LocalDateTime.now())
        );
        if (dueArticles.isEmpty()) {
            return;
        }
        for (TbArticle article : dueArticles) {
            try {
                articleMapper.update(null, new LambdaUpdateWrapper<TbArticle>()
                        .eq(TbArticle::getId, article.getId())
                        .set(TbArticle::getStatus, STATUS_PUBLISHED));
                log.info("定时发布文章成功: id={}, title={}", article.getId(), article.getTitle());
                // 发布结果邮件通知（NotifyService 内部吞异常，不影响发布流程）
                notifyService.notifyPublishResult(true, article.getTitle(), null);
            } catch (Exception e) {
                log.error("定时发布文章失败: id={}, title={}", article.getId(), article.getTitle(), e);
                notifyService.notifyPublishResult(false, article.getTitle(), e.getMessage());
            }
        }
        log.info("本次定时发布共处理 {} 篇文章", dueArticles.size());
    }

    /**
     * 执行快照：记录每篇已发布文章截至当日的累计阅读量，并差分出当日新增（事务边界）
     */
    @Transactional(rollbackFor = Exception.class)
    public void doSnapshot(LocalDate date) {
        // 查询所有已发布且未删除的文章
        List<TbArticle> articles = articleMapper.selectList(
                new LambdaQueryWrapper<TbArticle>()
                        .eq(TbArticle::getDeleted, CommonConstant.NOT_DELETED)
                        .eq(TbArticle::getStatus, CommonConstant.STATUS_NORMAL)
        );
        if (articles.isEmpty()) {
            return;
        }

        // 当日已有快照的文章（幂等，支持重复执行）
        List<TbArticleDailyStats> todayRows = dailyStatsMapper.selectList(
                new LambdaQueryWrapper<TbArticleDailyStats>()
                        .eq(TbArticleDailyStats::getStatDate, date)
        );
        Map<Long, TbArticleDailyStats> todayMap = todayRows.stream()
                .collect(Collectors.toMap(TbArticleDailyStats::getArticleId, r -> r));

        // 各文章在 date 之前最近一次快照的累计阅读量（差分基准）
        Map<Long, Integer> prevTotalMap = new HashMap<>();
        for (Map<String, Object> row : dailyStatsMapper.selectLatestSnapshotBefore(date)) {
            Long articleId = Long.valueOf(row.get("articleId").toString());
            Integer totalCount = Integer.valueOf(row.get("totalCount").toString());
            prevTotalMap.put(articleId, totalCount);
        }

        for (TbArticle article : articles) {
            int total = article.getViewCount() != null ? article.getViewCount() : 0;
            TbArticleDailyStats existing = todayMap.get(article.getId());
            if (existing != null) {
                // 已有当日快照，仅刷新累计值与增量
                int delta = prevTotalMap.containsKey(article.getId())
                        ? Math.max(0, total - prevTotalMap.get(article.getId()))
                        : existing.getViewCount() != null ? existing.getViewCount() : 0;
                existing.setTotalCount(total);
                existing.setViewCount(delta);
                dailyStatsMapper.updateById(existing);
                continue;
            }
            TbArticleDailyStats stats = new TbArticleDailyStats();
            stats.setArticleId(article.getId());
            stats.setStatDate(date);
            stats.setTotalCount(total);
            // 首次快照基线为 0（避免把历史阅读量算进单日），此后按差分计算
            int delta = prevTotalMap.containsKey(article.getId())
                    ? Math.max(0, total - prevTotalMap.get(article.getId()))
                    : 0;
            stats.setViewCount(delta);
            dailyStatsMapper.insert(stats);
        }
        log.info("文章每日统计快照完成: date={}, articleCount={}", date, articles.size());
    }
}
