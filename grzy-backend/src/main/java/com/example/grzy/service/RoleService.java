package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author sodasmell
 */
public interface RoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表
     */
    PageResult<SysRole> getRolePage(Integer pageNum, Integer pageSize);

    /**
     * 获取所有角色（不分页）
     */
    List<SysRole> getAllRoles();

    /**
     * 创建角色
     */
    void createRole(SysRole role);

    /**
     * 更新角色
     */
    void updateRole(Long roleId, SysRole role);

    /**
     * 删除角色
     */
    void deleteRole(Long roleId);

    /**
     * 查询角色已分配的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 给角色分配菜单权限（全量覆盖）
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    void assignMenus(Long roleId, List<Long> menuIds);
}
