package com.example.grzy.service;

/**
 * 访问量统计服务
 *
 * @author sodasmell
 */
public interface VisitService {

    /**
     * 访问量 +1（同时记录当日趋势）
     */
    void incrementVisit();

    /**
     * 获取总访问量
     */
    long getVisitCount();

    /**
     * 获取指定日期的访问量
     *
     * @param date 日期字符串 yyyy-MM-dd
     */
    long getDailyVisitCount(String date);
}
