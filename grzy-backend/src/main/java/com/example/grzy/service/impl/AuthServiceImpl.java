package com.example.grzy.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.ResultCode;
import com.example.grzy.common.utils.ClientIpUtils;
import com.example.grzy.common.utils.SecurityUtils;
import com.example.grzy.dto.LoginDTO;
import com.example.grzy.dto.PasswordDTO;
import com.example.grzy.entity.SysUser;
import com.example.grzy.mapper.SysUserMapper;
import com.example.grzy.service.AuthService;
import com.example.grzy.service.NotifyService;
import com.example.grzy.vo.LoginVO;
import com.example.grzy.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;
    private final NotifyService notifyService;

    /** 登录失败计数 Redis Key 前缀（按账号） */
    private static final String LOGIN_FAIL_KEY = "grzy:login:fail:";
    /** 登录失败计数 Redis Key 前缀（按 IP） */
    private static final String LOGIN_FAIL_IP_KEY = "grzy:login:fail:ip:";
    /** 账号历史登录 IP 集合 Key 前缀 */
    private static final String LOGIN_IP_SET_KEY = "grzy:login:ips:";
    /** 在线会话元数据 Hash Key 前缀 */
    private static final String SESSION_INFO_KEY = "grzy:session:info:";
    /** 按账号最大连续失败次数 */
    private static final int MAX_LOGIN_FAIL = 5;
    /** 按 IP 最大失败次数 */
    private static final int MAX_IP_LOGIN_FAIL = 10;
    /** 账号锁定时长（分钟） */
    private static final int LOCK_MINUTES = 15;
    /** IP 锁定时长（分钟） */
    private static final int IP_LOCK_MINUTES = 30;
    /** 历史 IP 集合保留时长（天） */
    private static final long IP_SET_TTL_DAYS = 90;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String clientIp = ClientIpUtils.getClientIp();
        // 暴力破解防护 1：同一账号连续失败达到上限后锁定一段时间
        String failKey = LOGIN_FAIL_KEY + loginDTO.getUsername();
        String failCountStr = stringRedisTemplate.opsForValue().get(failKey);
        if (failCountStr != null && Integer.parseInt(failCountStr) >= MAX_LOGIN_FAIL) {
            log.warn("账号因连续登录失败被临时锁定: {}", loginDTO.getUsername());
            throw new BusinessException("登录失败次数过多，账号已临时锁定，请 " + LOCK_MINUTES + " 分钟后再试");
        }
        // 暴力破解防护 2：同一 IP 失败达到上限后锁定（防撞库遍历账号）
        String ipFailKey = LOGIN_FAIL_IP_KEY + clientIp;
        String ipFailCountStr = stringRedisTemplate.opsForValue().get(ipFailKey);
        if (ipFailCountStr != null && Integer.parseInt(ipFailCountStr) >= MAX_IP_LOGIN_FAIL) {
            log.warn("IP 因连续登录失败被临时锁定: {}", clientIp);
            throw new BusinessException("登录失败次数过多，请 " + IP_LOCK_MINUTES + " 分钟后再试");
        }

        // 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, loginDTO.getUsername())
                        .eq(SysUser::getDeleted, CommonConstant.NOT_DELETED)
        );

        // 用户不存在
        if (user == null) {
            recordLoginFail(failKey, LOCK_MINUTES);
            recordLoginFail(ipFailKey, IP_LOCK_MINUTES);
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        // 用户已禁用
        if (CommonConstant.STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 密码校验
        if (!SecurityUtils.checkPassword(loginDTO.getPassword(), user.getPassword())) {
            recordLoginFail(failKey, LOCK_MINUTES);
            recordLoginFail(ipFailKey, IP_LOCK_MINUTES);
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        // 登录成功，清除失败计数
        stringRedisTemplate.delete(failKey);
        stringRedisTemplate.delete(ipFailKey);

        // Sa-Token 登录
        StpUtil.login(user.getId());
        // 将用户名存入 Session
        StpUtil.getSession().set("username", user.getUsername());

        // 记录会话元数据（供后台「在线会话」查询与踢下线）
        recordSessionInfo(StpUtil.getTokenValue(), user, clientIp);

        // 新 IP 登录检测与邮件告警（首个 IP 不告警，避免站长自己首次登录被骚扰）
        checkNewIpAndAlert(user, clientIp);

        // 构建返回结果
        LoginVO loginVO = LoginVO.builder()
                .token(StpUtil.getTokenValue())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();

        log.info("用户登录成功: {}, ip={}", user.getUsername(), clientIp);
        return loginVO;
    }

    /**
     * 记录一次登录失败（首次失败起计时窗口内累计，达到上限即锁定）
     */
    private void recordLoginFail(String failKey, int lockMinutes) {
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(failKey, java.time.Duration.ofMinutes(lockMinutes));
        }
    }

    /**
     * 记录会话元数据到 Redis Hash（TTL 与 Sa-Token 会话时长对齐）
     */
    private void recordSessionInfo(String token, SysUser user, String ip) {
        try {
            String key = SESSION_INFO_KEY + token;
            String userAgent = "";
            if (org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
                    instanceof org.springframework.web.context.request.ServletRequestAttributes attrs) {
                userAgent = attrs.getRequest().getHeader("User-Agent");
            }
            java.util.Map<String, String> info = new java.util.HashMap<>();
            info.put("userId", String.valueOf(user.getId()));
            info.put("username", user.getUsername());
            info.put("ip", ip);
            info.put("userAgent", userAgent != null ? userAgent : "");
            info.put("loginTime", java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stringRedisTemplate.opsForHash().putAll(key, info);
            stringRedisTemplate.expire(key, java.time.Duration.ofSeconds(3600));
        } catch (Exception e) {
            // 会话元数据是旁路数据，失败不影响登录
            log.warn("记录会话元数据失败: {}", e.getMessage());
        }
    }

    /**
     * 新 IP 登录检测：Set 中已有该账号历史 IP 时，出现新 IP 则触发邮件告警
     */
    private void checkNewIpAndAlert(SysUser user, String ip) {
        try {
            String ipSetKey = LOGIN_IP_SET_KEY + user.getId();
            Long added = stringRedisTemplate.opsForSet().add(ipSetKey, ip);
            if (added == null || added == 0) {
                return; // 已知 IP，不告警
            }
            stringRedisTemplate.expire(ipSetKey, java.time.Duration.ofDays(IP_SET_TTL_DAYS));
            Long knownCount = stringRedisTemplate.opsForSet().size(ipSetKey);
            if (knownCount != null && knownCount > 1) {
                notifyService.notifySecurityAlert(user.getUsername(), ip,
                        "检测到账号「" + user.getUsername() + "」在新的 IP 地址登录，若非本人操作请尽快修改密码。");
                log.info("新 IP 登录告警已触发: user={}, ip={}", user.getUsername(), ip);
            }
        } catch (Exception e) {
            log.warn("新 IP 登录检测失败: {}", e.getMessage());
        }
    }

    /**
     * 记录一次登录失败（首次失败起 15 分钟内累计，达到上限即锁定）
     */
    private void recordLoginFail(String failKey) {
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(failKey, java.time.Duration.ofMinutes(LOCK_MINUTES));
        }
    }

    @Override
    public void logout() {
        String username = SecurityUtils.getCurrentUsername();
        StpUtil.logout();
        log.info("用户登出成功: {}", username);
    }

    @Override
    public UserInfoVO getUserInfo() {
        Long userId = SecurityUtils.getCurrentUserId();

        // 查询用户信息
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 查询角色列表
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);

        // 查询权限列表
        List<String> permissions = sysUserMapper.selectPermissionsByUserId(userId);

        // 构建返回结果
        UserInfoVO userInfoVO = UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roles)
                .permissions(permissions)
                .build();

        return userInfoVO;
    }

    @Override
    public void changePassword(PasswordDTO passwordDTO) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 查询用户
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 验证旧密码
        if (!SecurityUtils.checkPassword(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 更新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(SecurityUtils.encryptPassword(passwordDTO.getNewPassword()));
        sysUserMapper.updateById(updateUser);

        log.info("用户修改密码成功: {}", user.getUsername());
    }
}
