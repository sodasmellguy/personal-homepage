package com.example.grzy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.grzy.entity.TbProfile;
import com.example.grzy.mapper.TbProfileMapper;
import com.example.grzy.service.OperLogService;
import com.example.grzy.service.ProfileService;
import com.example.grzy.vo.ProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 个人信息服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final TbProfileMapper profileMapper;
    private final OperLogService operLogService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "grzy:profile";
    private static final long CACHE_TTL_MINUTES = 30;

    @Override
    public ProfileVO getProfile() {
        // 先查 Redis 缓存
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached instanceof ProfileVO) {
            return (ProfileVO) cached;
        }

        // 查询个人信息（只有一条记录）
        TbProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<TbProfile>()
                        .orderByDesc(TbProfile::getId)
                        .last("LIMIT 1")
        );

        // 如果没有数据，返回空对象
        if (profile == null) {
            return new ProfileVO();
        }

        // 转换为 VO
        ProfileVO vo = new ProfileVO();
        BeanUtil.copyProperties(profile, vo);

        // 写入缓存
        redisTemplate.opsForValue().set(CACHE_KEY, vo, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(TbProfile profile) {
        // 查询现有数据
        TbProfile existing = profileMapper.selectOne(
                new LambdaQueryWrapper<TbProfile>()
                        .orderByDesc(TbProfile::getId)
                        .last("LIMIT 1")
        );

        if (existing == null) {
            // 新增
            profileMapper.insert(profile);
            log.info("创建个人信息成功");
            operLogService.log("个人信息管理", "创建了个人信息");
        } else {
            // 更新
            profile.setId(existing.getId());
            profileMapper.updateById(profile);
            log.info("更新个人信息成功");
            operLogService.log("个人信息管理", "更新了个人信息");
        }
        // 失效缓存
        redisTemplate.delete(CACHE_KEY);
    }
}
