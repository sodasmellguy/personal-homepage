package com.example.grzy.common.config;

import cn.dev33.satoken.stp.StpInterface;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限数据源
 * 为 StpUtil.checkRole / checkPermission 提供当前用户的角色与权限列表，
 * 使拦截器层能够进行角色级鉴权（此前仅有登录校验，任意注册用户可调用管理接口）
 *
 * @author sodasmell
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper sysUserMapper;

    /**
     * 返回指定用户的角色编码列表（如 admin）
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        return sysUserMapper.selectRoleCodesByUserId(userId);
    }

    /**
     * 返回指定用户的权限标识列表
     * admin 角色为超级管理员，返回通配符 "*" 匹配一切权限；
     * 其他角色返回其勾选菜单的权限标识集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        if (roles.contains(CommonConstant.ROLE_ADMIN)) {
            return Collections.singletonList("*");
        }
        return sysUserMapper.selectPermissionsByUserId(userId);
    }
}
