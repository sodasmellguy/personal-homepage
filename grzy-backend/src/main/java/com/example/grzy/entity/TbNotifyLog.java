package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮件通知日志实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_notify_log")
public class TbNotifyLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 通知类型：comment-新评论, reply-评论回复, friendlink-友链申请, publish-定时发布, security-安全告警 */
    private String type;

    /** 邮件标题 */
    private String title;

    /** 收件邮箱 */
    private String recipient;

    /** 发送状态：0-失败，1-成功 */
    private Integer status;

    /** 失败原因 */
    private String errorMsg;

    /** 重试次数 */
    private Integer retryCount;

    private LocalDateTime createTime;
}
