package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_article")
public class TbArticle implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签(逗号分隔)
     */
    private String tags;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;

    /**
     * 状态：0-草稿，1-已发布，2-定时发布
     */
    private Integer status;

    /**
     * 定时发布时间（status=2 时生效，到达时间后自动发布）
     */
    private LocalDateTime publishTime;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
