package com.example.grzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.utils.SecurityUtils;
import com.example.grzy.entity.SysOperLog;
import com.example.grzy.mapper.SysOperLogMapper;
import com.example.grzy.service.OperLogService;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志服务实现
 *
 * @author sodasmell
 */
@Slf4j
@Service
public class OperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements OperLogService {

    @Override
    public void log(String module, String operation) {
        try {
            SysOperLog operLog = new SysOperLog();
            operLog.setModule(module);
            operLog.setOperation(operation);
            operLog.setStatus(1);

            // 尝试获取当前登录用户（可能在异步/定时任务中无用户）
            try {
                Long userId = SecurityUtils.getCurrentUserId();
                operLog.setUserId(userId);
                String username = SecurityUtils.getCurrentUsername();
                operLog.setUsername(username);
            } catch (Exception ignored) {
                // 无登录用户时跳过
            }

            this.save(operLog);
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage());
        }
    }

    @Override
    public List<SysOperLog> getRecentLogs(int limit) {
        return this.list(
                new LambdaQueryWrapper<SysOperLog>()
                        .orderByDesc(SysOperLog::getCreateTime)
                        .last("LIMIT " + limit)
        );
    }

    @Override
    public PageResult<SysOperLog> getLogPage(Integer pageNum, Integer pageSize, String module, String username) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(module)) {
            wrapper.like(SysOperLog::getModule, module);
        }
        if (StrUtil.isNotBlank(username)) {
            wrapper.like(SysOperLog::getUsername, username);
        }
        wrapper.orderByDesc(SysOperLog::getCreateTime);
        IPage<SysOperLog> page = new Page<>(pageNum, pageSize);
        IPage<SysOperLog> result = this.page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }
}
