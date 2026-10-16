package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "评论响应数据")
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "文章ID")
    private Long articleId;

    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "回复的评论ID")
    private Long replyToId;

    @Schema(description = "回复的评论昵称")
    private String replyToNickname;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "状态：0-待审核，1-已通过")
    private Integer status;

    @Schema(description = "子评论列表")
    private List<CommentVO> children;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
