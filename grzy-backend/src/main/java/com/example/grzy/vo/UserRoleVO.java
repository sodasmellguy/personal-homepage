package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户角色关联VO（用于批量查询用户角色）
 *
 * @author sodasmell
 */
@Data
@Schema(description = "用户角色关联信息")
public class UserRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "角色名称")
    private String roleName;
}
