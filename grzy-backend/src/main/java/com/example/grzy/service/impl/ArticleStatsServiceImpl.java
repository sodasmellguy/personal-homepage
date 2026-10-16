package com.example.grzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.entity.TbArticle;
import com.example.grzy.mapper.TbArticleDailyStatsMapper;
import com.example.grzy.mapper.TbArticleMapper;
import com.example.grzy.mapper.TbCollectMapper;
import com.example.grzy.mapper.TbCommentMapper;
import com.example.grzy.mapper.TbLikeMapper;
import com.example.grzy.service.ArticleStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章数据统计服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleStatsServiceImpl implements ArticleStatsService {

    private final TbArticleDailyStatsMapper dailyStatsMapper;
    private final TbArticleMapper articleMapper;
    private final TbLikeMapper likeMapper;
    private final TbCollectMapper collectMapper;
    private final TbCommentMapper commentMapper;
    /** 事务支撑 Bean：跨 Bean 调用事务方法，避免 this 自调用绕过 AOP 代理导致 @Transactional 失效 */
    private final ArticleTxSupport articleTxSupport;
    private final StringRedisTemplate stringRedisTemplate;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 快照任务防重入锁 key */
    private static final String SNAPSHOT_LOCK_KEY = "grzy:task:lock:snapshot";
    /**
     * 锁 TTL：仅需覆盖单次执行时长即可。
     * 快照任务为每日一次（cron 间隔 24 小时），若按 2 倍间隔取 48 小时会拦截次日调度，
     * 故取 10 分钟兜底；正常执行完会主动释放锁。
     */
    private static final Duration SNAPSHOT_LOCK_TTL = Duration.ofMinutes(10);

    /**
     * 每天凌晨 00:05 快照当日数据
     */
    @Scheduled(cron = "0 5 0 * * ?")
    @Override
    public void snapshotDaily() {
        if (!tryLock()) {
            return;
        }
        LocalDate today = LocalDate.now();
        try {
            articleTxSupport.doSnapshot(today);
        } catch (Exception e) {
            log.error("文章每日统计快照失败: date={}", today, e);
        } finally {
            unlock();
        }
    }

    /**
     * 应用启动时补快照（覆盖服务器跨天未运行的情况）
     */
    @EventListener(ApplicationReadyEvent.class)
    public void snapshotOnStartup() {
        if (!tryLock()) {
            return;
        }
        try {
            articleTxSupport.doSnapshot(LocalDate.now());
        } catch (Exception e) {
            log.error("启动时文章统计快照失败", e);
        } finally {
            unlock();
        }
    }

    /**
     * 防重入加锁：拿到锁才执行，拿不到说明已有实例在执行，直接跳过本次
     */
    private boolean tryLock() {
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(SNAPSHOT_LOCK_KEY, "1", SNAPSHOT_LOCK_TTL);
        if (!Boolean.TRUE.equals(ok)) {
            log.info("文章统计快照任务已有实例在执行，本次跳过: key={}", SNAPSHOT_LOCK_KEY);
            return false;
        }
        return true;
    }

    /**
     * 执行完主动释放锁，避免残留锁拦截下一次调度（TTL 仅作异常兜底）
     */
    private void unlock() {
        try {
            stringRedisTemplate.delete(SNAPSHOT_LOCK_KEY);
        } catch (Exception e) {
            log.warn("释放文章统计快照任务锁失败: key={}", SNAPSHOT_LOCK_KEY, e);
        }
    }

    @Override
    public List<Map<String, Object>> getArticleTrend(Integer days, Long articleId) {
        int n = (days == null || days < 1) ? 30 : Math.min(days, 90);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(n - 1L);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();

        // 每日新增阅读量（快照表）
        Map<String, Long> viewMap = new HashMap<>();
        for (Map<String, Object> row : dailyStatsMapper.selectViewTrend(startDate, endDate, articleId)) {
            viewMap.put(String.valueOf(row.get("statDate")), Long.valueOf(row.get("viewCount").toString()));
        }

        // 每日点赞量、收藏量（按天聚合）
        Map<String, Long> likeMap = new HashMap<>();
        for (Map<String, Object> row : likeMapper.selectCountGroupByDay(startTime, endTime, articleId)) {
            likeMap.put(String.valueOf(row.get("statDate")), Long.valueOf(row.get("cnt").toString()));
        }
        Map<String, Long> collectMap = new HashMap<>();
        for (Map<String, Object> row : collectMapper.selectCountGroupByDay(startTime, endTime, articleId)) {
            collectMap.put(String.valueOf(row.get("statDate")), Long.valueOf(row.get("cnt").toString()));
        }

        // 组装连续日期序列（无数据的天补 0）
        List<Map<String, Object>> result = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            String key = d.format(DATE_FMT);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", key);
            item.put("viewCount", viewMap.getOrDefault(key, 0L));
            item.put("likeCount", likeMap.getOrDefault(key, 0L));
            item.put("collectCount", collectMap.getOrDefault(key, 0L));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMonthlyHotArticles(Integer limit) {
        int n = (limit == null || limit < 1) ? 5 : limit;
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // SQL GROUP BY 聚合本月点赞/评论数，避免把当月全部明细拉进内存
        Map<Long, Long> likeCountMap = toCountMap(likeMapper.selectCountGroupByArticle(monthStart));
        Map<Long, Long> commentCountMap = toCountMap(commentMapper.selectCountGroupByArticle(monthStart));

        // 热度 = 本月点赞 + 本月评论；先合并排序取 TOP N 的文章 ID，再回表查文章
        Map<Long, Long> hotScore = new HashMap<>();
        likeCountMap.forEach((id, c) -> hotScore.merge(id, c, Long::sum));
        commentCountMap.forEach((id, c) -> hotScore.merge(id, c, Long::sum));
        if (hotScore.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> topIds = hotScore.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<TbArticle> articles = articleMapper.selectList(
                new LambdaQueryWrapper<TbArticle>()
                        .select(TbArticle::getId, TbArticle::getTitle, TbArticle::getViewCount, TbArticle::getLikeCount)
                        .in(TbArticle::getId, topIds)
                        .eq(TbArticle::getDeleted, CommonConstant.NOT_DELETED)
                        .eq(TbArticle::getStatus, CommonConstant.STATUS_NORMAL)
        );
        Map<Long, TbArticle> articleMap = articles.stream()
                .collect(Collectors.toMap(TbArticle::getId, a -> a));
        return topIds.stream()
                .filter(articleMap::containsKey)
                .map(id -> {
                    TbArticle a = articleMap.get(id);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", a.getId());
                    item.put("title", a.getTitle());
                    item.put("viewCount", a.getViewCount());
                    item.put("likeCount", a.getLikeCount());
                    item.put("hotScore", hotScore.getOrDefault(id, 0L));
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * GROUP BY 聚合结果行（articleId/cnt）转 Map
     */
    private Map<Long, Long> toCountMap(List<Map<String, Object>> rows) {
        Map<Long, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            result.put(Long.valueOf(row.get("articleId").toString()), Long.valueOf(row.get("cnt").toString()));
        }
        return result;
    }
}
