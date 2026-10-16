package com.example.grzy.common.utils;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全工具类
 *
 * @author sodasmell
 */
public class SecurityUtils {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 密码加密
     */
    public static String encryptPassword(String password) {
        return PASSWORD_ENCODER.encode(password);
    }

    /**
     * 密码校验
     */
    public static boolean checkPassword(String password, String encryptedPassword) {
        return PASSWORD_ENCODER.matches(password, encryptedPassword);
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        return (String) StpUtil.getSession().get("username");
    }

    /**
     * 判断是否已登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }
}
