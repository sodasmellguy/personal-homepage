package com.example.grzy.controller.content;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbProfile;
import com.example.grzy.service.ProfileService;
import com.example.grzy.vo.ProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 个人信息控制器
 *
 * @author sodasmell
 */
@Tag(name = "个人信息管理", description = "个人信息增删改查接口")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@SaCheckPermission("content:profile:view")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 获取个人信息
     */
    @Operation(summary = "获取个人信息", description = "获取个人信息详情")
    @GetMapping
    public Result<ProfileVO> getProfile() {
        ProfileVO profileVO = profileService.getProfile();
        return Result.success(profileVO);
    }

    /**
     * 更新个人信息
     */
    @Operation(summary = "更新个人信息", description = "更新个人信息")
    @PutMapping
    public Result<Void> updateProfile(@RequestBody TbProfile profile) {
        profileService.updateProfile(profile);
        return Result.success();
    }
}
