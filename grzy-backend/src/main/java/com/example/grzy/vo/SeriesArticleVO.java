package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专栏文章VO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "专栏文章响应数据")
public class SeriesArticleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
