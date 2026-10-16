package com.example.grzy.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.ResultCode;
import com.example.grzy.common.utils.SecurityUtils;
import com.example.grzy.dto.UserDTO;
import com.example.grzy.entity.SysRole;
import com.example.grzy.entity.SysUser;
import com.example.grzy.entity.SysUserRole;
import com.example.grzy.mapper.SysRoleMapper;
import com.example.grzy.mapper.SysUserMapper;
import com.example.grzy.mapper.SysUserRoleMapper;
import com.example.grzy.service.OperLogService;
import com.example.grzy.service.UserService;
import com.example.grzy.vo.UserRoleVO;
import com.example.grzy.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final OperLogService operLogService;

    @Override
    public PageResult<UserVO> getUserPage(Integer pageNum, Integer pageSize, String username, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeleted, CommonConstant.NOT_DELETED);
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        // 分页查询
        IPage<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> result = sysUserMapper.selectPage(page, wrapper);

        // 转换为 VO 列表，并批量填充角色信息
        List<SysUser> records = result.getRecords();
        Map<Long, List<UserRoleVO>> roleMap = findUserRoles(records.stream()
                .map(SysUser::getId).collect(Collectors.toList()));

        List<UserVO> voList = records.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtil.copyProperties(user, vo);
            fillRoles(vo, roleMap.getOrDefault(user.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, userDTO.getUsername())
                        .eq(SysUser::getDeleted, CommonConstant.NOT_DELETED)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_EXIST, "用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        BeanUtil.copyProperties(userDTO, user);
        user.setPassword(SecurityUtils.encryptPassword(userDTO.getPassword()));
        user.setStatus(userDTO.getStatus() != null ? userDTO.getStatus() : CommonConstant.STATUS_NORMAL);
        sysUserMapper.insert(user);

        // 保存角色关联
        if (userDTO.getRoleIds() != null) {
            saveUserRoles(user.getId(), userDTO.getRoleIds());
        }

        log.info("创建用户成功: {}", user.getUsername());
        operLogService.log("用户管理", "创建了用户: " + user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserDTO userDTO) {
        // 查询用户
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 检查用户名是否已存在（排除当前用户）
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, userDTO.getUsername())
                        .ne(SysUser::getId, userId)
                        .eq(SysUser::getDeleted, CommonConstant.NOT_DELETED)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_EXIST, "用户名已存在");
        }

        // 更新用户
        SysUser updateUser = new SysUser();
        BeanUtil.copyProperties(userDTO, updateUser);
        updateUser.setId(userId);
        // 不更新密码
        updateUser.setPassword(null);
        sysUserMapper.updateById(updateUser);

        // 保存角色关联（仅在传入 roleIds 时覆盖）
        if (userDTO.getRoleIds() != null) {
            saveUserRoles(userId, userDTO.getRoleIds());
        }

        log.info("更新用户成功: {}", user.getUsername());
        operLogService.log("用户管理", "更新了用户: " + user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        // 查询用户
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // admin 用户是超级管理员，禁止删除
        if (CommonConstant.ROLE_ADMIN.equals(user.getUsername())) {
            throw new BusinessException("admin 用户不允许删除");
        }

        // 逻辑删除，并清理用户-角色关联
        sysUserMapper.deleteById(userId);
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

        log.info("删除用户成功: {}", user.getUsername());
        operLogService.log("用户管理", "删除了用户: " + user.getUsername());
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        fillRoles(vo, findUserRoles(Collections.singletonList(id)).getOrDefault(id, Collections.emptyList()));
        return vo;
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return sysUserMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // 允许修改自己的角色，但不能移除自己的 admin 角色（防止把自己锁在后台外）
        if (userId.equals(StpUtil.getLoginIdAsLong())) {
            boolean isAdmin = sysUserMapper.selectRoleCodesByUserId(userId)
                    .contains(CommonConstant.ROLE_ADMIN);
            boolean keepsAdmin = roleIds != null && sysRoleMapper.selectBatchIds(roleIds).stream()
                    .anyMatch(r -> CommonConstant.ROLE_ADMIN.equals(r.getRoleCode()));
            if (isAdmin && !keepsAdmin) {
                throw new BusinessException("不能移除自己的 admin 角色");
            }
        }

        saveUserRoles(userId, roleIds);

        log.info("分配用户角色成功: {}, {} 个角色", user.getUsername(), roleIds == null ? 0 : roleIds.size());
        operLogService.log("用户管理", "为用户 " + user.getUsername() + " 分配了角色");
    }

    /**
     * 保存用户-角色关联（全量覆盖）
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        // 校验角色都存在，避免写入无效关联
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
        if (roles.size() != roleIds.stream().distinct().count()) {
            throw new BusinessException("存在无效的角色，请重新选择");
        }
        for (Long roleId : roleIds.stream().distinct().collect(Collectors.toList())) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            sysUserRoleMapper.insert(ur);
        }
    }

    /**
     * 批量查询用户角色关联并按 userId 分组
     */
    private Map<Long, List<UserRoleVO>> findUserRoles(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectUserRolesByUserIds(userIds).stream()
                .collect(Collectors.groupingBy(UserRoleVO::getUserId));
    }

    /**
     * 把角色关联填充到 VO（roleIds + roleNames）
     */
    private void fillRoles(UserVO vo, List<UserRoleVO> userRoles) {
        List<Long> roleIds = new ArrayList<>();
        List<String> roleNames = new ArrayList<>();
        for (UserRoleVO ur : userRoles) {
            roleIds.add(ur.getRoleId());
            roleNames.add(ur.getRoleName());
        }
        vo.setRoleIds(roleIds);
        vo.setRoleNames(roleNames);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String password) {
        // 查询用户
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 更新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(SecurityUtils.encryptPassword(password));
        sysUserMapper.updateById(updateUser);

        log.info("重置用户密码成功: {}", user.getUsername());
    }
}
