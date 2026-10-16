package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 获奖经历实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_award")
public class TbAward implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 奖项名称
     */
    private String title;

    /**
     * 颁发机构
     */
    private String issuer;

    /**
     * 获奖日期
     */
    private LocalDate awardDate;

    /**
     * 奖项描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0-禁用，1-启用
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
