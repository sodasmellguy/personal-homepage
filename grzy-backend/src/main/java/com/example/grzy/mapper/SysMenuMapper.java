package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单Mapper
 *
 * @author sodasmell
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询所有启用的菜单（按排序）
     *
     * @return 菜单列表
     */
    List<SysMenu> selectEnabledMenus();
}
