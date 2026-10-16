package com.example.grzy.controller.auth;

import com.example.grzy.common.result.Result;
import com.example.grzy.dto.LoginDTO;
import com.example.grzy.dto.PasswordDTO;
import com.example.grzy.service.AuthService;
import com.example.grzy.vo.LoginVO;
import com.example.grzy.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author sodasmell
 */
@Tag(name = "认证管理", description = "用户登录、登出、获取用户信息等接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户名密码登录，返回Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "退出登录，清除Token")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        UserInfoVO userInfoVO = authService.getUserInfo();
        return Result.success(userInfoVO);
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @PutMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordDTO passwordDTO) {
        authService.changePassword(passwordDTO);
        return Result.success();
    }
}
