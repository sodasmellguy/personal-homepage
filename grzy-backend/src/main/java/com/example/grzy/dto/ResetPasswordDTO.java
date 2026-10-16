package com.example.grzy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 重置密码请求DTO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "重置密码请求参数")
public class ResetPasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在 6 到 20 个字符")
    @Schema(description = "新密码")
    private String password;
}
