package com.example.grzy.controller.content;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.common.utils.ClientIpUtils;
import com.example.grzy.dto.CommentCreateDTO;
import com.example.grzy.dto.FriendLinkApplyDTO;
import com.example.grzy.dto.GuestbookCreateDTO;
import com.example.grzy.entity.TbAlbumGroup;
import com.example.grzy.entity.TbAlbumPhoto;
import com.example.grzy.entity.TbComment;
import com.example.grzy.entity.TbFriendLink;
import com.example.grzy.entity.TbGuestbook;
import com.example.grzy.entity.TbOpenSource;
import com.example.grzy.mapper.TbCommentMapper;
import com.example.grzy.mapper.TbGuestbookMapper;
import com.example.grzy.service.*;
import org.springframework.data.redis.core.RedisTemplate;
import com.example.grzy.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 前台展示控制器（不需要认证）
 *
 * @author sodasmell
 */
@Slf4j
@Tag(name = "前台展示", description = "前台展示接口，不需要认证")
@RestController
@RequestMapping("/api/front")
@RequiredArgsConstructor
public class FrontController {

    private final ProfileService profileService;
    private final SkillService skillService;
    private final ProjectService projectService;
    private final InternshipService internshipService;
    private final AwardService awardService;
    private final ArticleService articleService;
    private final CommentService commentService;
    private final VisitService visitService;
    private final NotifyService notifyService;
    private final TbCommentMapper commentMapper;
    private final TbGuestbookMapper guestbookMapper;
    private final TechStackService techStackService;
    private final SeriesService seriesService;
    private final VisitorLocationService visitorLocationService;
    private final ArticleStatsService articleStatsService;
    private final FriendLinkService friendLinkService;
    private final AlbumService albumService;
    private final OpenSourceService openSourceService;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 评论/留言/友链频率限制：同一 IP 60 秒内只能提交一次 */
    private static final long COMMENT_RATE_LIMIT_SECONDS = 60;

    /** 前台 TOP N 类接口的最大返回条数（防一次拖取过多） */
    private static final int MAX_FRONT_LIMIT = 50;

    /** 留言点赞标记的过期天数（防止 Redis key 无限累积） */
    private static final long GUESTBOOK_LIKE_TTL_DAYS = 30;

    /** 浏览计数去重窗口（小时）：同一访客对同一目标在该窗口内只计一次 */
    private static final long VIEW_DEDUP_HOURS = 24;

    /** 建站日期（计算「本站已运行 X 天」） */
    @Value("${site.build-date:2025-01-01}")
    private String siteBuildDate;

    /**
     * 获取个人信息
     */
    @Operation(summary = "获取个人信息", description = "前台获取个人信息")
    @GetMapping("/profile")
    public Result<ProfileVO> getProfile() {
        ProfileVO profileVO = profileService.getProfile();
        return Result.success(profileVO);
    }

    /**
     * 获取所有技能
     */
    @Operation(summary = "获取所有技能", description = "前台获取所有技能列表")
    @GetMapping("/skill/list")
    public Result<List<SkillVO>> getSkillList() {
        List<SkillVO> skills = skillService.getAllSkills();
        return Result.success(skills);
    }

    /**
     * 获取所有技术栈
     */
    @Operation(summary = "获取所有技术栈", description = "前台获取所有技术栈列表")
    @GetMapping("/techstack/list")
    public Result<List<TechStackVO>> getTechStackList() {
        return Result.success(techStackService.getAllTechStacks());
    }

    /**
     * 获取所有项目
     */
    @Operation(summary = "获取所有项目", description = "前台获取所有项目列表")
    @GetMapping("/project/list")
    public Result<List<ProjectVO>> getProjectList() {
        List<ProjectVO> projects = projectService.getAllProjects();
        return Result.success(projects);
    }

    /**
     * 获取所有实习经历
     */
    @Operation(summary = "获取所有实习经历", description = "前台获取所有实习经历列表")
    @GetMapping("/internship/list")
    public Result<List<InternshipVO>> getInternshipList() {
        List<InternshipVO> internships = internshipService.getAllInternships();
        return Result.success(internships);
    }

    /**
     * 获取所有获奖经历
     */
    @Operation(summary = "获取所有获奖经历", description = "前台获取所有获奖经历列表")
    @GetMapping("/award/list")
    public Result<List<AwardVO>> getAwardList() {
        List<AwardVO> awards = awardService.getAllAwards();
        return Result.success(awards);
    }

    /**
     * 获取文章列表（分页）
     */
    @Operation(summary = "获取文章列表", description = "前台获取文章列表")
    @GetMapping("/article/list")
    public Result<PageResult<ArticleVO>> getArticleList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sortType", required = false) String sortType,
            @RequestParam(value = "tag", required = false) String tag) {
        // 钳制分页参数，防止 pageSize 过大一次拖取全表（含正文大字段）
        pageNum = CommonConstant.clampPageNum(pageNum);
        pageSize = CommonConstant.clampPageSize(pageSize);
        PageResult<ArticleVO> pageResult = articleService.getFrontArticlePage(pageNum, pageSize, category, keyword, sortType, tag);
        return Result.success(pageResult);
    }

    /**
     * 获取文章详情
     */
    @Operation(summary = "获取文章详情", description = "前台获取文章详情")
    @GetMapping("/article/{id}")
    public Result<ArticleDetailVO> getArticleDetail(@PathVariable Long id) {
        ArticleDetailVO detail = articleService.getArticleDetail(id);
        return Result.success(detail);
    }

    /**
     * 获取文章评论
     */
    @Operation(summary = "获取文章评论", description = "前台获取文章评论树")
    @GetMapping("/article/{id}/comments")
    public Result<List<CommentVO>> getArticleComments(@PathVariable Long id) {
        List<CommentVO> comments = commentService.getArticleComments(id);
        return Result.success(comments);
    }

    /**
     * 记录访问量
     */
    @Operation(summary = "记录访问", description = "前台记录访问量")
    @PostMapping("/visit")
    public Result<Void> recordVisit(HttpServletRequest request) {
        String visitorId = getVisitorId(request);
        // 防刷：同一访客（IP+UA 哈希）一天只计一次 PV
        String pvKey = "grzy:visit:pv:" + LocalDate.now() + ":" + visitorId;
        Boolean firstToday = redisTemplate.opsForValue().setIfAbsent(pvKey, 1, VIEW_DEDUP_HOURS, TimeUnit.HOURS);
        if (Boolean.TRUE.equals(firstToday)) {
            visitService.incrementVisit();
        }
        // 异步记录访客位置（内部有当日去重）
        visitorLocationService.recordVisitorLocation(visitorId, getClientIp(request));
        return Result.success();
    }

    /**
     * 提交评论
     */
    @Operation(summary = "提交评论", description = "前台提交评论")
    @PostMapping("/comment")
    public Result<Void> createComment(@Valid @RequestBody CommentCreateDTO dto, HttpServletRequest request) {
        // IP 频率限制：同一 IP 60 秒内只能评论一次（Redis 实现，支持多实例）
        String clientIp = getClientIp(request);
        String rateLimitKey = "rate_limit:comment:" + clientIp;
        Boolean allowed = redisTemplate.opsForValue().setIfAbsent(rateLimitKey, 1, COMMENT_RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(allowed)) {
            return Result.error("评论太频繁，请稍后再试");
        }

        // DTO 转实体，设置 IP（用户不可控制）
        TbComment comment = new TbComment();
        BeanUtil.copyProperties(dto, comment);
        comment.setIp(clientIp);

        commentService.createComment(comment);
        // 查询文章标题（用于邮件通知）
        String articleTitle = "";
        if (comment.getArticleId() != null) {
            try {
                var articleVO = articleService.getArticleDetail(comment.getArticleId());
                articleTitle = articleVO != null ? articleVO.getTitle() : "";
            } catch (Exception e) {
                log.warn("查询文章标题失败: articleId={}", comment.getArticleId(), e);
            }
        }
        // 异步发送新评论通知邮件给管理员
        try {
            notifyService.notifyNewComment(articleTitle, comment.getNickname(), comment.getContent());
        } catch (Exception e) {
            log.warn("发送新评论通知邮件失败", e);
        }
        // 如果是回复评论，通知被回复的评论作者
        // 优先使用 replyToId（精确指向被回复的评论），没有则回退到 parentId
        Long repliedCommentId = (comment.getReplyToId() != null && comment.getReplyToId() > 0)
                ? comment.getReplyToId()
                : comment.getParentId();
        if (repliedCommentId != null && repliedCommentId > 0) {
            try {
                TbComment repliedComment = commentMapper.selectById(repliedCommentId);
                if (repliedComment != null && repliedComment.getEmail() != null && !repliedComment.getEmail().isEmpty()) {
                    // 不给自己发回复通知（回复者邮箱与被回复者邮箱相同时跳过）
                    String replierEmail = comment.getEmail();
                    if (replierEmail == null || !replierEmail.equalsIgnoreCase(repliedComment.getEmail())) {
                        notifyService.notifyReply(
                            repliedComment.getEmail(),
                            comment.getNickname(),
                            comment.getContent(),
                            articleTitle
                        );
                    }
                } else {
                    log.debug("被回复评论未填写邮箱，跳过回复通知: repliedCommentId={}", repliedCommentId);
                }
            } catch (Exception e) {
                log.error("发送回复通知失败", e);
            }
        }
        return Result.success();
    }

    /**
     * 记录文章阅读量
     */
    @Operation(summary = "记录文章阅读量")
    @PostMapping("/article/{id}/view")
    public Result<Void> recordArticleView(@PathVariable Long id, HttpServletRequest request) {
        // 防刷：同一访客对同一文章 24 小时内只计一次阅读量
        String viewKey = "grzy:view:article:" + getVisitorId(request) + ":" + id;
        Boolean firstView = redisTemplate.opsForValue().setIfAbsent(viewKey, 1, VIEW_DEDUP_HOURS, TimeUnit.HOURS);
        if (Boolean.TRUE.equals(firstView)) {
            articleService.incrementArticleView(id);
        }
        return Result.success();
    }

    /**
     * 点赞文章
     */
    @Operation(summary = "点赞文章")
    @PostMapping("/article/{id}/like")
    public Result<Void> likeArticle(@PathVariable Long id, HttpServletRequest request) {
        articleService.likeArticle(id, getVisitorId(request));
        return Result.success();
    }

    /**
     * 取消点赞
     */
    @Operation(summary = "取消点赞")
    @DeleteMapping("/article/{id}/like")
    public Result<Void> unlikeArticle(@PathVariable Long id, HttpServletRequest request) {
        articleService.unlikeArticle(id, getVisitorId(request));
        return Result.success();
    }

    /**
     * 收藏文章
     */
    @Operation(summary = "收藏文章")
    @PostMapping("/article/{id}/collect")
    public Result<Void> collectArticle(@PathVariable Long id, HttpServletRequest request) {
        articleService.collectArticle(id, getVisitorId(request));
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏")
    @DeleteMapping("/article/{id}/collect")
    public Result<Void> uncollectArticle(@PathVariable Long id, HttpServletRequest request) {
        articleService.uncollectArticle(id, getVisitorId(request));
        return Result.success();
    }

    /**
     * 获取收藏列表
     */
    @Operation(summary = "获取收藏列表")
    @GetMapping("/article/collected")
    public Result<PageResult<ArticleVO>> getCollectedArticles(
            HttpServletRequest request,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        // 钳制分页参数，防止 pageSize 过大
        pageNum = CommonConstant.clampPageNum(pageNum);
        pageSize = CommonConstant.clampPageSize(pageSize);
        PageResult<ArticleVO> result = articleService.getCollectedArticles(getVisitorId(request), pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取归档数据
     */
    @Operation(summary = "获取归档数据")
    @GetMapping("/article/archives")
    public Result<List<Map<String, Object>>> getArchives() {
        return Result.success(articleService.getArchives());
    }

    /**
     * 获取热门标签
     */
    @Operation(summary = "获取热门标签")
    @GetMapping("/article/tags")
    public Result<List<Map<String, Object>>> getHotTags() {
        return Result.success(articleService.getHotTags());
    }

    /**
     * 阅读量排行 TOP N
     */
    @Operation(summary = "阅读量排行", description = "前台获取阅读量排行 TOP N 文章")
    @GetMapping("/article/top")
    public Result<List<Map<String, Object>>> getTopViewedArticles(
            @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        return Result.success(articleService.getTopArticles(clampFrontLimit(limit)));
    }

    /**
     * 本月热门文章
     */
    @Operation(summary = "本月热门", description = "前台获取本月热门文章（按本月点赞+评论数排序）")
    @GetMapping("/article/monthly-hot")
    public Result<List<Map<String, Object>>> getMonthlyHotArticles(
            @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        return Result.success(articleStatsService.getMonthlyHotArticles(clampFrontLimit(limit)));
    }

    /**
     * 获取留言列表
     */
    @Operation(summary = "获取留言列表")
    @GetMapping("/guestbook")
    public Result<List<GuestbookVO>> getGuestbookList() {
        List<TbGuestbook> list = guestbookMapper.selectList(
                new LambdaQueryWrapper<TbGuestbook>()
                        .eq(TbGuestbook::getDeleted, 0)
                        .orderByDesc(TbGuestbook::getCreateTime)
        );
        // 转换为 VO，过滤 ip 等敏感字段
        List<GuestbookVO> voList = list.stream().map(g -> {
            GuestbookVO vo = new GuestbookVO();
            BeanUtil.copyProperties(g, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 提交留言
     */
    @Operation(summary = "提交留言")
    @PostMapping("/guestbook")
    public Result<Void> createGuestbook(@Valid @RequestBody GuestbookCreateDTO dto, HttpServletRequest request) {
        // IP 频率限制：同一 IP 60 秒内只能留言一次（与评论/友链一致，防脚本刷屏）
        String clientIp = getClientIp(request);
        String rateLimitKey = "rate_limit:guestbook:" + clientIp;
        Boolean allowed = redisTemplate.opsForValue().setIfAbsent(rateLimitKey, 1, COMMENT_RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(allowed)) {
            return Result.error("留言太频繁，请稍后再试");
        }
        TbGuestbook guestbook = new TbGuestbook();
        BeanUtil.copyProperties(dto, guestbook);
        guestbook.setIp(clientIp);
        guestbook.setLikeCount(0);
        guestbookMapper.insert(guestbook);
        return Result.success();
    }

    /**
     * 留言点赞
     */
    @Operation(summary = "留言点赞")
    @PostMapping("/guestbook/{id}/like")
    public Result<Void> likeGuestbook(@PathVariable Long id, HttpServletRequest request) {
        String visitorId = getVisitorId(request);
        String redisKey = "guestbook_like:" + visitorId + ":" + id;
        // 检查是否已点赞
        Boolean alreadyLiked = redisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(alreadyLiked)) {
            return Result.error("您已经点过赞了");
        }
        // 原子自增点赞数，避免并发下的丢失更新
        int updated = guestbookMapper.update(null, new LambdaUpdateWrapper<TbGuestbook>()
                .eq(TbGuestbook::getId, id)
                .setSql("like_count = COALESCE(like_count, 0) + 1"));
        if (updated == 0) {
            return Result.error("留言不存在");
        }
        // 记录点赞状态到 Redis（设置过期，防止 key 无限累积）
        redisTemplate.opsForValue().set(redisKey, 1, GUESTBOOK_LIKE_TTL_DAYS, TimeUnit.DAYS);
        return Result.success();
    }

    /**
     * 获取所有专栏列表
     */
    @Operation(summary = "获取所有专栏", description = "前台获取所有启用的专栏列表")
    @GetMapping("/series/list")
    public Result<List<SeriesVO>> getSeriesList() {
        List<SeriesVO> series = seriesService.getAllSeries();
        return Result.success(series);
    }

    /**
     * 获取专栏详情
     */
    @Operation(summary = "获取专栏详情", description = "前台获取专栏详情及文章列表")
    @GetMapping("/series/{id}")
    public Result<SeriesDetailVO> getSeriesDetail(@PathVariable Long id) {
        SeriesDetailVO detail = seriesService.getSeriesDetail(id);
        return Result.success(detail);
    }

    /**
     * 获取访客位置数据（地图打点用）
     */
    @Operation(summary = "获取访客位置数据", description = "前台获取访客位置列表（脱敏，不含IP），用于地图打点展示")
    @GetMapping("/visitor-map/locations")
    public Result<List<VisitorLocationVO>> getVisitorLocations() {
        return Result.success(visitorLocationService.getAllLocations());
    }

    /**
     * 获取已通过的友情链接列表
     */
    @Operation(summary = "获取友链列表", description = "前台获取已通过的友情链接列表")
    @GetMapping("/friendlink/list")
    public Result<List<TbFriendLink>> getFriendLinkList() {
        return Result.success(friendLinkService.getApprovedLinks());
    }

    /**
     * 获取开源项目列表（前台公开）
     */
    @Operation(summary = "获取开源项目列表", description = "前台获取开源项目列表，置顶优先")
    @GetMapping("/open-source/list")
    public Result<List<TbOpenSource>> getOpenSourceList() {
        return Result.success(openSourceService.getFrontList());
    }

    /**
     * 访客申请友链（提交后进入待审核状态）
     */
    @Operation(summary = "申请友链", description = "前台访客提交友链申请，需站长审核")
    @PostMapping("/friendlink/apply")
    public Result<Void> applyFriendLink(@Valid @RequestBody FriendLinkApplyDTO dto,
                                        HttpServletRequest request) {
        // IP 频率限制：同一 IP 60 秒内只能申请一次（防刷）
        String clientIp = getClientIp(request);
        String rateLimitKey = "rate_limit:friendlink:" + clientIp;
        Boolean allowed = redisTemplate.opsForValue().setIfAbsent(rateLimitKey, 1, COMMENT_RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(allowed)) {
            return Result.error("提交太频繁，请稍后再试");
        }
        TbFriendLink link = new TbFriendLink();
        BeanUtil.copyProperties(dto, link);
        friendLinkService.apply(link);
        return Result.success();
    }

    /**
     * 获取相册照片列表（可按分组筛选）
     */
    @Operation(summary = "获取相册列表", description = "前台获取所有显示状态的照片，可按分组筛选")
    @GetMapping("/album/list")
    public Result<List<TbAlbumPhoto>> getAlbumList(
            @RequestParam(value = "groupId", required = false) Long groupId) {
        return Result.success(albumService.getVisiblePhotos(groupId));
    }

    /**
     * 获取相册分组列表
     */
    @Operation(summary = "获取相册分组", description = "前台获取所有显示状态的相册分组")
    @GetMapping("/album/groups")
    public Result<List<TbAlbumGroup>> getAlbumGroups() {
        return Result.success(albumService.getVisibleGroups());
    }

    /**
     * 获取站点统计（页脚「本站已运行」卡片）
     */
    @Operation(summary = "获取站点统计", description = "前台获取总访问量与建站天数")
    @GetMapping("/site-stats")
    public Result<Map<String, Object>> getSiteStats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalVisits", visitService.getVisitCount());
        long runDays = 0;
        try {
            LocalDate buildDate = LocalDate.parse(siteBuildDate);
            runDays = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(buildDate, LocalDate.now()));
        } catch (Exception e) {
            log.warn("解析建站日期失败: {}", siteBuildDate);
        }
        result.put("runDays", runDays);
        return Result.success(result);
    }

    /**
     * 获取最近访客
     */
    @Operation(summary = "获取最近访客", description = "前台获取最近访客位置列表（脱敏，不含IP）")
    @GetMapping("/visitor-map/recent")
    public Result<List<VisitorLocationVO>> getRecentVisitors(
            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        return Result.success(visitorLocationService.getRecentLocations(limit));
    }

    /**
     * 获取访客城市统计
     */
    @Operation(summary = "获取访客城市统计", description = "按城市统计访客数量")
    @GetMapping("/visitor-map/city-stats")
    public Result<List<Map<String, Object>>> getVisitorCityStats() {
        return Result.success(visitorLocationService.getCityStats());
    }

    /**
     * 获取访客国家统计
     */
    @Operation(summary = "获取访客国家统计", description = "按国家统计访客数量")
    @GetMapping("/visitor-map/country-stats")
    public Result<List<Map<String, Object>>> getVisitorCountryStats() {
        return Result.success(visitorLocationService.getCountryStats());
    }

    /**
     * 生成访客标识（IP + UserAgent 的 hash）
     */
    private String getVisitorId(HttpServletRequest request) {
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        if (StrUtil.isBlank(ua)) ua = "unknown";
        return DigestUtil.md5Hex(ip + ":" + ua);
    }

    /**
     * 获取客户端真实IP（统一走 ClientIpUtils，XFF 取末位防伪造）
     */
    private String getClientIp(HttpServletRequest request) {
        return ClientIpUtils.resolve(request);
    }

    /**
     * 限制前台 TOP N 类接口的返回条数在合法范围内
     */
    private static int clampFrontLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 5;
        }
        return Math.min(limit, MAX_FRONT_LIMIT);
    }
}
