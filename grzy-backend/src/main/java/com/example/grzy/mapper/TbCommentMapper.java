package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.TbComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 评论Mapper
 *
 * @author sodasmell
 */
@Mapper
public interface TbCommentMapper extends BaseMapper<TbComment> {

    /**
     * 指定时间起按文章聚合有效评论数量
     */
    List<Map<String, Object>> selectCountGroupByArticle(@Param("startTime") LocalDateTime startTime);
}
