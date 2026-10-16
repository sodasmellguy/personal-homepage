package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.TbArticleDailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 文章每日统计数据 Mapper
 *
 * @author sodasmell
 */
@Mapper
public interface TbArticleDailyStatsMapper extends BaseMapper<TbArticleDailyStats> {

    /**
     * 按日期范围聚合每日新增阅读量（可指定文章）
     */
    List<Map<String, Object>> selectViewTrend(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("articleId") Long articleId);

    /**
     * 查询指定日期之前每个文章最近一次快照的累计阅读量（用于差分计算当日新增）
     */
    List<Map<String, Object>> selectLatestSnapshotBefore(@Param("date") LocalDate date);
}
