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
import com.example.grzy.entity.TbTechStack;
import com.example.grzy.mapper.TbTechStackMapper;
import com.example.grzy.service.OperLogService;
import com.example.grzy.service.TechStackService;
import com.example.grzy.vo.TechStackVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 技术栈服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TechStackServiceImpl extends ServiceImpl<TbTechStackMapper, TbTechStack> implements TechStackService {

    private final TbTechStackMapper techStackMapper;
    private final OperLogService operLogService;

    @Override
    public PageResult<TbTechStack> getTechStackPage(Integer pageNum, Integer pageSize, String name) {
        // 构建查询条件
        LambdaQueryWrapper<TbTechStack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbTechStack::getDeleted, CommonConstant.NOT_DELETED);
        if (StringUtils.hasText(name)) {
            wrapper.like(TbTechStack::getName, name);
        }
        wrapper.orderByAsc(TbTechStack::getSort);

        // 分页查询
        IPage<TbTechStack> page = new Page<>(pageNum, pageSize);
        IPage<TbTechStack> result = techStackMapper.selectPage(page, wrapper);

        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }

    @Override
    public List<TechStackVO> getAllTechStacks() {
        // 查询所有启用的技术栈
        List<TbTechStack> techStacks = techStackMapper.selectList(
                new LambdaQueryWrapper<TbTechStack>()
                        .eq(TbTechStack::getStatus, CommonConstant.STATUS_NORMAL)
                        .eq(TbTechStack::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(TbTechStack::getSort)
        );

        // 转换为 VO
        return techStacks.stream().map(techStack -> {
            TechStackVO vo = new TechStackVO();
            BeanUtil.copyProperties(techStack, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public TbTechStack getTechStackById(Long id) {
        TbTechStack techStack = techStackMapper.selectById(id);
        if (techStack == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return techStack;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTechStack(TbTechStack techStack) {
        techStack.setStatus(techStack.getStatus() != null ? techStack.getStatus() : CommonConstant.STATUS_NORMAL);
        techStack.setSort(techStack.getSort() != null ? techStack.getSort() : 0);
        techStackMapper.insert(techStack);
        log.info("创建技术栈成功: {}", techStack.getName());
        operLogService.log("技术栈管理", "创建了技术栈: " + techStack.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTechStack(Long id, TbTechStack techStack) {
        // 查询技术栈
        TbTechStack existing = techStackMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 更新技术栈
        techStack.setId(id);
        techStackMapper.updateById(techStack);
        log.info("更新技术栈成功: {}", techStack.getName());
        operLogService.log("技术栈管理", "更新了技术栈: " + techStack.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTechStack(Long id) {
        // 查询技术栈
        TbTechStack techStack = techStackMapper.selectById(id);
        if (techStack == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 逻辑删除
        techStackMapper.deleteById(id);
        log.info("删除技术栈成功: {}", techStack.getName());
        operLogService.log("技术栈管理", "删除了技术栈: " + techStack.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteTechStacks(List<Long> ids) {
        ids.forEach(id -> {
            TbTechStack techStack = techStackMapper.selectById(id);
            if (techStack != null) {
                techStackMapper.deleteById(id);
                log.info("批量删除技术栈成功: {}", techStack.getName());
            }
        });
        operLogService.log("技术栈管理", "批量删除了技术栈，共" + ids.size() + "个");
    }
}
