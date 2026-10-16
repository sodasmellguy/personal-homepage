package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.TbCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TbCollectMapper extends BaseMapper<TbCollect> {

    /**
     * 按天聚合收藏数量（可指定文章）
     */
    List<Map<String, Object>> selectCountGroupByDay(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime,
                                                    @Param("articleId") Long articleId);
}
