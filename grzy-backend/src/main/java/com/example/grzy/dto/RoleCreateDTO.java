package com.example.grzy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色创建/更新 DTO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "角色创建/更新参数")
public class RoleCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称最多50个字符")
    @Schema(description = "角色名称")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码最多50个字符")
    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
