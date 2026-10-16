package com.example.grzy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.ResultCode;
import com.example.grzy.entity.TbArticle;
import com.example.grzy.entity.TbCollect;
import com.example.grzy.entity.TbComment;
import com.example.grzy.entity.TbLike;
import com.example.grzy.mapper.TbArticleMapper;
import com.example.grzy.mapper.TbCollectMapper;
import com.example.grzy.mapper.TbCommentMapper;
import com.example.grzy.mapper.TbLikeMapper;
import com.example.grzy.service.ArticleService;
import com.example.grzy.service.OperLogService;
import com.example.grzy.vo.ArticleDetailVO;
import com.example.grzy.vo.ArticleVO;
import com.example.grzy.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<TbArticleMapper, TbArticle> implements ArticleService {

    private final TbArticleMapper articleMapper;
    private final TbCommentMapper commentMapper;
    private final TbLikeMapper likeMapper;
    private final TbCollectMapper collectMapper;
    private final OperLogService operLogService;

    @Override
    public PageResult<ArticleVO> getArticlePage(Integer pageNum, Integer pageSize, String title, Integer status, String category) {
        LambdaQueryWrapper<TbArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbArticle::getDeleted, CommonConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(title)) {
            wrapper.like(TbArticle::getTitle, title);
        }
        if (status != null) {
            wrapper.eq(TbArticle::getStatus, status);
        }
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(TbArticle::getCategory, category);
        }
        wrapper.orderByDesc(TbArticle::getIsTop).orderByAsc(TbArticle::getSort).orderByDesc(TbArticle::getCreateTime);

        IPage<TbArticle> page = new Page<>(pageNum, pageSize);
        IPage<TbArticle> result = articleMapper.selectPage(page, wrapper);

        List<ArticleVO> voList = toVOBatch(result.getRecords());
        return PageResult.of(result.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public PageResult<ArticleVO> getFrontArticlePage(Integer pageNum, Integer pageSize, String category, String keyword, String sortType, String tag) {
        LambdaQueryWrapper<TbArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbArticle::getDeleted, CommonConstant.NOT_DELETED);
        wrapper.eq(TbArticle::getStatus, CommonConstant.STATUS_NORMAL);
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(TbArticle::getCategory, category);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(TbArticle::getTitle, keyword).or().like(TbArticle::getSummary, keyword));
        }
        if (StrUtil.isNotBlank(tag)) {
            wrapper.like(TbArticle::getTags, tag);
        }
        // 排序：置顶优先 → 排序字段 → 按 sortType
        wrapper.orderByDesc(TbArticle::getIsTop);
        wrapper.orderByAsc(TbArticle::getSort);
        if ("hot".equals(sortType)) {
            wrapper.orderByDesc(TbArticle::getViewCount);
        } else {
            wrapper.orderByDesc(TbArticle::getCreateTime);
        }

        IPage<TbArticle> page = new Page<>(pageNum, pageSize);
        IPage<TbArticle> result = articleMapper.selectPage(page, wrapper);

        List<ArticleVO> voList = toVOBatch(result.getRecords());
        return PageResult.of(result.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleDetailVO getArticleDetail(Long articleId) {
        TbArticle article = articleMapper.selectById(articleId);
        if (article == null || CommonConstant.DELETED.equals(article.getDeleted())) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 转换为详情VO
        ArticleDetailVO vo = new ArticleDetailVO();
        BeanUtil.copyProperties(article, vo);
        vo.setTagList(parseTags(article.getTags()));

        // 获取评论树
        List<TbComment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<TbComment>()
                        .eq(TbComment::getArticleId, articleId)
                        .eq(TbComment::getStatus, CommonConstant.STATUS_NORMAL)
                        .eq(TbComment::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(TbComment::getCreateTime)
        );
        vo.setComments(buildCommentTree(comments, 0L));
        vo.setCommentCount(comments.size());

        // 上一篇
        TbArticle prev = articleMapper.selectOne(
                new LambdaQueryWrapper<TbArticle>()
                        .eq(TbArticle::getStatus, CommonConstant.STATUS_NORMAL)
                        .eq(TbArticle::getDeleted, CommonConstant.NOT_DELETED)
                        .lt(TbArticle::getCreateTime, article.getCreateTime())
                        .orderByDesc(TbArticle::getCreateTime)
                        .last("LIMIT 1")
        );
        if (prev != null) {
            vo.setPrevId(prev.getId());
            vo.setPrevTitle(prev.getTitle());
        }

        // 下一篇
        TbArticle next = articleMapper.selectOne(
                new LambdaQueryWrapper<TbArticle>()
                        .eq(TbArticle::getStatus, CommonConstant.STATUS_NORMAL)
                        .eq(TbArticle::getDeleted, CommonConstant.NOT_DELETED)
                        .gt(TbArticle::getCreateTime, article.getCreateTime())
                        .orderByAsc(TbArticle::getCreateTime)
                        .last("LIMIT 1")
        );
        if (next != null) {
            vo.setNextId(next.getId());
            vo.setNextTitle(next.getTitle());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createArticle(TbArticle article) {
        article.setViewCount(article.getViewCount() != null ? article.getViewCount() : 0);
        article.setLikeCount(article.getLikeCount() != null ? article.getLikeCount() : 0);
        article.setIsTop(article.getIsTop() != null ? article.getIsTop() : 0);
        article.setStatus(article.getStatus() != null ? article.getStatus() : CommonConstant.STATUS_NORMAL);
        article.setSort(article.getSort() != null ? article.getSort() : 0);
        articleMapper.insert(article);
        log.info("创建文章成功: {}", article.getTitle());
        operLogService.log("文章管理", "创建了文章: " + article.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long articleId, TbArticle article) {
        TbArticle existing = articleMapper.selectById(articleId);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        article.setId(articleId);
        articleMapper.updateById(article);
        log.info("更新文章成功: {}", article.getTitle());
        operLogService.log("文章管理", "更新了文章: " + article.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long articleId) {
        TbArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        articleMapper.deleteById(articleId);
        log.info("删除文章成功: {}", article.getTitle());
        operLogService.log("文章管理", "删除了文章: " + article.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long articleId, String visitorId) {
        TbArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // 检查是否已点赞
        Long count = likeMapper.selectCount(
                new LambdaQueryWrapper<TbLike>()
                        .eq(TbLike::getArticleId, articleId)
                        .eq(TbLike::getVisitorId, visitorId)
        );
        if (count > 0) {
            return; // 已点赞，静默返回
        }
        TbLike like = new TbLike();
        like.setArticleId(articleId);
        like.setVisitorId(visitorId);
        likeMapper.insert(like);
        // 原子更新：避免并发下的丢失更新
        articleMapper.update(null, new LambdaUpdateWrapper<TbArticle>()
                .eq(TbArticle::getId, articleId)
                .setSql("like_count = like_count + 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeArticle(Long articleId, String visitorId) {
        TbArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        int deleted = likeMapper.delete(
                new LambdaQueryWrapper<TbLike>()
                        .eq(TbLike::getArticleId, articleId)
                        .eq(TbLike::getVisitorId, visitorId)
        );
        if (deleted > 0) {
            // 原子更新：避免并发下的丢失更新
            articleMapper.update(null, new LambdaUpdateWrapper<TbArticle>()
                    .eq(TbArticle::getId, articleId)
                    .setSql("like_count = GREATEST(0, like_count - 1)"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Long articleId, String visitorId) {
        TbArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        Long count = collectMapper.selectCount(
                new LambdaQueryWrapper<TbCollect>()
                        .eq(TbCollect::getArticleId, articleId)
                        .eq(TbCollect::getVisitorId, visitorId)
        );
        if (count > 0) {
            return; // 已收藏
        }
        TbCollect collect = new TbCollect();
        collect.setArticleId(articleId);
        collect.setVisitorId(visitorId);
        collectMapper.insert(collect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticle(Long articleId, String visitorId) {
        collectMapper.delete(
                new LambdaQueryWrapper<TbCollect>()
                        .eq(TbCollect::getArticleId, articleId)
                        .eq(TbCollect::getVisitorId, visitorId)
        );
    }

    @Override
    public PageResult<ArticleVO> getCollectedArticles(String visitorId, Integer pageNum, Integer pageSize) {
        // 查询收藏的文章ID列表
        IPage<TbCollect> collectPage = new Page<>(pageNum, pageSize);
        IPage<TbCollect> collects = collectMapper.selectPage(collectPage,
                new LambdaQueryWrapper<TbCollect>()
                        .eq(TbCollect::getVisitorId, visitorId)
                        .orderByDesc(TbCollect::getCreateTime)
        );
        if (collects.getRecords().isEmpty()) {
            return PageResult.of(0L, new ArrayList<>(), pageNum, pageSize);
        }
        List<Long> articleIds = collects.getRecords().stream().map(TbCollect::getArticleId).collect(Collectors.toList());
        List<TbArticle> articles = articleMapper.selectBatchIds(articleIds);
        List<ArticleVO> voList = toVOBatch(articles);
        return PageResult.of(collects.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public List<Map<String, Object>> getArchives() {
        // 只查归档所需列，避免整表（含 content 大字段）载入内存
        List<TbArticle> articles = articleMapper.selectList(
                new LambdaQueryWrapper<TbArticle>()
                        .select(TbArticle::getId, TbArticle::getTitle, TbArticle::getCreateTime, TbArticle::getCategory)
                        .eq(TbArticle::getDeleted, CommonConstant.NOT_DELETED)
                        .eq(TbArticle::getStatus, CommonConstant.STATUS_NORMAL)
                        .orderByDesc(TbArticle::getCreateTime)
        );
        // 按年月分组
        Map<Integer, Map<Integer, List<TbArticle>>> grouped = articles.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCreateTime().getYear(),
                        Collectors.groupingBy(a -> a.getCreateTime().getMonthValue())
                ));
        List<Map<String, Object>> result = new ArrayList<>();
        grouped.entrySet().stream()
                .sorted(Map.Entry.<Integer, Map<Integer, List<TbArticle>>>comparingByKey().reversed())
                .forEach(yearEntry -> {
                    Map<String, Object> yearMap = new LinkedHashMap<>();
                    yearMap.put("year", yearEntry.getKey());
                    List<Map<String, Object>> months = new ArrayList<>();
                    yearEntry.getValue().entrySet().stream()
                            .sorted(Map.Entry.<Integer, List<TbArticle>>comparingByKey().reversed())
                            .forEach(monthEntry -> {
                                Map<String, Object> monthMap = new LinkedHashMap<>();
                                monthMap.put("month", monthEntry.getKey());
                                List<Map<String, Object>> articleList = monthEntry.getValue().stream().map(a -> {
                                    Map<String, Object> am = new LinkedHashMap<>();
                                    am.put("id", a.getId());
                                    am.put("title", a.getTitle());
                                    am.put("createTime", a.getCreateTime());
                                    am.put("category", a.getCategory());
                                    return am;
                                }).collect(Collectors.toList());
                                monthMap.put("articles", articleList);
                                months.add(monthMap);
                            });
                    yearMap.put("months", months);
                    result.add(yearMap);
                });
        return result;
    }

    @Override
    public List<Map<String, Object>> getArticleStats() {
        // SQL GROUP BY 聚合，避免全表载入内存统计
        return articleMapper.selectCategoryStats();
    }

    @Override
    public List<Map<String, Object>> getTopArticles(Integer limit) {
        // 只查排行展示所需列，避免 content 大字段进内存
        List<TbArticle> articles = articleMapper.selectList(
                new LambdaQueryWrapper<TbArticle>()
                        .select(TbArticle::getId, TbArticle::getTitle, TbArticle::getViewCount)
                        .eq(TbArticle::getDeleted, CommonConstant.NOT_DELETED)
                        .eq(TbArticle::getStatus, CommonConstant.STATUS_NORMAL)
                        .orderByDesc(TbArticle::getViewCount)
                        .last("LIMIT " + limit)
        );
        return articles.stream().map(a -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("title", a.getTitle());
            item.put("viewCount", a.getViewCount());
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public void incrementArticleView(Long articleId) {
        // 原子更新：避免并发下的丢失更新
        articleMapper.update(null, new LambdaUpdateWrapper<TbArticle>()
                .eq(TbArticle::getId, articleId)
                .setSql("view_count = COALESCE(view_count, 0) + 1"));
    }

    @Override
    public List<Map<String, Object>> getHotTags() {
        // 只查 tags 列做内存拆分统计（逗号分隔的标签无法直接 GROUP BY），避免 content 大字段进内存
        List<TbArticle> articles = articleMapper.selectList(
                new LambdaQueryWrapper<TbArticle>()
                        .select(TbArticle::getTags)
                        .eq(TbArticle::getDeleted, CommonConstant.NOT_DELETED)
                        .eq(TbArticle::getStatus, CommonConstant.STATUS_NORMAL)
                        .isNotNull(TbArticle::getTags)
        );
        Map<String, Long> tagCount = articles.stream()
                .flatMap(a -> parseTags(a.getTags()).stream())
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.groupingBy(String::trim, Collectors.counting()));
        return tagCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 实体转VO（单条，保留兼容）
     */
    private ArticleVO toVO(TbArticle article) {
        ArticleVO vo = new ArticleVO();
        BeanUtil.copyProperties(article, vo);
        vo.setTagList(parseTags(article.getTags()));

        // 查询评论数量
        Long commentCount = commentMapper.selectCount(
                new LambdaQueryWrapper<TbComment>()
                        .eq(TbComment::getArticleId, article.getId())
                        .eq(TbComment::getStatus, CommonConstant.STATUS_NORMAL)
                        .eq(TbComment::getDeleted, CommonConstant.NOT_DELETED)
        );
        vo.setCommentCount(commentCount.intValue());
        return vo;
    }

    /**
     * 批量转换VO（解决 N+1 查询问题）
     * 一次性查询所有文章的评论数，避免逐条查询
     */
    private List<ArticleVO> toVOBatch(List<TbArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return new ArrayList<>();
        }
        // 批量查询评论数
        List<Long> articleIds = articles.stream().map(TbArticle::getId).collect(Collectors.toList());
        Map<Long, Integer> commentCountMap = batchQueryCommentCount(articleIds);

        return articles.stream().map(article -> {
            ArticleVO vo = new ArticleVO();
            BeanUtil.copyProperties(article, vo);
            vo.setTagList(parseTags(article.getTags()));
            vo.setCommentCount(commentCountMap.getOrDefault(article.getId(), 0));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 批量查询文章评论数（GROUP BY 优化）
     */
    private Map<Long, Integer> batchQueryCommentCount(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<TbComment> wrapper = new QueryWrapper<>();
        wrapper.select("article_id", "count(*) as cnt")
                .in("article_id", articleIds)
                .eq("status", CommonConstant.STATUS_NORMAL)
                .eq("deleted", CommonConstant.NOT_DELETED)
                .groupBy("article_id");
        List<Map<String, Object>> maps = commentMapper.selectMaps(wrapper);
        Map<Long, Integer> result = new HashMap<>();
        for (Map<String, Object> map : maps) {
            Long articleId = Long.valueOf(map.get("article_id").toString());
            Integer count = Integer.valueOf(map.get("cnt").toString());
            result.put(articleId, count);
        }
        return result;
    }

    /**
     * 解析标签
     */
    private List<String> parseTags(String tags) {
        if (StrUtil.isBlank(tags)) {
            return new ArrayList<>();
        }
        return Arrays.asList(tags.split(","));
    }

    /**
     * 构建评论树
     */
    private List<CommentVO> buildCommentTree(List<TbComment> allComments, Long parentId) {
        return allComments.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .map(c -> {
                    CommentVO vo = new CommentVO();
                    BeanUtil.copyProperties(c, vo);
                    // 安全：评论树仅用于前台公开接口，置空邮箱防止访客邮箱泄露（后台列表单独组装、保留邮箱）
                    vo.setEmail(null);
                    vo.setChildren(buildCommentTree(allComments, c.getId()));
                    // 设置回复昵称
                    if (c.getReplyToId() != null && c.getReplyToId() > 0) {
                        allComments.stream()
                                .filter(reply -> reply.getId().equals(c.getReplyToId()))
                                .findFirst()
                                .ifPresent(reply -> vo.setReplyToNickname(reply.getNickname()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
