package com.example.grzy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.ResultCode;
import com.example.grzy.entity.TbSkill;
import com.example.grzy.mapper.TbSkillMapper;
import com.example.grzy.service.OperLogService;
import com.example.grzy.service.SkillService;
import com.example.grzy.vo.SkillVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 技能服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl extends ServiceImpl<TbSkillMapper, TbSkill> implements SkillService {

    private final TbSkillMapper skillMapper;
    private final OperLogService operLogService;

    @Override
    public PageResult<SkillVO> getSkillPage(Integer pageNum, Integer pageSize, String name, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<TbSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbSkill::getDeleted, CommonConstant.NOT_DELETED);
        if (StringUtils.hasText(name)) {
            wrapper.like(TbSkill::getName, name);
        }
        if (status != null) {
            wrapper.eq(TbSkill::getStatus, status);
        }
        wrapper.orderByAsc(TbSkill::getSort);

        // 分页查询
        IPage<TbSkill> page = new Page<>(pageNum, pageSize);
        IPage<TbSkill> result = skillMapper.selectPage(page, wrapper);

        // 转换为 VO 列表
        List<SkillVO> voList = result.getRecords().stream().map(skill -> {
            SkillVO vo = new SkillVO();
            BeanUtil.copyProperties(skill, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public List<SkillVO> getAllSkills() {
        // 查询所有启用的技能
        List<TbSkill> skills = skillMapper.selectList(
                new LambdaQueryWrapper<TbSkill>()
                        .eq(TbSkill::getStatus, CommonConstant.STATUS_NORMAL)
                        .eq(TbSkill::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(TbSkill::getSort)
        );

        // 转换为 VO
        return skills.stream().map(skill -> {
            SkillVO vo = new SkillVO();
            BeanUtil.copyProperties(skill, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public SkillVO getSkillById(Long id) {
        TbSkill skill = skillMapper.selectById(id);
        if (skill == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        SkillVO vo = new SkillVO();
        BeanUtil.copyProperties(skill, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSkill(TbSkill skill) {
        skill.setStatus(skill.getStatus() != null ? skill.getStatus() : CommonConstant.STATUS_NORMAL);
        skill.setSort(skill.getSort() != null ? skill.getSort() : 0);
        skillMapper.insert(skill);
        log.info("创建技能成功: {}", skill.getName());
        operLogService.log("技能管理", "创建了技能: " + skill.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkill(Long skillId, TbSkill skill) {
        // 查询技能
        TbSkill existing = skillMapper.selectById(skillId);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 更新技能
        skill.setId(skillId);
        skillMapper.updateById(skill);
        log.info("更新技能成功: {}", skill.getName());
        operLogService.log("技能管理", "更新了技能: " + skill.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkill(Long skillId) {
        // 查询技能
        TbSkill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 逻辑删除
        skillMapper.deleteById(skillId);
        log.info("删除技能成功: {}", skill.getName());
        operLogService.log("技能管理", "删除了技能: " + skill.getName());
    }
}
