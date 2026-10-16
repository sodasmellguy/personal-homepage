package com.example.grzy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.ResultCode;
import com.example.grzy.entity.TbArticle;
import com.example.grzy.entity.TbSeries;
import com.example.grzy.entity.TbSeriesArticle;
import com.example.grzy.mapper.TbArticleMapper;
import com.example.grzy.mapper.TbSeriesArticleMapper;
import com.example.grzy.mapper.TbSeriesMapper;
import com.example.grzy.service.OperLogService;
import com.example.grzy.service.SeriesService;
import com.example.grzy.vo.SeriesArticleVO;
import com.example.grzy.vo.SeriesDetailVO;
import com.example.grzy.vo.SeriesVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专栏服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeriesServiceImpl extends ServiceImpl<TbSeriesMapper, TbSeries> implements SeriesService {

    private final TbSeriesMapper seriesMapper;
    private final TbSeriesArticleMapper seriesArticleMapper;
    private final TbArticleMapper articleMapper;
    private final OperLogService operLogService;

    @Override
    public PageResult<SeriesVO> getSeriesPage(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TbSeries> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbSeries::getDeleted, CommonConstant.NOT_DELETED);
        wrapper.orderByAsc(TbSeries::getSort).orderByDesc(TbSeries::getCreateTime);

        IPage<TbSeries> page = new Page<>(pageNum, pageSize);
        IPage<TbSeries> result = seriesMapper.selectPage(page, wrapper);

        List<SeriesVO> voList = toVOBatch(result.getRecords());
        return PageResult.of(result.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public List<SeriesVO> getAllSeries() {
        LambdaQueryWrapper<TbSeries> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbSeries::getDeleted, CommonConstant.NOT_DELETED);
        wrapper.eq(TbSeries::getStatus, CommonConstant.STATUS_NORMAL);
        wrapper.orderByAsc(TbSeries::getSort).orderByDesc(TbSeries::getCreateTime);

        List<TbSeries> list = seriesMapper.selectList(wrapper);
        return toVOBatch(list);
    }

    @Override
    public SeriesDetailVO getSeriesDetail(Long seriesId) {
        TbSeries series = seriesMapper.selectById(seriesId);
        if (series == null || CommonConstant.DELETED.equals(series.getDeleted())) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        SeriesDetailVO vo = new SeriesDetailVO();
        BeanUtil.copyProperties(series, vo);

        // 查询专栏下的文章关联
        List<TbSeriesArticle> seriesArticles = seriesArticleMapper.selectList(
                new LambdaQueryWrapper<TbSeriesArticle>()
                        .eq(TbSeriesArticle::getSeriesId, seriesId)
                        .orderByAsc(TbSeriesArticle::getSort)
        );

        if (!CollectionUtils.isEmpty(seriesArticles)) {
            List<Long> articleIds = seriesArticles.stream()
                    .map(TbSeriesArticle::getArticleId)
                    .collect(Collectors.toList());
            List<TbArticle> articles = articleMapper.selectBatchIds(articleIds);

            // 构建 articleId -> sort 的映射
            Map<Long, Integer> sortMap = seriesArticles.stream()
                    .collect(Collectors.toMap(TbSeriesArticle::getArticleId, TbSeriesArticle::getSort, (a, b) -> a));
            // 构建 articleId -> TbArticle 的映射
            Map<Long, TbArticle> articleMap = articles.stream()
                    .collect(Collectors.toMap(TbArticle::getId, a -> a, (a, b) -> a));

            List<SeriesArticleVO> articleVOs = seriesArticles.stream()
                    .filter(sa -> articleMap.containsKey(sa.getArticleId()))
                    .map(sa -> {
                        TbArticle article = articleMap.get(sa.getArticleId());
                        SeriesArticleVO articleVO = new SeriesArticleVO();
                        articleVO.setId(article.getId());
                        articleVO.setTitle(article.getTitle());
                        articleVO.setSummary(article.getSummary());
                        articleVO.setCoverImage(article.getCoverImage());
                        articleVO.setSort(sa.getSort());
                        articleVO.setCreateTime(article.getCreateTime());
                        return articleVO;
                    })
                    .collect(Collectors.toList());
            vo.setArticles(articleVOs);
        } else {
            vo.setArticles(new ArrayList<>());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSeries(TbSeries series, List<Long> articleIds) {
        series.setSort(series.getSort() != null ? series.getSort() : 0);
        series.setStatus(series.getStatus() != null ? series.getStatus() : CommonConstant.STATUS_NORMAL);
        seriesMapper.insert(series);

        // 保存专栏文章关联
        if (!CollectionUtils.isEmpty(articleIds)) {
            for (int i = 0; i < articleIds.size(); i++) {
                TbSeriesArticle sa = new TbSeriesArticle();
                sa.setSeriesId(series.getId());
                sa.setArticleId(articleIds.get(i));
                sa.setSort(i);
                seriesArticleMapper.insert(sa);
            }
        }

        log.info("创建专栏成功: {}", series.getName());
        operLogService.log("专栏管理", "创建了专栏: " + series.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeries(Long seriesId, TbSeries series, List<Long> articleIds) {
        TbSeries existing = seriesMapper.selectById(seriesId);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        series.setId(seriesId);
        seriesMapper.updateById(series);

        // 删除旧的专栏文章关联
        seriesArticleMapper.delete(
                new LambdaQueryWrapper<TbSeriesArticle>()
                        .eq(TbSeriesArticle::getSeriesId, seriesId)
        );

        // 插入新的专栏文章关联
        if (!CollectionUtils.isEmpty(articleIds)) {
            for (int i = 0; i < articleIds.size(); i++) {
                TbSeriesArticle sa = new TbSeriesArticle();
                sa.setSeriesId(seriesId);
                sa.setArticleId(articleIds.get(i));
                sa.setSort(i);
                seriesArticleMapper.insert(sa);
            }
        }

        log.info("更新专栏成功: {}", series.getName());
        operLogService.log("专栏管理", "更新了专栏: " + series.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSeries(Long seriesId) {
        TbSeries series = seriesMapper.selectById(seriesId);
        if (series == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        seriesMapper.deleteById(seriesId);

        // 删除专栏文章关联
        seriesArticleMapper.delete(
                new LambdaQueryWrapper<TbSeriesArticle>()
                        .eq(TbSeriesArticle::getSeriesId, seriesId)
        );

        log.info("删除专栏成功: {}", series.getName());
        operLogService.log("专栏管理", "删除了专栏: " + series.getName());
    }

    /**
     * 批量实体转VO（含文章数量）
     * 一次 GROUP BY 查出各专栏文章数，避免逐个 count（N+1）
     */
    private List<SeriesVO> toVOBatch(List<TbSeries> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        Map<Long, Integer> countMap = new HashMap<>();
        for (Map<String, Object> row : seriesArticleMapper.selectCountGroupBySeries()) {
            countMap.put(Long.valueOf(row.get("seriesId").toString()), Integer.valueOf(row.get("cnt").toString()));
        }
        return list.stream().map(series -> {
            SeriesVO vo = new SeriesVO();
            BeanUtil.copyProperties(series, vo);
            vo.setArticleCount(countMap.getOrDefault(series.getId(), 0));
            return vo;
        }).collect(Collectors.toList());
    }
}
