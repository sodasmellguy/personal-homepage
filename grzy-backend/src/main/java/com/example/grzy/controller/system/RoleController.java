package com.example.grzy.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.dto.RoleCreateDTO;
import com.example.grzy.entity.SysRole;
import com.example.grzy.service.RoleService;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 *
 * @author sodasmell
 */
@Tag(name = "角色管理", description = "角色增删改查接口")
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
@SaCheckPermission("system:role:view")
public class RoleController {

    private final RoleService roleService;

    /**
     * 分页查询角色列表
     */
    @Operation(summary = "角色列表", description = "分页查询角色列表")
    @GetMapping("/list")
    public Result<PageResult<SysRole>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return Result.success(roleService.getRolePage(pageNum, pageSize));
    }

    /**
     * 获取所有角色（下拉选择用）
     */
    @Operation(summary = "所有角色", description = "获取所有启用角色")
    @GetMapping("/all")
    public Result<List<SysRole>> all() {
        return Result.success(roleService.getAllRoles());
    }

    /**
     * 创建角色
     */
    @Operation(summary = "创建角色", description = "创建新角色")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody RoleCreateDTO dto) {
        SysRole role = new SysRole();
        BeanUtil.copyProperties(dto, role);
        roleService.createRole(role);
        return Result.success();
    }

    /**
     * 更新角色
     */
    @Operation(summary = "更新角色", description = "根据ID更新角色信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleCreateDTO dto) {
        SysRole role = new SysRole();
        BeanUtil.copyProperties(dto, role);
        roleService.updateRole(id, role);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色", description = "根据ID删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 查询角色已分配的菜单ID
     */
    @Operation(summary = "角色菜单ID", description = "查询角色已分配的菜单ID列表")
    @GetMapping("/{id}/menus")
    public Result<List<Long>> roleMenus(@PathVariable Long id) {
        return Result.success(roleService.getRoleMenuIds(id));
    }

    /**
     * 给角色分配菜单权限
     */
    @Operation(summary = "分配权限", description = "给角色分配菜单权限（全量覆盖）")
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success();
    }
}
