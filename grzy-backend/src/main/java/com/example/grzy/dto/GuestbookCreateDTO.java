package com.example.grzy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 留言创建 DTO
 * 防止用户篡改 id、ip、likeCount、deleted 等系统字段
 *
 * @author sodasmell
 */
@Data
@Schema(description = "留言创建参数")
public class GuestbookCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最多50个字符")
    @Schema(description = "留言者昵称")
    private String nickname;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言内容最多500个字符")
    @Schema(description = "留言内容")
    private String content;

    @Schema(description = "留言卡片颜色")
    private String color;
}
