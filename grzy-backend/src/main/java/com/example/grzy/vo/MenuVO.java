package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单树VO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "菜单树节点")
public class MenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "类型：0-目录，1-菜单，2-按钮")
    private Integer type;

    @Schema(description = "权限标识")
    private String permission;

    @Schema(description = "子菜单")
    private List<MenuVO> children;
}
