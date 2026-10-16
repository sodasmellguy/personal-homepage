package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 专栏文章关联实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_series_article")
public class TbSeriesArticle implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 专栏ID
     */
    private Long seriesId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 排序
     */
    private Integer sort;
}
