package com.example.grzy.service;

import com.example.grzy.dto.LoginDTO;
import com.example.grzy.dto.PasswordDTO;
import com.example.grzy.vo.LoginVO;
import com.example.grzy.vo.UserInfoVO;

/**
 * 认证服务接口
 *
 * @author sodasmell
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    UserInfoVO getUserInfo();

    /**
     * 修改密码
     *
     * @param passwordDTO 密码参数
     */
    void changePassword(PasswordDTO passwordDTO);
}
