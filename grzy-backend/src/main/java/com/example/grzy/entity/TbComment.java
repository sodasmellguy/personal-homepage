package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_comment")
public class TbComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 父评论ID(0为顶级评论)
     */
    private Long parentId;

    /**
     * 回复的评论ID
     */
    private Long replyToId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 状态：0-待审核，1-已通过
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
