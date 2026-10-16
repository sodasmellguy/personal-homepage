package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper
 *
 * @author sodasmell
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
