package com.example.grzy.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.Result;
import com.example.grzy.service.MenuService;
import com.example.grzy.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单管理控制器
 *
 * @author sodasmell
 */
@Tag(name = "菜单管理", description = "菜单树查询接口（角色分配权限用）")
@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
@SaCheckPermission("system:role:view")
public class MenuController {

    private final MenuService menuService;

    /**
     * 获取菜单树
     */
    @Operation(summary = "菜单树", description = "获取启用菜单的树形结构，用于角色分配权限")
    @GetMapping("/tree")
    public Result<List<MenuVO>> tree() {
        return Result.success(menuService.getMenuTree());
    }
}
