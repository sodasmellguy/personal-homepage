package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.SysUser;
import com.example.grzy.vo.UserRoleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户Mapper
 *
 * @author sodasmell
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户ID查询角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限标识列表
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询已分配的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 批量查询用户的角色关联（用户ID -> 角色ID/角色名）
     *
     * @param userIds 用户ID列表
     * @return 用户角色关联列表
     */
    List<UserRoleVO> selectUserRolesByUserIds(@Param("userIds") List<Long> userIds);
}
