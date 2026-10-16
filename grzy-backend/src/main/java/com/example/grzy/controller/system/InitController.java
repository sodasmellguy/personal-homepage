package com.example.grzy.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.result.Result;
import com.example.grzy.common.utils.SecurityUtils;
import com.example.grzy.entity.SysUser;
import com.example.grzy.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/**
 * 初始化控制器（仅用于开发调试）
 *
 * @author sodasmell
 */
@Slf4j
@Profile("dev")
@Tag(name = "初始化管理", description = "开发调试用接口")
@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
public class InitController {

    private final SysUserMapper sysUserMapper;

    /**
     * 重置管理员密码
     */
    @Operation(summary = "重置密码", description = "重置指定用户的密码")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(
            @RequestParam(value = "username", defaultValue = "admin") String username,
            @RequestParam(value = "password", defaultValue = "123456") String password) {
        // 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, CommonConstant.NOT_DELETED)
        );

        if (user == null) {
            return Result.error("用户不存在");
        }

        // 生成新的密码哈希（使用 Spring Security BCrypt）
        String newHash = SecurityUtils.encryptPassword(password);

        // 更新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setPassword(newHash);
        sysUserMapper.updateById(updateUser);

        log.info("重置用户密码成功: {}, 新密码: {}", username, password);

        return Result.success("密码重置成功，新密码: " + password);
    }
}
