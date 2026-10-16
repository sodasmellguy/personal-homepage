package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文章每日统计数据实体（定时任务快照）
 *
 * @author sodasmell
 */
@Data
@TableName("tb_article_daily_stats")
public class TbArticleDailyStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 截至当日的累计阅读量快照（用于差分计算）
     */
    private Integer totalCount;

    /**
     * 当日新增阅读量
     */
    private Integer viewCount;

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
