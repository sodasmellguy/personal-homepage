package com.example.grzy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户请求DTO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "用户请求参数")
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度在 3 到 20 个字符")
    @Schema(description = "用户名")
    private String username;

    @Size(min = 6, max = 20, message = "密码长度在 6 到 20 个字符")
    @Schema(description = "密码")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
}
