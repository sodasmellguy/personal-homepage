package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色菜单关联Mapper
 *
 * @author sodasmell
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 批量插入角色菜单关联
     *
     * @param list 关联列表
     */
    void batchInsert(@Param("list") List<SysRoleMenu> list);
}
