package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.SysOperLog;

import java.util.List;

/**
 * 操作日志服务
 *
 * @author sodasmell
 */
public interface OperLogService extends IService<SysOperLog> {

    /**
     * 记录操作日志
     *
     * @param module    操作模块
     * @param operation 操作描述
     */
    void log(String module, String operation);

    /**
     * 获取最近的操作日志
     *
     * @param limit 条数
     * @return 日志列表
     */
    List<SysOperLog> getRecentLogs(int limit);

    /**
     * 分页查询操作日志
     */
    PageResult<SysOperLog> getLogPage(Integer pageNum, Integer pageSize, String module, String username);
}
