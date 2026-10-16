package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 个人信息VO
 *
 * @author sodasmell
 */
@Data
@Schema(description = "个人信息响应数据")
public class ProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "学校")
    private String university;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "微信号")
    private String wechat;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "个人介绍")
    private String introduction;

    @Schema(description = "职业目标")
    private String careerGoal;

    @Schema(description = "Gitee地址")
    private String giteeUrl;

    @Schema(description = "GitHub地址")
    private String githubUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
