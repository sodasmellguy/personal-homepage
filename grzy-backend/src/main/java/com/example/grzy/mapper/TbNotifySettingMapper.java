package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.TbNotifySetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件通知设置 Mapper
 *
 * @author sodasmell
 */
@Mapper
public interface TbNotifySettingMapper extends BaseMapper<TbNotifySetting> {
}
