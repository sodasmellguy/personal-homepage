package com.example.grzy.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.dto.ResetPasswordDTO;
import com.example.grzy.dto.UserDTO;
import com.example.grzy.service.UserService;
import com.example.grzy.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 *
 * @author sodasmell
 */
@Tag(name = "用户管理", description = "用户增删改查接口")
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
@SaCheckPermission("system:user:view")
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "用户列表", description = "分页查询用户列表")
    @GetMapping("/list")
    public Result<PageResult<UserVO>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<UserVO> pageResult = userService.getUserPage(pageNum, pageSize, username, status);
        return Result.success(pageResult);
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "用户详情", description = "根据ID获取用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> get(@PathVariable Long id) {
        UserVO vo = userService.getUserById(id);
        return Result.success(vo);
    }

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody UserDTO userDTO) {
        userService.createUser(userDTO);
        return Result.success();
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户", description = "根据ID更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        userService.updateUser(id, userDTO);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @Operation(summary = "重置密码", description = "重置用户密码")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        userService.resetPassword(id, resetPasswordDTO.getPassword());
        return Result.success();
    }

    @Operation(summary = "批量删除用户", description = "根据ID列表批量删除用户")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        ids.forEach(userService::deleteUser);
        return Result.success();
    }

    /**
     * 查询用户已分配的角色ID
     */
    @Operation(summary = "用户角色ID", description = "查询用户已分配的角色ID列表")
    @GetMapping("/{id}/roles")
    public Result<List<Long>> userRoles(@PathVariable Long id) {
        return Result.success(userService.getUserRoleIds(id));
    }

    /**
     * 给用户分配角色
     */
    @Operation(summary = "分配角色", description = "给用户分配角色（全量覆盖）")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }
}
