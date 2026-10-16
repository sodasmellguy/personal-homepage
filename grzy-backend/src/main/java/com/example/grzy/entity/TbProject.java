package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_project")
public class TbProject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称
     */
    private String title;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 项目截图（多图，JSON数组格式）
     */
    private String images;

    /**
     * 技术栈
     */
    private String techStack;

    /**
     * 职责描述
     */
    private String responsibilities;

    /**
     * 项目链接
     */
    private String projectUrl;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

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
