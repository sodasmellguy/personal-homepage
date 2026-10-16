package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 *
 * @author sodasmell
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
