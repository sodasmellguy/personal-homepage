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
import com.example.grzy.entity.TbAward;
import com.example.grzy.mapper.TbAwardMapper;
import com.example.grzy.service.AwardService;
import com.example.grzy.service.OperLogService;
import com.example.grzy.vo.AwardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 获奖经历服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwardServiceImpl extends ServiceImpl<TbAwardMapper, TbAward> implements AwardService {

    private final TbAwardMapper awardMapper;
    private final OperLogService operLogService;

    @Override
    public PageResult<AwardVO> getAwardPage(Integer pageNum, Integer pageSize, String title, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<TbAward> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbAward::getDeleted, CommonConstant.NOT_DELETED);
        if (StringUtils.hasText(title)) {
            wrapper.like(TbAward::getTitle, title);
        }
        if (status != null) {
            wrapper.eq(TbAward::getStatus, status);
        }
        wrapper.orderByAsc(TbAward::getSort);

        // 分页查询
        IPage<TbAward> page = new Page<>(pageNum, pageSize);
        IPage<TbAward> result = awardMapper.selectPage(page, wrapper);

        // 转换为 VO 列表
        List<AwardVO> voList = result.getRecords().stream().map(award -> {
            AwardVO vo = new AwardVO();
            BeanUtil.copyProperties(award, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public List<AwardVO> getAllAwards() {
        // 查询所有启用的获奖经历
        List<TbAward> awards = awardMapper.selectList(
                new LambdaQueryWrapper<TbAward>()
                        .eq(TbAward::getStatus, CommonConstant.STATUS_NORMAL)
                        .eq(TbAward::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(TbAward::getSort)
        );

        // 转换为 VO
        return awards.stream().map(award -> {
            AwardVO vo = new AwardVO();
            BeanUtil.copyProperties(award, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public AwardVO getAwardById(Long id) {
        TbAward award = awardMapper.selectById(id);
        if (award == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        AwardVO vo = new AwardVO();
        BeanUtil.copyProperties(award, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAward(TbAward award) {
        award.setStatus(award.getStatus() != null ? award.getStatus() : CommonConstant.STATUS_NORMAL);
        award.setSort(award.getSort() != null ? award.getSort() : 0);
        awardMapper.insert(award);
        log.info("创建获奖经历成功: {}", award.getTitle());
        operLogService.log("获奖管理", "创建了获奖: " + award.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAward(Long awardId, TbAward award) {
        // 查询获奖经历
        TbAward existing = awardMapper.selectById(awardId);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 更新获奖经历
        award.setId(awardId);
        awardMapper.updateById(award);
        log.info("更新获奖经历成功: {}", award.getTitle());
        operLogService.log("获奖管理", "更新了获奖: " + award.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAward(Long awardId) {
        // 查询获奖经历
        TbAward award = awardMapper.selectById(awardId);
        if (award == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 逻辑删除
        awardMapper.deleteById(awardId);
        log.info("删除获奖经历成功: {}", award.getTitle());
        operLogService.log("获奖管理", "删除了获奖: " + award.getTitle());
    }
}
