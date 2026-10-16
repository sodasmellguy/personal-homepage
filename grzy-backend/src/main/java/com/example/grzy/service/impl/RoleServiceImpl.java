package com.example.grzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.ResultCode;
import com.example.grzy.entity.SysRole;
import com.example.grzy.entity.SysRoleMenu;
import com.example.grzy.entity.SysUserRole;
import com.example.grzy.mapper.SysRoleMapper;
import com.example.grzy.mapper.SysRoleMenuMapper;
import com.example.grzy.mapper.SysUserRoleMapper;
import com.example.grzy.service.OperLogService;
import com.example.grzy.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final OperLogService operLogService;

    @Override
    public PageResult<SysRole> getRolePage(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, CommonConstant.NOT_DELETED);
        wrapper.orderByDesc(SysRole::getCreateTime);

        IPage<SysRole> page = new Page<>(pageNum, pageSize);
        IPage<SysRole> result = sysRoleMapper.selectPage(page, wrapper);

        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }

    @Override
    public List<SysRole> getAllRoles() {
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getDeleted, CommonConstant.NOT_DELETED)
                        .eq(SysRole::getStatus, CommonConstant.STATUS_NORMAL)
                        .orderByDesc(SysRole::getCreateTime)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(SysRole role) {
        // 检查角色编码是否已存在
        if (StringUtils.hasText(role.getRoleCode())) {
            Long count = sysRoleMapper.selectCount(
                    new LambdaQueryWrapper<SysRole>()
                            .eq(SysRole::getRoleCode, role.getRoleCode())
                            .eq(SysRole::getDeleted, CommonConstant.NOT_DELETED)
            );
            if (count > 0) {
                throw new BusinessException(ResultCode.DATA_EXIST, "角色编码已存在");
            }
        }
        role.setStatus(role.getStatus() != null ? role.getStatus() : CommonConstant.STATUS_NORMAL);
        role.setDeleted(CommonConstant.NOT_DELETED);
        sysRoleMapper.insert(role);

        log.info("创建角色成功: {}", role.getRoleName());
        operLogService.log("角色管理", "创建了角色: " + role.getRoleName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, SysRole role) {
        SysRole existing = sysRoleMapper.selectById(roleId);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 检查角色编码是否已存在（排除当前角色）
        if (StringUtils.hasText(role.getRoleCode())) {
            Long count = sysRoleMapper.selectCount(
                    new LambdaQueryWrapper<SysRole>()
                            .eq(SysRole::getRoleCode, role.getRoleCode())
                            .ne(SysRole::getId, roleId)
                            .eq(SysRole::getDeleted, CommonConstant.NOT_DELETED)
            );
            if (count > 0) {
                throw new BusinessException(ResultCode.DATA_EXIST, "角色编码已存在");
            }
        }

        role.setId(roleId);
        sysRoleMapper.updateById(role);

        log.info("更新角色成功: {}", role.getRoleName());
        operLogService.log("角色管理", "更新了角色: " + role.getRoleName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // admin 角色是超级管理员，禁止删除
        if (CommonConstant.ROLE_ADMIN.equals(role.getRoleCode())) {
            throw new BusinessException("admin 角色不允许删除");
        }
        // 删除角色本身及其角色-菜单、用户-角色关联，避免残留脏数据
        sysRoleMapper.deleteById(roleId);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));

        log.info("删除角色成功: {}", role.getRoleName());
        operLogService.log("角色管理", "删除了角色: " + role.getRoleName());
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return sysRoleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
                ).stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // admin 角色恒为全部权限，无需分配
        if (CommonConstant.ROLE_ADMIN.equals(role.getRoleCode())) {
            throw new BusinessException("admin 角色拥有全部权限，无需分配");
        }

        // 全量覆盖：先清空再批量插入
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> list = menuIds.stream().map(menuId -> {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                return rm;
            }).collect(Collectors.toList());
            sysRoleMenuMapper.batchInsert(list);
        }

        log.info("分配角色权限成功: {}, {} 个菜单", role.getRoleName(), menuIds == null ? 0 : menuIds.size());
        operLogService.log("角色管理", "为角色 " + role.getRoleName() + " 分配了权限");
    }
}
