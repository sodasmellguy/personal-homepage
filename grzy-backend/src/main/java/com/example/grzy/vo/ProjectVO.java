package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目VO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "项目响应数据")
public class ProjectVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "项目名称")
    private String title;

    @Schema(description = "项目描述")
    private String description;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "项目截图（多图，JSON数组格式）")
    private String images;

    @Schema(description = "技术栈")
    private String techStack;

    @Schema(description = "职责描述")
    private String responsibilities;

    @Schema(description = "项目链接")
    private String projectUrl;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
