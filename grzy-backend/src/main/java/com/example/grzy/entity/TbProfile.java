package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 个人信息实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_profile")
public class TbProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 学校
     */
    private String university;

    /**
     * 专业
     */
    private String major;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 微信号
     */
    private String wechat;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 个人介绍
     */
    private String introduction;

    /**
     * 职业目标
     */
    private String careerGoal;

    /**
     * Gitee地址
     */
    private String giteeUrl;

    /**
     * GitHub地址
     */
    private String githubUrl;

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
}
