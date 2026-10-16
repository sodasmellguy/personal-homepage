package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实习经历VO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "实习经历响应数据")
public class InternshipVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司名称")
    private String company;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "工作描述")
    private String description;

    @Schema(description = "工作成果")
    private String achievements;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
