package com.example.grzy.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 仪表盘数据 VO
 *
 * @author sodasmell
 */
@Data
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总访问量 */
    private Long visitCount;

    /** 文章数量 */
    private Long articleCount;

    /** 项目数量 */
    private Long projectCount;

    /** 技能数量 */
    private Long skillCount;

    /** 实习经历数量 */
    private Long internshipCount;

    /** 获奖数量 */
    private Long awardCount;

    /** 评论数量 */
    private Long commentCount;

    /** 最近操作日志 */
    private List<LogItem> recentLogs;

    @Data
    public static class LogItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String module;
        private String operation;
        private String username;
        private LocalDateTime createTime;
    }
}
