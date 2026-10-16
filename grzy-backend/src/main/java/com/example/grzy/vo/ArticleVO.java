package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章VO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "文章响应数据")
public class ArticleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "标签列表")
    private List<String> tagList;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "点赞次数")
    private Integer likeCount;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "状态：0-草稿，1-已发布")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "评论数量")
    private Integer commentCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
