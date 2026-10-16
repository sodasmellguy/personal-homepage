package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章详情VO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "文章详情响应数据")
public class ArticleDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "文章内容")
    private String content;

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

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "评论数量")
    private Integer commentCount;

    @Schema(description = "评论列表")
    private List<CommentVO> comments;

    @Schema(description = "上一篇文章ID")
    private Long prevId;

    @Schema(description = "上一篇文章标题")
    private String prevTitle;

    @Schema(description = "下一篇文章ID")
    private Long nextId;

    @Schema(description = "下一篇文章标题")
    private String nextTitle;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
