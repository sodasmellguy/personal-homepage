package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_like")
public class TbLike implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private String visitorId; // IP + UA hash for anonymous users
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
