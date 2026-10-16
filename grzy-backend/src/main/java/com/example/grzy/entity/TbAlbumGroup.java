package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 相册分组实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_album_group")
public class TbAlbumGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组描述
     */
    private String description;

    /**
     * 分组封面图
     */
    private String coverImage;

    /**
     * 排序（越小越靠前）
     */
    private Integer sort;

    /**
     * 状态：0-隐藏，1-显示
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
