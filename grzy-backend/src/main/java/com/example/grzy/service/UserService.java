package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.dto.UserDTO;
import com.example.grzy.entity.SysUser;
import com.example.grzy.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author sodasmell
 */
public interface UserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param username 用户名（可选）
     * @param status   状态（可选）
     * @return 分页结果
     */
    PageResult<UserVO> getUserPage(Integer pageNum, Integer pageSize, String username, Integer status);

    /**
     * 创建用户
     *
     * @param userDTO 用户参数
     */
    void createUser(UserDTO userDTO);

    /**
     * 更新用户
     *
     * @param userId  用户ID
     * @param userDTO 用户参数
     */
    void updateUser(Long userId, UserDTO userDTO);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 重置密码
     *
     * @param userId   用户ID
     * @param password 新密码
     */
    void resetPassword(Long userId, String password);

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户VO
     */
    UserVO getUserById(Long id);

    /**
     * 查询用户已分配的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 给用户分配角色（全量覆盖）
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(Long userId, List<Long> roleIds);
}
