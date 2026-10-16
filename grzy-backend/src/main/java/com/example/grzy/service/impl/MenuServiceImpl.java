package com.example.grzy.service.impl;

import com.example.grzy.entity.SysMenu;
import com.example.grzy.mapper.SysMenuMapper;
import com.example.grzy.service.MenuService;
import com.example.grzy.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 *
 * @author sodasmell
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper sysMenuMapper;

    /** 顶层菜单的父ID */
    private static final long ROOT_PARENT_ID = 0L;

    @Override
    public List<MenuVO> getMenuTree() {
        List<SysMenu> menus = sysMenuMapper.selectEnabledMenus();

        // 按 parentId 分组，逐层组装（目录/菜单/按钮都保留，树形完整）
        Map<Long, List<MenuVO>> grouped = menus.stream()
                .map(this::toVO)
                .collect(Collectors.groupingBy(MenuVO::getParentId));

        List<MenuVO> tree = grouped.getOrDefault(ROOT_PARENT_ID, new ArrayList<>())
                .stream()
                .sorted(Comparator.comparing(vo -> vo.getSort() == null ? 0 : vo.getSort()))
                .collect(Collectors.toList());
        tree.forEach(vo -> vo.setChildren(
                grouped.getOrDefault(vo.getId(), new ArrayList<>())
                        .stream()
                        .sorted(Comparator.comparing(child -> child.getSort() == null ? 0 : child.getSort()))
                        .collect(Collectors.toList())
        ));
        return tree;
    }

    /**
     * 实体转VO
     */
    private MenuVO toVO(SysMenu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setName(menu.getName());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setIcon(menu.getIcon());
        vo.setSort(menu.getSort());
        vo.setType(menu.getType());
        vo.setPermission(menu.getPermission());
        return vo;
    }
}
