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
import com.example.grzy.entity.TbInternship;
import com.example.grzy.mapper.TbInternshipMapper;
import com.example.grzy.service.InternshipService;
import com.example.grzy.service.OperLogService;
import com.example.grzy.vo.InternshipVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 实习经历服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternshipServiceImpl extends ServiceImpl<TbInternshipMapper, TbInternship> implements InternshipService {

    private final TbInternshipMapper internshipMapper;
    private final OperLogService operLogService;

    @Override
    public PageResult<InternshipVO> getInternshipPage(Integer pageNum, Integer pageSize, String company, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<TbInternship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbInternship::getDeleted, CommonConstant.NOT_DELETED);
        if (StringUtils.hasText(company)) {
            wrapper.like(TbInternship::getCompany, company);
        }
        if (status != null) {
            wrapper.eq(TbInternship::getStatus, status);
        }
        wrapper.orderByAsc(TbInternship::getSort);

        // 分页查询
        IPage<TbInternship> page = new Page<>(pageNum, pageSize);
        IPage<TbInternship> result = internshipMapper.selectPage(page, wrapper);

        // 转换为 VO 列表
        List<InternshipVO> voList = result.getRecords().stream().map(internship -> {
            InternshipVO vo = new InternshipVO();
            BeanUtil.copyProperties(internship, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public List<InternshipVO> getAllInternships() {
        // 查询所有启用的实习经历
        List<TbInternship> internships = internshipMapper.selectList(
                new LambdaQueryWrapper<TbInternship>()
                        .eq(TbInternship::getStatus, CommonConstant.STATUS_NORMAL)
                        .eq(TbInternship::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(TbInternship::getSort)
        );

        // 转换为 VO
        return internships.stream().map(internship -> {
            InternshipVO vo = new InternshipVO();
            BeanUtil.copyProperties(internship, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public InternshipVO getInternshipById(Long id) {
        TbInternship internship = internshipMapper.selectById(id);
        if (internship == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        InternshipVO vo = new InternshipVO();
        BeanUtil.copyProperties(internship, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInternship(TbInternship internship) {
        internship.setStatus(internship.getStatus() != null ? internship.getStatus() : CommonConstant.STATUS_NORMAL);
        internship.setSort(internship.getSort() != null ? internship.getSort() : 0);
        internshipMapper.insert(internship);
        log.info("创建实习经历成功: {} - {}", internship.getCompany(), internship.getPosition());
        operLogService.log("实习管理", "创建了实习: " + internship.getCompany());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInternship(Long internshipId, TbInternship internship) {
        // 查询实习经历
        TbInternship existing = internshipMapper.selectById(internshipId);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 更新实习经历
        internship.setId(internshipId);
        internshipMapper.updateById(internship);
        log.info("更新实习经历成功: {} - {}", internship.getCompany(), internship.getPosition());
        operLogService.log("实习管理", "更新了实习: " + internship.getCompany());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInternship(Long internshipId) {
        // 查询实习经历
        TbInternship internship = internshipMapper.selectById(internshipId);
        if (internship == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 逻辑删除
        internshipMapper.deleteById(internshipId);
        log.info("删除实习经历成功: {} - {}", internship.getCompany(), internship.getPosition());
        operLogService.log("实习管理", "删除了实习: " + internship.getCompany());
    }
}
