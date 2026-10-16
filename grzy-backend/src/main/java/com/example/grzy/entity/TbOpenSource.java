package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 开源项目实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_open_source")
public class TbOpenSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 项目名称 */
    private String name;

    /** 一句话简介 */
    private String description;

    /** 详细描述（选填） */
    private String detail;

    /** 仓库地址（Gitee/GitHub） */
    private String repoUrl;

    /** 在线体验地址（选填） */
    private String demoUrl;

    /** 项目图标（选填） */
    private String logo;

    /** 主语言（Java/Vue等） */
    private String language;

    /** 技术栈标签，逗号分隔 */
    private String techStack;

    /** 开源协议：MIT/Apache-2.0等 */
    private String license;

    /** Star 数（手动填或同步覆盖） */
    private Integer stars;

    /** Fork 数（手动填或同步覆盖） */
    private Integer forks;

    /** 状态：0-已归档，1-维护中 */
    private Integer status;

    /** 是否置顶：0-否，1-是 */
    private Integer isTop;

    /** 排序，越小越前 */
    private Integer sort;

    /** 最近同步时间（NULL=从未同步） */
    private LocalDateTime syncTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    private Integer deleted;
}
