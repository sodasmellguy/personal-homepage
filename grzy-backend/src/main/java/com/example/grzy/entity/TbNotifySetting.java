package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮件通知设置实体（单行表，id 固定为 1）
 *
 * @author sodasmell
 */
@Data
@TableName("tb_notify_setting")
public class TbNotifySetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long id;

    /** 站长收件邮箱（空则使用 app.admin-email 配置） */
    private String adminEmail;

    /** 新评论通知站长：0-关，1-开 */
    private Integer enableCommentNotify;

    /** 评论回复通知访客：0-关，1-开 */
    private Integer enableReplyNotify;

    /** 友链申请通知站长：0-关，1-开 */
    private Integer enableFriendlinkNotify;

    /** 定时发布结果通知：0-关，1-开 */
    private Integer enablePublishNotify;

    /** 安全告警通知：0-关，1-开 */
    private Integer enableSecurityNotify;

    /** 同类安全告警最小间隔（分钟），防止邮件轰炸 */
    private Integer alertIntervalMinutes;

    private LocalDateTime updateTime;
}
