package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbSeries;
import com.example.grzy.vo.SeriesDetailVO;
import com.example.grzy.vo.SeriesVO;

import java.util.List;

/**
 * 专栏服务接口
 *
 * @author sodasmell
 */
public interface SeriesService extends IService<TbSeries> {

    /**
     * 分页查询专栏列表（后台）
     */
    PageResult<SeriesVO> getSeriesPage(Integer pageNum, Integer pageSize);

    /**
     * 获取所有启用的专栏（前台）
     */
    List<SeriesVO> getAllSeries();

    /**
     * 获取专栏详情（含文章列表）
     */
    SeriesDetailVO getSeriesDetail(Long seriesId);

    /**
     * 创建专栏
     */
    void createSeries(TbSeries series, List<Long> articleIds);

    /**
     * 更新专栏
     */
    void updateSeries(Long seriesId, TbSeries series, List<Long> articleIds);

    /**
     * 删除专栏
     */
    void deleteSeries(Long seriesId);
}
