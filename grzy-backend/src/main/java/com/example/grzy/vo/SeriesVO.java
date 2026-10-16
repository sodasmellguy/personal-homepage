package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专栏VO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "专栏响应数据")
public class SeriesVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "专栏名称")
    private String name;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "专栏描述")
    private String description;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "文章数量")
    private Integer articleCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
