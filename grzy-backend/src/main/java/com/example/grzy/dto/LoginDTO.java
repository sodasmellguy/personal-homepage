package com.example.grzy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求DTO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "登录请求参数")
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度在 3 到 20 个字符")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在 6 到 20 个字符")
    @Schema(description = "密码", example = "123456")
    private String password;
}
