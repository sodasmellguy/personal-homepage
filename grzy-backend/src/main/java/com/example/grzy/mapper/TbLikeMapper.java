package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.TbLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TbLikeMapper extends BaseMapper<TbLike> {

    /**
     * 按天聚合点赞数量（可指定文章）
     */
    List<Map<String, Object>> selectCountGroupByDay(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime,
                                                    @Param("articleId") Long articleId);

    /**
     * 指定时间起按文章聚合点赞数量
     */
    List<Map<String, Object>> selectCountGroupByArticle(@Param("startTime") LocalDateTime startTime);
}
