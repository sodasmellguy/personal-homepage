package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 技术栈VO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "技术栈响应数据")
public class TechStackVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "技术名称")
    private String name;

    @Schema(description = "图标URL")
    private String icon;

    @Schema(description = "是否Featured(首页大图标)")
    private Integer featured;

    @Schema(description = "排序")
    private Integer sort;
}
