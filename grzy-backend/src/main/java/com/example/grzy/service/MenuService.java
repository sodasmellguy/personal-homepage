package com.example.grzy.service;

import com.example.grzy.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author sodasmell
 */
public interface MenuService {

    /**
     * 获取菜单树（仅启用菜单，角色分配权限用）
     *
     * @return 菜单树
     */
    List<MenuVO> getMenuTree();
}
