package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 友情链接实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_friend_link")
public class TbFriendLink implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 站点名称
     */
    private String name;

    /**
     * 站点链接
     */
    private String url;

    /**
     * 站点图标/头像
     */
    private String logo;

    /**
     * 站点描述
     */
    private String description;

    /**
     * 状态：0-待审核，1-已通过，2-已驳回
     */
    private Integer status;

    /**
     * 排序（越小越靠前）
     */
    private Integer sort;

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
