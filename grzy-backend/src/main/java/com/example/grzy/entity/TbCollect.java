package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_collect")
public class TbCollect implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private String visitorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
