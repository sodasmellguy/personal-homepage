package com.example.grzy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 文章创建/更新 DTO
 * 防止 Mass Assignment 漏洞，只暴露用户可控制的字段
 *
 * @author sodasmell
 */
@Data
@Schema(description = "文章创建/更新参数")
public class ArticleCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "标题最多200个字符")
    @Schema(description = "文章标题", example = "我的第一篇文章")
    private String title;

    @Schema(description = "文章摘要")
    private String summary;

    @NotBlank(message = "文章内容不能为空")
    @Schema(description = "文章内容（Markdown）")
    private String content;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "标签（逗号分隔）")
    private String tags;

    @Schema(description = "是否置顶：0-否，1-是")
    private Integer isTop;

    @Schema(description = "状态：0-草稿，1-已发布")
    private Integer status;

    @Schema(description = "排序值")
    private Integer sort;
}
