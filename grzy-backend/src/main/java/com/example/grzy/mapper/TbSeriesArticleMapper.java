package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.TbSeriesArticle;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 专栏文章关联Mapper
 *
 * @author sodasmell
 */
@Mapper
public interface TbSeriesArticleMapper extends BaseMapper<TbSeriesArticle> {

    /**
     * 按专栏聚合文章数量
     */
    List<Map<String, Object>> selectCountGroupBySeries();
}
