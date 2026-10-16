package com.example.grzy.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.SysOperLog;
import com.example.grzy.entity.TbVisitorLocation;
import com.example.grzy.mapper.SysOperLogMapper;
import com.example.grzy.mapper.TbVisitorLocationMapper;
import com.example.grzy.service.*;
import com.example.grzy.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘控制器
 *
 * @author sodasmell
 */
@Tag(name = "仪表盘", description = "仪表盘数据接口")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SaCheckPermission("system:dashboard:view")
public class DashboardController {

    private final VisitService visitService;
    private final OperLogService operLogService;
    private final ArticleService articleService;
    private final ArticleStatsService articleStatsService;
    private final ProjectService projectService;
    private final SkillService skillService;
    private final InternshipService internshipService;
    private final AwardService awardService;
    private final CommentService commentService;
    private final UserService userService;
    private final TbVisitorLocationMapper visitorLocationMapper;
    private final SysOperLogMapper operLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String VISIT_TREND_KEY = "grzy:visit:trend:";

    @Operation(summary = "获取仪表盘数据", description = "获取统计数据和最近活动")
    @GetMapping("/stats")
    public Result<DashboardVO> getStats() {
        DashboardVO vo = new DashboardVO();

        // 统计数量
        vo.setVisitCount(visitService.getVisitCount());
        vo.setArticleCount(articleService.count());
        vo.setProjectCount(projectService.count());
        vo.setSkillCount(skillService.count());
        vo.setInternshipCount(internshipService.count());
        vo.setAwardCount(awardService.count());
        vo.setCommentCount(commentService.count());

        // 最近操作日志
        List<SysOperLog> logs = operLogService.getRecentLogs(10);
        List<DashboardVO.LogItem> logItems = logs.stream().map(log -> {
            DashboardVO.LogItem item = new DashboardVO.LogItem();
            item.setId(log.getId());
            item.setModule(log.getModule());
            item.setOperation(log.getOperation());
            item.setUsername(log.getUsername());
            item.setCreateTime(log.getCreateTime());
            return item;
        }).collect(Collectors.toList());
        vo.setRecentLogs(logItems);

        return Result.success(vo);
    }

    /**
     * 获取访问趋势（近N天每天访问量）
     */
    @Operation(summary = "访问趋势", description = "获取近N天每天访问量")
    @GetMapping("/visit-trend")
    public Result<List<Map<String, Object>>> getVisitTrend(
            @RequestParam(value = "days", defaultValue = "30") Integer days) {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String key = VISIT_TREND_KEY + date.format(fmt);
            Object value = redisTemplate.opsForValue().get(key);
            long count = 0;
            if (value instanceof Number) {
                count = ((Number) value).longValue();
            } else if (value != null) {
                try { count = Long.parseLong(value.toString()); } catch (NumberFormatException ignored) {}
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.format(fmt));
            item.put("count", count);
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 文章分类统计
     */
    @Operation(summary = "文章分类统计")
    @GetMapping("/article-stats")
    public Result<List<Map<String, Object>>> getArticleStats() {
        return Result.success(articleService.getArticleStats());
    }

    /**
     * 热门文章排行
     */
    @Operation(summary = "热门文章排行")
    @GetMapping("/top-articles")
    public Result<List<Map<String, Object>>> getTopArticles(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return Result.success(articleService.getTopArticles(limit));
    }

    /**
     * 文章数据趋势（近N天每日阅读/点赞/收藏量）
     */
    @Operation(summary = "文章数据趋势", description = "获取近N天每日阅读/点赞/收藏量，可指定单篇文章")
    @GetMapping("/article-trend")
    public Result<List<Map<String, Object>>> getArticleTrend(
            @RequestParam(value = "days", defaultValue = "30") Integer days,
            @RequestParam(value = "articleId", required = false) Long articleId) {
        return Result.success(articleStatsService.getArticleTrend(days, articleId));
    }

    /**
     * 网站运行监控（今日PV/UV、接口平均耗时、Redis状态、JVM运行时长）
     */
    @Operation(summary = "网站运行监控", description = "获取今日PV/UV、接口平均耗时、Redis状态、服务运行时长")
    @GetMapping("/monitor")
    public Result<Map<String, Object>> getMonitor() {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 今日 PV（Redis 每日访问计数）
        result.put("todayPv", visitService.getDailyVisitCount(today.format(fmt)));

        // 今日 UV（今日访问记录中去重访客数）
        QueryWrapper<TbVisitorLocation> uvWrapper = new QueryWrapper<>();
        uvWrapper.select("COUNT(DISTINCT visitor_id) AS uv")
                .ge("visit_time", today.atStartOfDay());
        long todayUv = 0;
        List<Map<String, Object>> uvMaps = visitorLocationMapper.selectMaps(uvWrapper);
        if (!uvMaps.isEmpty() && uvMaps.get(0) != null && uvMaps.get(0).get("uv") != null) {
            todayUv = Long.parseLong(uvMaps.get(0).get("uv").toString());
        }
        result.put("todayUv", todayUv);

        // 接口平均耗时（近7天操作日志的 avg duration）
        QueryWrapper<SysOperLog> durationWrapper = new QueryWrapper<>();
        durationWrapper.select("COALESCE(AVG(duration), 0) AS avgDuration")
                .ge("create_time", LocalDateTime.now().minusDays(7));
        double avgDuration = 0;
        List<Map<String, Object>> durationMaps = operLogMapper.selectMaps(durationWrapper);
        if (!durationMaps.isEmpty() && durationMaps.get(0) != null && durationMaps.get(0).get("avgDuration") != null) {
            avgDuration = Double.parseDouble(durationMaps.get(0).get("avgDuration").toString());
        }
        result.put("avgDuration", Math.round(avgDuration * 10) / 10.0);

        // Redis 状态
        String redisStatus = "正常";
        try {
            RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
            if (factory == null) {
                redisStatus = "不可用";
            } else {
                factory.getConnection().ping();
            }
        } catch (Exception e) {
            redisStatus = "异常";
        }
        result.put("redisStatus", redisStatus);

        // JVM 运行时长（秒）
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        result.put("uptimeSeconds", uptimeSeconds);

        return Result.success(result);
    }
}
