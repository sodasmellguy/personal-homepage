package com.example.grzy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 评论创建 DTO
 * 防止用户篡改 id、ip、status、deleted 等系统字段
 *
 * @author sodasmell
 */
@Data
@Schema(description = "评论创建参数")
public class CommentCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID")
    private Long articleId;

    @Schema(description = "父评论ID（0为顶级评论）")
    private Long parentId;

    @Schema(description = "回复的评论ID")
    private Long replyToId;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最多50个字符")
    @Schema(description = "评论者昵称")
    private String nickname;

    @Schema(description = "评论者邮箱（用于回复通知）")
    private String email;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容最多1000个字符")
    @Schema(description = "评论内容")
    private String content;
}
