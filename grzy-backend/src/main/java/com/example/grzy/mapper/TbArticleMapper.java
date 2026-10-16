package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.TbArticle;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 文章Mapper
 *
 * @author sodasmell
 */
@Mapper
public interface TbArticleMapper extends BaseMapper<TbArticle> {

    /**
     * 按分类聚合正常文章数量（空分类归入「未分类」）
     */
    List<Map<String, Object>> selectCategoryStats();
}
