package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 获奖经历VO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "获奖经历响应数据")
public class AwardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "奖项名称")
    private String title;

    @Schema(description = "颁发机构")
    private String issuer;

    @Schema(description = "获奖日期")
    private LocalDate awardDate;

    @Schema(description = "奖项描述")
    private String description;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
